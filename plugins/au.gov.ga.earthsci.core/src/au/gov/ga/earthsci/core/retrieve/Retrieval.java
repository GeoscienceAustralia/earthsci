/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.core.retrieve;

import java.io.Closeable;
import java.net.URL;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import au.gov.ga.earthsci.core.util.AbstractPropertyChangeBean;
import au.gov.ga.earthsci.core.util.collection.HashSetAndArray;
import au.gov.ga.earthsci.core.util.collection.SetAndArray;

/**
 * Basic {@link IRetrieval} implementation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Retrieval extends AbstractPropertyChangeBean implements IRetrieval, IRetrieverMonitor
{
	private final SetAndArray<Object> callers = new HashSetAndArray<Object>();
	private final URL url;
	private final IRetrievalProperties retrievalProperties;
	private final IRetriever retriever;

	private RetrievalStatus status = RetrievalStatus.NOT_STARTED;
	private long position = 0;
	private long length = UNKNOWN_LENGTH;
	private final CompoundRetrievalListener listeners = new CompoundRetrievalListener();

	private final Object jobSemaphore = new Object();
	private RetrievalJob job;
	private boolean canceled = false;
	private boolean paused = false;
	private final Object pausedSemaphore = new Object();

	private IRetrievalData cachedData;
	private IRetrievalResult result;

	public Retrieval(Object caller, URL url, IRetrievalProperties retrievalProperties, IRetriever retriever)
	{
		addCaller(caller);
		this.url = url;
		this.retrievalProperties = retrievalProperties;
		this.retriever = retriever;
	}

	void addCaller(Object caller)
	{
		synchronized (callers)
		{
			callers.add(caller);
			listeners.callersChanged(this);
		}
	}

	RetrieverResult retrieve(IRetrieverMonitor monitor) throws Exception
	{
		if (retrievalProperties.isUseCache())
		{
			cachedData = retriever.checkCache(url);
			if (cachedData != null)
			{
				listeners.cached(this);
			}
		}
		return retriever.retrieve(url, monitor, retrievalProperties, cachedData);
	}

	@Override
	public Object[] getCallers()
	{
		synchronized (callers)
		{
			return callers.getArray(Object.class);
		}
	}

	@Override
	public URL getURL()
	{
		return url;
	}

	@Override
	public RetrievalStatus getStatus()
	{
		return status;
	}

	@Override
	public long getPosition()
	{
		return position;
	}

	@Override
	public long getLength()
	{
		return length;
	}

	@Override
	public float getPercentage()
	{
		return length == 0 ? 1 : length < 0 ? -1 : Math.max(0, Math.min(1, (float) (position / (double) length)));
	}

	@Override
	public void addListener(IRetrievalListener listener)
	{
		listeners.addListener(listener);
	}

	@Override
	public void removeListener(IRetrievalListener listener)
	{
		listeners.removeListener(listener);
	}

	@Override
	public void start()
	{
		synchronized (jobSemaphore)
		{
			if (job == null)
			{
				job = new RetrievalJob(this);
				setPaused(false);
				setCanceled(false);
				job.addJobChangeListener(new JobChangeAdapter()
				{
					@Override
					public void done(IJobChangeEvent event)
					{
						RetrieverResult rr = job.getRetrievalResult();
						result = rr == null ? null : rr.result;

						//ensure the retriever's paused/canceled state matches the result:
						boolean wasPaused = rr == null ? false : rr.status == RetrieverResultStatus.PAUSED;
						boolean wasCanceled = rr == null ? true : rr.status == RetrieverResultStatus.CANCELED;
						setPaused(wasPaused);
						setCanceled(wasCanceled);

						synchronized (jobSemaphore)
						{
							job.removeJobChangeListener(this);
							job = null;
						}

						//if the retrieval wasn't paused, notify the listeners
						if (wasPaused)
						{
							listeners.paused(Retrieval.this);
						}
						else
						{
							listeners.complete(Retrieval.this);
						}
					}
				});
				try
				{
					job.schedule();
				}
				catch (IllegalStateException e)
				{
					//job manager shutdown, ignore
				}
			}
		}
	}

	@Override
	public void pause()
	{
		synchronized (jobSemaphore)
		{
			//can only pause a currently running job
			if (job != null)
			{
				setPaused(true);
			}
		}
	}

	@Override
	public boolean isPaused()
	{
		return paused;
	}

	void setPaused(boolean paused)
	{
		synchronized (pausedSemaphore)
		{
			this.paused = paused;
			if (!paused)
			{
				pausedSemaphore.notifyAll();
			}
		}
	}

	@Override
	public void cancel()
	{
		synchronized (jobSemaphore)
		{
			//can only cancel a currently running job
			if (job != null)
			{
				setCanceled(true);
				job.cancel();
			}
		}
	}

	@Override
	public boolean isCanceled()
	{
		return canceled;
	}

	void setCanceled(boolean canceled)
	{
		this.canceled = canceled;
	}

	@Override
	public IRetrievalData getCachedData()
	{
		return cachedData;
	}

	@Override
	public IRetrievalResult getResult()
	{
		return result;
	}

	@Override
	public boolean hasResult()
	{
		return getResult() != null;
	}

	@Override
	public IRetrievalData getData()
	{
		synchronized (jobSemaphore)
		{
			if (result != null && result.getData() != null)
				return result.getData();
			return cachedData;
		}
	}

	@Override
	public IRetrievalResult waitAndGetResult() throws InterruptedException
	{
		while (checkAndWaitIfPaused() || joinJob())
			;
		return getResult();
	}

	private boolean joinJob() throws InterruptedException
	{
		RetrievalJob job;
		synchronized (jobSemaphore)
		{
			job = this.job;
		}
		if (job != null)
		{
			job.join();
			return true;
		}
		return false;
	}

	private boolean checkAndWaitIfPaused() throws InterruptedException
	{
		synchronized (pausedSemaphore)
		{
			if (isPaused())
			{
				pausedSemaphore.wait();
				return true;
			}
		}
		return false;
	}

	@Override
	public void updateStatus(RetrievalStatus status)
	{
		firePropertyChange("status", getStatus(), this.status = status); //$NON-NLS-1$
		listeners.statusChanged(this);
	}

	@Override
	public void progress(long amount)
	{
		setPosition(getPosition() + amount);
	}

	@Override
	public void setPosition(long position)
	{
		firePropertyChange("position", getPosition(), this.position = position); //$NON-NLS-1$
		listeners.progress(this);
	}

	@Override
	public void setLength(long length)
	{
		firePropertyChange("length", getLength(), this.length = length); //$NON-NLS-1$
	}

	@Override
	public void setCloseable(Closeable closeable)
	{
	}
}
