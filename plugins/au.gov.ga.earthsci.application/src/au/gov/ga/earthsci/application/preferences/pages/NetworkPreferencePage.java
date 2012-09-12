package au.gov.ga.earthsci.application.preferences.pages;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

import au.gov.ga.earthsci.application.preferences.PreferenceConstants;
import au.gov.ga.earthsci.application.preferences.ScopedPreferenceStore;

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