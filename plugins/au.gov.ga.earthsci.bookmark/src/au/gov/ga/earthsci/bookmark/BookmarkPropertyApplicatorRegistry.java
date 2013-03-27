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
package au.gov.ga.earthsci.bookmark;

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

import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.common.util.ExtensionRegistryUtil;
import au.gov.ga.earthsci.common.util.ExtensionRegistryUtil.Callback;

/**
 * A registry that can be used to retrieve applicators for
 * {@link IBookmarkProperty}s.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Singleton
@Creatable
public class BookmarkPropertyApplicatorRegistry
{
	private static final String EXTENSION_POINT_ID = "au.gov.ga.earthsci.bookmark.applicator"; //$NON-NLS-1$
	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

	private static Map<String, IBookmarkPropertyApplicator> applicators =
			new ConcurrentHashMap<String, IBookmarkPropertyApplicator>();

	private static final Logger logger = LoggerFactory.getLogger(BookmarkPropertyApplicatorRegistry.class);

	@Inject
	public static void loadFromExtensions()
	{
		logger.debug("Registering bookmark property applicators"); //$NON-NLS-1$
		try
		{
			ExtensionRegistryUtil.createFromExtension(EXTENSION_POINT_ID, CLASS_ATTRIBUTE,
					IBookmarkPropertyApplicator.class, new Callback<IBookmarkPropertyApplicator>()
					{
						@Override
						public void run(IBookmarkPropertyApplicator applicator, IConfigurationElement element,
								IEclipseContext context)
						{
							registerApplicator(applicator);
						}
					});
		}
		catch (CoreException e)
		{
			logger.error("Exception occurred while loading applicator from extension", e); //$NON-NLS-1$
		}
	}

	/**
	 * Return the applicator to use for the given property, if one exists.
	 * 
	 * @param property
	 *            The property an applicator is required for
	 * 
	 * @return The applicator to use for the given property, if one is available
	 */
	public static IBookmarkPropertyApplicator getApplicator(IBookmarkProperty property)
	{
		if (property == null)
		{
			return null;
		}

		return applicators.get(property.getType());
	}

	/**
	 * Register the given applicator in the registry
	 * 
	 * @param applicator
	 *            The applicator to register
	 */
	public static void registerApplicator(IBookmarkPropertyApplicator applicator)
	{
		if (applicator == null)
		{
			return;
		}

		logger.debug("Registeried applicator: {}", applicator.getClass()); //$NON-NLS-1$

		for (String supportedType : applicator.getSupportedTypes())
		{
			applicators.put(supportedType, applicator);
		}
	}
}
