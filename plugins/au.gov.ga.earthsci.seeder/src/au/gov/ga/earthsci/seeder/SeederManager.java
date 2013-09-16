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
package au.gov.ga.earthsci.seeder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.common.persistence.Exportable;
import au.gov.ga.earthsci.common.persistence.Persistent;
import au.gov.ga.earthsci.common.persistence.Persister;
import au.gov.ga.earthsci.common.util.ConfigurationUtil;
import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.injectable.ExtensionPointHelper;

/**
 * Manages a collection of seeders, defined by the
 * {@value #SEEDERS_EXTENSION_POINT_ID} extension point.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
public class SeederManager
{
	public static final String SEEDERS_EXTENSION_POINT_ID = "au.gov.ga.earthsci.seeders"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(SeederManager.class);

	@Inject
	private static IEclipseContext context;

	private static final Map<String, Class<ISeeder>> seeders = new HashMap<String, Class<ISeeder>>();
	private static final Map<Class<ISeeder>, ISeeder> seederCache = new HashMap<Class<ISeeder>, ISeeder>();

	private static final String seederFilename = "seeder.xml"; //$NON-NLS-1$
	private static final File seederFile = ConfigurationUtil.getWorkspaceFile(seederFilename);
	private static final Persister persister;

	private static SeederHistory history = new SeederHistory();

	static
	{
		persister = new Persister();
		persister.setIgnoreMissing(true);
		persister.setIgnoreNulls(true);
		persister.registerNamedExportable(SeederHistory.class, "History"); //$NON-NLS-1$
		persister.registerNamedExportable(SeederRevision.class, "Seeder"); //$NON-NLS-1$

		IConfigurationElement[] config =
				RegistryFactory.getRegistry().getConfigurationElementsFor(SEEDERS_EXTENSION_POINT_ID);
		for (IConfigurationElement element : config)
		{
			try
			{
				String elementName = element.getAttribute("element"); //$NON-NLS-1$
				if (seeders.containsKey(elementName))
				{
					throw new Exception("Element name '" + elementName + "' already exists in the seeder registry"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				@SuppressWarnings("unchecked")
				Class<ISeeder> seederClass =
						(Class<ISeeder>) ExtensionPointHelper.getClassForProperty(element, "class"); //$NON-NLS-1$
				seeders.put(elementName, seederClass);
			}
			catch (Exception e)
			{
				logger.error("Error processing intent filter", e); //$NON-NLS-1$
			}
		}
	}

	public static void addSeeder(String elementName, Class<ISeeder> seederClass)
	{
		seeders.put(elementName, seederClass);
	}

	public static void removeSeeder(String elementName)
	{
		seeders.remove(elementName);
	}

	public static void seed(Document document, URL url)
	{
		int lastRevision = history.getRevision(url);
		int maxRevision = -1;

		Element documentElement = document.getDocumentElement();
		Element[] children = XmlUtil.getElements(documentElement);
		for (Element child : children)
		{
			int revision = -1;
			try
			{
				String revisionString = child.getAttribute("revision"); //$NON-NLS-1$
				revision = Integer.parseInt(revisionString);
			}
			catch (Exception e)
			{
				//ignore
			}

			//if newer than last (or revision attribute doesn't exist), then apply this revision
			if (revision > lastRevision || revision < 0)
			{
				Class<ISeeder> seederClass = seeders.get(child.getNodeName());
				if (seederClass != null)
				{
					ISeeder seeder = seederCache.get(seederClass);
					if (seeder == null)
					{
						seeder = ContextInjectionFactory.make(seederClass, context);
						seederCache.put(seederClass, seeder);
					}
					seeder.seed(child, url);
					maxRevision = Math.max(maxRevision, revision);
				}
				else
				{
					logger.error("No seeder found for element: " + child.getNodeName()); //$NON-NLS-1$
				}
			}
		}

		if (maxRevision > lastRevision)
		{
			history.setRevision(url, maxRevision);
		}
	}

	@PostConstruct
	public void loadSeederFile()
	{
		try
		{
			if (seederFile.exists())
			{
				Document document = XmlUtil.createDocumentBuilder().parse(seederFile);
				Element parent = document.getDocumentElement();
				Element element = XmlUtil.getFirstChildElement(parent);
				history = (SeederHistory) persister.load(element, seederFile.toURI());
			}
		}
		catch (Exception e)
		{
			logger.warn("Error loading seeder configuration file", e); //$NON-NLS-1$
		}
	}

	@PreDestroy
	protected void saveSeederFile()
	{
		OutputStream os = null;
		try
		{
			DocumentBuilder documentBuilder = XmlUtil.createDocumentBuilder();
			Document document = documentBuilder.newDocument();
			Element element = document.createElement("Seeder"); //$NON-NLS-1$
			document.appendChild(element);
			persister.save(history, element, seederFile.toURI());
			os = new FileOutputStream(seederFile);
			XmlUtil.saveDocumentToFormattedStream(document, os);
		}
		catch (Exception e)
		{
			logger.error("Error saving seeder configuration file", e); //$NON-NLS-1$
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
	private static class SeederHistory
	{
		private final Map<URL, SeederRevision> map = new HashMap<URL, SeederRevision>();

		@Persistent
		public SeederRevision[] getRevisions()
		{
			Collection<SeederRevision> values = map.values();
			return values.toArray(new SeederRevision[values.size()]);
		}

		//used by persistence mechanism
		@SuppressWarnings("unused")
		public void setRevisions(SeederRevision[] revisions)
		{
			map.clear();
			if (revisions != null)
			{
				for (SeederRevision revision : revisions)
				{
					map.put(revision.url, revision);
				}
			}
		}

		public int getRevision(URL url)
		{
			SeederRevision revision = map.get(url);
			return revision != null ? revision.revision : -1;
		}

		public void setRevision(URL url, int revision)
		{
			SeederRevision value = new SeederRevision();
			value.url = url;
			value.revision = revision;
			map.put(url, value);
		}
	}

	@Exportable
	private static class SeederRevision
	{
		@Persistent(attribute = true)
		public URL url;
		@Persistent(attribute = true)
		public int revision;
	}
}
