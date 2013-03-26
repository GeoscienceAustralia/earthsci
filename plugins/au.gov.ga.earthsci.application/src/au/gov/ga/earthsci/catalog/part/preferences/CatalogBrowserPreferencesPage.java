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
package au.gov.ga.earthsci.catalog.part.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import au.gov.ga.earthsci.application.util.UserActionPreference;
import au.gov.ga.earthsci.catalog.part.Messages;
import au.gov.ga.earthsci.core.preferences.ScopedPreferenceStore;
import au.gov.ga.earthsci.core.preferences.SpacerFieldEditor;

/**
 * The preference page for the
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CatalogBrowserPreferencesPage extends FieldEditorPreferencePage
{

	private RadioGroupFieldEditor addNodeStructureModeEditor;
	private RadioGroupFieldEditor deleteEmptyFoldersModeEditor;

	public CatalogBrowserPreferencesPage()
	{
		super(FLAT);
		setTitle(Messages.CatalogBrowserPreferencesPage_PageTitle);
		setDescription(Messages.CatalogBrowserPreferencesPage_PageDescription);

		IPreferenceStore store =
				new ScopedPreferenceStore(InstanceScope.INSTANCE, ICatalogBrowserPreferences.QUALIFIER_ID);

		setPreferenceStore(store);
	}

	@Override
	protected void createFieldEditors()
	{
		addField(new SpacerFieldEditor(getFieldEditorParent()));

		String[][] userActionPreferenceLabelValues =
				new String[][] {
						{ Messages.CatalogBrowserPreferencesPage_AwlaysOptionLabel, UserActionPreference.ALWAYS.name() },
						{ Messages.CatalogBrowserPreferencesPage_NeverOptionLabel, UserActionPreference.NEVER.name() },
						{ Messages.CatalogBrowserPreferencesPage_AskOptionLabel, UserActionPreference.ASK.name() } };

		addNodeStructureModeEditor =
				new RadioGroupFieldEditor(ICatalogBrowserPreferences.ADD_NODE_STRUCTURE_MODE,
						Messages.CatalogBrowserPreferencesPage_AddNodeStructureMessage, 3,
						userActionPreferenceLabelValues, getFieldEditorParent(), false);
		addField(addNodeStructureModeEditor);

		addField(new SpacerFieldEditor(getFieldEditorParent()));

		deleteEmptyFoldersModeEditor =
				new RadioGroupFieldEditor(ICatalogBrowserPreferences.DELETE_EMPTY_FOLDERS_MODE,
						Messages.CatalogBrowserPreferencesPage_DeleteEmptyFoldersMessage, 3,
						userActionPreferenceLabelValues, getFieldEditorParent(), false);
		addField(deleteEmptyFoldersModeEditor);
	}

}
