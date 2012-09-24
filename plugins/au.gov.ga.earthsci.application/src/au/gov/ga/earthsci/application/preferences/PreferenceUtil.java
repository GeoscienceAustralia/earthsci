/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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
package au.gov.ga.earthsci.application.preferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Utility class containing methods used to handle the application preferences.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PreferenceUtil
{
	private static final String ELMT_PAGE = "page"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_CATEGORY = "category"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$

	// Some plugins from v3 provide preferences that break the system. This is a
	// hack to fix that. Add the ID of blacklist plugins here.
	private static final Set<String> BLACKLIST_PREFERENCES = new HashSet<String>();
	static
	{
		BLACKLIST_PREFERENCES.add("org.eclipse.help.ui"); //$NON-NLS-1$
	}
	
	@Inject
	public static Logger logger;
	
	/**
	 * Create a {@link PreferenceManager} instance which contains all the
	 * {@link PreferencePage}s listed in the legacy plugin.xml. These are all
	 * the 'org.eclipse.ui.preferencePages' extension points.
	 * <p/>
	 * The e4 platform doesn't support these extension points; this method helps
	 * enumerate them manually.
	 * 
	 * @param context
	 *            Application context
	 * @param registry
	 *            Extension registry
	 * @return {@link PreferenceManager} containing the preference pages
	 */
	public static PreferenceManager createLegacyPreferenceManager(IEclipseContext context, IExtensionRegistry registry)
	{
		List<IConfigurationElement> preferenceElements = findPreferenceElementsFromRegistry(registry);
		
		PreferenceManager pm = new PreferenceManager();
		
		populatePreferenceManager(pm, context, preferenceElements);
		
		return pm;
	}

	/**
	 * Populate the provided preference manager with nodes from the provided list of preference elements.
	 * 
	 * @param pm The preference manager to populate
	 * @param preferenceElements The list of preference elements from which to populate the manager
	 * @param context The current eclipse context for DI etc.
	 */
	private static void populatePreferenceManager(PreferenceManager pm, IEclipseContext context, List<IConfigurationElement> preferenceElements)
	{
		// Add nodes in 3 phases:
		// 1. Add all root nodes (no category specified)
		// 2. Progressively add child nodes to build up the node tree, breadth first
		// 3. Add remaining (orphan) child nodes, along with a warning
		
		// Add root nodes
		int maxPathLength = 0;
		List<IConfigurationElement> remainingElements = new ArrayList<IConfigurationElement>();
		for (IConfigurationElement elmt : preferenceElements)
		{
			if (isEmpty(elmt.getAttribute(ATTR_CATEGORY)))
			{
				IPreferenceNode node = createPreferenceNode(elmt, context);
				if (node == null)
				{
					continue;
				}
				pm.addToRoot(node);
			}
			else
			{
				int categoryPathLength = elmt.getAttribute(ATTR_CATEGORY).split("\\\\").length; //$NON-NLS-1$
				maxPathLength = Math.max(maxPathLength, categoryPathLength);
				remainingElements.add(elmt);
			}
		}
		
		// Add child nodes
		preferenceElements = remainingElements;
		for (int i = 0; i < maxPathLength; i++)
		{
			remainingElements = new ArrayList<IConfigurationElement>();
			for (IConfigurationElement elmt : preferenceElements)
			{
				IPreferenceNode parent = findNode(pm, elmt.getAttribute(ATTR_CATEGORY));
				if (parent != null)
				{
					IPreferenceNode node = createPreferenceNode(elmt, context);
					parent.add(node);
				}
				else
				{
					remainingElements.add(elmt);
				}
			}
			preferenceElements = remainingElements;
		}
		
		// Finally, add any nodes that are parent-less, along with a warning
		for (IConfigurationElement elmt : preferenceElements)
		{
			logger.warn("Preference page {0} expected category {1} but none was found.", elmt.getAttribute(ATTR_ID), elmt.getAttribute(ATTR_CATEGORY)); //$NON-NLS-1$
			IPreferenceNode node = createPreferenceNode(elmt, context);
			if (node == null)
			{
				continue;
			}
			pm.addToRoot(node);
		}
	}

	private static List<IConfigurationElement> findPreferenceElementsFromRegistry(IExtensionRegistry registry)
	{
		List<IConfigurationElement> elements = new ArrayList<IConfigurationElement>();
		for (IConfigurationElement elmt : registry.getConfigurationElementsFor(PreferenceConstants.PAGES_EXTENSION_POINT))
		{
			if (BLACKLIST_PREFERENCES.contains(elmt.getContributor().getName()))
			{
				logger.info("Ignoring preferences from element {0}", elmt.getContributor().getName()); //$NON-NLS-1$
				continue;
			}
			if (!elmt.getName().equals(ELMT_PAGE))
			{
				logger.warn("Unexpected element: {0}", elmt.getName()); //$NON-NLS-1$
				continue;
			}
			else if (isEmpty(elmt.getAttribute(ATTR_ID)) || isEmpty(elmt.getAttribute(ATTR_NAME)))
			{
				logger.warn("Missing id and/or name: {0}", elmt.getNamespaceIdentifier()); //$NON-NLS-1$
				continue;
			}
			
			elements.add(elmt);
		}
		return elements;
	}

	private static IPreferenceNode createPreferenceNode(IConfigurationElement elmt, IEclipseContext context)
	{
		if (elmt.getAttribute(ATTR_CLASS) != null)
		{
			IPreferencePage page = null;
			try
			{
				String prefPageURI = getClassURI(elmt.getNamespaceIdentifier(), elmt.getAttribute(ATTR_CLASS));
				Object object = context.get(IContributionFactory.class).create(prefPageURI, context);
				if (!(object instanceof IPreferencePage))
				{
					logger.error("Expected instance of IPreferencePage: {0}", elmt.getAttribute(ATTR_CLASS)); //$NON-NLS-1$
					return null;
				}
				page = (IPreferencePage) object;
			}
			catch (Exception e)
			{
				logger.error(e);
				return null;
			}
			
			ContextInjectionFactory.inject(page, context);
			if ((page.getTitle() == null || page.getTitle().isEmpty()) && elmt.getAttribute(ATTR_NAME) != null)
			{
				page.setTitle(elmt.getAttribute(ATTR_NAME));
			}
			return new PreferenceNode(elmt.getAttribute(ATTR_ID), page);
		}
		else
		{
			return new PreferenceNode(elmt.getAttribute(ATTR_ID), new EmptyPreferencePage(elmt.getAttribute(ATTR_NAME)));
		}
	}
	
	private static IPreferenceNode findNode(PreferenceManager pm, String categoryId)
	{
		for (Object o : pm.getElements(PreferenceManager.POST_ORDER))
		{
			if (o instanceof IPreferenceNode && ((IPreferenceNode) o).getId().equals(categoryId))
			{
				return (IPreferenceNode) o;
			}
		}
		return null;
	}

	private static String getClassURI(String definingBundleId, String spec) throws ClassNotFoundException
	{
		if (spec.startsWith("platform:") || spec.startsWith("bundleclass:")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			return spec;
		}
		//the 'platform:/plugin/<bundleid>/<spec>' URI is depreciated; replaced with 'bundleclass:'
		return "bundleclass://" + definingBundleId + "/" + spec; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static boolean isEmpty(String value)
	{
		return value == null || value.trim().isEmpty();
	}

	/**
	 * A simple {@link PreferencePage} that is blank except for a title
	 * <p/>
	 * Used to provide a stub page
	 */
	private static class EmptyPreferencePage extends PreferencePage
	{
		public EmptyPreferencePage(String title)
		{
			setTitle(title);
			noDefaultAndApplyButton();
		}

		@Override
		protected Control createContents(Composite parent)
		{
			return new Label(parent, SWT.NONE);
		}
	}
	
	public static void setLogger(Logger logger)
	{
		PreferenceUtil.logger = logger;
	}
}
