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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;
import au.gov.ga.earthsci.worldwind.common.util.HSLColor;
import au.gov.ga.earthsci.worldwind.common.util.Validate;
import au.gov.ga.earthsci.worldwind.common.util.io.FloatReader;
import au.gov.ga.earthsci.worldwind.common.util.io.FloatReader.FloatFormat;

/**
 * {@link GocadReader} implementation for reading Voxet GOCAD files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadVoxetReader implements GocadReader<FastShape>
{
	// Constant indices for array access
	private static final int U=0,V=1,W=2;
	
	public final static String HEADER_REGEX = "(?i).*voxet.*";

	private String name;
	private boolean zPositive = true;

	private Vec4 axisO;
	private Vec4 axisU;
	private Vec4 axisV;
	private Vec4 axisW;
	private Vec4 axisMIN;
	private Vec4 axisMAX;
	private Vec4 axisN;
	private Vec4 axisD;

	private Double noDataValue = null;
	private int esize = 4;
	private String etype = "IEEE";
	private String format = "RAW";
	private int offset = 0;
	private String file;

	private GocadReaderParameters parameters;

	@Override
	public void begin(GocadReaderParameters parameters)
	{
		this.parameters = parameters;
	}

	@Override
	public void addLine(String line)
	{
		Matcher matcher = axis3Pattern.matcher(line);
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
		}
	}

	private void parseAxis(Matcher matcher)
	{
		String type = matcher.group(1);
		double d0 = Double.parseDouble(matcher.group(2));
		double d1 = Double.parseDouble(matcher.group(3));
		double d2 = Double.parseDouble(matcher.group(4));
		Vec4 v = new Vec4(d0, d1, d2);

		if (type.equals("O"))
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
			axisN = v;
		}
		else if (type.equals("D"))
		{
			axisD = v;
		}
	}

	private void parseProperty(Matcher matcher)
	{
		String type = matcher.group(1);
		String value = matcher.group(3);

		if (type.equals("NO_DATA_VALUE"))
		{
			noDataValue = Double.parseDouble(value);
		}
		else if (type.equals("ESIZE"))
		{
			esize = Integer.parseInt(value);
		}
		/*else if (type.equals("SIGNED"))
		{
			signed = Integer.parseInt(value) != 0;
		}*/
		else if (type.equals("ETYPE"))
		{
			etype = value;
		}
		else if (type.equals("FORMAT"))
		{
			format = value;
		}
		else if (type.equals("OFFSET"))
		{
			offset = Integer.parseInt(value);
		}
		else if (type.equals("FILE"))
		{
			file = value;
		}
	}

	@Override
	public FastShape end(URL context)
	{
		validateProperties();
		
		if (axisN == null)
		{
			if (axisD == null || axisMIN == null || axisMAX == null)
			{
				return null;
			}

			double nx = (axisMAX.x - axisMIN.x) / axisD.x + 1;
			double ny = (axisMAX.y - axisMIN.y) / axisD.y + 1;
			double nz = (axisMAX.z - axisMIN.z) / axisD.z + 1;
			axisN = new Vec4(nx, ny, nz);
		}

		Vec4 axisUStride = axisU.multiply3((axisMAX.x - axisMIN.x) / (axisN.x - 1));
		Vec4 axisVStride = axisV.multiply3((axisMAX.y - axisMIN.y) / (axisN.y - 1));
		Vec4 axisWStride = axisW.multiply3((axisMAX.z - axisMIN.z) / (axisN.z - 1));

		Vec4 origin = calculateAxisOrigin();
		int[] strides = calculateStrides();
		long[] axisN = calculateAxisN();
		int[] samples = calculateSamples(strides, axisN);

		List<Position> positions = new ArrayList<Position>();
		float[] values = createValuesArray(samples);

		double[] transformed = new double[3];
		float[] minmax = new float[]{Float.MAX_VALUE, -Float.MAX_VALUE};
		try
		{
			URL fileUrl = new URL(context, file);
			InputStream is = new BufferedInputStream(fileUrl.openStream());
			FloatReader reader = FloatReader.Builder.newFloatReaderForStream(is)
													.withOffset(offset)
													.withFormat(FloatFormat.valueOf(etype))
													.withByteOrder(parameters.getByteOrder())
													.build();
			float[] floatValue = new float[1];
			if (parameters.isBilinearMinification())
			{
				//contains the number of values summed
				int[] count = new int[values.length];

				//read all the values, and sum them in regions
				for (int w = 0; w < axisN[W]; w++)
				{
					int wRegion = (w / strides[W]) * samples[V] * samples[U];
					for (int v = 0; v < axisN[V]; v++)
					{
						int vRegion = (v / strides[V]) * samples[U];
						for (int u = 0; u < axisN[U]; u++)
						{
							reader.readNextValues(floatValue);
							if (!Float.isNaN(floatValue[0]) && floatValue[0] != noDataValue)
							{
								int uRegion = (u / strides[U]);
								int valueIndex = wRegion + vRegion + uRegion;

								//if this is the first value for this region, set it, otherwise add it
								if (count[valueIndex] == 0)
								{
									values[valueIndex] = floatValue[0];
								}
								else
								{
									values[valueIndex] += floatValue[0];
								}
								count[valueIndex]++;
							}
						}
					}
				}

				normaliseValues(values, minmax, count);

				//create points for each summed region that has a value
				for (int w = 0, wi = 0; w < axisN[W]; w += strides[W], wi++)
				{
					int wOffset = wi * samples[V] * samples[U];
					Vec4 wAdd = axisWStride.multiply3(w);
					for (int v = 0, vi = 0; v < axisN[V]; v += strides[V], vi++)
					{
						int vOffset = vi * samples[U];
						Vec4 vAdd = axisVStride.multiply3(v);
						for (int u = 0, ui = 0; u < axisN[U]; u += strides[U], ui++)
						{
							int uOffset = ui;
							int valueIndex = wOffset + vOffset + uOffset;
							float value = values[valueIndex];

							if (!Float.isNaN(value))
							{
								Vec4 uAdd = axisUStride.multiply3(u);
								Vec4 point = new Vec4(origin.x + uAdd.x + vAdd.x + wAdd.x, 
												  	  origin.y + uAdd.y + vAdd.y + wAdd.y, 
												  	  origin.z + uAdd.z + vAdd.z + wAdd.z);
								
								positions.add(createPositionFromPoint(transformed, point));
							}
						}
					}
				}
			}
			else
			{
				//non-bilinear is simple; we can skip over any input values that don't contribute to the points
				int valueIndex = 0;
				for (int w = 0; w < axisN[W]; w += strides[W])
				{
					Vec4 wAdd = axisWStride.multiply3(w);
					for (int v = 0; v < axisN[V]; v += strides[V])
					{
						Vec4 vAdd = axisVStride.multiply3(v);
						for (int u = 0; u < axisN[U]; u += strides[U])
						{
							reader.readNextValues(floatValue);
							if (!Float.isNaN(floatValue[0]) && floatValue[0] != noDataValue)
							{
								values[valueIndex] = floatValue[0];
								minmax[0] = Math.min(minmax[0], floatValue[0]);
								minmax[1] = Math.max(minmax[1], floatValue[0]);

								Vec4 uAdd = axisUStride.multiply3(u);
								Vec4 point = new Vec4(origin.x + uAdd.x + vAdd.x + wAdd.x, 
											      	  origin.y + uAdd.y + vAdd.y + wAdd.y, 
											      	  origin.z + uAdd.z + vAdd.z + wAdd.z);
								
								positions.add(createPositionFromPoint(transformed, point));
							}
							valueIndex++;
							reader.skip(esize * Math.min(strides[U] - 1, axisN[U] - u - 1));
						}
						reader.skip(esize * axisN[U] * Math.min(strides[V] - 1, axisN[V] - v - 1));
					}
					reader.skip(esize * axisN[U] * axisN[V] * Math.min(strides[W] - 1, axisN[W] - w - 1));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		FloatBuffer colorBuffer = createColorBuffer(values, minmax);

		if (name == null)
		{
			name = "Voxet";
		}

		FastShape shape = new FastShape(positions, GL2.GL_POINTS);
		shape.setName(name);
		shape.setColorBuffer(colorBuffer.array());
		shape.setColorBufferElementSize(4);
		shape.setForceSortedPrimitives(true);
		shape.setFollowTerrain(true);
		return shape;
	}

	
	private void normaliseValues(float[] values, float[] minmax, int[] count)
	{
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
	}

	private Position createPositionFromPoint(double[] transformed, Vec4 point)
	{
		if (parameters.getCoordinateTransformation() != null)
		{
			parameters.getCoordinateTransformation().TransformPoint(transformed, point.x, point.y, zPositive ? point.z : -point.z);
			return Position.fromDegrees(transformed[1], transformed[0], transformed[2]);
		}
		else
		{
			return Position.fromDegrees(point.y, point.x, zPositive ? point.z : -point.z);
		}
	}

	private void validateProperties()
	{
		Validate.isTrue(esize == 4, "Unsupported PROP_ESIZE value: " + esize); //TODO support "1"?
		Validate.isTrue("RAW".equals(format), "Unsupported PROP_FORMAT value: " + format); //TODO support "SEGY"?
		Validate.isTrue("IBM".equals(etype) || "IEEE".equals(etype), "Unsupported PROP_ETYPE value: " + etype);
	}
	
	private FloatBuffer createColorBuffer(float[] values, float[] minmax)
	{
		FloatBuffer colorBuffer = FloatBuffer.allocate(values.length * 4);
		for (float value : values)
		{
			//check that this value is valid; only non-NaN floats have points associated
			if (!Float.isNaN(value))
			{
				if (parameters.getColorMap() != null)
				{
					Color color = parameters.getColorMap().calculateColorNotingIsValuesPercentages(value, minmax[0], minmax[1]);
					colorBuffer.put(color.getRed() / 255f)
							   .put(color.getGreen() / 255f)
							   .put(color.getBlue() / 255f)
							   .put(color.getAlpha() / 255f);
				}
				else
				{
					float percent = (value - minmax[0]) / (minmax[1] - minmax[0]);
					HSLColor hsl = new HSLColor((1f - percent) * 300f, 100f, 50f);
					Color color = hsl.getRGB();
					colorBuffer.put(color.getRed() / 255f)
							   .put(color.getGreen() / 255f)
							   .put(color.getBlue() / 255f)
							   .put(255);
				}
			}
		}
		return colorBuffer;
	}

	private float[] createValuesArray(int[] samples)
	{
		float[] values = new float[samples[U] * samples[V] * samples[W]];
		for (int i = 0; i < values.length; i++)
		{
			values[i] = Float.NaN;
		}
		return values;
	}

	private int[] calculateSamples(int[] strides, long[] axisN)
	{
		int uSamples = (int) (1 + (axisN[U] - 1) / strides[U]);
		int vSamples = (int) (1 + (axisN[V] - 1) / strides[V]);
		int wSamples = (int) (1 + (axisN[W] - 1) / strides[W]);
		int[] samples = new int[]{uSamples, vSamples, wSamples};
		return samples;
	}

	private long[] calculateAxisN()
	{
		long nu = Math.round(axisN.x);
		long nv = Math.round(axisN.y);
		long nw = Math.round(axisN.z);
		long[] axisN = new long[]{nu, nv, nw};
		return axisN;
	}

	private int[] calculateStrides()
	{
		int strideU = parameters.getSubsamplingU();
		int strideV = parameters.getSubsamplingV();
		int strideW = parameters.getSubsamplingW();

		if (parameters.isDynamicSubsampling())
		{
			float samplesPerAxis = parameters.getDynamicSubsamplingSamplesPerAxis();
			strideU = Math.max(1, Math.round((float) axisN.x / samplesPerAxis));
			strideV = Math.max(1, Math.round((float) axisN.y / samplesPerAxis));
			strideW = Math.max(1, Math.round((float) axisN.z / samplesPerAxis));
		}
		int[] strides = new int[]{strideU, strideV, strideW};
		return strides;
	}

	private Vec4 calculateAxisOrigin()
	{
		Vec4 axisUOrigin = axisU.multiply3(axisMIN.x);
		Vec4 axisVOrigin = axisV.multiply3(axisMIN.y);
		Vec4 axisWOrigin = axisW.multiply3(axisMIN.z);
		Vec4 origin = new Vec4(axisO.x + axisUOrigin.x + axisVOrigin.x + axisWOrigin.x, 
							   axisO.y + axisUOrigin.y + axisVOrigin.y + axisWOrigin.y, 
							   axisO.z + axisUOrigin.z + axisVOrigin.z + axisWOrigin.z);
		return origin;
	}
	
}
