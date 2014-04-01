package au.gov.ga.earthsci.worldwind.common.layers.model.gdal;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.gdal.GDALUtils;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.earthsci.worldwind.common.layers.Bounds;
import au.gov.ga.earthsci.worldwind.common.layers.data.AbstractDataProvider;
import au.gov.ga.earthsci.worldwind.common.layers.model.ModelLayer;
import au.gov.ga.earthsci.worldwind.common.layers.model.ModelProvider;
import au.gov.ga.earthsci.worldwind.common.layers.volume.btt.BinaryTriangleTree;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;
import au.gov.ga.earthsci.worldwind.common.util.CoordinateTransformationUtil;
import au.gov.ga.earthsci.worldwind.common.util.URLUtil;
import au.gov.ga.earthsci.worldwind.common.util.Util;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * A {@link ModelProvider} that reads a band from a GDAL-supported raster file
 * and treats band values as depth/elevation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModelProvider extends AbstractDataProvider<ModelLayer> implements ModelProvider
{
	private static final int COLOR_BUFFER_ELEMENT_SIZE = 4;

	private Bounds bounds = null;
	private boolean followTerrain = false;
	private GDALRasterModelParameters modelParameters = null;

	public GDALRasterModelProvider()
	{
		this(null);
	}

	public GDALRasterModelProvider(GDALRasterModelParameters parameters)
	{
		if (parameters == null)
		{
			this.modelParameters = new GDALRasterModelParameters();
		}
		else
		{
			this.modelParameters = parameters;
		}
	}

	@Override
	public Bounds getBounds()
	{
		return bounds;
	}

	@Override
	public boolean isFollowTerrain()
	{
		return followTerrain;
	}

	@Override
	protected boolean doLoadData(URL url, ModelLayer layer)
	{
		Validate.notNull(url, "A URL is required");
		Validate.notNull(layer, "A model layer is required");

		File file = URLUtil.urlToFile(url);

		// TODO: Add zip support

		Dataset gdalDataset;
		try
		{
			gdalDataset = GDALUtils.open(file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}

		List<Position> positions = new ArrayList<Position>();
		float[][] values = new float[gdalDataset.getRasterXSize()][gdalDataset.getRasterYSize()];
		float[] minmax = new float[] { Float.MAX_VALUE, -Float.MAX_VALUE };

		readValuesFromDataset(gdalDataset, positions, values, minmax);

		BinaryTriangleTree btt =
				new BinaryTriangleTree(positions, gdalDataset.GetRasterXSize(), gdalDataset.GetRasterYSize());
		btt.setForceGLTriangles(true);
		FastShape shape = btt.buildMesh(modelParameters.getMaxVariance());

		positions = shape.getPositions();

		shape.setForceSortedPrimitives(true);
		shape.setLighted(true);
		shape.setCalculateNormals(true);
		shape.setTwoSidedLighting(true);

		shape.setColorBuffer(createColorBufferForDataset(positions, values, minmax, gdalDataset));
		shape.setColorBufferElementSize(4);

		layer.addShape(shape);

		this.bounds = shape.getBounds();
		this.followTerrain = shape.isFollowTerrain();

		return true;
	}

	/**
	 * Reads the values from the provided dataset into:
	 * <ul>
	 * <li>The provided positions list <code>(lat,lon,elevation)</code>
	 * <li>The provided values array
	 * <code>values[x,y] = elevation | NaN (nodata)</code>
	 * </ul>
	 */
	private void readValuesFromDataset(Dataset gdalDataset, List<Position> positions, float[][] values, float[] minmax)
	{
		Band band = getModelBand(gdalDataset);

		int columns = band.getXSize();
		int rows = band.getYSize();

		ByteBuffer buffer = band.ReadRaster_Direct(0, 0, columns, rows, band.getDataType());
		buffer.order(ByteOrder.nativeOrder()); // @see Band.ReadRaster_Direct
		buffer.rewind();


		double elevationOffset = getOffset(band);
		double elevationScale = getScale(band);

		float nodata = getModelBandNodata(gdalDataset);

		double[] geoTransform = gdalDataset.GetGeoTransform();
		CoordinateTransformation coordinateTransformation = getCoordinateTransformation(gdalDataset);

		int dataType = band.getDataType();

		for (int y = 0; y < rows; y++)
		{
			for (int x = 0; x < columns; x++)
			{
				double datasetValue = getValue(buffer, dataType);
				double elevation = toElevation(elevationOffset, elevationScale, datasetValue);

				double[] transformedCoords = transformCoordinates(geoTransform, x, y);
				Position projectedCoordinates =
						projectCoordinates(coordinateTransformation, transformedCoords[0], transformedCoords[1],
								elevation);

				Position position;
				if (isNoData(nodata, (float) datasetValue))
				{
					// 'Smooth' out the mesh by setting nodata elevations to the last 'real' elevation value if available
					// This avoids nodata values 'falling' to the centre of the globe
					if (positions.size() > 0)
					{
						position =
								new PositionWithCoord(projectedCoordinates.latitude, projectedCoordinates.longitude,
										positions.get(positions.size() - 1).elevation, x, y);
					}
					else
					{
						position = new PositionWithCoord(projectedCoordinates, x, y);
					}
					values[x][y] = Float.NaN;
				}
				else
				{
					position = new PositionWithCoord(projectedCoordinates, x, y);
					minmax[0] = Math.min((float) position.elevation, minmax[0]);
					minmax[1] = Math.max((float) position.elevation, minmax[1]);
					values[x][y] = (float) position.elevation;
				}

				positions.add(position);
			}
		}
	}

	/**
	 * @return A coordinate transform to use for this raster
	 */
	private CoordinateTransformation getCoordinateTransformation(Dataset gdalDataset)
	{
		String rasterProjection = gdalDataset.GetProjection();

		// If no coordinate system can be found, default to wgs84
		if (Util.isBlank(rasterProjection) && Util.isBlank(modelParameters.getCoordinateSystem()))
		{
			Logging.logger().warning("Cannot determine coordinate system. Assuming EPSG:4326");
			return CoordinateTransformationUtil.getTransformationToWGS84("EPSG:4326");
		}

		// Only use the layer definition coordinate system if one is not present in the raster
		return CoordinateTransformationUtil.getTransformationToWGS84(Util.isBlank(rasterProjection) ? modelParameters
				.getCoordinateSystem() : rasterProjection);
	}

	/**
	 * @return The band to use for loading the model data
	 */
	private Band getModelBand(Dataset gdalDataset)
	{
		int band = modelParameters.getBand();
		if (band > gdalDataset.GetRasterCount())
		{
			Logging.logger().warning(
					"Cannot use specified band " + band + ". Raster dataset only has " + gdalDataset.GetRasterCount()
							+ " bands.");
			return gdalDataset.GetRasterBand(1);
		}
		return gdalDataset.GetRasterBand(band);
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
	private double[] transformCoordinates(double[] geoTransform, double x, double y)
	{
		double Xp = geoTransform[0] + x * geoTransform[1] + y * geoTransform[2];
		double Yp = geoTransform[3] + x * geoTransform[4] + y * geoTransform[5];
		return new double[] { Xp, Yp };
	}

	/**
	 * Project the provided x,y,z coordinates from the source SRS to WGS84
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
	 * 
	 * @return The coordinates projected in WGS84
	 */
	private Position projectCoordinates(CoordinateTransformation ct, double x, double y, double elevation)
	{
		if (ct == null)
		{
			return new Position(Angle.fromDegrees(y),
					Angle.fromDegrees(x),
					elevation);
		}

		double transformed[] = new double[3];
		ct.TransformPoint(transformed, x, y, elevation);

		Position projectedPosition = new Position(Angle.fromDegrees(transformed[1]),
				Angle.fromDegrees(transformed[0]),
				transformed[2]);
		return projectedPosition;
	}

	/**
	 * @return The data scale for the provided band
	 */
	private double getScale(Band band)
	{
		if (modelParameters.getScaleFactor() != null)
		{
			return modelParameters.getScaleFactor();
		}

		Double[] vals = new Double[1];
		band.GetScale(vals);
		return vals[0] != null ? vals[0] : 1.0;
	}

	/**
	 * @return The data offset for the provided band
	 */
	private double getOffset(Band band)
	{
		if (modelParameters.getOffset() != null)
		{
			return modelParameters.getOffset();
		}

		Double[] vals = new Double[1];
		band.GetOffset(vals);
		return vals[0] != null ? vals[0] : 0.0;
	}

	/**
	 * Convert a provided value (sampled from the raster band) to an elevation
	 * value in metres
	 */
	private double toElevation(double offset, double scale, double value)
	{
		return offset + (scale * value);
	}

	/**
	 * @return The next value from the provided buffer of the provided type
	 * 
	 * @see gdalconstConstants
	 */
	private double getValue(ByteBuffer buffer, int bufferType)
	{
		if (bufferType == gdalconstConstants.GDT_Float32 || bufferType == gdalconstConstants.GDT_CFloat32)
		{
			return buffer.getFloat();
		}
		else if (bufferType == gdalconstConstants.GDT_Float64 || bufferType == gdalconstConstants.GDT_CFloat64)
		{
			return buffer.getDouble();
		}
		else if (bufferType == gdalconstConstants.GDT_Byte)
		{
			return buffer.get() & 0xff;
		}
		else if (bufferType == gdalconstConstants.GDT_Int16 || bufferType == gdalconstConstants.GDT_CInt16)
		{
			return buffer.getShort();
		}
		else if (bufferType == gdalconstConstants.GDT_Int32 || bufferType == gdalconstConstants.GDT_CInt32)
		{
			return buffer.getInt();
		}
		else if (bufferType == gdalconstConstants.GDT_UInt16)
		{
			return getUInt16(buffer);
		}
		else if (bufferType == gdalconstConstants.GDT_UInt32)
		{
			return getUInt32(buffer);
		}
		else
		{
			throw new IllegalStateException("Unknown buffer type");
		}
	}

	private static int getUInt16(ByteBuffer buffer)
	{
		int first = 0xff & buffer.get();
		int second = 0xff & buffer.get();
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			return (first << 8 | second);
		}
		else
		{
			return (first | second << 8);
		}
	}

	private static long getUInt32(ByteBuffer buffer)
	{
		long first = 0xff & buffer.get();
		long second = 0xff & buffer.get();
		long third = 0xff & buffer.get();
		long fourth = 0xff & buffer.get();
		if (buffer.order() == ByteOrder.LITTLE_ENDIAN)
		{
			return (first << 24l | second << 16l | third << 8l | fourth);
		}
		else
		{
			return (first | second << 8l | third << 16l | fourth << 24l);
		}
	}

	/**
	 * Create and return an RGBA color buffer with an entry for each position in
	 * the provided dataset
	 * 
	 * @return An RGBA color buffer that can be used directly by the
	 *         {@link FastShape} class
	 */
	private float[] createColorBufferForDataset(List<Position> positions, float[][] values, float[] minmax,
			Dataset gdalDataset)
	{
		FloatBuffer colorBuffer = FloatBuffer.allocate(positions.size() * COLOR_BUFFER_ELEMENT_SIZE);
		for (Position position : positions)
		{
			PositionWithCoord pwv = (PositionWithCoord) position;

			int u = pwv.u;
			int v = pwv.v;

			float[] adjacentValues = getAdjacentValues(values, u, v);
			//check all values around the current position for NODATA; if NODATA, use a transparent color
			if (isNaN(adjacentValues))
			{
				for (int i = 0; i < COLOR_BUFFER_ELEMENT_SIZE; i++)
				{
					colorBuffer.put(0);
				}
			}
			else
			{
				Color color;
				if (modelParameters.getColorMap() != null)
				{
					color =
							modelParameters.getColorMap().calculateColorNotingIsValuesPercentages(values[u][v],
									minmax[0], minmax[1]);
				}
				else
				{
					color = modelParameters.getDefaultColor();
				}

				colorBuffer.put(color.getRed() / 255f)
						.put(color.getGreen() / 255f)
						.put(color.getBlue() / 255f)
						.put(color.getAlpha() / 255f);
			}
		}
		return colorBuffer.array();
	}

	private float[] getAdjacentValues(float[][] values, int u, int v)
	{
		int un = u > 0 ? u - 1 : u;
		int up = u < values.length - 1 ? u + 1 : u;
		int vn = v > 0 ? v - 1 : v;
		int vp = v < values[0].length - 1 ? v + 1 : v;

		float[] adjacentValues = new float[] {
				values[u][v], values[un][v],
				values[up][v], values[u][vn],
				values[u][vp], values[un][vn],
				values[up][vn], values[un][vp],
				values[up][vp],
		};
		return adjacentValues;
	}

	private float getModelBandNodata(Dataset gdalDataset)
	{
		Double[] nodatas = new Double[1];
		getModelBand(gdalDataset).GetNoDataValue(nodatas);
		float nodata = (float) (double) nodatas[0];
		return nodata;
	}

	/**
	 * @return <code>true</code> if any value in the provided values is NODATA
	 */
	private boolean isNoData(float nodata, float... values)
	{
		for (float f : values)
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
	private boolean isNaN(float... values)
	{
		for (float f : values)
		{
			if (Float.isNaN(f))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the modelParameters
	 */
	protected GDALRasterModelParameters getModelParameters()
	{
		return modelParameters;
	}

	/**
	 * A utility class that stores the pixel coordinates of a raster cell
	 * alongside it's real-world position
	 */
	protected static class PositionWithCoord extends Position
	{
		public final int u;
		public final int v;

		public PositionWithCoord(Position p, int u, int v)
		{
			this(p.latitude, p.longitude, p.elevation, u, v);
		}

		public PositionWithCoord(Angle latitude, Angle longitude, double elevation, int u, int v)
		{
			super(latitude, longitude, elevation);
			this.u = u;
			this.v = v;
		}

		public static PositionWithCoord fromDegrees(double latitude, double longitude, double elevation, int u, int v)
		{
			return new PositionWithCoord(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), elevation, u, v);
		}
	}
}
