/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.notification.ui.statushandlers;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.statushandlers.AbstractStatusHandler;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

import au.gov.ga.earthsci.notification.INotification;
import au.gov.ga.earthsci.notification.Notification;
import au.gov.ga.earthsci.notification.NotificationCategory;
import au.gov.ga.earthsci.notification.NotificationManager;

/**
 * An {@link AbstractStatusHandler} that diverts IStatus reporting to the
 * Notification mechanism
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class NotificationErrorHandler extends AbstractStatusHandler
{

	@Override
	public void handle(StatusAdapter statusAdapter, int style)
	{
		NotificationManager.notify(createNotification(statusAdapter, style));
	}

	private INotification createNotification(StatusAdapter adapter, int style)
	{

		INotification notification = new Notification.Builder(adapter.getStatus())
				.requiringAcknowledgement(style == StatusManager.BLOCK)
				.inCategory(getCategory(adapter.getStatus()))
				.build();

		return notification;
	}

	private NotificationCategory getCategory(IStatus s)
	{
		// Try some guessing here...
		if (s.getException() == null)
		{
			return NotificationCategory.GENERAL;
		}

		if (s.getException().getClass().getPackage().getName().equalsIgnoreCase("java.net"))
		{
			return NotificationCategory.DOWNLOAD;
		}

		if (s.getException() instanceof IOException)
		{
			return NotificationCategory.FILE_IO;
		}

		return NotificationCategory.GENERAL;
	}

}
