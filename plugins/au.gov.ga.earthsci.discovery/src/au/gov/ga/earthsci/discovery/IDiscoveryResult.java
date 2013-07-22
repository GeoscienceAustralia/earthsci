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

import au.gov.ga.earthsci.common.util.IInformationed;
import au.gov.ga.earthsci.worldwind.common.layers.Bounded;

/**
 * Single result contained by an {@link IDiscovery}.
 * <p/>
 * Implementations can add whatever methods/fields they like; these can be
 * displayed to the user through the {@link IDiscoveryResultLabelProvider}
 * implementation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IDiscoveryResult extends Bounded, IInformationed
{
	/**
	 * @return Discovery that this result is from
	 */
	IDiscovery getDiscovery();

	/**
	 * @return Index of this result in the {@link IDiscovery}
	 */
	int getIndex();
}
