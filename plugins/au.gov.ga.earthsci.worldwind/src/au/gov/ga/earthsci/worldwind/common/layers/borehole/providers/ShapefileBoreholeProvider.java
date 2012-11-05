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
package au.gov.ga.earthsci.worldwind.common.layers.borehole.providers;

import gov.nasa.worldwind.formats.shapefile.DBaseRecord;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileUtils;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.VecBuffer;

import java.net.URL;
import java.util.logging.Level;

import au.gov.ga.earthsci.worldwind.common.layers.borehole.BoreholeLayer;
import au.gov.ga.earthsci.worldwind.common.layers.borehole.BoreholeProvider;
import au.gov.ga.earthsci.worldwind.common.layers.data.AbstractDataProvider;
import au.gov.ga.earthsci.worldwind.common.util.URLUtil;

/**
 * Implementation of {@link BoreholeProvider} that provides borehole data to a
 * {@link BoreholeLayer} from a zipped shapefile source.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ShapefileBoreholeProvider extends AbstractDataProvider<BoreholeLayer> implements BoreholeProvider
{
	private Sector sector;

	@Override
	protected boolean doLoadData(URL url, BoreholeLayer layer)
	{
		try
		{
			Shapefile shapefile = ShapefileUtils.openZippedShapefile(URLUtil.urlToFile(url));
			while (shapefile.hasNext())
			{
				ShapefileRecord record = shapefile.nextRecord();
				DBaseRecord values = record.getAttributes();

				for (int part = 0; part < record.getNumberOfParts(); part++)
				{
					VecBuffer buffer = record.getPointBuffer(part);
					int size = buffer.getSize();

					if (Shapefile.isPointType(shapefile.getShapeType()))
					{
						for (int i = 0; i < size; i++)
						{
							layer.addBoreholeSample(buffer.getPosition(i), values);
						}
					}
					else
					{
						//if the shapefile is not a point shapefile, then calculate the centroid of the feature and use that instead
						
						Sector sector = null;
						double elevation = 0;
						for (int i = 0; i < size; i++)
						{
							Position position = buffer.getPosition(i);
							if (sector == null)
							{
								sector =
										new Sector(position.latitude, position.longitude, position.latitude,
												position.longitude);
							}
							else
							{
								sector = sector.union(position.latitude, position.longitude);
							}
							elevation += position.elevation;
						}
						if (sector != null)
						{
							layer.addBoreholeSample(new Position(sector.getCentroid(), elevation / size), values);
						}
					}
				}
			}

			sector = Sector.fromDegrees(shapefile.getBoundingRectangle());
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
	public Sector getSector()
	{
		return sector;
	}
}
