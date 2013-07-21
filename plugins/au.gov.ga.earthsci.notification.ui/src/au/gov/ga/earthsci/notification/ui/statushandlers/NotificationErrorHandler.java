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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(NotificationErrorHandler.class);

	@Override
	public void handle(StatusAdapter statusAdapter, int style)
	{
		IStatus status = statusAdapter.getStatus();
		NotificationManager.notify(createNotification(status, style));
		switch (status.getSeverity())
		{
		case IStatus.ERROR:
			logger.error(status.getMessage(), status.getException());
			break;
		case IStatus.WARNING:
			logger.warn(status.getMessage(), status.getException());
			break;
		default:
			logger.info(status.getMessage(), status.getException());
			break;
		}
	}

	private INotification createNotification(IStatus status, int style)
	{
		INotification notification = new Notification.Builder(status)
				.requiringAcknowledgement(style == StatusManager.BLOCK)
				.inCategory(getCategory(status)).withThrowable(status.getException())
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

		if (s.getException().getClass().getPackage().getName().startsWith("java.net")) //$NON-NLS-1$
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
