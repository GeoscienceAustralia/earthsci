package au.gov.ga.earthsci.notification.popup.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.IPreferenceStore;

import au.gov.ga.earthsci.application.preferences.ScopedPreferenceStore;

/**
 * Initialises the popup notification preferences to default values
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{

	@Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = new ScopedPreferenceStore(DefaultScope.INSTANCE, PopupNotificationPreferencePage.QUALIFIER_ID);
		store.setDefault(PopupNotificationPreferencePage.ENABLE_POPUPS, true);
		store.setDefault(PopupNotificationPreferencePage.SHOW_INFO_NOTIFICATIONS, true);
		store.setDefault(PopupNotificationPreferencePage.SHOW_WARNING_NOTIFICATIONS, true);
		store.setDefault(PopupNotificationPreferencePage.SHOW_ERROR_NOTIFICATIONS, true);
		store.setDefault(PopupNotificationPreferencePage.POPUP_DURATION, 5000);
	}

}
