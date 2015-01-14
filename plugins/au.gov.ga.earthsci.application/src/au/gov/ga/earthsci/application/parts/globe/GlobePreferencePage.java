/*******************************************************************************
 * Copyright 2015 Geoscience Australia
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
package au.gov.ga.earthsci.application.parts.globe;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;

import au.gov.ga.earthsci.common.ui.preferences.FieldEditorPreferencePage;
import au.gov.ga.earthsci.core.preferences.ScopedPreferenceStore;

/**
 * Preference page for the globe part.
 *
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GlobePreferencePage extends FieldEditorPreferencePage
{
	public static final String QUALIFIER_ID = "au.gov.ga.earthsci.globe"; //$NON-NLS-1$
	public static final String FPS_PREFERENCE_NAME = "au.gov.ga.earthsci.globe.preferences.fps"; //$NON-NLS-1$

	public GlobePreferencePage()
	{
		super(GRID);
		setTitle(Messages.GlobePreferencePage_Title);
		setDescription(Messages.GlobePreferencePage_Description);

		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, QUALIFIER_ID);
		setPreferenceStore(store);
	}

	@Override
	protected void createFieldEditors()
	{
		BooleanFieldEditor fpsEditor =
				new BooleanFieldEditor(FPS_PREFERENCE_NAME, Messages.GlobePreferencePage_FPS, getFieldEditorParent());
		addField(fpsEditor);
	}
}
