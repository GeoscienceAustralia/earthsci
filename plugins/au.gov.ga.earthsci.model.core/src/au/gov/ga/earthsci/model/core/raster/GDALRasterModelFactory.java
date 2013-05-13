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
import static au.gov.ga.earthsci.common.buffer.BufferUtil.skipValues;
import static au.gov.ga.earthsci.core.raster.GDALRasterUtil.getBufferType;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.osr.CoordinateTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.buffer.BufferType;
import au.gov.ga.earthsci.common.buffer.BufferUtil;
import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.common.color.ColorMap.InterpolationMode;
import au.gov.ga.earthsci.common.spatial.SpatialReferences;
import au.gov.ga.earthsci.common.util.Validate;
import au.gov.ga.earthsci.model.IModel;
import au.gov.ga.earthsci.model.bounds.BoundingBox;
import au.gov.ga.earthsci.model.data.IModelData;
import au.gov.ga.earthsci.model.data.ModelDataBuilder;
import au.gov.ga.earthsci.model.geometry.BasicColouredMeshGeometry;
import au.gov.ga.earthsci.model.geometry.ColourType;
import au.gov.ga.earthsci.model.geometry.FaceType;
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
	private static final int RGBA_GROUP_SIZE = 4;

	private static final Logger logger = LoggerFactory.getLogger(GDALRasterModelFactory.class);

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
		IModelData vertexColours = createVertexColours(ds, parameters, stats, vertices);
		IModelData edges = createEdges(ds, parameters, stats, vertices);

		BasicColouredMeshGeometry geometry =
				new BasicColouredMeshGeometry(UUID.randomUUID().toString(),
						ds.GetDescription(), ds.GetDescription());

		geometry.setVertices(vertices);
		geometry.setEdgeIndices(edges);
		geometry.setFaceType(FaceType.TRIANGLE_STRIP);
		geometry.setVertexColour(vertexColours);
		geometry.setColourType(ColourType.RGBA);

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

		int rasterXSize = band.getXSize();
		int rasterYSize = band.getYSize();

		ByteBuffer buffer = readRasterBuffer(band, rasterXSize, rasterYSize);

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

		int stride = parameters.getNormalisedSubsample();
		ByteBuffer vertices = allocateVerticesBuffer(rasterXSize, rasterYSize, stride);

		for (int y = 0; y < rasterYSize; y += stride)
		{
			for (int x = 0; x < rasterXSize; x += stride)
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

				int step = Math.min(stride, rasterXSize - x) - 1; // Skip stride values, or move to the end of the column
				skipValues(step, buffer, sourceBufferType);
			}
			int step = Math.min(stride, rasterYSize - y) - 1;
			skipValues(rasterXSize * step, buffer, sourceBufferType);
		}

		// TODO Move name/description to constant somewhere for reuse as standard name
		return ModelDataBuilder.createFromBuffer(vertices)
				.ofType(BufferType.FLOAT)
				.withNodata(nodata == null ? null : nodata.floatValue())
				.named("Vertices")
				.describedAs("Vertices")
				.withGroupSize(3)
				.build();

	}

	private static IModelData createVertexColours(Dataset ds, GDALRasterModelParameters parameters,
			ModelGeometryStatistics stats, IModelData vertexData)
	{
		// TODO: replace this with a user-configured map
		// ----------------------------------------------
		Map<Double, Color> entries = new HashMap<Double, Color>();
		entries.put(0.0, new Color(1.0f, 0.0f, 0.0f, 1.0f));
		entries.put(0.5, new Color(0.0f, 1.0f, 0.0f, 1.0f));
		entries.put(1.0, new Color(0.0f, 0.0f, 1.0f, 1.0f));
		ColorMap map = new ColorMap(entries, new Color(0, 0, 0, 0), InterpolationMode.INTERPOLATE_HUE, true);
		// -----------------------------------------------

		int numVertices = vertexData.getNumberOfGroups();

		ByteBuffer colours = allocateVertexColourBuffer(numVertices);
		ByteBuffer vertices = vertexData.getSource();

		float[] rgba = new float[4];
		for (int i = 0; i < numVertices; i++)
		{
			BufferUtil.skipValues(2, vertices, vertexData.getBufferType());
			float elevation = BufferUtil.getValue(vertices, vertexData.getBufferType()).floatValue();

			Color color = map.getColor(elevation, stats.getMinElevation(), stats.getMaxElevation());
			color.getRGBComponents(rgba);

			colours.putFloat(rgba[0]);
			colours.putFloat(rgba[1]);
			colours.putFloat(rgba[2]);
			colours.putFloat(rgba[3]);
		}

		return ModelDataBuilder.createFromBuffer(colours)
				.ofType(BufferType.FLOAT)
				.withGroupSize(RGBA_GROUP_SIZE)
				.named("Vertex Colours")
				.describedAs("Vertex Colours")
				.build();

	}

	/**
	 * Create and return edge indices data from the provided dataset parameters
	 * and vertices.
	 * <p/>
	 * The returned edges are intended for use with the
	 * {@link FaceType#TRIANGLE_STRIP} type.
	 */
	private static IModelData createEdges(Dataset ds, GDALRasterModelParameters parameters,
			ModelGeometryStatistics stats, IModelData vertices)
	{
		Band band = ds.GetRasterBand(parameters.getElevationBandIndex());
		int rasterXSize = band.getXSize();
		int rasterYSize = band.getYSize();

		int subsample = parameters.getNormalisedSubsample();

		int numColumns = subsample(rasterXSize, subsample);
		int numRows = subsample(rasterYSize, subsample);

		ByteBuffer edges = allocateEdgesBuffer(rasterXSize, rasterYSize, subsample);

		for (int y = 0; y < numRows - 1; y++)
		{
			for (int x = 0; x < numColumns; x++)
			{
				int top = y * numColumns + x;
				int bottom = top + numColumns;

				edges.putInt(top);
				edges.putInt(bottom);
				if (x == numColumns - 1 && y < numRows - 2)
				{
					// Put two empty triangles to reset back to the next strip
					edges.putInt(bottom);
					edges.putInt(bottom);

					int nextTop = (y + 1) * numColumns;
					edges.putInt(nextTop);
					edges.putInt(nextTop);
				}
			}
		}

		// Edges are full, so make sure limit is set appropriately
		edges.limit(edges.position());

		// TODO Move name/description to constant somewhere for reuse as standard name
		return ModelDataBuilder.createFromBuffer(edges)
				.ofType(BufferType.INT)
				.named("Edges")
				.describedAs("Edges")
				.withGroupSize(1)
				.build();
	}

	private static ByteBuffer allocateVerticesBuffer(int rasterXSize, int rasterYSize, int subsample)
	{
		int numColumns = subsample(rasterXSize, subsample);
		int numRows = subsample(rasterYSize, subsample);

		int numVerts = numColumns * numRows;

		ByteBuffer vertices = allocateBuffer(numVerts * VERTEX_GROUP_SIZE * BufferType.FLOAT.getNumberOfBytes());
		return vertices;
	}

	private static ByteBuffer allocateVertexColourBuffer(int numVertices)
	{
		ByteBuffer colours = allocateBuffer(numVertices * RGBA_GROUP_SIZE * BufferType.FLOAT.getNumberOfBytes());
		return colours;
	}

	private static ByteBuffer allocateEdgesBuffer(int rasterXSize, int rasterYSize, int subsample)
	{
		int numColumns = subsample(rasterXSize, subsample);
		int numRows = subsample(rasterYSize, subsample);

		// Each row has 2 indices for each vertex, plus 4 terminating indices (2 @ start and end)
		// The exception is first and last row, which have only 1 index per vertex and no terminating indices
		int numIndices = (2 * numColumns * (numRows - 1)) + 4 * (numRows - 2);

		ByteBuffer edges = allocateBuffer(numIndices * BufferType.INT.getNumberOfBytes());
		return edges;
	}

	private static CoordinateTransformation getCoordinateTransform(GDALRasterModelParameters parameters)
	{
		String sourceProjection = parameters.getSourceProjection();
		if (Util.isBlank(sourceProjection))
		{
			logger.info("No source projection found. Assuming WGS84."); //$NON-NLS-1$
			return new CoordinateTransformation(SpatialReferences.WGS84, SpatialReferences.WGS84);
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

	private static int subsample(int original, int subsample)
	{
		return (original + subsample - 1) / subsample;
	}

	private static ByteBuffer allocateBuffer(int size)
	{
		ByteBuffer result = ByteBuffer.allocate(size);
		result.order(ByteOrder.nativeOrder());
		return result;
	}

}
