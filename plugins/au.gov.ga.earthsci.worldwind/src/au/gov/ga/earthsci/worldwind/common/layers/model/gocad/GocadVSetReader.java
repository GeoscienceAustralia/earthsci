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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;

/**
 * A {@link GocadReader} that reads a VSet object into a {@link FastShape}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GocadVSetReader implements GocadReader<FastShape>
{

	public final static String HEADER_REGEX = "(?i).*vset.*";

	private final static Pattern atomSizePattern = Pattern.compile("\\*atoms\\*size:(.+)");
	private final static Pattern atomColorPattern = Pattern.compile("\\*atoms\\*color:.+");

	private GocadReaderParameters parameters;
	private List<Position> positions;

	private boolean zPositive;
	private String name;
	private Float size;
	private Color color;

	private List<Float> values;
	private float min, max;
	private String paintedVariableName;
	private int paintedVariableId = 0;
	private float noDataValue = -Float.MAX_VALUE;
	private Map<Integer, Integer> vertexIdMap;

	@Override
	public void begin(GocadReaderParameters parameters)
	{
		this.parameters = parameters;
		positions = new ArrayList<Position>();
		values = new ArrayList<Float>();
		min = Float.MAX_VALUE;
		max = -Float.MAX_VALUE;
		vertexIdMap = new HashMap<Integer, Integer>();
		paintedVariableName = parameters.getPaintedVariable();
	}

	@Override
	public void addLine(String line)
	{
		Matcher matcher;

		// Vertex / PVertex
		matcher = vertexPattern.matcher(line);
		if (matcher.matches())
		{
			processVertexLine(matcher);
			return;
		}

		// ZPOSITIVE directive
		matcher = zpositivePattern.matcher(line);
		if (matcher.matches())
		{
			zPositive = !matcher.group(1).equalsIgnoreCase("depth");
			return;
		}

		// Atom size
		matcher = atomSizePattern.matcher(line);
		if (matcher.matches())
		{
			size = Float.parseFloat(matcher.group(1));
			return;
		}

		// Atom color
		matcher = atomColorPattern.matcher(line);
		if (matcher.matches())
		{
			color = GocadColor.gocadLineToColor(line);
			return;
		}

		// NODATA value
		matcher = nodataValuesPattern.matcher(line);
		if (matcher.matches())
		{
			processNodataValue(matcher);
			return;
		}

		// Properties
		matcher = propertiesPattern.matcher(line);
		if (matcher.matches())
		{
			processPropertiesLine(matcher);
			return;
		}

		// Painted variable
		matcher = paintedVariablePattern.matcher(line);
		if (matcher.matches())
		{
			if (parameters.getPaintedVariable() == null)
			{
				paintedVariableName = matcher.group(1);
			}
			return;
		}

		// Name
		matcher = namePattern.matcher(line);
		if (matcher.matches())
		{
			name = matcher.group(1);
			return;
		}
	}

	@Override
	public FastShape end(URL context)
	{
		if (name == null)
		{
			name = "VSet";
		}

		FastShape shape = new FastShape(positions, GL2.GL_POINTS);
		shape.setName(name);

		if (parameters.getPointSize() != null)
		{
			shape.setPointSize(parameters.getPointSize());
		}
		else
		{
			shape.setPointSize((double) size);
		}

		shape.setPointMaxSize(parameters.getPointMaxSize());
		shape.setPointMinSize(parameters.getPointMinSize());
		shape.setPointConstantAttenuation(parameters.getPointConstantAttenuation());
		shape.setPointLinearAttenuation(parameters.getPointLinearAttenuation());
		shape.setPointQuadraticAttenuation(parameters.getPointQuadraticAttenuation());

		if (parameters.getColorMap() != null)
		{
			float[] colorBuffer = createColorBuffer();
			shape.setColorBufferElementSize(4);
			shape.setColorBuffer(colorBuffer);
		}
		else if (parameters.getColor() != null)
		{
			shape.setColor(parameters.getColor());
		}
		else if (color != null)
		{
			shape.setColor(color);
		}

		return shape;
	}

	private float[] createColorBuffer()
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
		return colorBuffer.array();
	}

	private void processVertexLine(Matcher matcher)
	{
		int id = Integer.parseInt(matcher.group(1));

		Position position =
				createPositionFromVertex(Double.parseDouble(matcher.group(2)), Double.parseDouble(matcher.group(3)),
						Double.parseDouble(matcher.group(4)));
		positions.add(position);

		vertexIdMap.put(id, positions.size() - 1);

		float value = Float.NaN;
		if (paintedVariableId <= 0)
		{
			value = (float) position.elevation;
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
	}

	private Position createPositionFromVertex(double x, double y, double z)
	{
		if (!zPositive)
		{
			z = -z;
		}
		double[] xyz = transformVertex(x, y, z);
		return Position.fromDegrees(xyz[1], xyz[0], xyz[2]);
	}

	private double[] transformVertex(double... xyz)
	{
		if (parameters.getCoordinateTransformation() == null)
		{
			return xyz;
		}
		parameters.getCoordinateTransformation().TransformPoint(xyz);
		return xyz;
	}

	private void processNodataValue(Matcher matcher)
	{
		double[] values = GocadTSurfReader.splitStringToDoubles(matcher.group(1));
		if (0 < paintedVariableId && paintedVariableId <= values.length)
		{
			noDataValue = (float) values[paintedVariableId - 1];
		}
	}

	private void processPropertiesLine(Matcher matcher)
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
	}

}
