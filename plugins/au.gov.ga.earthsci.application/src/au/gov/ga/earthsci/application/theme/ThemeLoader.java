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
package au.gov.ga.earthsci.application.theme;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.application.Activator;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * Sets the theme on the current theme engine to that specified in the
 * {@value #DEFAULT_THEME_EXTENSION_POINT_ID} extension, and loads icon
 * overrides specified in the {@value #ICON_PROVIDER_EXTENSION_POINT_ID}
 * extension.
 * <p/>
 * Allows plugins to provide a complete theme replacement for the application
 * without the need for user-controlled switching. If no contributions are found
 * in the extension points, the default platform look-and-feel is used.
 * <p/>
 * Uses extension points:
 * <ul>
 * <li>{@value #ICON_PROVIDER_EXTENSION_POINT_ID}
 * <li>{@value #DEFAULT_THEME_EXTENSION_POINT_ID}
 * </ul>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
@Singleton
public class ThemeLoader
{
	public static final String DEFAULT_ICONS_PROPERTIES = "icons/icons.properties"; //$NON-NLS-1$

	public static final String DEFAULT_THEME_EXTENSION_POINT_ID = "au.gov.ga.earthsci.application.defaultTheme"; //$NON-NLS-1$
	public static final String DEFAULT_THEME_ID_ATTRIBUTE = "themeId"; //$NON-NLS-1$

	public static final String ICON_PROVIDER_EXTENSION_POINT_ID =
			"au.gov.ga.earthsci.application.iconReplacementProviders"; //$NON-NLS-1$

	public static final String ID_TO_ICON_MAPPING_ELEMENT = "idToIconMapping"; //$NON-NLS-1$
	public static final String ID_ATTRIBUTE = "elementID"; //$NON-NLS-1$
	public static final String ICON_ATTRIBUTE = "icon"; //$NON-NLS-1$

	public static final String ID_TO_ICON_PROPERTIES_ELEMENT = "idToIconProperties"; //$NON-NLS-1$
	public static final String PROPERTIES_ATTRIBUTE = "properties"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(ThemeLoader.class);

	@Inject
	private IExtensionRegistry registry;

	@Optional
	@Inject
	public void setupTheme(IThemeEngine engine)
	{
		if (engine == null)
		{
			return;
		}

		// This is done in the setter to ensure that the theme engine is available
		// (the engine only becomes available once the part renderer is instantiated)
		// The DI engine invokes the setter only once the engine is available 
		// (vs. a PostConstruct method which is invoked immediately after the object is instantiated).

		logger.debug("Loading plugin contributed theme"); //$NON-NLS-1$
		IConfigurationElement[] config = registry.getConfigurationElementsFor(DEFAULT_THEME_EXTENSION_POINT_ID);
		if (config.length == 0)
		{
			logger.debug("No plugin contributed theme found"); //$NON-NLS-1$
			return;
		}

		String themeId = config[0].getAttribute(DEFAULT_THEME_ID_ATTRIBUTE);
		engine.setTheme(themeId, true);
		logger.debug("Switched to theme {} from plugin {}", themeId, config[0].getContributor().getName()); //$NON-NLS-1$
	}

	@Optional
	@Inject
	public void setupIcons(MApplication application, EModelService modelService)
	{
		if (application == null)
		{
			return;
		}

		logger.debug("Loading plugin contributed icons"); //$NON-NLS-1$
		IConfigurationElement[] config = registry.getConfigurationElementsFor(ICON_PROVIDER_EXTENSION_POINT_ID);

		Set<MUILabel> elements = findAllLabels(application, modelService);
		Map<String, URI> iconOverrides = findIdToIconMappings(config);

		applyIconOverrides(elements, iconOverrides);
	}

	/**
	 * Apply the provided icon mappings to the set of {@link MUILabel}s
	 */
	private void applyIconOverrides(Set<MUILabel> elements, Map<String, URI> iconOverrides)
	{
		// Apply the icon overrides
		for (MUILabel l : elements)
		{
			String elementId = ((MUIElement) l).getElementId();
			URI icon = iconOverrides.get(elementId);

			// A mapping to null means no icon
			// Absence of a mapping means leave as-is
			if (icon != null)
			{
				l.setIconURI(icon.toString());
			}
			else if (iconOverrides.containsKey(elementId))
			{
				l.setIconURI(null);
			}
		}
	}

	/**
	 * Extract all of the {@code elementID->Icon} mappings from the provided
	 * configuration elements
	 */
	private Map<String, URI> findIdToIconMappings(IConfigurationElement[] config)
	{
		Map<String, URI> elementMappings = new HashMap<String, URI>();

		// Load the mappings from the config elements
		for (IConfigurationElement element : config)
		{
			String contributingPlugin = element.getContributor().getName();

			logger.debug("Loading icons from plugin {}", contributingPlugin); //$NON-NLS-1$

			if (ID_TO_ICON_MAPPING_ELEMENT.equals(element.getName()))
			{
				String iconRelativePath = element.getAttribute(ICON_ATTRIBUTE);
				String elementID = element.getAttribute(ID_ATTRIBUTE);

				addMapping(elementMappings, elementID, contributingPlugin, iconRelativePath);
			}
			if (ID_TO_ICON_PROPERTIES_ELEMENT.equals(element.getName()))
			{
				String propertiesRelativePath = element.getAttribute(PROPERTIES_ATTRIBUTE);

				addMappingsFromProperties(elementMappings, contributingPlugin, propertiesRelativePath);
			}
		}

		if (config.length == 0)
		{
			logger.debug("Loading default icons"); //$NON-NLS-1$
			addMappingsFromProperties(elementMappings, Activator.getBundleName(), DEFAULT_ICONS_PROPERTIES);
		}

		return elementMappings;
	}

	/**
	 * Add a single mapping to the provided map
	 */
	private void addMapping(Map<String, URI> elementMappings, String elementID, String contributingPlugin,
			String iconRelativePath)
	{
		try
		{
			URI iconURI = makePluginUri(contributingPlugin, iconRelativePath);

			elementMappings.put(elementID, iconURI);
		}
		catch (Exception e)
		{
			logger.error("Invalid icon URI " + iconRelativePath, e); //$NON-NLS-1$
		}
	}

	/**
	 * Add all mappings from the provided properties file
	 */
	private void addMappingsFromProperties(Map<String, URI> elementMappings, String contributingPlugin,
			String propertiesRelativePath)
	{
		URI propertiesURI = null;
		try
		{
			propertiesURI = makePluginUri(contributingPlugin, propertiesRelativePath);
		}
		catch (Exception e)
		{
			logger.error("Invalid properties URI " + propertiesRelativePath); //$NON-NLS-1$
			return;
		}

		Properties properties = new Properties();
		try
		{
			properties.load(propertiesURI.toURL().openStream());
		}
		catch (Exception e)
		{
			logger.error("Unable to open properties file " + propertiesURI.toString(), e); //$NON-NLS-1$
			return;
		}

		for (String key : properties.stringPropertyNames())
		{
			try
			{

				String iconPath = properties.getProperty(key);
				if (Util.isBlank(iconPath))
				{
					elementMappings.put(key, null);
				}
				else
				{
					URI iconURI = makePluginUri(contributingPlugin, iconPath);
					elementMappings.put(key, iconURI);
				}
			}
			catch (Exception e)
			{
				logger.error("Invalid icon URI " + properties.getProperty(key)); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Search the application model from the top-level application and find all
	 * elements that can have icons set on them.
	 */
	private Set<MUILabel> findAllLabels(MApplication application, EModelService modelService)
	{
		Set<MUILabel> labels = new HashSet<MUILabel>();
		labels.addAll(modelService.findElements(application, null, MUILabel.class, null));

		// Model service does not include part descriptors 
		for (MPartDescriptor d : application.getDescriptors())
		{
			for (MMenu menu : d.getMenus())
			{
				if (menu != null)
				{
					labels.addAll(modelService.findElements(menu, null, MUILabel.class, null));
				}
			}
			if (d.getToolbar() != null)
			{
				labels.addAll(modelService.findElements(d.getToolbar(), null, MUILabel.class, null));
			}
		}

		// Model service does not include menus and toolbars - need to find them ourselves
		List<MPart> parts = modelService.findElements(application, null, MPart.class, null);
		for (MPart part : parts)
		{
			for (MMenu menu : part.getMenus())
			{
				if (menu != null)
				{
					labels.addAll(modelService.findElements(menu, null, MUILabel.class, null));
				}
			}
			if (part.getToolbar() != null)
			{
				labels.addAll(modelService.findElements(part.getToolbar(), null, MUILabel.class, null));
			}
		}

		return labels;
	}

	/**
	 * Create a plugin URI for the given resource located in the given plugin
	 * <p/>
	 * Plugin URIs have the form
	 * {@code platform:/plugin/[plugin name]/[resource path]}
	 */
	private URI makePluginUri(String pluginName, String resourcePath) throws Exception
	{
		if (resourcePath.startsWith("platform:/plugin/")) //$NON-NLS-1$
		{
			return new URI(resourcePath);
		}
		return new URI("platform:/plugin/" + pluginName + "/" + resourcePath); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
