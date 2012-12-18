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
import java.io.IOException;

import org.eclipse.core.internal.jobs.JobStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import au.gov.ga.earthsci.core.retrieve.result.ErrorRetrievalResult;

/**
 * {@link Job} used by a {@link Retrieval} to retrieve resources.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RetrievalJob extends Job
{
	private final Retrieval retrieval;
	private RetrieverResult result;
	private Closeable closeable;

	public RetrievalJob(Retrieval retrieval)
	{
		super("Retrieving " + retrieval.getURL()); //$NON-NLS-1$
		this.retrieval = retrieval;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor)
	{
		final int totalWork = 1000;

		//mark the job as begun
		monitor.beginTask(getName(), totalWork);

		//create a listener for updating the monitor progress
		IRetrievalListener listener = new RetrievalAdapter()
		{
			private int work = 0;

			@Override
			public void progress(IRetrieval retrieval)
			{
				synchronized (this)
				{
					float percentage = Math.max(0f, retrieval.getPercentage());
					int progress = Math.max(work, Math.min(totalWork, Math.round(percentage * totalWork)));
					int delta = progress - work;
					work = progress;
					if (delta > 0)
					{
						monitor.worked(delta);
					}
				}
			}

			@Override
			public void complete(IRetrieval retrieval)
			{
				retrieval.removeListener(this);
			}
		};
		retrieval.addListener(listener);

		//set the initial state of the monitor progress
		listener.progress(retrieval);

		//perform the retrieval
		IRetrieverMonitor retrieverMonitor = new RetrieverMonitorDelegate(retrieval)
		{
			@Override
			public boolean isCanceled()
			{
				if (monitor.isCanceled())
				{
					retrieval.setCanceled(true);
					//super.isCanceled will now return true
				}
				return super.isCanceled();
			}

			@Override
			public void setCloseable(Closeable closeable)
			{
				super.setCloseable(closeable);
				RetrievalJob.this.closeable = closeable;
			}
		};
		try
		{
			result = retrieval.retrieve(retrieverMonitor);
		}
		catch (Exception e)
		{
			result = new RetrieverResult(new ErrorRetrievalResult(e), RetrieverResultStatus.ERROR);
		}

		//mark the job as done
		monitor.done();

		//TODO not sure about this return value
		boolean error = result.status == RetrieverResultStatus.ERROR;
		Exception exception = result.result == null ? null : result.result.getError();
		String message = exception == null ? null : exception.getLocalizedMessage();
		return !error ? JobStatus.OK_STATUS : new JobStatus(IStatus.ERROR, this, message);
	}

	public RetrieverResult getRetrievalResult()
	{
		return result;
	}

	@Override
	protected void canceling()
	{
		if (getState() == Job.RUNNING && closeable != null)
		{
			try
			{
				closeable.close();
			}
			catch (IOException e)
			{
				//ignore
			}
		}
	}
}
