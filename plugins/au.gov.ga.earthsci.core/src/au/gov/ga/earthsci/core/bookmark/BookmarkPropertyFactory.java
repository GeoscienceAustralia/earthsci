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
package au.gov.ga.earthsci.core.bookmark;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.core.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.core.util.ExtensionRegistryUtil;
import au.gov.ga.earthsci.core.util.ExtensionRegistryUtil.Callback;

/**
 * A factory class that uses registered {@link IBookmarkPropertyCreator}s to instantiate
 * {@link IBookmarkProperty} instances
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
@Singleton
public class BookmarkPropertyFactory
{
	
	private static final String EXTENSION_POINT_ID = "au.gov.ga.earthsci.core.bookmark.creator"; //$NON-NLS-1$
	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	private static Logger logger = LoggerFactory.getLogger(BookmarkPropertyFactory.class);
	
	private static Map<String, IBookmarkPropertyCreator> creators = new ConcurrentHashMap<String, IBookmarkPropertyCreator>();
	
	/**
	 * Create and return a new property of the given type using the given context.
	 * 
	 * @param type The type of property to create
	 * @param context The context to use for creating the property
	 * 
	 * @return A new bookmark property of the given type, created from the given context
	 */
	public static IBookmarkProperty createProperty(String type, Map<String, Object> context)
	{
		if (type == null)
		{
			return null;
		}
		
		IBookmarkPropertyCreator propertyCreator = creators.get(type);
		if (propertyCreator == null)
		{
			return null;
		}
		
		return propertyCreator.createFromContext(type, context);
	}
	
	/**
	 * Create and return a new property of the given type using the current world state.
	 * 
	 * @param type The type of property to create
	 * 
	 * @return A new bookmark property of the given type, created from the current world state
	 */
	public static IBookmarkProperty createProperty(String type)
	{
		if (type == null)
		{
			return null;
		}
		
		IBookmarkPropertyCreator propertyCreator = creators.get(type);
		if (propertyCreator == null)
		{
			return null;
		}
		
		return propertyCreator.createFromCurrentState(type);
	}
	
	/**
	 * Load the bookmark property creators from the extension registry
	 */
	@Inject
	public static void loadFromExtensions()
	{
		logger.debug("Registering bookmark property creators"); //$NON-NLS-1$
		try
		{
			ExtensionRegistryUtil.createFromExtension(EXTENSION_POINT_ID, CLASS_ATTRIBUTE, IBookmarkPropertyCreator.class, new Callback(){
				@Override
				public void run(Object object, IConfigurationElement element, IEclipseContext context)
				{
					registerCreator((IBookmarkPropertyCreator) object);
				}
			});
		}
		catch (CoreException e)
		{
			logger.error("Exception occurred while loading creator from extension", e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Register the given bookmark property creator with this factory
	 */
	public static void registerCreator(IBookmarkPropertyCreator creator)
	{
		if (creator == null)
		{
			return;
		}
		
		for (String type : creator.getSupportedTypes())
		{
			creators.put(type, creator);
		}
		logger.debug("Registered bookmark property creator: {}", creator.getClass()); //$NON-NLS-1$
	}

	/**
	 * @return The list of known property types which have creators registered in this factory
	 */
	public static String[] getKnownPropertyTypes()
	{
		return creators.keySet().toArray(new String[creators.size()]);
	}
	
}
