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
package au.gov.ga.earthsci.application.console;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Addon that creates a {@link StandardOutConsole} on app startup.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ConsoleViewAddon
{
	@Inject
	private IEventBroker broker;
	private EventHandler handler;

	@PostConstruct
	void hookListeners()
	{
		if (handler == null)
		{
			handler = new EventHandler()
			{
				@Override
				public void handleEvent(Event event)
				{
					new StandardOutConsoleFactory().openConsole();
					broker.unsubscribe(handler);
					handler = null;
				}
			};
			broker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, handler);
		}
	}
}
