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

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.osgi.util.NLS;

import au.gov.ga.earthsci.core.retrieve.cache.IURLResourceCache;
import au.gov.ga.earthsci.core.retrieve.preferences.IRetrievalServicePreferences;

/**
 * A default base implementation of the {@link IRetrievalResult} interface.
 * <p/>
 * Provides a mechanism to register {@link IRetriever}s to perform the task of retrieving from specific URL types.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class RetrievalService implements IRetrievalService
{

	@Inject
	private Logger logger;
	
	@Inject
	private IRetrievalServicePreferences preferences;
	
	@Inject
	private IURLResourceCache cache;
	
	private Set<IRetriever> retrievers = new LinkedHashSet<IRetriever>();
	private ReadWriteLock retrieversLock = new ReentrantReadWriteLock();
	
	/**
	 * Register a retriever on this service instance.
	 * 
	 * @param retriever The retriever to register
	 */
	public void registerRetriever(IRetriever retriever)
	{
		if (retriever == null)
		{
			return;
		}
		
		retrieversLock.writeLock().lock();
		try
		{
			retrievers.add(retriever);
		}
		finally
		{
			retrieversLock.writeLock().unlock();
		}
	}

	@Override
	public RetrievalJob retrieve(URL url)
	{
		return retrieve(url, RetrievalMode.BACKGROUND, false);
	}

	@Override
	public RetrievalJob retrieve(final URL url, RetrievalMode mode, final boolean forceRefresh)
	{
		
		if (url == null)
		{
			return null;
		}
		
		// Determine the correct URL to use (cached or live)
		final URL retrievalUrl;
		final boolean fromCache;
		if (cachingEnabled() && !forceRefresh)
		{
			URL cachedUrl = cache.getResource(url);
			if (cachedUrl != null)
			{
				retrievalUrl = cachedUrl;
				fromCache = true;
			}
			else
			{
				retrievalUrl = url;
				fromCache = false;
			}
		}
		else
		{
			retrievalUrl = url;
			fromCache = false;
		}
		
		// Find the correct retriever for the url
		final IRetriever retriever = findRetrieverFor(retrievalUrl);
		if (retriever == null)
		{
			return null;
		}
		
		// Run the retrieval job
		final RetrievalJob job = new RetrievalJob(retrievalUrl)
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				monitor.beginTask(NLS.bind(Messages.RetrievalService_TaskName, retrievalUrl.toExternalForm()), IProgressMonitor.UNKNOWN);

				IRetrievalMonitor retrievalMonitor = new RetrievalMonitor(monitor, this);
				
				IRetrievalResult result;
				result = retriever.retrieve(retrievalUrl, retrievalMonitor);
				if (fromCache)
				{
					markFromCache(retrievalUrl);
				}
				else if (cachingEnabled())
				{
					cache.putResource(retrievalUrl, result.getAsInputStream());
				}
				
				setRetrievalResult(result);
				
				monitor.done();
				return Status.OK_STATUS;
			}

			
		};
		job.setPriority(mode == RetrievalMode.IMMEDIATE ? Job.INTERACTIVE : Job.SHORT);
		job.schedule();
		
		if (mode == RetrievalMode.IMMEDIATE)
		{
			try
			{
				job.join();
			}
			catch (InterruptedException e)
			{
				logger.debug(e, "Thread interrupted while waiting for job completion"); //$NON-NLS-1$
			}
		}
		
		return job;
	}
	
	private boolean cachingEnabled()
	{
		return preferences != null && preferences.isCachingEnabled() && cache != null;
	}
	
	private IRetriever findRetrieverFor(URL url)
	{
		if (url == null)
		{
			return null;
		}
		
		retrieversLock.readLock().lock();
		try
		{
			for (IRetriever r : retrievers)
			{
				if (r.supports(url))
				{
					return r;
				}
			}
			return null;
		}
		finally
		{
			retrieversLock.readLock().unlock();
		}
	}
	
	public void setLogger(Logger l)
	{
		this.logger = l;
	}
	
}
