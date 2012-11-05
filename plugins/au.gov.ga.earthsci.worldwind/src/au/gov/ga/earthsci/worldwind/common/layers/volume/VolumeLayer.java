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
package au.gov.ga.earthsci.worldwind.common.layers.volume;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.earthsci.worldwind.common.layers.data.DataLayer;

/**
 * Data layer that renders a volume, using a 6-sided cube whose sides can be
 * dragged to slice the volume.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface VolumeLayer extends DataLayer
{
	/**
	 * Notifies this layer that the data is available. This should be called by
	 * the {@link VolumeDataProvider} once it has loaded the volume data.
	 * 
	 * @param provider
	 *            {@link VolumeDataProvider} containing the volume's data.
	 */
	void dataAvailable(VolumeDataProvider provider);

	/**
	 * @return {@link CoordinateTransformation} used to project the points in
	 *         the data into WGS84 projection. Null if no re-projection is
	 *         required.
	 */
	CoordinateTransformation getCoordinateTransformation();
	
	/**
	 * @return The name of the variable used to colour this volume layer, as specified in the layer definition file. 
	 * If <code>null</code>, default colouring behaviour is to be followed as per the volume data provider. 
	 */
	String getPaintedVariableName();
}
