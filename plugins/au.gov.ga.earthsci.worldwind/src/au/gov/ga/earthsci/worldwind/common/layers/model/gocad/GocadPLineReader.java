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
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;

/**
 * {@link GocadReader} implementation for reading PLine GOCAD files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadPLineReader implements GocadReader<FastShape>
{
	public final static String HEADER_REGEX = "(?i).*pline.*";

	private final static Pattern segmentPattern = Pattern.compile("SEG\\s+(\\d+)\\s+(\\d+).*");


	private GocadReaderParameters parameters;
	private List<Position> positions;
	private List<Integer> segmentIds;
	private Color color;
	private Map<Integer, Integer> vertexIdMap;
	private String name;
	private boolean zPositive = true;
	private List<Float> values;
	private float min, max;
	private String paintedVariableName;
	private int paintedVariableId = 0;
	private float noDataValue = -Float.MAX_VALUE;

	@Override
	public void begin(GocadReaderParameters parameters)
	{
		this.parameters = parameters;
		positions = new ArrayList<Position>();
		values = new ArrayList<Float>();
		min = Float.MAX_VALUE;
		max = -Float.MAX_VALUE;
		segmentIds = new ArrayList<Integer>();
		vertexIdMap = new HashMap<Integer, Integer>();
		paintedVariableName = parameters.getPaintedVariable();
	}

	@Override
	public void addLine(String line)
	{
		Matcher matcher;

		matcher = vertexPattern.matcher(line);
		if (matcher.matches())
		{
			int id = Integer.parseInt(matcher.group(1));
			if (vertexIdMap.containsKey(id))
			{
				throw new IllegalArgumentException("Duplicate vertex id: " + id);
			}

			double x = Double.parseDouble(matcher.group(2));
			double y = Double.parseDouble(matcher.group(3));
			double z = Double.parseDouble(matcher.group(4));
			z = zPositive ? z : -z;
			if (parameters.getCoordinateTransformation() != null)
			{
				double[] transformed = new double[3];
				parameters.getCoordinateTransformation().TransformPoint(transformed, x, y, z);
				x = transformed[0];
				y = transformed[1];
				z = transformed[2];
			}
			Position position = Position.fromDegrees(y, x, z);
			vertexIdMap.put(id, positions.size());
			positions.add(position);

			float value = Float.NaN;
			if (paintedVariableId <= 0)
			{
				value = (float) z;
			}
			else
			{
				double[] values = GocadTSurfReader.splitStringToDoubles(matcher.group(5));
				if (paintedVariableId <= values.length)
				{
					value = (float) values[paintedVariableId - 1];
				}
			}
			if (!Float.isNaN(value) && value != noDataValue)
			{
				min = Math.min(min, value);
				max = Math.max(max, value);
			}
			values.add(value);

			return;
		}

		matcher = atomPattern.matcher(line);
		if (matcher.matches())
		{
			int id1 = Integer.parseInt(matcher.group(1));
			int id2 = Integer.parseInt(matcher.group(2));

			if (vertexIdMap.containsKey(id1))
			{
				throw new IllegalArgumentException("Duplicate vertex id: " + id1);
			}
			if (!vertexIdMap.containsKey(id2))
			{
				throw new IllegalArgumentException("Unknown vertex id: " + id2);
			}

			Position position = positions.get(vertexIdMap.get(id2));
			vertexIdMap.put(id1, positions.size());
			positions.add(position);

			float value = Float.NaN;
			if (paintedVariableId <= 0)
			{
				value = (float) position.elevation;
			}
			else
			{
				double[] values = GocadTSurfReader.splitStringToDoubles(matcher.group(3));
				if (paintedVariableId <= values.length)
				{
					value = (float) values[paintedVariableId - 1];
				}
			}
			if (!Float.isNaN(value) && value != noDataValue)
			{
				min = Math.min(min, value);
				max = Math.max(max, value);
			}
			values.add(value);

			return;
		}

		matcher = segmentPattern.matcher(line);
		if (matcher.matches())
		{
			int s1 = Integer.parseInt(matcher.group(1));
			int s2 = Integer.parseInt(matcher.group(2));
			segmentIds.add(s1);
			segmentIds.add(s2);
			return;
		}

		matcher = lineColorPattern.matcher(line);
		if (matcher.matches())
		{
			color = GocadColor.gocadLineToColor(line);
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

		matcher = paintedVariablePattern.matcher(line);
		if (matcher.matches())
		{
			if (parameters.getPaintedVariable() == null)
			{
				paintedVariableName = matcher.group(1);
			}
			return;
		}

		matcher = propertiesPattern.matcher(line);
		if (matcher.matches())
		{
			String properties = matcher.group(1).trim();
			String[] split = properties.split("\\s+");
			for (int i = 0; i < split.length; i++)
			{
				if (split[i].equalsIgnoreCase(paintedVariableName))
				{
					paintedVariableId = i + 1;
					break;
				}
			}
			return;
		}

		matcher = nodataValuesPattern.matcher(line);
		if (matcher.matches())
		{
			double[] values = GocadTSurfReader.splitStringToDoubles(matcher.group(1));
			if (0 < paintedVariableId && paintedVariableId <= values.length)
			{
				noDataValue = (float) values[paintedVariableId - 1];
			}
			return;
		}
	}

	@Override
	public FastShape end(URL context)
	{
		IntBuffer indicesBuffer = IntBuffer.allocate(segmentIds.size());
		for (Integer i : segmentIds)
		{
			if (!vertexIdMap.containsKey(i))
			{
				throw new IllegalArgumentException("Unknown vertex id: " + i);
			}
			indicesBuffer.put(vertexIdMap.get(i));
		}

		if (name == null)
		{
			name = "PLine";
		}

		FastShape shape = new FastShape(positions, indicesBuffer.array(), GL2.GL_LINES);
		shape.setName(name);
		if (parameters.getColorMap() != null)
		{
			FloatBuffer colorBuffer = FloatBuffer.allocate(positions.size() * 4);
			for (float value : values)
			{
				if (Float.isNaN(value) || value == noDataValue)
				{
					colorBuffer.put(0).put(0).put(0).put(0);
				}
				else
				{
					Color color = parameters.getColorMap().calculateColorNotingIsValuesPercentages(value, min, max);
					colorBuffer.put(color.getRed() / 255f).put(color.getGreen() / 255f).put(color.getBlue() / 255f)
							.put(color.getAlpha() / 255f);
				}
			}
			shape.setColorBufferElementSize(4);
			shape.setColorBuffer(colorBuffer.array());
		}
		else if (color != null)
		{
			shape.setColor(color);
		}
		return shape;
	}
}
