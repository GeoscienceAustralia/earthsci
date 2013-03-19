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
package au.gov.ga.earthsci.intent.locator;

import java.net.URL;
import java.util.Comparator;
import java.util.List;

import javax.inject.Singleton;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.injectable.ExtensionPointHelper;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.util.collection.ArrayListTreeMap;
import au.gov.ga.earthsci.util.collection.ListSortedMap;

/**
 * Injectable manager of {@link IIntentResourceLocator}s. Used to translate an
 * {@link Intent}'s URI to a URL from which the resource can be retrieved.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
public class IntentResourceLocatorManager
{
	private static IntentResourceLocatorManager instance;

	public static IntentResourceLocatorManager getInstance()
	{
		return instance;
	}

	private static final String LOCATOR_FILTER_ID = "au.gov.ga.earthsci.intentResourceLocators"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(IntentResourceLocatorManager.class);

	//filters, sorted descending by priority
	private final ListSortedMap<Integer, IIntentResourceLocator> locators =
			new ArrayListTreeMap<Integer, IIntentResourceLocator>(new Comparator<Integer>()
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
	public IntentResourceLocatorManager()
	{
		if (instance != null)
		{
			throw new IllegalStateException(IntentResourceLocatorManager.class.getSimpleName()
					+ " should not be instantiated"); //$NON-NLS-1$
		}
		instance = this;

		IConfigurationElement[] config = RegistryFactory.getRegistry().getConfigurationElementsFor(LOCATOR_FILTER_ID);
		for (IConfigurationElement element : config)
		{
			try
			{
				boolean isLocator = "locator".equals(element.getName()); //$NON-NLS-1$
				if (isLocator)
				{
					@SuppressWarnings("unchecked")
					Class<? extends IIntentResourceLocator> locatorClass =
							(Class<? extends IIntentResourceLocator>) ExtensionPointHelper.getClassForProperty(element,
									"class"); //$NON-NLS-1$
					IIntentResourceLocator locator = locatorClass.newInstance();
					int priority = ExtensionPointHelper.getIntegerForProperty(element, "priority", 0); //$NON-NLS-1$
					locators.putSingle(priority, locator);
				}
			}
			catch (Exception e)
			{
				logger.error("Error processing dispatch filter", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Locate the given Intent's URI as a URL, using the registered
	 * {@link IIntentResourceLocator}s. If no locator can be found that
	 * recognises the Intent's URI, null is returned.
	 * 
	 * @param intent
	 *            Intent whose URI will be translated
	 * @return The URL for the Intent's URI, or null if no locator could be
	 *         found to translate
	 */
	public URL locate(Intent intent)
	{
		for (List<IIntentResourceLocator> list : locators.values())
		{
			for (IIntentResourceLocator locator : list)
			{
				URL url = locator.locate(intent);
				if (url != null)
				{
					return url;
				}
			}
		}
		return null;
	}
}