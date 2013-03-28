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
package au.gov.ga.earthsci.bookmark.ui.editor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.bookmark.BookmarkPropertyApplicatorRegistry;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.injectable.ExtensionPointHelper;

/**
 * A factory class used to obtain a new {@link IBookmarkPropertyEditor} instance
 * for use with a given {@link IBookmarkProperty}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Singleton
@Creatable
public class BookmarkPropertyEditorFactory
{
	private static final String EXTENSION_POINT_ID = "au.gov.ga.earthsci.bookmark.ui.propertyEditors"; //$NON-NLS-1$
	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$

	private static Map<String, Class<? extends IBookmarkPropertyEditor>> editors =
			new ConcurrentHashMap<String, Class<? extends IBookmarkPropertyEditor>>();

	private static final Logger logger = LoggerFactory.getLogger(BookmarkPropertyApplicatorRegistry.class);

	@Inject
	public static void loadFromExtensions(IExtensionRegistry registry)
	{
		logger.debug("Registering bookmark property editors"); //$NON-NLS-1$
		try
		{
			IConfigurationElement[] config = registry.getConfigurationElementsFor(EXTENSION_POINT_ID);
			for (IConfigurationElement e : config)
			{
				@SuppressWarnings("unchecked")
				Class<? extends IBookmarkPropertyEditor> clazz =
						(Class<? extends IBookmarkPropertyEditor>) ExtensionPointHelper.getClassForProperty(e,
								CLASS_ATTRIBUTE);
				String typeName = e.getAttribute(TYPE_ATTRIBUTE);

				registerEditor(typeName, clazz);
			}
		}
		catch (Exception e)
		{
			logger.error("Exception occurred while loading editor from extension", e); //$NON-NLS-1$
		}
	}

	/**
	 * Return a new editor to use for the given property, if one exists.
	 * <p/>
	 * The returned editor will be primed with the given property using
	 * {@link IBookmarkPropertyEditor#setProperty(IBookmarkProperty)}
	 * 
	 * @param property
	 *            The property an editor is required for
	 * 
	 * @return The editor to use for the given property, if one is available
	 */
	public static IBookmarkPropertyEditor createEditor(IBookmarkProperty property)
	{
		if (property == null)
		{
			return null;
		}

		IBookmarkPropertyEditor result = createEditor(property.getType());
		if (result != null)
		{
			result.setProperty(property);
		}
		return result;
	}

	/**
	 * Return a new editor to use for the given property type, if one exists.
	 * 
	 * @param property
	 *            The property an editor is required for
	 * 
	 * @return The editor to use for the given property, if one is available
	 */
	public static IBookmarkPropertyEditor createEditor(String propertyType)
	{
		if (propertyType == null || !editors.containsKey(propertyType))
		{
			return null;
		}

		try
		{
			IBookmarkPropertyEditor editor = editors.get(propertyType).newInstance();
			return editor;
		}
		catch (Exception e)
		{
			logger.error("Unable to create a new editor for property type " + propertyType, e); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Register the given editor with the factory
	 * 
	 * @param editor
	 *            The editor to register
	 * @param type
	 *            The type of bookmark property to register this editor for
	 */
	public static void registerEditor(String propertyType, Class<? extends IBookmarkPropertyEditor> editor)
	{
		if (editor == null)
		{
			return;
		}

		logger.debug("Registered editor: {}", editor); //$NON-NLS-1$
		editors.put(propertyType, editor);
	}

	/**
	 * Return the types of {@link IBookmarkProperty}s for which
	 * {@link IBookmarkPropertyEditor}s are available
	 * 
	 * @return The types of properties for which editors are available.
	 */
	public static String[] getSupportedTypes()
	{
		return editors.keySet().toArray(new String[editors.size()]);
	}
}
