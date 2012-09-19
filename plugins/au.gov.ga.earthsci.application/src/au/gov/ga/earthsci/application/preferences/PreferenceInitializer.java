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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{
	@Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = new ScopedPreferenceStore(DefaultScope.INSTANCE, PreferenceConstants.QUALIFIER_ID);
		store.setDefault(PreferenceConstants.PROXY_TYPE, PreferenceConstants.PROXY_TYPE_SYSTEM);
		store.setDefault(PreferenceConstants.PROXY_HOST, ""); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.PROXY_PORT, 80);
		store.setDefault(PreferenceConstants.NON_PROXY_HOSTS, "localhost"); //$NON-NLS-1$
	}
}
