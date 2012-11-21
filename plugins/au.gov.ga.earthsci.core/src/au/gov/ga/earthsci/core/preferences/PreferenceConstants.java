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
package au.gov.ga.earthsci.core.preferences;

/**
 * Constant definitions for preference constants
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PreferenceConstants
{
	public static final String PAGES_EXTENSION_POINT = "org.eclipse.ui.preferencePages"; //$NON-NLS-1$
	public static final String QUALIFIER_ID = "au.gov.ga.earthsci.core"; //$NON-NLS-1$

	public static final String PROXY_TYPE = "proxyType"; //$NON-NLS-1$
	public static final String PROXY_TYPE_NONE = "none"; //$NON-NLS-1$
	public static final String PROXY_TYPE_SYSTEM = "system"; //$NON-NLS-1$
	public static final String PROXY_TYPE_USER = "user"; //$NON-NLS-1$
	public static final String PROXY_HOST = "proxyHost"; //$NON-NLS-1$
	public static final String PROXY_PORT = "proxyPort"; //$NON-NLS-1$
	public static final String NON_PROXY_HOSTS = "nonProxyHosts"; //$NON-NLS-1$
}
