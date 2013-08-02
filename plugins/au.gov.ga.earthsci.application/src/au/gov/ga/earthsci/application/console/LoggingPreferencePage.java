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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import au.gov.ga.earthsci.common.ui.preferences.FieldEditorPreferencePage;
import au.gov.ga.earthsci.core.preferences.ScopedPreferenceStore;

/**
 * Preference page for setting logging preferences
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class LoggingPreferencePage extends FieldEditorPreferencePage
{

	public RadioGroupFieldEditor levelGroupEditor;

	public LoggingPreferencePage()
	{
		super(GRID);
		setTitle(Messages.LoggingPreferencePage_PageTitle);
		setDescription(Messages.LoggingPreferencePage_PageDescription);

		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, ILoggingPreferences.QUALIFIER_ID);
		setPreferenceStore(store);
	}

	@Override
	protected void createFieldEditors()
	{
		levelGroupEditor = new RadioGroupFieldEditor(ILoggingPreferences.LOG_LEVEL,
				Messages.LoggingPreferencePage_LogLevelTitle, 1,
				new String[][] {
						{ Messages.LoggingPreferencePage_TraceLevel, ILoggingPreferences.TRACE_LEVEL },
						{ Messages.LoggingPreferencePage_DebugLevel, ILoggingPreferences.DEBUG_LEVEL },
						{ Messages.LoggingPreferencePage_InfoLevel, ILoggingPreferences.INFO_LEVEL },
						{ Messages.LoggingPreferencePage_WarnLevel, ILoggingPreferences.WARN_LEVEL },
						{ Messages.LoggingPreferencePage_ErrorLevel, ILoggingPreferences.ERROR_LEVEL },
						{ Messages.LoggingPreferencePage_FatalLevel, ILoggingPreferences.FATAL_LEVEL },
				},
				getFieldEditorParent());

		addField(levelGroupEditor);
	}

}
