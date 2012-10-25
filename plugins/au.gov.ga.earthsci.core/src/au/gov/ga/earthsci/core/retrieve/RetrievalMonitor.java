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

import org.eclipse.core.runtime.IProgressMonitor;

import au.gov.ga.earthsci.core.util.Validate;

/**
 * An implementation of the {@link IRetrievalMonitor} that reports progress change
 * to {@link IProgressMonitor} and {@link RetrievalJob} instances.
 * <p/>
 * To be used from the {@link RetrievalService} to ensure that job status's are updated appropriately etc.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class RetrievalMonitor implements IRetrievalMonitor
{
	
	private IProgressMonitor progressMonitor;
	private RetrievalJob job;
	
	private RetrievalStatus currentStatus = RetrievalStatus.NOT_STARTED;

	public RetrievalMonitor(IProgressMonitor progressMonitor, RetrievalJob job)
	{
		Validate.notNull(progressMonitor, "A progress monitor is required"); //$NON-NLS-1$
		Validate.notNull(job, "A retrieval job is required"); //$NON-NLS-1$
		
		this.progressMonitor = progressMonitor;
		this.job = job;
	}
	
	@Override
	public RetrievalStatus getRetrievalStatus()
	{
		return currentStatus;
	}

	@Override
	public void notifyStarted()
	{
		currentStatus = RetrievalStatus.STARTED;
		progressMonitor.subTask(Messages.RetrievalMonitor_StartingRetrievalTask);
		updateJobStatus();
	}

	@Override
	public void notifyConnecting()
	{
		currentStatus = RetrievalStatus.CONNECTING;
		progressMonitor.subTask(Messages.RetrievalMonitor_ConnectingResourceTask);
		updateJobStatus();
	}

	@Override
	public void notifyConnected()
	{
		currentStatus = RetrievalStatus.CONNECTED;
		progressMonitor.subTask(Messages.RetrievalMonitor_ConnectedResourceTask);
		updateJobStatus();
	}

	@Override
	public void notifyReading()
	{
		currentStatus = RetrievalStatus.READING;
		progressMonitor.subTask(Messages.RetrievalMonitor_ReadingResourceTask);
		updateJobStatus();
	}

	@Override
	public void notifyCompleted(boolean success)
	{
		currentStatus = success ? RetrievalStatus.SUCCESS : RetrievalStatus.ERROR;
		progressMonitor.subTask(Messages.RetrievalMonitor_CompleteTask);
		updateJobStatus();
	}

	private void updateJobStatus()
	{
		job.setStatus(currentStatus);
	}
	
}
