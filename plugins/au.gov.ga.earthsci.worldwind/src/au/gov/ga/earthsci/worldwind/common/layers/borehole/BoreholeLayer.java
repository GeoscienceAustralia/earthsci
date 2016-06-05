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
package au.gov.ga.earthsci.worldwind.common.layers.borehole;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;

import java.awt.Color;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.earthsci.worldwind.common.layers.data.DataLayer;
import au.gov.ga.earthsci.worldwind.common.util.ColorMap;

/**
 * Layer used to visualise borehole data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface BoreholeLayer extends DataLayer
{
	/**
	 * Add a borehole to this layer.
	 * 
	 * @param borehole
	 *            Borehole to add
	 */
	void addBorehole(Borehole borehole);

	/**
	 * Add a borehole sample to this layer. Called by the
	 * {@link BoreholeProvider}.
	 * <p/>
	 * If a borehole doesn't yet exist for this sample/position, a new borehole
	 * is created.
	 * 
	 * @param position
	 *            Position of the borehole that contains this sample
	 * @param attributeValues
	 *            Attribute values for this sample, including from/to depth
	 */
	void addBoreholeSample(Position position, AVList attributeValues);

	/**
	 * Called by the {@link BoreholeProvider} after all boreholes have been
	 * loaded.
	 */
	void loadComplete();

	/**
	 * @return The minimum distance a Borehole must be from the camera to render
	 *         it. Any borehole closer to the camera than this minimum distance
	 *         should be rendered. If null, there's no minimum distance.
	 */
	Double getMinimumDistance();

	/**
	 * @return The default colour to use when rendering samples that have no
	 *         colour information available.
	 */
	Color getDefaultSampleColor();

	/**
	 * @return Optional {@link CoordinateTransformation} used to transform
	 *         coordinates into WGS84
	 */
	CoordinateTransformation getCoordinateTransformation();

	/**
	 * @return Optional {@link ColorMap} used to calculate colors of markers and
	 *         samples
	 */
	ColorMap getColorMap();
}
