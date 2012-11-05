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
package au.gov.ga.earthsci.worldwind.common.util;

import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;

/**
 * Utility class for the creation of {@link CoordinateTransformation} and
 * {@link SpatialReference} instances.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CoordinateTransformationUtil
{
	/**
	 * Create a {@link CoordinateTransformation} that projects from the given
	 * projection string back to WGS84 (EPSG:4326).
	 * 
	 * @param wktOrEpsgOrProj4
	 *            Projection string in Well Known Text form, EPSG code, or a
	 *            Proj4 string
	 * @return Coordinate transformation from the given projection to WGS84
	 */
	public static CoordinateTransformation getTransformationToWGS84(String wktOrEpsgOrProj4)
	{
		SpatialReference src = stringToSpatialReference(wktOrEpsgOrProj4);
		if (src == null)
		{
			return null;
		}

		SpatialReference dst = new SpatialReference();
		dst.ImportFromEPSG(4326);

		return new CoordinateTransformation(src, dst);
	}

	/**
	 * Create a {@link SpatialReference} instance for the given projection
	 * string.
	 * 
	 * @param s
	 *            Projection string in Well Known Text form, EPSG code, or a
	 *            Proj4 string
	 * @return {@link SpatialReference} for the given projection string
	 */
	public static SpatialReference stringToSpatialReference(String s)
	{
		if (s == null)
		{
			return null;
		}

		s = s.trim();

		//remove EPSG: from the front if it exists
		if (s.toLowerCase().startsWith("epsg:"))
		{
			s = s.substring(5);
		}

		//first try a single integer (assume it is an EPSG code)
		try
		{
			int intValue = Integer.parseInt(s);
			SpatialReference reference = new SpatialReference();
			reference.ImportFromEPSG(intValue);
			return reference;
		}
		catch (NumberFormatException e)
		{
			//ignore
		}

		//check for proj4 format
		if (s.startsWith("+"))
		{
			SpatialReference reference = new SpatialReference();
			reference.ImportFromProj4(s);
			return reference;
		}

		//assume wkt format
		return new SpatialReference(s);
	}
}
