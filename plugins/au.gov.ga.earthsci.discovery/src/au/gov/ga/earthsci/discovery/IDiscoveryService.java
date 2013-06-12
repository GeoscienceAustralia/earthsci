/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.discovery;

import java.net.URL;


/**
 * Instance of a searchable discovery service.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IDiscoveryService
{
	/**
	 * @return The name of this service; used in the UI
	 */
	String getName();

	/**
	 * @return URL of this service
	 */
	URL getServiceURL();

	/**
	 * @return {@link IDiscoveryProvider} that this service is associated with
	 *         (the provider that created this service)
	 */
	IDiscoveryProvider getProvider();

	/**
	 * @return Is this service enabled for searching?
	 */
	boolean isEnabled();

	/**
	 * Enable/disable this service.
	 * 
	 * @param enabled
	 */
	void setEnabled(boolean enabled);

	/**
	 * Create a new discovery, searching this service for the given parameters.
	 * 
	 * @param parameters
	 *            Parameters to used in the search/discovery
	 * @return New discovery, or null if no discovery could be created for the
	 *         given parameters
	 */
	IDiscovery createDiscovery(IDiscoveryParameters parameters);
}
