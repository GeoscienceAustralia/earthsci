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
	public void useSystemProxiesChanged(
			@Preference(nodePath = PreferenceConstants.QUALIFIER_ID, value = PreferenceConstants.USE_SYSTEM_PROXIES) boolean useSystemProxies)
	{
		System.setProperty("java.net.useSystemProxies", useSystemProxies ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Inject
	public void proxyHostChanged(
			@Preference(nodePath = PreferenceConstants.QUALIFIER_ID, value = PreferenceConstants.PROXY_HOST) String proxyHost)
	{
		System.setProperty("http.proxyHost", proxyHost); //$NON-NLS-1$
		System.setProperty("ftp.proxyHost", proxyHost); //$NON-NLS-1$
	}

	@Inject
	public void proxyPortChanged(
			@Preference(nodePath = PreferenceConstants.QUALIFIER_ID, value = PreferenceConstants.PROXY_PORT) int proxyPort)
	{
		System.setProperty("http.proxyPort", "" + proxyPort); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("ftp.proxyPort", "" + proxyPort); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Inject
	public void nonProxyHostsChanged(
			@Preference(nodePath = PreferenceConstants.QUALIFIER_ID, value = PreferenceConstants.NON_PROXY_HOSTS) String nonProxyHosts)
	{
		System.setProperty("http.nonProxyHosts", "" + nonProxyHosts); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("ftp.nonProxyHosts", "" + nonProxyHosts); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
