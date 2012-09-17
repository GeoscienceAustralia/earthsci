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
package au.gov.ga.earthsci.core.worldwind.layers;

import au.gov.ga.earthsci.core.model.layer.TreeLayerList;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;

/**
 * Factory used for generating {@link Layer}s and {@link LayerList}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerFactory extends AbstractLayerFactory
{
	@Override
	protected LayerList createLayerList()
	{
		return new TreeLayerList();
	}
}
