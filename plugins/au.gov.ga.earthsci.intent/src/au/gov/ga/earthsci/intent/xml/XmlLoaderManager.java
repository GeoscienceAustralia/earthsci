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
package au.gov.ga.earthsci.intent.xml;

import java.net.URL;
import java.util.Comparator;
import java.util.List;

import javax.inject.Singleton;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import au.gov.ga.earthsci.common.collection.ArrayListTreeMap;
import au.gov.ga.earthsci.common.collection.ListSortedMap;
import au.gov.ga.earthsci.injectable.ExtensionPointHelper;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.util.ContextInjectionFactoryThreadSafe;

/**
 * Injectable {@link IXmlLoader} manager. Provides a centralised mechanism for
 * loading XML documents to objects. {@link IXmlLoader}s can be registered via
 * an extension point.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
public class XmlLoaderManager
{
	private static XmlLoaderManager instance;

	public static XmlLoaderManager getInstance()
	{
		return instance;
	}

	private static final String LOADER_FILTER_ID = "au.gov.ga.earthsci.xmlLoaders"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(XmlLoaderManager.class);

	//filters, sorted descending by priority
	private final ListSortedMap<Integer, XmlLoaderAndFilter> loaders =
			new ArrayListTreeMap<Integer, XmlLoaderAndFilter>(new Comparator<Integer>()
			{
				@Override
				public int compare(Integer o1, Integer o2)
				{
					return -o1.compareTo(o2);
				}
			});

	/**
	 * Constructor, should not be called directly. Instead it should be
	 * injected, or accessed via the static singleton method.
	 */
	public XmlLoaderManager()
	{
		if (instance != null)
		{
			throw new IllegalStateException(XmlLoaderManager.class.getSimpleName() + " should not be instantiated"); //$NON-NLS-1$
		}
		instance = this;

		IConfigurationElement[] config = RegistryFactory.getRegistry().getConfigurationElementsFor(LOADER_FILTER_ID);
		for (IConfigurationElement element : config)
		{
			try
			{
				boolean isLoader = "loader".equals(element.getName()); //$NON-NLS-1$
				if (isLoader)
				{
					@SuppressWarnings("unchecked")
					Class<? extends IXmlLoader> loaderClass =
							(Class<? extends IXmlLoader>) ExtensionPointHelper.getClassForProperty(element, "class"); //$NON-NLS-1$
					@SuppressWarnings("unchecked")
					Class<? extends IXmlLoaderFilter> filterClass =
							(Class<? extends IXmlLoaderFilter>) ExtensionPointHelper.getClassForProperty(element,
									"filter"); //$NON-NLS-1$
					IXmlLoaderFilter filter = filterClass.newInstance();
					XmlLoaderAndFilter loaderAndFilter = new XmlLoaderAndFilter(filter, loaderClass);
					int priority = ExtensionPointHelper.getIntegerForProperty(element, "priority", 0); //$NON-NLS-1$
					loaders.putSingle(priority, loaderAndFilter);
				}
			}
			catch (Exception e)
			{
				logger.error("Error processing dispatch filter", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Load the given document using one of the registered {@link IXmlLoader}s.
	 * 
	 * @param document
	 *            Document to load
	 * @param url
	 *            URL of the document, used to locate relative URLs within the
	 *            document
	 * @param intent
	 *            Intent associated with the XML loading
	 * @param context
	 *            Eclipse context injected into the loader (if found)
	 * @return Object loaded by a matched {@link IXmlLoader}
	 * @throws XmlLoaderNotFoundException
	 *             If a loader that knows how to load the document cannot be
	 *             found
	 */
	public void load(Document document, URL url, Intent intent, IXmlLoaderCallback callback, IEclipseContext context)
			throws XmlLoaderNotFoundException
	{
		XmlLoaderAndFilter loader = findLoader(document, intent);
		if (loader == null)
		{
			throw new XmlLoaderNotFoundException(String.format(
					"XML loader not found for [%s] document from URL: %s", //$NON-NLS-1$
					document == null ? null : document.getDocumentElement().getNodeName(),
					url == null ? null : url.toString()));
		}
		IEclipseContext child = context.createChild();
		IXmlLoader loaderInstance = ContextInjectionFactoryThreadSafe.make(loader.loaderClass, child);
		loaderInstance.load(document, url, intent, callback);
	}

	private XmlLoaderAndFilter findLoader(Document document, Intent intent)
	{
		for (List<XmlLoaderAndFilter> list : loaders.values())
		{
			for (XmlLoaderAndFilter loader : list)
			{
				try
				{
					if (loader.filter.canLoad(document, intent))
					{
						return loader;
					}
				}
				catch (Exception e)
				{
					//ignore exception from any loader's canLoad() method
				}
			}
		}
		return null;
	}

	private class XmlLoaderAndFilter
	{
		public final IXmlLoaderFilter filter;
		public final Class<? extends IXmlLoader> loaderClass;

		public XmlLoaderAndFilter(IXmlLoaderFilter filter, Class<? extends IXmlLoader> loaderClass)
		{
			this.filter = filter;
			this.loaderClass = loaderClass;
		}
	}
}