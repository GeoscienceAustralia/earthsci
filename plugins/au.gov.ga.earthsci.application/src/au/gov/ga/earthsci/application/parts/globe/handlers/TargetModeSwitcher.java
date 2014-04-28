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
package au.gov.ga.earthsci.application.parts.globe.handlers;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.services.events.IEventBroker;

import au.gov.ga.earthsci.worldwind.common.view.target.ITargetView;
import au.gov.ga.earthsci.worldwind.common.view.target.TargetOrbitView;

/**
 * Toggles a {@link TargetOrbitView}'s target mode on/off, and sends an event
 * using the Eclipse {@link IEventBroker}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
public class TargetModeSwitcher
{
	public static final String TARGET_MODE_EVENT_TOPIC = "au/gov/ga/earthsci/application/view/targetMode"; //$NON-NLS-1$

	@Inject
	private static IEventBroker eventBroker;

	public static void setTargetMode(ITargetView view, boolean targetMode)
	{
		view.setTargetMode(targetMode);
		eventBroker.send(TARGET_MODE_EVENT_TOPIC, view);
	}
}
