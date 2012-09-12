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
package au.gov.ga.earthsci.application.preferences.pages;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;

import au.gov.ga.earthsci.application.preferences.PreferenceConstants;
import au.gov.ga.earthsci.application.preferences.ScopedPreferenceStore;

/**
 * {@link PreferencePage} which allows configuration of how the application
 * connects to the internet, such as the proxy.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NetworkPreferencePage extends FieldEditorPreferencePage
{
	public NetworkPreferencePage()
	{
		super(GRID);
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, PreferenceConstants.QUALIFIER_ID);
		setPreferenceStore(store);
		setDescription("Configure how the application connects to the internet");
	}

	@Override
	public void createFieldEditors()
	{
		addField(new BooleanFieldEditor(PreferenceConstants.USE_SYSTEM_PROXIES, "Use &system proxies",
				getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.PROXY_HOST, "Proxy &host", getFieldEditorParent()));
		addField(new IntegerFieldEditor(PreferenceConstants.PROXY_PORT, "Proxy &port", getFieldEditorParent()));
		addField(new NonProxyHostsListEditor(PreferenceConstants.NON_PROXY_HOSTS, "&Non-proxy hosts",
				getFieldEditorParent()));

		//addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH, "&Directory preference:", getFieldEditorParent()));
		//addField(new BooleanFieldEditor(PreferenceConstants.P_BOOLEAN, "&An example of a boolean preference", getFieldEditorParent()));
		//addField(new RadioGroupFieldEditor(PreferenceConstants.P_CHOICE, "An example of a multiple-choice preference", 1, new String[][] { { "&Choice 1", "choice1" }, { "C&hoice 2", "choice2" } }, getFieldEditorParent()));
		//addField(new StringFieldEditor(PreferenceConstants.P_STRING, "A &text preference:", getFieldEditorParent()));
	}
}
