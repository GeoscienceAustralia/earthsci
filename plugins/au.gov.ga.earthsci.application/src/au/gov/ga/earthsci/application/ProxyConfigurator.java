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
package au.gov.ga.earthsci.application;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.di.extensions.Preference;

import au.gov.ga.earthsci.application.preferences.PreferenceConstants;

/**
 * Singleton class which is injected into the {@link LifeCycleManager} which
 * handles proxy preference changes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Creatable
@Singleton
public class ProxyConfigurator
{
	@Inject
	public void configureProxy(
			@Preference(nodePath = PreferenceConstants.QUALIFIER_ID, value = PreferenceConstants.PROXY_TYPE) String proxyType,
			@Preference(nodePath = PreferenceConstants.QUALIFIER_ID, value = PreferenceConstants.PROXY_HOST) String proxyHost,
			@Preference(nodePath = PreferenceConstants.QUALIFIER_ID, value = PreferenceConstants.PROXY_PORT) int proxyPort,
			@Preference(nodePath = PreferenceConstants.QUALIFIER_ID, value = PreferenceConstants.NON_PROXY_HOSTS) String nonProxyHosts)
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
}
