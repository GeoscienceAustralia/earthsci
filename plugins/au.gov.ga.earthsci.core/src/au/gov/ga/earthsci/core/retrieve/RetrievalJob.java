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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

/**
 * A {@link Job} that performs retrieval of a resource.
 * <p/>
 * Clients can obtain the retrieval result when it is available by using {@link #getRetrievalResult()}. Job completion can be queried
 * using {@link #isCompleted()}. Alternatively, clients can use the blocking method {@link #waitAndGetRetrievalResult()} to synchronise
 * on the job and retrieve the result when it becomes available.
 * E.g.
 * <pre>
 * IRetrievalResult result = job.waitAndGetResult();
 * if (result.isSuccessful())
 * {
 *   // do something
 * }
 * </pre>
 * 
 * <p/>
 * For asynchronous access to the result, the standard Jobs API listener mechanism can be used to listen for completion.
 * E.g.
 * <pre>
 * job.addJobChangeListener(new JobChangeAdapter(){
 *      private void done(JobChangeEvent e)
 *      {
 *          if (event.getResult() != Status.OK_STATUS)
 *          {
 *              // do something
 *          }
 *          
 *          IRetrievalResult result = job.getResult();
 *          if (result.isSuccessful())
 *          {
 *             // do something
 *          }
 *      }
 * });
 * </pre>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class RetrievalJob extends Job
{
	
	/** 
	 * The URL provided to initiate the retrieval 
	 */
	private URL sourceURL;
	
	/** 
	 * The URL of the actual resource retrieved. 
	 * May differ from {@link #sourceURL} if returned from a cache etc.
	 */
	private URL actualURL;
	
	/**
	 * Whether this job used a cached version of the resource
	 */
	private boolean fromCache = false;

	/**
	 * Whether this job has completed yet or not
	 */
	private boolean completed = false;
	
	/**
	 * The result of the retrieval. Will be <code>null</code> until the job completes.
	 * 
	 * @see #isCompleted()
	 */
	private IRetrievalResult result;
	
	/**
	 * The current status of the retrieval job
	 */
	private AtomicReference<RetrievalStatus> currentStatus = new AtomicReference<RetrievalStatus>(RetrievalStatus.NOT_STARTED);
	
	public RetrievalJob(URL resourceURL)
	{
		super(NLS.bind(Messages.RetrievalJob_JobName, resourceURL.toExternalForm()));
		this.sourceURL = resourceURL;
	}
	
	/**
	 * Returns the retrieval result if it is available, or <code>null</code> if the retrieval has not yet completed.
	 * <p/>
	 * Clients can use {@link #isCompleted()} to query the completion status of the job. Alternatively, the method 
	 * {@link #waitAndGetRetrievalResult()} can be used to block until 
	 * 
	 * @return the retrieval result, or <code>null</code> if the retrieval has not yet completed.
	 * 
	 * @see #waitAndGetRetrievalResult()
	 * @see #isCompleted()
	 */
	public IRetrievalResult getRetrievalResult()
	{
		return result;
	}

	/**
	 * Set the retrieval result on this job
	 */
	protected void setRetrievalResult(IRetrievalResult result)
	{
		this.result = result;
	}
	
	/**
	 * Blocks the calling thread until the job has completed and a retrieval result is available.
	 * 
	 * @return the retrieval result
	 * 
	 * @throws InterruptedException if the thread is interrupted prior to job completion
	 * 
	 * @see #getRetrievalResult()
	 */
	public IRetrievalResult waitAndGetRetrievalResult() throws InterruptedException
	{
		join();
		return result;
	}
	
	/**
	 * Returns whether this job has completed or not.
	 * <p/>
	 * If <code>true</code>, a result will be immediately available via {@link #getRetrievalResult()}.
	 * 
	 * @return <code>true</code> if completed, <code>false</code> otherwise
	 */
	public boolean isCompleted()
	{
		return completed;
	}
	
	/**
	 * Returns the URL provided to initiate this retrieval job.
	 * 
	 * @return the URL provided to initiate this retrieval job
	 */
	public URL getSourceURL()
	{
		return sourceURL;
	}
	
	/**
	 * Returns the URL of the resource returned in {@link #getRetrievalResult()}. This may differ from
	 * the URL returned by {@link #getSourceURL()} if the result was obtained from e.g. a cache.
	 * 
	 * @return the actual URL of the resource returned in {@link #getRetrievalResult()}.
	 */
	public URL getResultURL()
	{
		return actualURL;
	}
	
	/**
	 * Mark that this job is actually retrieving a result from a cache.
	 * <p/>
	 * Intended to be called only within a {@link RetrievalJob} instance 
	 * (e.g. within {@link #run(org.eclipse.core.runtime.IProgressMonitor)})
	 * 
	 * @param cachedURL The URL of the cached resource
	 */
	protected void markFromCache(URL cachedURL)
	{
		this.actualURL = cachedURL;
		this.fromCache = true;
	}
	
	/**
	 * Returns whether this retrieval job is actually retrieving a cached resource.
	 * 
	 * @return <code>true</code> if the resource being retrieved is a cached resource, <code>false</code> otherwise.
	 * 
	 * @see #getResultURL()
	 */
	public boolean isFromCache()
	{
		return fromCache;
	}
	
	/**
	 * Returns the status of the retrieval this job represents.
	 * <p/>
	 * <b>Note:</b> that this is different to the {@link #getState()} method, which returns the state
	 * of the job itself within the job manager.
	 * 
	 * @return the status of this job
	 */
	public RetrievalStatus getStatus()
	{
		return currentStatus.get();
	}
	
	/**
	 * Set the status of the retrieval this job represents.
	 * <p/>
	 * @param status
	 */
	void setStatus(RetrievalStatus status)
	{
		if (status == null)
		{
			return;
		}
		currentStatus.set(status);
	}
}
