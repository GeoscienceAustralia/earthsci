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
package au.gov.ga.earthsci.application;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import au.gov.ga.earthsci.application.catalog.CatalogSelectionDialog;
import au.gov.ga.earthsci.intent.IntentManager;
import au.gov.ga.earthsci.layer.worldwind.WorldWindModel;

/**
 * Addon that runs some initialization after the application startup is
 * complete.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PostApplicationStartupAddon
{
	@Inject
	private IEventBroker broker;
	private EventHandler handler;

	@Inject
	private WorldWindModel worldWindModel;

	@PostConstruct
	public void subscribe(final MApplication application, final EModelService service, final EPartService partService)
	{
		if (handler == null)
		{
			handler = new EventHandler()
			{
				@Override
				public void handleEvent(Event event)
				{
					CatalogSelectionDialog.openDialog(application.getContext(), worldWindModel);
					PreferencePageFilter.filter();
					PartDescriptorFilter.run(application, service);
					PartInstantiator.createParts(application, service, partService);
					IntentManager.getInstance().beginExecution();
					QuickAccessHider.hide(application, service);

					unsubscribe();
				}
			};
			broker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, handler);
		}
	}

	@PreDestroy
	public void unsubscribe()
	{
		if (handler != null)
		{
			// in case it wasn't unhooked earlier
			broker.unsubscribe(handler);
			handler = null;
		}
	}
}
