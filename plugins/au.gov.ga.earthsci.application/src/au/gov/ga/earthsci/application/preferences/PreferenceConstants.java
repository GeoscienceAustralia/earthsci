package au.gov.ga.earthsci.application.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public interface PreferenceConstants
{
	static final String PAGES_EXTENSION_POINT = "org.eclipse.ui.preferencePages"; //$NON-NLS-1$
	static final String QUALIFIER_ID = "au.gov.ga.earthsci.application"; //$NON-NLS-1$

	static final String USE_SYSTEM_PROXIES = "useSystemProxies"; //$NON-NLS-1$
	static final String PROXY_HOST = "proxyHost"; //$NON-NLS-1$
	static final String PROXY_PORT = "proxyPort"; //$NON-NLS-1$
	static final String NON_PROXY_HOSTS = "nonProxyHosts"; //$NON-NLS-1$
}
