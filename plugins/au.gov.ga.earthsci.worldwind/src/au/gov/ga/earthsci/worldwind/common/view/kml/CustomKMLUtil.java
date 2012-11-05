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
package au.gov.ga.earthsci.worldwind.common.view.kml;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.ogc.kml.KMLAbstractGeometry;
import gov.nasa.worldwind.ogc.kml.KMLLocation;
import gov.nasa.worldwind.ogc.kml.KMLModel;
import gov.nasa.worldwind.ogc.kml.impl.KMLUtil;

/**
 * {@link KMLUtil} extension that adds KMLModel support to the
 * {@link KMLUtil#getPositions(Globe, KMLAbstractGeometry, java.util.List)}
 * method.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CustomKMLUtil extends KMLUtil
{
	/**
	 * Get all of the positions that make up a {@link KMLAbstractGeometry}. If
	 * the geometry contains other geometries, this method collects all the
	 * points from all of the geometries.
	 * 
	 * @param globe
	 *            Globe to use to determine altitude above terrain.
	 * @param geometry
	 *            Geometry to collect positions from.
	 * @param positions
	 *            Placemark positions will be added to this list.
	 */
	public static void getPositions(Globe globe, KMLAbstractGeometry geometry, java.util.List<Position> positions)
	{
		KMLUtil.getPositions(globe, geometry, positions);
		if (positions.isEmpty() && geometry instanceof KMLModel)
		{
			positions.add(locationToPosition(((KMLModel) geometry).getLocation()));
		}
	}

	/**
	 * Convert a {@link KMLLocation} to a WorldWind {@link Position}.
	 * 
	 * @param location
	 *            Location to convert
	 * @return Position containing location information
	 */
	public static Position locationToPosition(KMLLocation location)
	{
		if (location == null)
			return null;

		Double lat = location.getLatitude(), lon = location.getLongitude(), alt = location.getAltitude();
		return new Position(Angle.fromDegrees(lat != null ? lat : 0), Angle.fromDegrees(lon != null ? lon : 0),
				alt != null ? alt : 0);
	}
}
