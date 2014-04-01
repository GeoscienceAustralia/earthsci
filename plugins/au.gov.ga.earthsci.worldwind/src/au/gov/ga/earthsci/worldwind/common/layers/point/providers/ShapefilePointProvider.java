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
package au.gov.ga.earthsci.worldwind.common.layers.point.providers;

import gov.nasa.worldwind.formats.shapefile.DBaseRecord;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileUtils;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.VecBuffer;

import java.net.URL;
import java.util.logging.Level;

import au.gov.ga.earthsci.worldwind.common.layers.Bounds;
import au.gov.ga.earthsci.worldwind.common.layers.data.AbstractDataProvider;
import au.gov.ga.earthsci.worldwind.common.layers.point.PointLayer;
import au.gov.ga.earthsci.worldwind.common.layers.point.PointProvider;
import au.gov.ga.earthsci.worldwind.common.util.URLUtil;

/**
 * {@link PointProvider} implementation which loads points from a zipped
 * shapefile.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ShapefilePointProvider extends AbstractDataProvider<PointLayer> implements PointProvider
{
	private Bounds bounds;

	@Override
	protected boolean doLoadData(URL url, PointLayer layer)
	{
		try
		{
			bounds = null;
			Shapefile shapefile = ShapefileUtils.openZippedShapefile(URLUtil.urlToFile(url));
			while (shapefile.hasNext())
			{
				ShapefileRecord record = shapefile.nextRecord();
				DBaseRecord values = record.getAttributes();

				for (int part = 0; part < record.getNumberOfParts(); part++)
				{
					VecBuffer buffer = record.getPointBuffer(part);
					int size = buffer.getSize();
					for (int i = 0; i < size; i++)
					{
						Position position = buffer.getPosition(i);
						layer.addPoint(position, values);
						bounds = Bounds.union(bounds, position);
					}
				}
			}

			layer.loadComplete();
		}
		catch (Exception e)
		{
			String message = "Error loading points";
			Logging.logger().log(Level.SEVERE, message, e);
			return false;
		}
		return true;
	}

	@Override
	public Bounds getBounds()
	{
		return bounds;
	}

	@Override
	public boolean isFollowTerrain()
	{
		return true;
	}
}
