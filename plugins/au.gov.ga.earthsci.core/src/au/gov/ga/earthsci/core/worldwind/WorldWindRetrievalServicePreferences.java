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
package au.gov.ga.earthsci.core.worldwind;

import gov.nasa.worldwind.WorldWind;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.jface.preference.IPreferenceStore;

import au.gov.ga.earthsci.core.preferences.PreferenceConstants;
import au.gov.ga.earthsci.core.preferences.ScopedPreferenceStore;

/**
 * Contains the preferences for the {@link WorldWindRetrievalService}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Creatable
@Singleton
public class WorldWindRetrievalServicePreferences extends AbstractPreferenceInitializer
{
	public static final String POOL_SIZE = "wwRetrievalServicePoolSize"; //$NON-NLS-1$

	@Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = new ScopedPreferenceStore(DefaultScope.INSTANCE, PreferenceConstants.QUALIFIER_ID);
		store.setDefault(POOL_SIZE, 10);
	}

	@Inject
	public void preferencesChanged(
			@Preference(nodePath = PreferenceConstants.QUALIFIER_ID, value = POOL_SIZE) int poolSize)
	{
		WorldWind.getRetrievalService().setRetrieverPoolSize(poolSize);
	}
}
