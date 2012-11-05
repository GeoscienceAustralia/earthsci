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
package au.gov.ga.earthsci.worldwind.common.layers.geometry;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import au.gov.ga.earthsci.worldwind.common.layers.data.DataLayer;

/**
 * The interface for all geometry layers.
 * <p/>
 * A geometry layer is composed of multiple {@link Position}s, grouped into shapes.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface GeometryLayer extends DataLayer
{

	/**
	 * @return The shapes associated with this layer
	 */
	Iterable<? extends Shape> getShapes();
	
	/**
	 * Add the provided shape to this layer
	 */
	void addShape(Shape shape);
	
	/**
	 * Invoked when this layer's shape source has been loaded.
	 * <p/>
	 * Provides a hook for implementing classes to perform post-load processing.
	 */
	void loadComplete();
	
	/**
	 * Render the geometry in this layer
	 */
	void renderGeometry(DrawContext dc);
}
