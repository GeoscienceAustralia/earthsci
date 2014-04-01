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
package au.gov.ga.earthsci.worldwind.common.layers.geometry.provider;

import static au.gov.ga.earthsci.worldwind.common.util.Util.isBlank;
import gov.nasa.worldwind.formats.shapefile.DBaseRecord;
import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.formats.shapefile.ShapefileRecord;
import gov.nasa.worldwind.formats.shapefile.ShapefileUtils;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.VecBuffer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import au.gov.ga.earthsci.worldwind.common.layers.Bounds;
import au.gov.ga.earthsci.worldwind.common.layers.data.AbstractDataProvider;
import au.gov.ga.earthsci.worldwind.common.layers.geometry.BasicShapeImpl;
import au.gov.ga.earthsci.worldwind.common.layers.geometry.GeometryLayer;
import au.gov.ga.earthsci.worldwind.common.layers.geometry.Shape;
import au.gov.ga.earthsci.worldwind.common.layers.geometry.Shape.Type;
import au.gov.ga.earthsci.worldwind.common.layers.geometry.ShapeProvider;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.URLUtil;

/**
 * A {@link ShapeProvider} that loads shapes from a zipped Shapefile.
 * <p/>
 * A new shape is defined for each record in the shapefile.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ShapefileShapeProvider extends AbstractDataProvider<GeometryLayer> implements ShapeProvider
{
	private static Map<String, Type> shapeTypeMap = new HashMap<String, Type>();
	static
	{
		shapeTypeMap.put(Shapefile.SHAPE_POINT, Type.POINT);
		shapeTypeMap.put(Shapefile.SHAPE_POINT_M, Type.POINT);
		shapeTypeMap.put(Shapefile.SHAPE_POINT_Z, Type.POINT);

		shapeTypeMap.put(Shapefile.SHAPE_POLYLINE, Type.LINE);
		shapeTypeMap.put(Shapefile.SHAPE_POLYLINE_M, Type.LINE);
		shapeTypeMap.put(Shapefile.SHAPE_POLYLINE_Z, Type.LINE);

		shapeTypeMap.put(Shapefile.SHAPE_POLYGON, Type.POLYGON);
		shapeTypeMap.put(Shapefile.SHAPE_POLYGON_M, Type.POLYGON);
		shapeTypeMap.put(Shapefile.SHAPE_POLYGON_Z, Type.POLYGON);
	}

	private Bounds bounds;

	@Override
	protected boolean doLoadData(URL url, GeometryLayer layer)
	{
		try
		{
			bounds = null;
			Shapefile shapefile = ShapefileUtils.openZippedShapefile(URLUtil.urlToFile(url));
			while (shapefile.hasNext())
			{
				ShapefileRecord record = shapefile.nextRecord();
				DBaseRecord values = record.getAttributes();

				Shape loadedShape =
						new BasicShapeImpl(url.getPath() + record.getRecordNumber(), getShapeTypeForRecord(layer,
								record));
				for (int part = 0; part < record.getNumberOfParts(); part++)
				{
					VecBuffer buffer = record.getPointBuffer(part);
					int size = buffer.getSize();
					for (int i = 0; i < size; i++)
					{
						Position position = buffer.getPosition(i);
						loadedShape.addPoint(position, values);
						bounds = Bounds.union(bounds, position);
					}
				}

				layer.addShape(loadedShape);
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

	/**
	 * @return The shape type to use for the provided record. Checks for an
	 *         override in the layer before inspecting the shapefile record.
	 */
	private Type getShapeTypeForRecord(GeometryLayer layer, ShapefileRecord record)
	{
		if (!isBlank(layer.getStringValue(AVKeyMore.SHAPE_TYPE)))
		{
			return Type.valueOf(layer.getStringValue(AVKeyMore.SHAPE_TYPE).toUpperCase());
		}
		return getShapeTypeFromRecord(record);
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

	private static Type getShapeTypeFromRecord(ShapefileRecord record)
	{
		return shapeTypeMap.get(record.getShapeType());
	}

}
