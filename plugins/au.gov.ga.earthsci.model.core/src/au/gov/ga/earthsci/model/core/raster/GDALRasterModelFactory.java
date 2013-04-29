/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.model.core.raster;

import static au.gov.ga.earthsci.common.buffer.BufferUtil.getValue;
import static au.gov.ga.earthsci.core.raster.GDALRasterUtil.getBufferType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.osr.CoordinateTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.buffer.BufferType;
import au.gov.ga.earthsci.common.util.Validate;
import au.gov.ga.earthsci.model.IModel;
import au.gov.ga.earthsci.model.bounds.BoundingBox;
import au.gov.ga.earthsci.model.data.IModelData;
import au.gov.ga.earthsci.model.data.ModelDataBuilder;
import au.gov.ga.earthsci.model.geometry.BasicColouredMeshGeometry;
import au.gov.ga.earthsci.model.geometry.ModelGeometryStatistics;
import au.gov.ga.earthsci.model.render.RendererCreatorRegistry;
import au.gov.ga.earthsci.worldwind.common.util.CoordinateTransformationUtil;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * A factory class used to create {@link IModel} instances from GDAL raster
 * datasets according to a set of parameters.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModelFactory
{

	private static final int VERTEX_GROUP_SIZE = 3;

	private static final Logger logger = LoggerFactory.getLogger(GDALRasterModelFactory.class);

	// TODO: Move this somewhere
	private static final String WGS84 = "EPSG:4326";

	/**
	 * Create a new {@link GDALRasterModel} from the provided GDAL dataset and
	 * parameters
	 */
	public static GDALRasterModel createModel(Dataset ds, GDALRasterModelParameters parameters) throws Exception
	{
		Validate.notNull(ds, "A GDAL dataset is required"); //$NON-NLS-1$
		Validate.notNull(parameters, "Model parameters are required"); //$NON-NLS-1$

		ModelGeometryStatistics stats = new ModelGeometryStatistics();

		IModelData vertices = readVertices(ds, parameters, stats);

		BasicColouredMeshGeometry geometry =
				new BasicColouredMeshGeometry(UUID.randomUUID().toString(),
						ds.GetDescription(), ds.GetDescription());

		geometry.setVertices(vertices);

		geometry.setBoundingVolume(new BoundingBox(stats.getMinLon(), stats.getMaxLon(),
				stats.getMinLat(), stats.getMaxLat(),
				stats.getMinElevation(), stats.getMaxElevation()));

		geometry.setRenderer(RendererCreatorRegistry.getDefaultCreator(geometry).createRenderer(geometry));

		return new GDALRasterModel(null, geometry, ds, parameters,
				parameters.getModelName(),
				parameters.getModelDescription());
	}

	private GDALRasterModelFactory()
	{
	};

	/**
	 * Read vertice data from the given raster dataset using the provided
	 * parameters, and store calculated statistics about the mesh in the
	 * provided object for later use.
	 */
	private static IModelData readVertices(Dataset ds, GDALRasterModelParameters parameters,
			ModelGeometryStatistics stats)
	{
		Band band = ds.GetRasterBand(parameters.getElevationBandIndex());

		int columns = band.getXSize();
		int rows = band.getYSize();

		ByteBuffer buffer = readRasterBuffer(band, columns, rows);

		Double nodata = getNodata(band);

		// Transform pixel coords -> source coordinate system coords
		double[] geoTransform = ds.GetGeoTransform();

		// Transform source coordinate system -> WGS84
		CoordinateTransformation coordinateTransformation = getCoordinateTransform(parameters);

		BufferType sourceBufferType = getBufferType(band);

		double elevationOffset = getOffset(band, parameters);
		double elevationScale = getScale(band, parameters);

		double[] transformedCoords = new double[2];
		double[] projectedCoords = new double[VERTEX_GROUP_SIZE];

		ByteBuffer vertices = allocateVerticesBuffer(columns, rows);

		for (int y = 0; y < rows; y++)
		{
			for (int x = 0; x < columns; x++)
			{
				double datasetValue = getValue(buffer, sourceBufferType).doubleValue();
				double elevation = toElevation(elevationOffset, elevationScale, datasetValue, nodata);

				transformCoordinates(geoTransform, x, y, transformedCoords);
				projectCoordinates(coordinateTransformation,
						transformedCoords[0],
						transformedCoords[1],
						elevation,
						projectedCoords);

				vertices.putFloat((float) projectedCoords[0])
						.putFloat((float) projectedCoords[1])
						.putFloat((float) projectedCoords[2]);

				stats.updateStats(projectedCoords[1], projectedCoords[0], projectedCoords[2]);
			}
		}

		// TODO Move name/description to constant somewhere for reuse as standard name
		return ModelDataBuilder.createFromBuffer(vertices)
				.ofType(BufferType.FLOAT)
				.withNodata(nodata == null ? null : nodata.floatValue())
				.named("Vertices")
				.describedAs("Vertices")
				.build();

	}

	private static ByteBuffer allocateVerticesBuffer(int columns, int rows)
	{
		int bufferSize = columns * rows * VERTEX_GROUP_SIZE * BufferType.FLOAT.getNumberOfBytes();
		ByteBuffer vertices = ByteBuffer.allocate(bufferSize);
		vertices.order(ByteOrder.nativeOrder());
		return vertices;
	}

	private static CoordinateTransformation getCoordinateTransform(GDALRasterModelParameters parameters)
	{
		String sourceProjection = parameters.getSourceProjection();
		if (Util.isBlank(sourceProjection))
		{
			logger.info("No source projection found. Assuming WGS84."); //$NON-NLS-1$
			sourceProjection = WGS84;
		}
		CoordinateTransformation coordinateTransformation =
				CoordinateTransformationUtil.getTransformationToWGS84(sourceProjection);
		return coordinateTransformation;
	}

	private static ByteBuffer readRasterBuffer(Band band, int columns, int rows)
	{
		ByteBuffer buffer = band.ReadRaster_Direct(0, 0, columns, rows, band.getDataType());
		buffer.order(ByteOrder.nativeOrder()); // @see Band.ReadRaster_Direct
		buffer.rewind();
		return buffer;
	}

	private static Double getNodata(Band band)
	{
		Double[] nodatas = new Double[1];
		band.GetNoDataValue(nodatas);
		Double nodata = nodatas[0];
		return nodata;
	}

	private static double toElevation(double elevationOffset, double elevationScale, double datasetValue, double nodata)
	{
		if (isNoData(nodata, datasetValue))
		{
			return nodata;
		}
		return elevationOffset + (elevationScale * datasetValue);
	}

	/**
	 * @return The data scale for the provided band
	 */
	private static double getScale(Band band, GDALRasterModelParameters parameters)
	{
		if (parameters.getScaleFactor() != null)
		{
			return parameters.getScaleFactor();
		}

		Double[] vals = new Double[1];
		band.GetScale(vals);
		return vals[0] != null ? vals[0] : 1.0;
	}

	/**
	 * @return The data offset for the provided band
	 */
	private static double getOffset(Band band, GDALRasterModelParameters parameters)
	{
		if (parameters.getOffset() != null)
		{
			return parameters.getOffset();
		}

		Double[] vals = new Double[1];
		band.GetOffset(vals);
		return vals[0] != null ? vals[0] : 0.0;
	}

	/**
	 * Transform the dataset pixel coordinates [Xs,Ys] into coordinates in the
	 * dataset's source projection [Xp,Yp]
	 * 
	 * @param geoTransform
	 *            The geo transform coefficients
	 * @param x
	 *            Pixel X coordinate
	 * @param y
	 *            Pixel Y coordinate
	 * 
	 * @return The provided coordinates transformed into the source projection
	 *         [Xp,Yp]
	 * 
	 * @see Dataset#GetGeoTransform()
	 */
	private static double[] transformCoordinates(double[] geoTransform, double x, double y, double[] out)
	{
		double Xp = geoTransform[0] + x * geoTransform[1] + y * geoTransform[2];
		double Yp = geoTransform[3] + x * geoTransform[4] + y * geoTransform[5];

		if (out == null)
		{
			out = new double[2];
		}
		out[0] = Xp;
		out[1] = Yp;

		return out;
	}

	/**
	 * Project the provided x,y,z coordinates from the source SRS using the
	 * provided coordinate transformation
	 * 
	 * @param ct
	 *            The coordinate transformation to use for transforming the
	 *            coordinates
	 * @param x
	 *            The x coordinate in source SRS
	 * @param y
	 *            The y coordinate in source SRS
	 * @param elevation
	 *            The elevation in source SRS
	 * @param out
	 *            A 3-element array to hold the projected coordinates
	 * 
	 * @return The projected coordinates contained in {@code out}
	 */
	private static double[] projectCoordinates(CoordinateTransformation ct, double x, double y, double elevation,
			double[] out)
	{
		if (out == null)
		{
			out = new double[3];
		}

		if (ct == null)
		{
			out[0] = x;
			out[1] = y;
			out[2] = elevation;
		}

		ct.TransformPoint(out, x, y, elevation);

		return out;
	}

	/**
	 * @return <code>true</code> if any value in the provided values is NODATA
	 */
	private static boolean isNoData(double nodata, double... values)
	{
		for (double f : values)
		{
			if (f == nodata)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @return <code>true</code> if any value in the provided values is NaN
	 */
	private static boolean isNaN(double... values)
	{
		for (double f : values)
		{
			if (Double.isNaN(f))
			{
				return true;
			}
		}
		return false;
	}

}
