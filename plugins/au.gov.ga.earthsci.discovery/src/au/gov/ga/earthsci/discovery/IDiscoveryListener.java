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

/**
 * Listens to change events on an {@link IDiscovery} instance, such as when new
 * results are added.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IDiscoveryListener
{
	/**
	 * Called when the discovery's total result count has changed.
	 * 
	 * @param discovery
	 *            Discovery whose result count changed
	 */
	void resultCountChanged(IDiscovery discovery);

	/**
	 * Called when a result has been added to the discovery.
	 * 
	 * @param discovery
	 *            Discovery that the result was added to
	 * @param result
	 *            Result that was added
	 */
	void resultAdded(IDiscovery discovery, IDiscoveryResult result);
}
