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
package au.gov.ga.earthsci.discovery;

import gov.nasa.worldwind.util.WWXML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.core.persistence.Exportable;
import au.gov.ga.earthsci.core.persistence.Persistent;
import au.gov.ga.earthsci.core.persistence.Persister;
import au.gov.ga.earthsci.core.util.ConfigurationUtil;

/**
 * Manages the {@link IDiscoveryService}s in the application.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
public class DiscoveryServiceManager
{
	private static Persister persister;
	static
	{
		persister = new Persister();
		persister.setIgnoreMissing(true);
		persister.setIgnoreNulls(true);
		persister.registerNamedExportable(PersistentDiscoveryService.class, "Service"); //$NON-NLS-1$
	}

	private static final String servicesFilename = "discoveryServices.xml"; //$NON-NLS-1$
	private static final File servicesFile = ConfigurationUtil.getWorkspaceFile(servicesFilename);
	private static final List<IDiscoveryService> services = new ArrayList<IDiscoveryService>();
	private static final Logger logger = LoggerFactory.getLogger(DiscoveryServiceManager.class);
	private static final Listeners listeners = new Listeners();

	/**
	 * Add a listener for change events on this manager.
	 * 
	 * @param listener
	 *            Listener to add
	 */
	public static void addListener(IDiscoveryServiceManagerListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Remove a listener from this manager.
	 * 
	 * @param listener
	 *            Listener to remove
	 */
	public static void removeListener(IDiscoveryServiceManagerListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * Add a new {@link IDiscoveryService}.
	 * 
	 * @param service
	 *            Service to add
	 */
	public static void addService(IDiscoveryService service)
	{
		services.add(service);
		listeners.serviceAdded(service);
	}

	/**
	 * Add a new {@link IDiscoveryService} at a specific index.
	 * 
	 * @param index
	 *            Index at which to add the service
	 * @param service
	 *            Service to add
	 */
	public static void addService(int index, IDiscoveryService service)
	{
		services.add(index, service);
		listeners.serviceAdded(service);
	}

	/**
	 * Remove the given {@link IDiscoveryService}.
	 * 
	 * @param service
	 *            Service to remove
	 */
	public static void removeService(IDiscoveryService service)
	{
		services.remove(service);
		listeners.serviceRemoved(service);
	}

	/**
	 * @return List of discovery services
	 */
	public static List<IDiscoveryService> getServices()
	{
		return services;
	}

	@PostConstruct
	protected void loadServices()
	{
		if (!servicesFile.exists())
		{
			return;
		}

		FileInputStream is = null;
		try
		{
			is = new FileInputStream(servicesFile);
			Document document = WWXML.createDocumentBuilder(false).parse(is);
			Element parent = document.getDocumentElement();
			Element[] elements = XmlUtil.getElements(parent);
			List<IDiscoveryService> services = new ArrayList<IDiscoveryService>(elements.length);
			for (Element element : elements)
			{
				PersistentDiscoveryService persistent = (PersistentDiscoveryService) persister.load(element, null);
				services.add(persistent.createService());
			}
			DiscoveryServiceManager.services.clear();
			DiscoveryServiceManager.services.addAll(services);
		}
		catch (Exception e)
		{
			logger.error("Error unpersisting discovery services", e); //$NON-NLS-1$		
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	@PreDestroy
	protected void saveServices()
	{
		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(servicesFile);
			DocumentBuilder documentBuilder = WWXML.createDocumentBuilder(false);
			Document document = documentBuilder.newDocument();
			Element element = document.createElement("Services"); //$NON-NLS-1$
			document.appendChild(element);
			for (IDiscoveryService service : services)
			{
				PersistentDiscoveryService persistent = new PersistentDiscoveryService(service);
				persister.save(persistent, element, null);
			}
			XmlUtil.saveDocumentToFormattedStream(document, os);
		}
		catch (Exception e)
		{
			logger.error("Error persisting discovery services", e); //$NON-NLS-1$
		}
		finally
		{
			if (os != null)
			{
				try
				{
					os.close();
				}
				catch (IOException e)
				{
				}
			}
		}
	}

	@Exportable
	protected static class PersistentDiscoveryService
	{
		private String providerId;
		private String name;
		private URL serviceURL;
		private boolean enabled;

		@SuppressWarnings("unused")
		private PersistentDiscoveryService()
		{
			//for unpersistence
		}

		public PersistentDiscoveryService(IDiscoveryService service)
		{
			setProviderId(service.getProvider().getId());
			setServiceURL(service.getServiceURL());
			setName(service.getName());
			boolean enabled =
					(service instanceof MissingPluginPlaceholderDiscoveryService)
							? ((MissingPluginPlaceholderDiscoveryService) service).wasEnabled() : service.isEnabled();
			setEnabled(enabled);
		}

		public IDiscoveryService createService()
		{
			IDiscoveryProvider provider = DiscoveryProviderRegistry.getProviderForId(getProviderId());
			if (provider == null)
			{
				return new MissingPluginPlaceholderDiscoveryService(getProviderId(), getName(), getServiceURL(),
						isEnabled());
			}
			IDiscoveryService service = provider.createService(getName(), getServiceURL());
			service.setEnabled(isEnabled());
			return service;
		}

		@Persistent(attribute = true)
		public String getProviderId()
		{
			return providerId;
		}

		public void setProviderId(String providerId)
		{
			this.providerId = providerId;
		}

		@Persistent(attribute = true)
		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		@Persistent(attribute = true)
		public URL getServiceURL()
		{
			return serviceURL;
		}

		public void setServiceURL(URL serviceURL)
		{
			this.serviceURL = serviceURL;
		}

		@Persistent(attribute = true)
		public boolean isEnabled()
		{
			return enabled;
		}

		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;
		}
	}

	private static class Listeners extends ArrayList<IDiscoveryServiceManagerListener> implements
			IDiscoveryServiceManagerListener
	{
		@Override
		public void serviceAdded(IDiscoveryService service)
		{
			for (int i = size() - 1; i >= 0; i--)
			{
				get(i).serviceAdded(service);
			}
		}

		@Override
		public void serviceRemoved(IDiscoveryService service)
		{
			for (int i = size() - 1; i >= 0; i--)
			{
				get(i).serviceRemoved(service);
			}
		}

	}
}
