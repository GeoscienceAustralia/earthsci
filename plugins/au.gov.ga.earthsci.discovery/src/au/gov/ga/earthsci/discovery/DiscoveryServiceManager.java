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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.gov.ga.earthsci.common.persistence.Adapter;
import au.gov.ga.earthsci.common.persistence.ElementPersistentAdapter;
import au.gov.ga.earthsci.common.persistence.Exportable;
import au.gov.ga.earthsci.common.persistence.PersistenceException;
import au.gov.ga.earthsci.common.persistence.Persistent;
import au.gov.ga.earthsci.common.persistence.Persister;
import au.gov.ga.earthsci.common.util.ConfigurationUtil;
import au.gov.ga.earthsci.common.util.XmlUtil;

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
	private static final Set<IDiscoveryService> services = new HashSet<IDiscoveryService>();
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
	public static Set<IDiscoveryService> getServices()
	{
		return Collections.unmodifiableSet(services);
	}

	@PostConstruct
	protected void loadServices()
	{
		try
		{
			List<IDiscoveryService> services = loadServices(servicesFile);
			if (services != null)
			{
				for (IDiscoveryService service : services)
				{
					DiscoveryServiceManager.addService(service);
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Error unpersisting discovery services", e); //$NON-NLS-1$		
		}
	}

	@PreDestroy
	protected void saveServices()
	{
		try
		{
			saveServices(services, servicesFile);
		}
		catch (Exception e)
		{
			logger.error("Error persisting discovery services", e); //$NON-NLS-1$
		}
	}

	/**
	 * Load a list of discovery services from an XML file. Returns
	 * <code>null</code> if the file doesn't exist.
	 * 
	 * @param inputFile
	 *            XML file to load from
	 * @return List of loaded discovery services
	 * @throws IOException
	 * @throws SAXException
	 * @throws PersistenceException
	 */
	public static List<IDiscoveryService> loadServices(File inputFile) throws IOException, SAXException,
			PersistenceException
	{
		if (!inputFile.exists())
		{
			return null;
		}

		FileInputStream is = null;
		try
		{
			is = new FileInputStream(inputFile);
			Document document = WWXML.createDocumentBuilder(false).parse(is);
			Element parent = document.getDocumentElement();
			return loadServices(parent);
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

	public static List<IDiscoveryService> loadServices(Element servicesElement) throws PersistenceException
	{
		Element[] elements = XmlUtil.getElements(servicesElement);
		List<IDiscoveryService> services = new ArrayList<IDiscoveryService>(elements.length);
		for (Element element : elements)
		{
			PersistentDiscoveryService persistent = (PersistentDiscoveryService) persister.load(element, null);
			services.add(persistent.createService());
		}
		return services;
	}

	/**
	 * Save the given collection of discovery services to a file as formatted
	 * XML.
	 * 
	 * @param services
	 *            Services to save
	 * @param outputFile
	 *            Output file to save to
	 * @throws TransformerException
	 * @throws IOException
	 * @throws PersistenceException
	 */
	public static void saveServices(Collection<IDiscoveryService> services, File outputFile)
			throws TransformerException, IOException, PersistenceException
	{
		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(outputFile);
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
		private static final String PROPERTY_ELEMENT_NAME = "property"; //$NON-NLS-1$
		private static final String ID_ATTRIBUTE_NAME = "id"; //$NON-NLS-1$

		private String providerId;
		private String name;
		private URL serviceURL;
		private boolean enabled;
		private Element propertiesElement;

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

			if (service instanceof MissingPluginPlaceholderDiscoveryService)
			{
				MissingPluginPlaceholderDiscoveryService missing = (MissingPluginPlaceholderDiscoveryService) service;
				setEnabled(missing.wasEnabled());
				setProperties(missing.getPropertiesElement());
			}
			else
			{
				setEnabled(service.isEnabled());
				setupPropertiesElement(service);
			}
		}

		public IDiscoveryService createService()
		{
			IDiscoveryProvider provider = DiscoveryProviderRegistry.getProviderForId(getProviderId());
			if (provider == null)
			{
				logger.warn("Missing Discovery provider with id '" + getProviderId() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
				return new MissingPluginPlaceholderDiscoveryService(getProviderId(), getName(), getServiceURL(),
						isEnabled(), propertiesElement);
			}

			Map<IDiscoveryServiceProperty<?>, Object> propertyValues = getPropertyValues(provider);
			IDiscoveryService service = provider.createService(getName(), getServiceURL(), propertyValues);
			service.setEnabled(isEnabled());
			return service;
		}

		private void setupPropertiesElement(IDiscoveryService service)
		{
			propertiesElement = null;
			if (service == null || service.getProvider() == null || service.getProvider().getProperties() == null)
			{
				return;
			}

			try
			{
				@SuppressWarnings("unchecked")
				IDiscoveryServiceProperty<Object>[] objectProperties =
						(IDiscoveryServiceProperty<Object>[]) service.getProvider().getProperties();
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				propertiesElement = document.createElement("root"); //$NON-NLS-1$

				for (IDiscoveryServiceProperty<Object> property : objectProperties)
				{
					Object value = property.getValue(service);
					Element propertyElement = document.createElement(PROPERTY_ELEMENT_NAME);
					propertyElement.setAttribute(ID_ATTRIBUTE_NAME, property.getId());
					property.persist(propertyElement, value);
					propertiesElement.appendChild(propertyElement);
				}
			}
			catch (ParserConfigurationException e)
			{
				logger.error("Error creating discovery properties XML element", e); //$NON-NLS-1$
			}
		}

		private Map<IDiscoveryServiceProperty<?>, Object> getPropertyValues(IDiscoveryProvider provider)
		{
			Map<IDiscoveryServiceProperty<?>, Object> propertyValues =
					new HashMap<IDiscoveryServiceProperty<?>, Object>();

			@SuppressWarnings("unchecked")
			IDiscoveryServiceProperty<Object>[] properties =
					(IDiscoveryServiceProperty<Object>[]) provider.getProperties();
			if (properties != null)
			{
				Map<String, Element> propertyElements = new HashMap<String, Element>();
				if (propertiesElement != null)
				{
					NodeList children = propertiesElement.getElementsByTagName(PROPERTY_ELEMENT_NAME);
					for (int i = 0; i < children.getLength(); i++)
					{
						Element child = (Element) children.item(i);
						String id = child.getAttribute(ID_ATTRIBUTE_NAME);
						if (id != null)
						{
							propertyElements.put(id, child);
						}
					}
				}

				for (IDiscoveryServiceProperty<Object> property : properties)
				{
					Element propertyElement = propertyElements.get(property.getId());
					Object value = propertyElement == null ? null : property.unpersist(propertyElement);
					propertyValues.put(property, value);
				}
			}

			return propertyValues;
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

		@Persistent
		@Adapter(ElementPersistentAdapter.class)
		public Element getProperties()
		{
			return propertiesElement;
		}

		public void setProperties(Element propertiesElement)
		{
			this.propertiesElement = propertiesElement;
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
