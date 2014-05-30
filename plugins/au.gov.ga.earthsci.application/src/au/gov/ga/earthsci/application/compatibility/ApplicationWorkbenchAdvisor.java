/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.application.compatibility;

import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.statushandlers.AbstractStatusHandler;

import au.gov.ga.earthsci.notification.ui.statushandlers.NotificationErrorHandler;

/**
 * This workbench advisor creates the window advisor, and specifies the
 * perspective id for the initial window.
 */
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor
{
	@Override
	public String getInitialWindowPerspectiveId()
	{
		return "au.gov.ga.earthsci.application.perspective"; //$NON-NLS-1$
	}

	@Override
	public synchronized AbstractStatusHandler getWorkbenchErrorHandler()
	{
		return new NotificationErrorHandler();
	}
}
