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
package au.gov.ga.earthsci.worldwind.common.layers.volume;

import gov.nasa.worldwind.geom.Position;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.earthsci.worldwind.common.layers.Bounds;
import au.gov.ga.earthsci.worldwind.common.util.URLUtil;
import au.gov.ga.earthsci.worldwind.common.util.io.FloatReader;
import au.gov.ga.earthsci.worldwind.common.util.io.FloatReader.FloatFormat;

/**
 * {@link VolumeDataProvider} implementation which reads volume data from a
 * GOCAD SGrid (.sg) file.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
// TODO: Some properties are being read but not used
@SuppressWarnings("unused")
public class SGridVolumeDataProvider extends AbstractVolumeDataProvider
{
	private final static Pattern paintedVariablePattern = Pattern.compile("\\*painted\\*variable:\\s*(.*?)\\s*");
	private final static Pattern axisPattern = Pattern
			.compile("AXIS_(\\S+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+).*");
	private final static Pattern asciiDataFilePattern = Pattern.compile("ASCII_DATA_FILE\\s+(.*?)\\s*");
	private final static Pattern pointsOffsetPattern = Pattern.compile("POINTS_OFFSET\\s+(\\d+)");
	private final static Pattern pointsFilePattern = Pattern.compile("POINTS_FILE\\s+([^\\s]*)\\s*");
	private final static Pattern flagsOffsetPattern = Pattern.compile("FLAGS_OFFSET\\s+(\\d+)");
	private final static Pattern flagsFilePattern = Pattern.compile("FLAGS_FILE\\s+([^\\s]*)\\s*");

	private final static Pattern propertyDefinition = Pattern.compile("(?:PROPERTY|PROP_).*?(\\d).*");
	private final static Pattern propertyNamePattern = Pattern.compile("PROPERTY\\s+(\\d+)\\s+\"?(.*?)\"?\\s*");
	private final static Pattern propertyOffsetPattern = Pattern.compile("PROP_OFFSET\\s+(\\d+)");
	private final static Pattern propertyAlignmentPattern = Pattern.compile("PROP_ALIGNMENT.+?(CELLS|POINTS)\\s*");
	private final static Pattern propertyFilePattern = Pattern.compile("PROP_FILE\\s+(\\d+)\\s+([^\\s]*)\\s*");
	private final static Pattern propertyFormatPattern = Pattern.compile("PROP_FORMAT\\s+(\\d+)\\s+([^\\s]*)\\s*");
	private final static Pattern propertySizePattern = Pattern.compile("PROP_ESIZE\\s+(\\d+)\\s+([^\\s]*)\\s*");
	private final static Pattern propertyTypePattern = Pattern.compile("PROP_ETYPE\\s+(\\d+)\\s+([^\\s]*)\\s*");
	private final static Pattern propertyNoDataPattern = Pattern
			.compile("PROP_NO_DATA_VALUE\\s+(\\d+)\\s+([\\d.\\-]+)\\s*");

	private VolumeLayer layer;

	private String asciiDataFile;
	private String pointsDataFile;
	private int pointsOffset = 0;
	private String flagsDataFile;
	private int flagsOffset = 0;

	private String paintedVariableName;

	private List<GocadPropertyDefinition> properties;
	private GocadPropertyDefinition paintedProperty;

	@Override
	protected boolean doLoadData(URL url, VolumeLayer layer)
	{
		this.layer = layer;

		Object source = null;
		try
		{
			source = openSource(url);
			if (source == null)
			{
				throw new IOException("Unable to load SGrid from URL " + url);
			}

			parseHeaderFile(source);

			validatePaintedPropertyAvailable();
			validateDataFileSpecified();
			validateNonZeroDimensions();

			readSGridData(source);

			validateDataFileLoadedCorrectly();

			correctForReversedAxes();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			closeSource(source);
		}

		layer.dataAvailable(this);
		return true;
	}


	/**
	 * Load the sgrid data from the specified data file(s)
	 */
	private void readSGridData(Object source) throws IOException
	{
		initialiseDataVariables();

		if (asciiDataFile != null)
		{
			readAsciiDataFile(source);
		}
		else
		{
			readBinaryDataFile(source);
		}
	}

	/**
	 * Load sgrid data from an ASCII data file
	 */
	private void readAsciiDataFile(Object source) throws IOException
	{
		InputStream dataInputStream = null;
		try
		{
			dataInputStream = openSGridDataStream(source, asciiDataFile);

			Pattern linePattern = createAsciiLineMatchingPattern(getPaintedProperty());

			CoordinateTransformation transformation = layer.getCoordinateTransformation();
			double firstXValue = 0, firstYValue = 0, firstZValue = 0;
			double[] transformed = new double[3];
			int positionIndex = 0;
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(dataInputStream));
			while ((line = reader.readLine()) != null)
			{
				Matcher matcher = linePattern.matcher(line);
				if (!matcher.matches())
				{
					continue;
				}

				// Only need to look at positions in the first slice of the volume or in the first position of the top slice
				if ((positionIndex < xSize * ySize) || (positionIndex == xSize * ySize * (zSize - 1)))
				{
					double x = Double.parseDouble(matcher.group(1));
					double y = Double.parseDouble(matcher.group(2));
					double z = Double.parseDouble(matcher.group(3));

					//transform the point;
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
						Position position = Position.fromDegrees(y, x, z);
						positions.add(position);
						top += z / (xSize * ySize);

						//update the sector to include this latitude/longitude
						updateSectorToIncludePosition(position);
					}

					if (positionIndex == 0)
					{
						firstXValue = x;
						firstYValue = y;
						firstZValue = z;
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
					else if (positionIndex == xSize * ySize * (zSize - 1))
					{
						//positionIndex is the same x/y as 0, but at the bottom elevation instead of top,
						//so we can calculate the depth as the difference between the two elevations
						reverseZ = z > firstZValue;
						depth = reverseZ ? z - firstZValue : firstZValue - z;
						top += reverseZ ? depth : 0;
					}
				}

				float value = Float.parseFloat(matcher.group(4));
				if (putDataValue(positionIndex, value))
				{
					minValue = Math.min(minValue, value);
					maxValue = Math.max(maxValue, value);
				}

				positionIndex++;
			}
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
					e.printStackTrace();
				}
			}
		}
	}


	private boolean putDataValue(int positionIndex, float value)
	{
		if (!cellCentred)
		{
			// For point-centred data store all values
			data.put(value);
			return true;
		}

		// For cell centred data, only store the 'real' values
		// Re-create the (x,y,z) coords of the vertex from the position index
		int x = positionIndex % xSize;
		int y = ((positionIndex - x) / ySize) % ySize;
		int z = positionIndex / (xSize * ySize);

		// Ignore property values at the edges of the volume
		if ((x < xSize - 1) && (y < ySize - 1) && (z < zSize - 1))
		{
			data.put(value);
			return true;
		}
		return false;
	}


	/**
	 * Load SGrid data from binary points and flags files
	 */
	private void readBinaryDataFile(Object source) throws IOException
	{
		InputStream pointsInputStream = null;
		InputStream propertiesInputStream = null;
		try
		{
			pointsInputStream = openSGridDataStream(source, pointsDataFile);
			FloatReader pointsReader = FloatReader.Builder.newFloatReaderForStream(pointsInputStream)
					.withGroupSize(3)
					.withOffset(pointsOffset)
					.build();

			CoordinateTransformation transformation = layer.getCoordinateTransformation();
			double firstXValue = 0, firstYValue = 0, firstZValue = 0;
			double[] transformed = new double[3];
			float[] coords = new float[3];
			for (int positionIndex = 0; positionIndex < totalNumberOfPositions(); positionIndex++)
			{

				// We only care about a specific subset of points (bottom slice and first point on the top slice).
				// All other points can be ignored
				if ((positionIndex >= xSize * ySize) && (positionIndex != xSize * ySize * (zSize - 1)))
				{
					pointsReader.skipToNextGroup();
					continue;
				}

				pointsReader.readNextValues(coords);

				//transform the point;
				if (transformation != null)
				{
					transformation.TransformPoint(transformed, coords[0], coords[1], coords[2]);
					coords[0] = (float) transformed[0];
					coords[1] = (float) transformed[1];
					coords[2] = (float) transformed[2];
				}

				//only store the first width*height positions (the rest are evenly spaced at different depths)
				if (positionIndex < xSize * ySize)
				{
					Position position = Position.fromDegrees(coords[1], coords[0], coords[2]);
					positions.add(position);
					top += coords[2] / (xSize * ySize);

					//update the sector to include this latitude/longitude
					updateSectorToIncludePosition(position);
				}

				if (positionIndex == 0)
				{
					firstXValue = coords[0];
					firstYValue = coords[1];
					firstZValue = coords[2];
				}
				else if (positionIndex == 1)
				{
					//second x value
					reverseX = coords[0] < firstXValue;
				}
				else if (positionIndex == xSize)
				{
					//second y value
					reverseY = coords[1] < firstYValue;
				}
				else if (positionIndex == xSize * ySize * (zSize - 1))
				{
					//positionIndex is the same x/y as 0, but at the bottom elevation instead of top,
					//so we can calculate the depth as the difference between the two elevations
					reverseZ = coords[2] > firstZValue;
					depth = reverseZ ? coords[2] - firstZValue : firstZValue - coords[2];
					top += reverseZ ? depth : 0;
				}
			}

			// Read the painted property from the nominated property file
			GocadPropertyDefinition paintedProperty = getPaintedProperty();
			propertiesInputStream = openSGridDataStream(source, paintedProperty.getFile());
			FloatReader propertiesReader = FloatReader.Builder.newFloatReaderForStream(propertiesInputStream)
					.withGroupSize(1)
					.withOffset(paintedProperty.getOffset())
					.withFormat(FloatFormat.valueOf(paintedProperty.getType()))
					.build();

			float[] value = new float[1];
			for (int positionIndex = 0; positionIndex < totalNumberDataPoints(); positionIndex++)
			{
				propertiesReader.readNextValues(value);

				data.put(value[0]);

				minValue = Math.min(minValue, value[0]);
				maxValue = Math.max(maxValue, value[0]);
			}
		}
		finally
		{
			if (pointsInputStream != null)
			{
				pointsInputStream.close();
			}
			if (propertiesInputStream != null)
			{
				propertiesInputStream.close();
			}
		}
	}

	/**
	 * Create a regex pattern that matches ASCII data file lines, with a
	 * capturing group matching the painted variable.
	 */
	private Pattern createAsciiLineMatchingPattern(GocadPropertyDefinition paintedProperty)
	{
		final String doublePattern = "([\\d.\\-]+)";
		final String nonCapturingDoublePattern = "(?:[\\d.\\-]+)";
		final String spacerPattern = "\\s+";

		//regex for coordinates
		String lineRegex = "\\s*" + doublePattern + spacerPattern + doublePattern + spacerPattern + doublePattern;
		for (int property = 1; property < paintedProperty.getId(); property++)
		{
			//ignore all properties in between coordinates and painted property
			lineRegex += spacerPattern + nonCapturingDoublePattern;
		}
		//only capture the painted property
		lineRegex += spacerPattern + doublePattern + ".*";

		return Pattern.compile(lineRegex);
	}


	private void initialiseDataVariables()
	{
		bounds = null;
		positions = new ArrayList<Position>(xSize * ySize);
		data = FloatBuffer.allocate(totalNumberDataPoints());
		top = 0;
		minValue = Float.MAX_VALUE;
		maxValue = -Float.MAX_VALUE;
	}

	private void validatePaintedPropertyAvailable() throws IOException
	{
		if (getPaintedProperty() == null)
		{
			throw new IOException("No property found for painting");
		}
	}

	private void validateDataFileSpecified() throws IOException
	{
		if (asciiDataFile == null &&
				(pointsDataFile == null || getPaintedProperty().getFile() == null))
		{
			throw new IOException("Data file not specified");
		}
	}

	private void validateNonZeroDimensions() throws IOException
	{
		if (xSize == 0 || ySize == 0 || zSize == 0)
		{
			throw new IOException("Volume dimensions are 0");
		}
	}

	private void validateDataFileLoadedCorrectly() throws IOException
	{
		if (positions.size() != xSize * ySize)
		{
			throw new IOException("Data file doesn't contain the correct number of positions. Contains "
					+ positions.size() + ". Expected " + xSize * ySize);
		}
	}

	/**
	 * Load the source file. If the source is contained within a ZIP file, will
	 * return a {@link ZipFile} instance. Otherwise, returns the .sg
	 * {@link File}
	 */
	private Object openSource(URL url) throws IOException
	{
		File file = URLUtil.urlToFile(url);
		if (file == null)
		{
			// Note a file:// url
			// TODO: Support HTTP etc.
			return null;
		}

		if (file.getName().toLowerCase().endsWith(".zip"))
		{
			return new ZipFile(file);
		}
		return file;
	}

	/**
	 * Open an input stream that reads from the SGrid header file. Supports
	 * loading .sg files from within Zip archives.
	 */
	private InputStream openSGridHeaderStream(Object source) throws IOException
	{
		if (source instanceof ZipFile)
		{
			ZipFile zip = (ZipFile) source;
			Enumeration<? extends ZipEntry> entries = zip.entries();
			ZipEntry sgEntry = null;
			while (entries.hasMoreElements())
			{
				ZipEntry entry = entries.nextElement();
				if (entry.getName().toLowerCase().endsWith(".sg"))
				{
					sgEntry = entry;
					break;
				}
			}

			if (sgEntry == null)
			{
				throw new IOException("Could not find .sg file in zip");
			}

			return new BufferedInputStream(zip.getInputStream(sgEntry));
		}
		else
		{
			return new BufferedInputStream(new FileInputStream(((File) source)));
		}
	}

	/**
	 * Open an input stream that reads from the named data file
	 */
	private InputStream openSGridDataStream(Object source, String file) throws IOException
	{
		if (source instanceof ZipFile)
		{
			ZipFile zip = (ZipFile) source;
			ZipEntry dataEntry = zip.getEntry(file);
			return new BufferedInputStream(zip.getInputStream(dataEntry));
		}
		else
		{
			File data = new File(((File) source).getParent(), file);
			if (data.exists())
			{
				return new BufferedInputStream(new FileInputStream(data));
			}
		}
		throw new IOException("Data file '" + file + "' not found");
	}

	/** Close the source file as appropriate */
	private void closeSource(Object source)
	{
		if (source instanceof ZipFile)
		{
			try
			{
				((ZipFile) source).close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/** Grow the volume sector to include the provided position */
	private void updateSectorToIncludePosition(Position position)
	{
		bounds = Bounds.union(bounds, position);
	}

	/** Reverse coordinate axes as appropriate */
	private void correctForReversedAxes()
	{
		if (reverseX || reverseY || reverseZ)
		{
			//if the z-axis is reversed, bring all the positions up to the
			//top depth (they are currently at the bottom depth)
			if (reverseZ)
			{
				List<Position> oldPositions = positions;
				positions = new ArrayList<Position>(oldPositions.size());
				for (Position position : oldPositions)
				{
					positions.add(new Position(position, position.elevation + depth));
				}
			}

			//if the x-axis or y-axis are reversed, mirror them
			if (reverseX || reverseY)
			{
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
	}

	private int totalNumberOfPositions()
	{
		return xSize * ySize * zSize;
	}

	private int totalNumberDataPoints()
	{
		if (isCellCentred())
		{
			return (xSize - 1) * (ySize - 1) * (zSize - 1);
		}
		return xSize * ySize * zSize;
	}

	/**
	 * @return The property definition for the painted property
	 */
	private GocadPropertyDefinition getPaintedProperty()
	{
		if (paintedProperty != null)
		{
			return paintedProperty;
		}

		if (properties.isEmpty())
		{
			return null;
		}

		// Layer definition overrides gocad header definition
		String thePaintedVariableName =
				layer.getPaintedVariableName() == null ? paintedVariableName : layer.getPaintedVariableName();

		// If none specified, use the first property
		if (thePaintedVariableName == null)
		{
			paintedProperty = properties.get(0);
			return paintedProperty;
		}

		// Otherwise match by property name
		for (GocadPropertyDefinition d : properties)
		{
			if (thePaintedVariableName.equalsIgnoreCase(d.getName()))
			{
				paintedProperty = d;
				return paintedProperty;
			}
		}
		return null;
	}

	/**
	 * Parse the contents of the SGrid header file referred to by the provided
	 * source object.
	 */
	private void parseHeaderFile(Object source) throws IOException
	{
		properties = new ArrayList<GocadPropertyDefinition>();
		InputStream sgInputStream = null;
		try
		{
			sgInputStream = openSGridHeaderStream(source);
			BufferedReader reader = new BufferedReader(new InputStreamReader(sgInputStream));
			String line;
			while ((line = reader.readLine()) != null)
			{
				parseLine(line);
			}
		}
		finally
		{
			if (sgInputStream != null)
			{
				sgInputStream.close();
			}
		}
	}

	/**
	 * Parse a single line from the SGrid header file and update properties as
	 * appropriate
	 */
	private void parseLine(String line)
	{
		Matcher matcher = paintedVariablePattern.matcher(line);
		if (matcher.matches())
		{
			paintedVariableName = matcher.group(1);
			return;
		}

		matcher = axisPattern.matcher(line);
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

		matcher = propertyDefinition.matcher(line);
		if (matcher.matches())
		{
			int index = Integer.valueOf(matcher.group(1));
			parsePropertyDefinition(line, index - 1);
			return;
		}

		matcher = propertyAlignmentPattern.matcher(line);
		if (matcher.matches())
		{
			String propAlignment = matcher.group(1);
			cellCentred = propAlignment.toLowerCase().equals("cells");
			return;
		}

		matcher = asciiDataFilePattern.matcher(line);
		if (matcher.matches())
		{
			asciiDataFile = matcher.group(1);
			return;
		}

		matcher = pointsFilePattern.matcher(line);
		if (matcher.matches())
		{
			pointsDataFile = matcher.group(1);
			return;
		}

		matcher = pointsOffsetPattern.matcher(line);
		if (matcher.matches())
		{
			pointsOffset = Integer.parseInt(matcher.group(1));
			return;
		}

		matcher = flagsFilePattern.matcher(line);
		if (matcher.matches())
		{
			flagsDataFile = matcher.group(1);
			return;
		}

		matcher = flagsOffsetPattern.matcher(line);
		if (matcher.matches())
		{
			flagsOffset = Integer.parseInt(matcher.group(1));
			return;
		}

	}

	private void parsePropertyDefinition(String line, int definitionIndex)
	{
		GocadPropertyDefinition definition;
		if (definitionIndex >= properties.size())
		{
			definition = new GocadPropertyDefinition();
			// TODO: Support property-specific alignment specification
			definition.setCellCentred(cellCentred);
			definition.setId(definitionIndex + 1);
			properties.add(definition);
		}
		else
		{
			definition = properties.get(definitionIndex);
		}

		Matcher matcher = propertyFilePattern.matcher(line);
		if (matcher.matches())
		{
			definition.setFile(matcher.group(2));
			return;
		}

		matcher = propertyOffsetPattern.matcher(line);
		if (matcher.matches())
		{
			definition.setOffset(Integer.parseInt(matcher.group(1)));
			return;
		}

		matcher = propertySizePattern.matcher(line);
		if (matcher.matches())
		{
			definition.setBytes(Integer.parseInt(matcher.group(2)));
			return;
		}

		matcher = propertyFormatPattern.matcher(line);
		if (matcher.matches())
		{
			definition.setFormat(matcher.group(2));
			return;
		}

		matcher = propertyTypePattern.matcher(line);
		if (matcher.matches())
		{
			definition.setType(matcher.group(2));
			return;
		}

		matcher = propertyNamePattern.matcher(line);
		if (matcher.matches())
		{
			definition.setName(matcher.group(2));
			return;
		}

		matcher = propertyNoDataPattern.matcher(line);
		if (matcher.matches())
		{
			definition.setNoDataValue(Float.parseFloat(matcher.group(2)));
			return;
		}
	}
}
