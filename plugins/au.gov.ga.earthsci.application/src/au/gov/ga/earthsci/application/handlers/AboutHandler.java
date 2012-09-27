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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

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
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell)
	{
		notifications.notify(Notification.create(NotificationLevel.WARNING, "About launched", "You opened the About dialog!")
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
		}).inCategory(NotificationCategory.FILE_IO).build());
		
		MessageDialog.openInformation(shell, "About", "e4 Application example.");
		
	}
}
