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
package au.gov.ga.earthsci.core.model.raster;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.osr.CoordinateTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.buffer.BufferType;
import au.gov.ga.earthsci.common.buffer.BufferUtil;
import au.gov.ga.earthsci.core.raster.GDALRasterUtil;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.IIntentHandler;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.model.IModel;
import au.gov.ga.earthsci.model.data.IModelData;
import au.gov.ga.earthsci.model.data.ModelDataBuilder;
import au.gov.ga.earthsci.model.geometry.BasicColouredMeshGeometry;
import au.gov.ga.earthsci.worldwind.common.util.CoordinateTransformationUtil;
import au.gov.ga.earthsci.worldwind.common.util.URLUtil;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * An intent handler that responds to intents that match GDAL-supported raster
 * formats and generates an IModel
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModelIntentHandler implements IIntentHandler
{

	private static final String WGS84 = "EPSG:4326"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(GDALRasterModelIntentHandler.class);

	@Override
	public void handle(final Intent intent, final IIntentCallback callback)
	{
		try
		{
			final URL url = intent.getURL();
			if (url == null)
			{
				throw new IllegalArgumentException("Intent URL is null"); //$NON-NLS-1$
			}

			// TODO Use retrieval service to retrieve URL and attach model creation to the completed
			// lifecycle phase. This requires Issue #15 to be addressed so that a File object can
			// be obtained from the retrieval result.

			if (!URLUtil.isFileUrl(url))
			{
				throw new IllegalArgumentException("Currently only file:// URLs are supported for this feature"); //$NON-NLS-1$
			}

			File source = URLUtil.urlToFile(url);
			IModel result = createModel(source);
			callback.completed(result, intent);
		}
		catch (Exception e)
		{
			callback.error(e, intent);
		}
	}

	/**
	 * Create an {@link IModel} instance from the GDAL raster referenced by the
	 * provided file
	 * 
	 * @param source
	 *            The source raster to load
	 * 
	 * @return A created {@link IModel} instance, or <code>null</code> if one
	 *         could not be created
	 * 
	 * @throws Exception
	 *             If something goes wrong during creation
	 */
	private IModel createModel(File source) throws Exception
	{
		Dataset ds = gdal.Open(source.getAbsolutePath());
		if (ds == null)
		{
			throw new IllegalArgumentException(gdal.GetLastErrorMsg());
		}
		return createModel(ds);
	}

	/**
	 * Create an {@link IModel} instance from the GDAL raster referenced by the
	 * provided dataset.
	 * 
	 * @param ds
	 *            The GDAL dataset to load the model from
	 * 
	 * @return A created {@link IModel} instance, or <code>null</code> if one
	 *         could not be created
	 * 
	 * @throws Exception
	 *             If something goes wrong during creation
	 */
	private IModel createModel(Dataset ds) throws Exception
	{
		GDALRasterModelParameters parameters = new GDALRasterModelParameters(ds);

		// TODO: Launch wizard to collect additional params

		IModelData vertices = readVertices(ds, parameters);

		BasicColouredMeshGeometry geometry =
				new BasicColouredMeshGeometry(UUID.randomUUID().toString(),
						ds.GetDescription(), ds.GetDescription());

		geometry.setVertices(vertices);

		return new GDALRasterModel(null, geometry, ds, parameters);
	}

	private IModelData readVertices(Dataset ds, GDALRasterModelParameters parameters)
	{
		Band band = ds.GetRasterBand(parameters.getElevationBandIndex());

		int columns = band.getXSize();
		int rows = band.getYSize();

		// Get the buffer from the raster band
		ByteBuffer buffer = band.ReadRaster_Direct(0, 0, columns, rows, band.getDataType());
		buffer.order(ByteOrder.nativeOrder()); // @see Band.ReadRaster_Direct
		buffer.rewind();

		// Retrieve the nodata value
		Double[] nodatas = new Double[1];
		band.GetNoDataValue(nodatas);

		// Create a coordinate transformation to transform into WGS84
		String sourceProjection = parameters.getSourceProjection();
		if (Util.isBlank(sourceProjection))
		{
			logger.info("No source projection found. Assuming WGS84."); //$NON-NLS-1$
			sourceProjection = WGS84;
		}
		double[] geoTransform = ds.GetGeoTransform();
		CoordinateTransformation coordinateTransformation =
				CoordinateTransformationUtil.getTransformationToWGS84(sourceProjection);

		BufferType sourceBufferType = GDALRasterUtil.getBufferType(band);

		double elevationOffset = getOffset(band, parameters);
		double elevationScale = getScale(band, parameters);

		double[] transformedCoords = new double[2];
		double[] projectedCoords = new double[3];

		ByteBuffer vertices =
				ByteBuffer.allocate(columns * rows * projectedCoords.length * BufferType.FLOAT.getNumberOfBytes());

		for (int y = 0; y < rows; y++)
		{
			for (int x = 0; x < columns; x++)
			{
				double datasetValue = BufferUtil.getValue(buffer, sourceBufferType).doubleValue();
				double elevation = elevationOffset + (elevationScale * datasetValue);

				transformCoordinates(geoTransform, x, y, transformedCoords);
				projectCoordinates(coordinateTransformation,
						transformedCoords[0],
						transformedCoords[1],
						elevation,
						projectedCoords);

				vertices.putFloat((float) projectedCoords[0])
						.putFloat((float) projectedCoords[1])
						.putFloat((float) projectedCoords[2]);
			}
		}

		// TODO Move name/description to constant somewhere for reuse as standard name
		return ModelDataBuilder.createFromBuffer(vertices)
				.ofType(BufferType.FLOAT)
				.named("Vertices")
				.withDescription("Vertices")
				.build();

	}

	/**
	 * @return The data scale for the provided band
	 */
	private double getScale(Band band, GDALRasterModelParameters parameters)
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
	private double getOffset(Band band, GDALRasterModelParameters parameters)
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
	private double[] transformCoordinates(double[] geoTransform, double x, double y, double[] out)
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
	private double[] projectCoordinates(CoordinateTransformation ct, double x, double y, double elevation, double[] out)
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
	private boolean isNoData(double nodata, double... values)
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
	private boolean isNaN(double... values)
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
