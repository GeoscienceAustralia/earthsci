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
import java.util.Map;

/**
 * A provider of discovery services. Handles creating services for each service
 * URL associated with this provider.
 * <p/>
 * For example, a CSW discovery provider could create multiple CSW discovery
 * services, one for each CSW URL registered in the application.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IDiscoveryProvider
{
	/**
	 * @return Unique id of this discovery provider
	 */
	String getId();

	/**
	 * @return Pretty name of this discovery provider; used in the UI
	 */
	String getName();

	/**
	 * @return Image to show as the icon for this discovery provider; used in
	 *         the UI
	 */
	URL getIconURL();

	/**
	 * Create a new discovery service for discovering data from the given URL,
	 * which should be a URL pointing to a service that implements this
	 * particular provider's method of discovering data.
	 * <p/>
	 * For example, if this provider was a CSW discovery provider, the URL
	 * should point to a CSW endpoint, and a CSW discovery service should be
	 * returned by this method.
	 * <p/>
	 * The implementation of this method should be lightweight. It should not
	 * perform any network activity.
	 * 
	 * @param name
	 *            Name of the service; used in the UI
	 * @param url
	 *            URL of the discovery service
	 * @return {@link IDiscoveryService} that can be used to discover results
	 *         from the given URL
	 */
	IDiscoveryService createService(String name, URL url, Map<IDiscoveryServiceProperty<?>, Object> propertyValues);

	/**
	 * @return Array of properties that can be changed for services created by
	 *         this provider
	 */
	IDiscoveryServiceProperty<?>[] getProperties();

	/**
	 * @return Handler that can handle opening discovery results created by this
	 *         provider's services
	 */
	IDiscoveryResultHandler getHandler();
}
