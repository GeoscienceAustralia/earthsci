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
package au.gov.ga.earthsci.worldwind.common.layers.model.gocad;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.earthsci.worldwind.common.layers.volume.btt.BinaryTriangleTree;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * {@link GocadReader} implementation for reading GOCAD GSurf files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadGSurfReader implements GocadReader<FastShape>
{
	public final static String HEADER_REGEX = "(?i).*gsurf.*";

	//axis pattern is different to voxet axis pattern, because AXIS_N only has 2 values instead of 3
	private final static Pattern axisPattern = Pattern
			.compile("(?:(ORIGIN)|(?:AXIS_(\\S+)))\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)(?:\\s+([\\d.\\-]+))?.*");
	private final static Pattern typePattern = Pattern.compile("TYPE\\s+(\\w+)\\s*");
	private final static Pattern propertyIdPattern = Pattern.compile("PROPERTY\\s+(\\d+)\\s+\"?([^\"]+)\"?.*");

	private String name;
	private boolean zPositive = true;

	private Vec4 axisO;
	private Vec4 axisU;
	private Vec4 axisV;
	private Vec4 axisW;
	private Vec4 axisMIN;
	private Vec4 axisMAX;
	private Vec4 axisN;

	private String paintedVariableName;
	private int paintedVariableId = 1;

	private Double elevationNoDataValue = null;
	private int elevationEsize = 4;
	private String elevationEtype = "IEEE";
	private int elevationOffset = 0;
	private String elevationFile;

	private Double propertyNoDataValue = null;
	private int propertyEsize = 4;
	private String propertyEtype = "IEEE";
	private int propertyOffset = 0;
	private String propertyFile;

	private Color color;
	private boolean cellCentered = false;

	private GocadReaderParameters parameters;

	@Override
	public void begin(GocadReaderParameters parameters)
	{
		this.parameters = parameters;
		paintedVariableName = parameters.getPaintedVariable();
	}

	@Override
	public void addLine(String line)
	{
		Matcher matcher = axisPattern.matcher(line);
		if (matcher.matches())
		{
			parseAxis(matcher);
			return;
		}

		matcher = propertyPattern.matcher(line);
		if (matcher.matches())
		{
			parseProperty(matcher);
			return;
		}

		matcher = namePattern.matcher(line);
		if (matcher.matches())
		{
			name = matcher.group(1);
			return;
		}

		matcher = zpositivePattern.matcher(line);
		if (matcher.matches())
		{
			zPositive = !matcher.group(1).equalsIgnoreCase("depth");
			return;
		}

		matcher = solidColorPattern.matcher(line);
		if (matcher.matches())
		{
			color = GocadColor.gocadLineToColor(line);
			return;
		}

		matcher = typePattern.matcher(line);
		if (matcher.matches())
		{
			cellCentered = matcher.group(1).equalsIgnoreCase("cells");
			return;
		}

		matcher = paintedVariablePattern.matcher(line);
		if (matcher.matches())
		{
			if (parameters.getPaintedVariable() == null)
			{
				paintedVariableName = matcher.group(1);
			}
			return;
		}

		matcher = propertyIdPattern.matcher(line);
		if (matcher.matches())
		{
			int id = Integer.parseInt(matcher.group(1));
			String propertyName = matcher.group(2);
			if (propertyName.equalsIgnoreCase(paintedVariableName))
			{
				paintedVariableId = id;
			}
		}
	}

	private void parseAxis(Matcher matcher)
	{
		String type = matcher.group(1);
		if (type == null)
		{
			type = matcher.group(2);
		}
		double d0 = Double.parseDouble(matcher.group(3));
		double d1 = Double.parseDouble(matcher.group(4));
		double d2 = 0;
		String group5 = matcher.group(5);
		if (group5 != null)
		{
			d2 = Double.parseDouble(group5);
		}

		Vec4 v = new Vec4(d0, d1, d2);
		if (type.equals("O") || type.equals("ORIGIN"))
		{
			axisO = v;
		}
		else if (type.equals("U"))
		{
			axisU = v;
		}
		else if (type.equals("V"))
		{
			axisV = v;
		}
		else if (type.equals("W"))
		{
			axisW = v;
		}
		else if (type.equals("MIN"))
		{
			axisMIN = v;
		}
		else if (type.equals("MAX"))
		{
			axisMAX = v;
		}
		else if (type.equals("N"))
		{
			axisN = new Vec4(v.x, v.y, 1);
		}
	}

	private void parseProperty(Matcher matcher)
	{
		String type = matcher.group(1);
		int id = Integer.parseInt(matcher.group(2));
		String value = matcher.group(3);

		//currently only read the first property's parameters:
		if (type.equals("NO_DATA_VALUE"))
		{
			if (id == 1)
			{
				elevationNoDataValue = Double.parseDouble(value);
			}
			else if (id == paintedVariableId)
			{
				propertyNoDataValue = Double.parseDouble(value);
			}
		}
		else if (type.equals("ESIZE"))
		{
			if (id == 1)
			{
				elevationEsize = Integer.parseInt(value);
			}
			else if (id == paintedVariableId)
			{
				propertyEsize = Integer.parseInt(value);
			}
		}
		else if (type.equals("TYPE"))
		{
			if (id == 1)
			{
				elevationEtype = value;
			}
			else if (id == paintedVariableId)
			{
				propertyEtype = value;
			}
		}
		else if (type.equals("OFFSET"))
		{
			if (id == 1)
			{
				elevationOffset = Integer.parseInt(value);
			}
			else if (id == paintedVariableId)
			{
				propertyOffset = Integer.parseInt(value);
			}
		}
		else if (type.equals("FILE"))
		{
			if (id == 1)
			{
				elevationFile = value;
			}
			else if (id == paintedVariableId)
			{
				propertyFile = value;
			}
		}
	}

	@Override
	public FastShape end(URL context)
	{
		if (axisN == null)
		{
			return null;
		}

		if (cellCentered)
		{
			axisN = new Vec4(axisN.x - 1, axisN.y - 1, 1);
		}

		int nu = (int) Math.round(axisN.x), nv = (int) Math.round(axisN.y);
		Vec4 axisUStride = axisU.multiply3((axisMAX.x - axisMIN.x) / (axisN.x - 1));
		Vec4 axisVStride = axisV.multiply3((axisMAX.y - axisMIN.y) / (axisN.y - 1));
		Vec4 axisUOrigin = axisU.multiply3(axisMIN.x);
		Vec4 axisVOrigin = axisV.multiply3(axisMIN.y);
		Vec4 axisWOrigin = axisW.multiply3(axisMIN.z);
		Vec4 origin =
				new Vec4(axisO.x + axisUOrigin.x + axisVOrigin.x + axisWOrigin.x, axisO.y + axisUOrigin.y
						+ axisVOrigin.y + axisWOrigin.y, axisO.z + axisUOrigin.z + axisVOrigin.z + axisWOrigin.z);

		Validate.isTrue(elevationEsize == 4, "Unsupported PROP_ESIZE value: " + elevationEsize);
		Validate.isTrue("IEEE".equals(elevationEtype), "Unsupported PROP_ETYPE value: " + elevationEtype);

		int strideU = parameters.getSubsamplingU();
		int strideV = parameters.getSubsamplingV();

		if (parameters.isDynamicSubsampling())
		{
			float samplesPerAxis = parameters.getDynamicSubsamplingSamplesPerAxis();
			strideU = Math.max(1, Math.round((float) axisN.x / samplesPerAxis));
			strideV = Math.max(1, Math.round((float) axisN.y / samplesPerAxis));
		}

		int uSamples = (1 + (nu - 1) / strideU);
		int vSamples = (1 + (nv - 1) / strideV);

		List<Position> positions = new ArrayList<Position>(uSamples * vSamples);
		float[] values = new float[uSamples * vSamples];
		float[] minmax = new float[2];

		try
		{
			if (propertyFile != null)
			{
				//first read elevations into array, to create the list of positions
				readFileIntoFloatArray(context, elevationFile, elevationOffset, elevationEtype, elevationEsize,
						elevationNoDataValue, positions, values, minmax, nu, nv, uSamples, vSamples, strideU, strideV,
						origin, axisUStride, axisVStride, parameters.isBilinearMinification());
				//next read the property file into the array for the actual values (passing null for positions)
				readFileIntoFloatArray(context, propertyFile, propertyOffset, propertyEtype, propertyEsize,
						propertyNoDataValue, null, values, minmax, nu, nv, uSamples, vSamples, strideU, strideV,
						origin, axisUStride, axisVStride, parameters.isBilinearMinification());
			}
			else
			{
				//no property file, so use elevations as values
				readFileIntoFloatArray(context, elevationFile, elevationOffset, elevationEtype, elevationEsize,
						elevationNoDataValue, positions, values, minmax, nu, nv, uSamples, vSamples, strideU, strideV,
						origin, axisUStride, axisVStride, parameters.isBilinearMinification());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}

		BinaryTriangleTree btt = new BinaryTriangleTree(positions, uSamples, vSamples);
		btt.setForceGLTriangles(true); //ensures that the shape's triangles can be sorted when transparent
		FastShape shape = btt.buildMesh(parameters.getMaxVariance());
		positions = shape.getPositions();

		if (name == null)
		{
			name = "GSurf";
		}

		shape.setName(name);
		shape.setForceSortedPrimitives(true);
		shape.setLighted(true);
		shape.setCalculateNormals(true);
		shape.setTwoSidedLighting(true);

		//create a color buffer containing a color for each point
		int colorBufferElementSize = 4;
		FloatBuffer colorBuffer = FloatBuffer.allocate(positions.size() * colorBufferElementSize);
		for (Position position : positions)
		{
			PositionWithCoord pwv = (PositionWithCoord) position;
			int u = pwv.u, v = pwv.v;
			int un = u > 0 ? u - 1 : u, up = u < uSamples - 1 ? u + 1 : u, vn = v > 0 ? v - 1 : v, vp =
					v < vSamples - 1 ? v + 1 : v;
			v *= uSamples;
			vn *= uSamples;
			vp *= uSamples;
			//check all values around the current position for NODATA; if NODATA, use a transparent color
			float value = values[u + v], l = values[un + v], r = values[up + v], t = values[u + vn], b = values[u + vp], tl =
					values[un + vn], tr = values[up + vn], bl = values[un + vp], br = values[up + vp];
			if (Float.isNaN(value) || Float.isNaN(l) || Float.isNaN(r) || Float.isNaN(t) || Float.isNaN(b)
					|| Float.isNaN(tl) || Float.isNaN(tr) || Float.isNaN(bl) || Float.isNaN(br))
			{
				//this or adjacent cell is NODATA
				for (int i = 0; i < colorBufferElementSize; i++)
				{
					colorBuffer.put(0);
				}
			}
			else
			{
				Color color = this.color;
				if (parameters.getColorMap() != null)
				{
					color =
							parameters.getColorMap().calculateColorNotingIsValuesPercentages(value, minmax[0],
									minmax[1]);
				}
				colorBuffer.put(color.getRed() / 255f).put(color.getGreen() / 255f).put(color.getBlue() / 255f)
						.put(color.getAlpha() / 255f);
			}
		}
		shape.setColorBuffer(colorBuffer.array());
		shape.setColorBufferElementSize(colorBufferElementSize);

		return shape;
	}

	protected void readFileIntoFloatArray(URL context, String file, int offset, String etype, int esize,
			Double noDataValue, List<Position> positions, float[] values, float[] minmax, int nu, int nv, int uSamples,
			int vSamples, int strideU, int strideV, Vec4 origin, Vec4 axisUStride, Vec4 axisVStride,
			boolean bilinearMinification) throws IOException
	{
		for (int i = 0; i < values.length; i++)
		{
			values[i] = Float.NaN;
		}
		minmax[0] = Float.MAX_VALUE;
		minmax[1] = -Float.MAX_VALUE;

		double[] transformed = new double[3];
		CoordinateTransformation transformation = parameters.getCoordinateTransformation();

		URL eFileUrl = new URL(context, file);
		InputStream eis = new BufferedInputStream(eFileUrl.openStream());
		eis.skip(offset);
		boolean ieee = "IEEE".equals(etype);

		if (bilinearMinification)
		{
			//contains the number of values summed
			int[] count = new int[values.length];

			//read all the values, and sum them in regions
			for (int v = 0; v < nv; v++)
			{
				int vRegion = (v / strideV) * uSamples;
				for (int u = 0; u < nu; u++)
				{
					float value = readNextFloat(eis, parameters.getByteOrder(), ieee);
					if (!Float.isNaN(value) && (noDataValue == null || value != noDataValue))
					{
						int uRegion = (u / strideU);
						int valueIndex = vRegion + uRegion;

						//if this is the first value for this region, set it, otherwise add it
						if (count[valueIndex] == 0)
						{
							values[valueIndex] = value;
						}
						else
						{
							values[valueIndex] += value;
						}
						count[valueIndex]++;
					}
				}
			}

			//divide all the sums by the number of values summed (basically, average)
			for (int i = 0; i < values.length; i++)
			{
				if (count[i] > 0)
				{
					values[i] /= count[i];
					minmax[0] = Math.min(minmax[0], values[i]);
					minmax[1] = Math.max(minmax[1], values[i]);
				}
			}

			if (positions != null)
			{
				//create points for each summed region that has a value
				for (int v = 0, vi = 0; v < nv; v += strideV, vi++)
				{
					int vOffset = vi * uSamples;
					Vec4 vAdd = axisVStride.multiply3(v);
					for (int u = 0, ui = 0; u < nu; u += strideU, ui++)
					{
						int uOffset = ui;
						int valueIndex = vOffset + uOffset;
						float value = values[valueIndex];

						Vec4 uAdd = axisUStride.multiply3(u);
						Vec4 p =
								Float.isNaN(value) ? new Vec4(origin.x + uAdd.x + vAdd.x, origin.y + uAdd.y + vAdd.y,
										origin.z + uAdd.z + vAdd.z) : new Vec4(origin.x + uAdd.x + vAdd.x + axisW.x
										* value, origin.y + uAdd.y + vAdd.y + axisW.y * value, origin.z + uAdd.z
										+ vAdd.z + axisW.z * value);

						if (transformation != null)
						{
							transformation.TransformPoint(transformed, p.x, p.y, zPositive ? p.z : -p.z);
							positions.add(PositionWithCoord.fromDegrees(transformed[1], transformed[0], transformed[2],
									ui, vi));
						}
						else
						{
							positions.add(PositionWithCoord.fromDegrees(p.y, p.x, zPositive ? p.z : -p.z, ui, vi));
						}
					}
				}
			}
		}
		else
		{
			//non-bilinear is simple; we can skip over any input values that don't contribute to the points
			int valueIndex = 0;
			for (int v = 0, vi = 0; v < nv; v += strideV, vi++)
			{
				Vec4 vAdd = axisVStride.multiply3(v);
				for (int u = 0, ui = 0; u < nu; u += strideU, ui++)
				{
					Vec4 uAdd = axisUStride.multiply3(u);
					Vec4 p;

					float value = readNextFloat(eis, parameters.getByteOrder(), ieee);
					boolean valid = !Float.isNaN(value) && (noDataValue == null || value != noDataValue);
					if (valid)
					{
						values[valueIndex] = value;
						minmax[0] = Math.min(minmax[0], value);
						minmax[1] = Math.max(minmax[1], value);
					}

					if (positions != null)
					{
						if (valid)
						{
							p =
									new Vec4(origin.x + uAdd.x + vAdd.x + axisW.x * value, origin.y + uAdd.y + vAdd.y
											+ axisW.y * value, origin.z + uAdd.z + vAdd.z + axisW.z * value);
						}
						else
						{
							p =
									new Vec4(origin.x + uAdd.x + vAdd.x, origin.y + uAdd.y + vAdd.y, origin.z + uAdd.z
											+ vAdd.z);
						}
						if (transformation != null)
						{
							transformation.TransformPoint(transformed, p.x, p.y, zPositive ? p.z : -p.z);
							positions.add(PositionWithCoord.fromDegrees(transformed[1], transformed[0], transformed[2],
									ui, vi));
						}
						else
						{
							positions.add(PositionWithCoord.fromDegrees(p.y, p.x, zPositive ? p.z : -p.z, ui, vi));
						}
					}

					valueIndex++;
					skipBytes(eis, esize * Math.min(strideU - 1, nu - u - 1));
				}
				skipBytes(eis, esize * nu * Math.min(strideV - 1, nv - v - 1));
			}
		}
	}

	protected static class PositionWithCoord extends Position
	{
		public final int u;
		public final int v;

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
	
	public static void skipBytes(InputStream is, long n) throws IOException
	{
		while (n > 0)
		{
			long skipped = is.skip(n);
			if (skipped < 0)
			{
				throw new IOException("Error skipping in InputStream");
			}
			n -= skipped;
		}
	}

	public static float readNextFloat(InputStream is, ByteOrder byteOrder, boolean ieee) throws IOException
	{
		int b0, b1, b2, b3;
		if (byteOrder == ByteOrder.LITTLE_ENDIAN)
		{
			b3 = is.read();
			b2 = is.read();
			b1 = is.read();
			b0 = is.read();
		}
		else
		{
			b0 = is.read();
			b1 = is.read();
			b2 = is.read();
			b3 = is.read();
		}
		return bytesToFloat(b0, b1, b2, b3, ieee);
	}

	private static float bytesToFloat(int b0, int b1, int b2, int b3, boolean ieee)
	{
		if (ieee)
		{
			return Float.intBitsToFloat((b0) | (b1 << 8) | (b2 << 16) | b3 << 24);
		}
		else
		{
			//ibm
			byte S = (byte) ((b3 & 0x80) >> 7);
			int E = (b3 & 0x7f);
			long F = (b2 << 16) + (b1 << 8) + b0;

			if (S == 0 && E == 0 && F == 0)
			{
				return 0;
			}

			double A = 16.0;
			double B = 64.0;
			double e24 = 16777216.0; // 2^24
			double M = F / e24;

			double F1 = S == 0 ? 1.0 : -1.0;
			return (float) (F1 * M * Math.pow(A, E - B));
		}
	}
}
