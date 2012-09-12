package au.gov.ga.earthsci.application.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{
	@Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = new ScopedPreferenceStore(DefaultScope.INSTANCE, PreferenceConstants.QUALIFIER_ID);
		store.setDefault(PreferenceConstants.USE_SYSTEM_PROXIES, true);
		store.setDefault(PreferenceConstants.PROXY_HOST, ""); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.PROXY_PORT, 80);
		store.setDefault(PreferenceConstants.NON_PROXY_HOSTS, "localhost"); //$NON-NLS-1$
	}
}
