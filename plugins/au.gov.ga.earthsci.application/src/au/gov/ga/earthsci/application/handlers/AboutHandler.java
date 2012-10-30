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
package au.gov.ga.earthsci.application.handlers;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.core.retrieve.IRetrievalService;
import au.gov.ga.earthsci.core.retrieve.RetrievalJob;
import au.gov.ga.earthsci.notification.INotificationAction;
import au.gov.ga.earthsci.notification.Notification;
import au.gov.ga.earthsci.notification.NotificationCategory;
import au.gov.ga.earthsci.notification.NotificationLevel;
import au.gov.ga.earthsci.notification.NotificationManager;

/**
 * Handler which shows the About dialog box.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AboutHandler
{
	@Inject
	private NotificationManager notifications;
	
	@Inject
	private IRetrievalService retrievalService;
	
	private int count = 0;
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell)
	{
		count++;
		String title = count + " About launched";
		
		NotificationLevel level = NotificationLevel.values()[count%3];
		NotificationCategory category = (NotificationCategory)NotificationCategory.getRegisteredCategories().toArray()[count%3];
		
		notifications.notify(Notification.create(level, title, count + "You opened the About dialog sssssssssssssssssss sssssssssss ssssss ssss s sssssssssssssssss!")
		.withAction(new INotificationAction()
		{
			@Override
			public void run()
			{
				System.out.println(NotificationCategory.FILE_IO.getLabel());
			}
			
			@Override
			public String getTooltip()
			{
				return "This is a test action";
			}
			
			@Override
			public String getText()
			{
				return "Click me";
			} 
		}).inCategory(category)
		.requiringAcknowledgement(new INotificationAction()
		{
			
			@Override
			public void run()
			{
				System.out.println("Alrighty then!");
			}
			
			@Override
			public String getTooltip()
			{
				return "This is a tooltip";
			}
			
			@Override
			public String getText()
			{
				return "Action!";
			}
		}).build());
		
		MessageDialog.openInformation(shell, "About", "e4 Application example.");
		
		Job dummyJob = new Job("Test Job " + count)
		{
			
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				monitor.beginTask("Some work", 10);
				for (int i = 0; i < 10; i++)
				{
					monitor.worked(1);
					try
					{
						monitor.subTask("Some work..." + i);
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						
					}
				}
				monitor.done();
				
				return Status.OK_STATUS;
			}
		};
		dummyJob.schedule();
	
		try
		{
			final RetrievalJob retrievalJob = retrievalService.retrieve(new URL("http://www.google.com")); //$NON-NLS-1$
			retrievalJob.addJobChangeListener(new JobChangeAdapter()
			{
				@Override
				public void done(IJobChangeEvent event)
				{
					System.out.println("Retrieval done: " + event.getResult().getMessage() + ", " + retrievalJob.getRetrievalResult().isSuccessful());
					if (!retrievalJob.getRetrievalResult().isSuccessful())
					{
						System.out.println(retrievalJob.getRetrievalResult().getMessage());
					}
					if (retrievalJob.getRetrievalResult().getException() != null)
					{
						retrievalJob.getRetrievalResult().getException().printStackTrace();
					}
					if (retrievalJob.getRetrievalResult().isSuccessful())
					{
						System.out.println(retrievalJob.getRetrievalResult().getAsString());
					}
				}
			});
			System.out.println("Im asynchronous!");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		
	}
}
