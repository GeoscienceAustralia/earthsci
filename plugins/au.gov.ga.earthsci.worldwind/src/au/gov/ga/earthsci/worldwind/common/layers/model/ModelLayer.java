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
package au.gov.ga.earthsci.worldwind.common.layers.model;

import au.gov.ga.earthsci.worldwind.common.layers.Hierarchical;
import au.gov.ga.earthsci.worldwind.common.layers.Wireframeable;
import au.gov.ga.earthsci.worldwind.common.layers.data.DataLayer;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;

/**
 * Layer that renders a model. Uses {@link FastShape}s for storing and rendering
 * geometry.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ModelLayer extends DataLayer, Hierarchical, Wireframeable
{
	/**
	 * Add a shape to this layer
	 * 
	 * @param shape
	 *            Shape to add
	 */
	void addShape(FastShape shape);

	/**
	 * Remove a shape from this layer
	 * 
	 * @param shape
	 *            Shape to remove
	 */
	void removeShape(FastShape shape);
}
