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
package au.gov.ga.earthsci.application.console;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.IPreferenceStore;

import au.gov.ga.earthsci.core.preferences.ScopedPreferenceStore;
import au.gov.ga.earthsci.logging.LoggingConfigurator;

/**
 * A preference initializer for the logging preferences that retrieves the
 * global default logging level from the {@link LoggingConfigurator}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class LoggingPreferenceInitializer extends AbstractPreferenceInitializer
{

	@Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = new ScopedPreferenceStore(DefaultScope.INSTANCE, ILoggingPreferences.QUALIFIER_ID);

		store.putValue(ILoggingPreferences.LOG_LEVEL,
				ILoggingPreferences.LOG_LEVELS[LoggingConfigurator.getDefaultLoggingLevel() - 1]);
	}

}
