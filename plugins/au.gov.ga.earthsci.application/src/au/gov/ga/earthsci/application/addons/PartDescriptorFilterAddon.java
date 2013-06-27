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
package au.gov.ga.earthsci.application.addons;

import java.util.HashSet;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An addon for the application model that cleans up unwanted contributed part
 * descriptors from the application model before it is sent for instantiation.
 * <p/>
 * Used to remove the welcome part etc. which are contributed by external
 * plugins but are not wanted in the platform.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class PartDescriptorFilterAddon
{
	private Logger logger = LoggerFactory.getLogger(PartDescriptorFilterAddon.class);

	@SuppressWarnings("nls")
	/**
	 * The blacklist of {@link MPartDescriptor} element IDs that are to be filtered
	 */
	private static final HashSet<String> BLACKLIST = new HashSet<String>()
	{
		{
			add("org.eclipse.ui.internal.introview");
			add("org.eclipse.e4.ui.compatibility.editor");
		}
	};

	@Inject
	private IEventBroker broker;

	private EventHandler handler;

	@PostConstruct
	void hookListeners(final MApplication application, final EModelService service)
	{
		// Listen to the e4 core service's event broker to find the magical time
		// when the application model has been created and all contributions made
		if (handler == null)
		{
			handler = new EventHandler()
			{
				@Override
				public void handleEvent(Event event)
				{
					try
					{
						Iterator<MPartDescriptor> it = application.getDescriptors().iterator();
						while (it.hasNext())
						{
							MPartDescriptor d = it.next();
							if (BLACKLIST.contains(d.getElementId()))
							{
								logger.trace("Filtering descriptor: " + d.getElementId()); //$NON-NLS-1$
								it.remove();
							}
						}
						// finished successfully: stop listening to the broker.
						broker.unsubscribe(handler);
					}
					catch (Exception e)
					{
						// Something went wrong, the application model was not ready yet.
						// Keep on listening.
						logger.debug("Error filtering descriptors", e); //$NON-NLS-1$
					}
				}
			};
			broker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, handler);
		}
	}

	@PreDestroy
	void unhookListeners()
	{
		if (handler != null)
		{
			// in case it wasn't unhooked earlier
			broker.unsubscribe(handler);
		}
	}

}
