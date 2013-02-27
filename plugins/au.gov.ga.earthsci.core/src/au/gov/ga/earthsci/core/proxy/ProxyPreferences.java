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
package au.gov.ga.earthsci.core.proxy;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.jface.preference.IPreferenceStore;

import au.gov.ga.earthsci.core.preferences.PreferenceConstants;
import au.gov.ga.earthsci.core.preferences.ScopedPreferenceStore;

/**
 * Class used to initialize default preference values, and configure the proxy
 * when the preferences change.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Creatable
@Singleton
public class ProxyPreferences extends AbstractPreferenceInitializer
{
	private static IPreferenceStore createStore()
	{
		return new ScopedPreferenceStore(DefaultScope.INSTANCE, PreferenceConstants.QUALIFIER_ID);
	}

	public static void configureProxy(String proxyType, String proxyHost, int proxyPort, String nonProxyHosts)
	{
		boolean system = PreferenceConstants.PROXY_TYPE_SYSTEM.equals(proxyType);
		boolean user = PreferenceConstants.PROXY_TYPE_USER.equals(proxyType);
		System.setProperty("java.net.useSystemProxies", system ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		String actualProxyHost = user ? proxyHost : ""; //$NON-NLS-1$
		System.setProperty("http.proxyHost", actualProxyHost); //$NON-NLS-1$
		System.setProperty("ftp.proxyHost", actualProxyHost); //$NON-NLS-1$

		System.setProperty("http.proxyPort", "" + proxyPort); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("ftp.proxyPort", "" + proxyPort); //$NON-NLS-1$ //$NON-NLS-2$

		System.setProperty("http.nonProxyHosts", "" + nonProxyHosts); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("ftp.nonProxyHosts", "" + nonProxyHosts); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = createStore();
		store.setDefault(PreferenceConstants.PROXY_TYPE, PreferenceConstants.PROXY_TYPE_SYSTEM);
		store.setDefault(PreferenceConstants.PROXY_HOST, ""); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.PROXY_PORT, 80);
		store.setDefault(PreferenceConstants.NON_PROXY_HOSTS, "localhost"); //$NON-NLS-1$
	}

	@Inject
	public void preferencesChanged(
			@Preference(nodePath = PreferenceConstants.QUALIFIER_ID, value = PreferenceConstants.PROXY_TYPE) String proxyType,
			@Preference(nodePath = PreferenceConstants.QUALIFIER_ID, value = PreferenceConstants.PROXY_HOST) String proxyHost,
			@Preference(nodePath = PreferenceConstants.QUALIFIER_ID, value = PreferenceConstants.PROXY_PORT) int proxyPort,
			@Preference(nodePath = PreferenceConstants.QUALIFIER_ID, value = PreferenceConstants.NON_PROXY_HOSTS) String nonProxyHosts)
	{
		configureProxy(proxyType, proxyHost, proxyPort, nonProxyHosts);
	}
}
