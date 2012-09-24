package au.gov.ga.earthsci.notification.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;

import au.gov.ga.earthsci.notification.Messages;

/**
 * Preference page that serves as the root page for all notification provider
 * plugins.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class NotificationPreferencesPage extends FieldEditorPreferencePage
{

	public NotificationPreferencesPage()
	{
		super(FLAT);
		noDefaultAndApplyButton();
		setTitle(Messages.NotificationPreferences_Title);
		setDescription(Messages.NotificationPreferences_Description);
	}
	
	@Override
	protected void createFieldEditors()
	{
		
	}

}
