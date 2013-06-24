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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;

import au.gov.ga.earthsci.common.ui.preferences.FieldEditorPreferencePage;
import au.gov.ga.earthsci.core.preferences.PreferenceConstants;
import au.gov.ga.earthsci.core.preferences.ScopedPreferenceStore;

/**
 * Preferences page for editing retrieval preferences.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RetrievePreferencePage extends FieldEditorPreferencePage
{
	public RetrievePreferencePage()
	{
		super(GRID);
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, PreferenceConstants.QUALIFIER_ID);
		setPreferenceStore(store);
		setDescription("Configure properties used for retrieval of resources");
	}

	@Override
	public void createFieldEditors()
	{
		addField(new IntegerFieldEditor(WorldWindRetrievalServicePreferences.POOL_SIZE, "&Simultaneous connections",
				getFieldEditorParent()));
	}
}
