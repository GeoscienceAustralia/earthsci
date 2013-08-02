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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;

import au.gov.ga.earthsci.logging.LoggingConfigurator;

/**
 * The default implementation of the {@link ILoggingPreferences} interface that
 * bridges between the Eclipse preference mechanism and the Slf4j logging level
 * mechanism
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
@Singleton
public class LoggingPreferences implements ILoggingPreferences
{

	private IEclipsePreferences preferenceStore;

	@SuppressWarnings("deprecation")
	@Inject
	public void setStore(@Preference(nodePath = QUALIFIER_ID) IEclipsePreferences preferenceStore)
	{
		// Setup the defaults here, rather than in an initializer
		IEclipsePreferences defaults = new DefaultScope().getNode(QUALIFIER_ID);
		defaults.put(LOG_LEVEL, LOG_LEVELS[LoggingConfigurator.getDefaultLoggingLevel() - 1]);

		this.preferenceStore = preferenceStore;

		// Change the global logging to reflect preferences
		String level = preferenceStore.get(LOG_LEVEL, null);
		setLogLevel(level);

		// Add a listener to detect user preference changes
		this.preferenceStore.addPreferenceChangeListener(new IPreferenceChangeListener()
		{
			@Override
			public void preferenceChange(PreferenceChangeEvent event)
			{
				if (!event.getKey().equals(ILoggingPreferences.LOG_LEVEL))
				{
					return;
				}

				String newValue = (String) event.getNewValue();
				setLogLevel(newValue);
			}
		});
	}

	@Override
	public int getLogLevel()
	{
		return LoggingConfigurator.getGlobalLoggingLevel();
	}

	@Override
	public String getLogLevelStr()
	{
		return LOG_LEVELS[getLogLevel() - 1];
	}

	@Override
	public void setLogLevel(int level)
	{
		LoggingConfigurator.setGlobalLoggingLevel(level);
	}

	@Override
	@SuppressWarnings("nls")
	public void setLogLevel(String levelStr)
	{
		if (levelStr == null)
		{
			setLogLevel(LoggingConfigurator.getDefaultLoggingLevel());
			return;
		}

		int level = 1;
		if (levelStr.equalsIgnoreCase("trace"))
		{
			level = 1;
		}
		else if (levelStr.equalsIgnoreCase("debug"))
		{
			level = 2;
		}
		else if (levelStr.equalsIgnoreCase("info"))
		{
			level = 3;
		}
		else if (levelStr.equalsIgnoreCase("warn"))
		{
			level = 4;
		}
		else if (levelStr.equalsIgnoreCase("error"))
		{
			level = 5;
		}
		else if (levelStr.equalsIgnoreCase("fatal"))
		{
			level = 6;
		}
		setLogLevel(level);
	}

}
