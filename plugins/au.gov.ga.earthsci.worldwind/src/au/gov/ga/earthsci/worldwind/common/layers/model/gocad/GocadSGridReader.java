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

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.earthsci.worldwind.common.layers.model.gocad.GocadGSurfReader.PositionWithCoord;
import au.gov.ga.earthsci.worldwind.common.layers.volume.VolumeLayer;
import au.gov.ga.earthsci.worldwind.common.layers.volume.btt.BinaryTriangleTree;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * {@link GocadReader} implementation for reading GOCAD SGrid files (only
 * supports single slice, use the {@link VolumeLayer} for volumetric SGrid
 * files).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadSGridReader implements GocadReader<FastShape>
{
	public final static String HEADER_REGEX = "(?i).*sgrid.*";

	public final static Pattern headerPattern = Pattern.compile("GOCAD\\s+SGrid\\s+.*");
	private final static Pattern propAlignmentPattern = Pattern.compile("PROP_ALIGNMENT\\s+(.*?)\\s*");
	private final static Pattern asciiDataFilePattern = Pattern.compile("ASCII_DATA_FILE\\s+(.*?)\\s*");
	public final static Pattern propertyNamePattern = Pattern.compile("PROPERTY\\s+(\\d+)\\s+\"?(.*?)\"?\\s*");
	private final static Pattern propertyNoDataPattern = Pattern
			.compile("PROP_NO_DATA_VALUE\\s+(\\d+)\\s+([\\d.\\-]+)\\s*");

	private String asciiDataFile;
	private String paintedVariableName;
	private int paintedVariableId = 1;
	private boolean cellCentered;

	private String name;
	private int xSize;
	private int ySize;
	private int zSize;
	private Float noDataValue;

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
		Matcher matcher = paintedVariablePattern.matcher(line);
		if (matcher.matches())
		{
			paintedVariableName = matcher.group(1);
			return;
		}

		matcher = namePattern.matcher(line);
		if (matcher.matches())
		{
			name = matcher.group(1);
			return;
		}

		matcher = axis3Pattern.matcher(line);
		if (matcher.matches())
		{
			//check for AXIS_N
			if (matcher.group(1).equals("N"))
			{
				xSize = (int) Double.parseDouble(matcher.group(2));
				ySize = (int) Double.parseDouble(matcher.group(3));
				zSize = (int) Double.parseDouble(matcher.group(4));
			}
			return;
		}

		matcher = propAlignmentPattern.matcher(line);
		if (matcher.matches())
		{
			String propAlignment = matcher.group(1);
			cellCentered = propAlignment.toLowerCase().equals("cells");
			return;
		}

		matcher = asciiDataFilePattern.matcher(line);
		if (matcher.matches())
		{
			asciiDataFile = matcher.group(1);
			return;
		}

		matcher = propertyNamePattern.matcher(line);
		if (matcher.matches())
		{
			int propertyId = Integer.parseInt(matcher.group(1));
			String propertyName = matcher.group(2);
			if (propertyName.equals(paintedVariableName))
			{
				paintedVariableId = propertyId;
			}
			return;
		}

		matcher = propertyNoDataPattern.matcher(line);
		if (matcher.matches())
		{
			int propertyId = Integer.parseInt(matcher.group(1));
			float noDataValue = Float.parseFloat(matcher.group(2));
			if (propertyId == paintedVariableId)
			{
				this.noDataValue = noDataValue;
			}
			return;
		}
	}

	@Override
	public FastShape end(URL context)
	{
		if (cellCentered)
		{
			xSize--;
			ySize--;
			zSize--;
		}

		Validate.isTrue(asciiDataFile != null, "Data file not specified");
		Validate.isTrue(xSize > 0 && ySize > 0 && zSize > 0, "Volume dimensions are 0");
		Validate.isTrue(zSize == 1, "Unsupported AXIS_N z-value: " + zSize + ", only 1 is supported");

		List<Position> positions = new ArrayList<Position>(xSize * ySize);
		float[] values = new float[xSize * ySize];
		float minValue = Float.MAX_VALUE;
		float maxValue = -Float.MAX_VALUE;
		for (int i = 0; i < values.length; i++)
		{
			values[i] = Float.NaN;
		}

		InputStream dataInputStream = null;
		try
		{
			URL fileUrl = new URL(context, asciiDataFile);
			dataInputStream = new BufferedInputStream(fileUrl.openStream());

			//TODO add support for SGrid binary property files

			//setup data variables
			double[] transformed = new double[3];
			CoordinateTransformation transformation = parameters.getCoordinateTransformation();

			int positionIndex = 0;
			double firstXValue = 0, firstYValue = 0;
			boolean reverseX = false, reverseY = false;

			//setup the ASCII data file line regex
			String doublePattern = "([\\d.\\-]+)";
			String nonCapturingDoublePattern = "(?:[\\d.\\-]+)";
			String spacerPattern = "\\s+";
			//regex for coordinates
			String lineRegex = "\\s*" + doublePattern + spacerPattern + doublePattern + spacerPattern + doublePattern;
			for (int property = 1; property < paintedVariableId; property++)
			{
				//ignore all properties in between coordinates and painted property
				lineRegex += spacerPattern + nonCapturingDoublePattern;
			}
			//only capture the painted property
			lineRegex += spacerPattern + doublePattern + ".*";
			Pattern linePattern = Pattern.compile(lineRegex);

			//read the ASCII data file
			BufferedReader reader = new BufferedReader(new InputStreamReader(dataInputStream));
			String line;
			while ((line = reader.readLine()) != null)
			{
				Matcher matcher = linePattern.matcher(line);
				if (matcher.matches())
				{
					double x = Double.parseDouble(matcher.group(1));
					double y = Double.parseDouble(matcher.group(2));
					double z = Double.parseDouble(matcher.group(3));
					float value = Float.parseFloat(matcher.group(4));

					//transform the point
					if (transformation != null)
					{
						transformation.TransformPoint(transformed, x, y, z);
						x = transformed[0];
						y = transformed[1];
						z = transformed[2];
					}

					//only store the first width*height positions (the rest are evenly spaced at different depths)
					if (positionIndex < xSize * ySize)
					{
						Position position =
								PositionWithCoord.fromDegrees(y, x, z, positionIndex % xSize, positionIndex / xSize);
						positions.add(position);
					}

					if (positionIndex == 0)
					{
						firstXValue = x;
						firstYValue = y;
					}
					else if (positionIndex == 1)
					{
						//second x value
						reverseX = x < firstXValue;
					}
					else if (positionIndex == xSize)
					{
						//second y value
						reverseY = y < firstYValue;
					}

					//put the data into the float array
					if (!Float.isNaN(value) && (noDataValue == null || value != noDataValue))
					{
						values[positionIndex] = value;
						minValue = Math.min(minValue, value);
						maxValue = Math.max(maxValue, value);
					}

					positionIndex++;
				}
			}

			Validate.isTrue(positions.size() == xSize * ySize,
					"Data file doesn't contain the correct number of positions");

			if (reverseX || reverseY)
			{
				//if the x-axis or y-axis are reversed, mirror them
				List<Position> oldPositions = positions;
				positions = new ArrayList<Position>(oldPositions.size());
				for (int y = 0; y < ySize; y++)
				{
					int ry = reverseY ? ySize - y - 1 : y;
					for (int x = 0; x < xSize; x++)
					{
						int rx = reverseX ? xSize - x - 1 : x;
						positions.add(oldPositions.get(rx + ry * xSize));
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			if (dataInputStream != null)
			{
				try
				{
					dataInputStream.close();
				}
				catch (IOException e)
				{
				}
			}
		}

		BinaryTriangleTree btt = new BinaryTriangleTree(positions, xSize, ySize);
		btt.setForceGLTriangles(true); //ensures that the shape's triangles can be sorted when transparent
		FastShape shape = btt.buildMesh(parameters.getMaxVariance());
		positions = shape.getPositions();

		if (name == null)
		{
			name = "SGrid";
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
			int un = u > 0 ? u - 1 : u, up = u < xSize - 1 ? u + 1 : u, vn = v > 0 ? v - 1 : v, vp =
					v < ySize - 1 ? v + 1 : v;
			v *= xSize;
			vn *= xSize;
			vp *= xSize;
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
				Color color = Color.white;
				if (parameters.getColorMap() != null)
				{
					color = parameters.getColorMap().calculateColorNotingIsValuesPercentages(value, minValue, maxValue);
				}
				colorBuffer.put(color.getRed() / 255f).put(color.getGreen() / 255f).put(color.getBlue() / 255f)
						.put(color.getAlpha() / 255f);
			}
		}
		shape.setColorBuffer(colorBuffer.array());
		shape.setColorBufferElementSize(colorBufferElementSize);

		return shape;
	}
}
