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
package au.gov.ga.earthsci.notification.ui.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;

import au.gov.ga.earthsci.notification.ui.NotificationPart;
import au.gov.ga.earthsci.notification.ui.NotificationPartReceiver;

/**
 * Handler that clears the notification view.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ClearHandler
{
	@Execute
	public void execute(NotificationPartReceiver receiver, NotificationPart part)
	{
		receiver.getNotifications().clear();
		part.reloadNotificationTree();
	}

	@CanExecute
	public boolean canExecute(NotificationPartReceiver receiver)
	{
		return !receiver.getNotifications().isEmpty();
	}
}
