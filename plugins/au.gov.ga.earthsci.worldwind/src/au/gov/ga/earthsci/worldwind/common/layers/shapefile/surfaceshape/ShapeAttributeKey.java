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
package au.gov.ga.earthsci.worldwind.common.layers.shapefile.surfaceshape;

import gov.nasa.worldwind.avlist.AVKey;

/**
 * Some extra AVKeys that define ShapeAttributes in SurfaceShape layers
 * 
 * @author Michael de Hoog
 */
public interface ShapeAttributeKey extends AVKey
{
	final String DRAW_INTERIOR = "gov.nasa.worldwind.render.ShapeAttributes.DrawInterior";
	final String DRAW_OUTLINE = "gov.nasa.worldwind.render.ShapeAttributes.DrawOutline";
	final String ANTIALIASING = "gov.nasa.worldwind.render.ShapeAttributes.Antialiasing";
	final String INTERIOR_COLOR = "gov.nasa.worldwind.render.ShapeAttributes.InteriorColor";
	final String OUTLINE_COLOR = "gov.nasa.worldwind.render.ShapeAttributes.OutlineColor";
	final String INTERIOR_OPACITY = "gov.nasa.worldwind.render.ShapeAttributes.InteriorOpacity";
	final String OUTLINE_OPACITY = "gov.nasa.worldwind.render.ShapeAttributes.OutlineOpacity";
	final String OUTLINE_WIDTH = "gov.nasa.worldwind.render.ShapeAttributes.OutlineWidth";
	final String STIPPLE_FACTOR = "gov.nasa.worldwind.render.ShapeAttributes.StippleFactor";
	final String STIPPLE_PATTERN = "gov.nasa.worldwind.render.ShapeAttributes.StipplePattern";
}
