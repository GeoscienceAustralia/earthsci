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
package au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.elevationreader;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.WWXML;

import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;

/**
 * Treats retrieved image tiles as elevation data, and generates a shading based
 * on the elevation data combined with a provided virtual sun position.
 * <p/>
 * 
 * <pre>
 * &lt;Delegate&gt;
 *   ShadedElevationReader(pixelType,byteOrder,missingData,(sunX,sunY,sunZ),exaggeration[,(min,max)])
 * &lt;/Delegate&gt;
 * </pre>
 * 
 * Where:
 * <ul>
 * <li>pixelType = the pixel format of the elevation tiles (one of "
 * <code>Float32</code>", "<code>Int32</code>", "<code>Int16</code>" or "
 * <code>Int8</code>")
 * <li>byteOrder = the byte order of the elevation tiles (one of "
 * <code>little</code>" or "<code>big</code>")
 * <li>missingData = the value used in the elevation tiles to represent missing
 * data (float)
 * <li>(sunX, sunY, sunZ) = the vector representing the location of the virtual
 * sun. Expressed in arbitrary Cartesian coordinates (not geographic)
 * <li>exaggeration = The vertical exaggeration to bake into the shading
 * (double)
 * <li>(min,max) = (Optional) The minimum and maximum elevation values to use
 * when calculating shading (in metres as doubles)
 * </ul>
 * Shading is calculated as a simple dot product between the calculated normals
 * of the elevation model and the sun vector.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ShadedElevationImageReaderDelegate extends ElevationImageReaderDelegate
{
	private final static String DEFINITION_STRING = "ShadedElevationReader";

	protected final double exaggeration;
	protected final Vec4 sunPosition;
	protected final double minElevation;
	protected final double maxElevation;

	@SuppressWarnings("unused")
	private ShadedElevationImageReaderDelegate()
	{
		this(AVKey.INT16, AVKey.LITTLE_ENDIAN, -Double.MAX_VALUE, 10, new Vec4(-0.7, 0.7, -1).normalize3(),
				-Double.MAX_VALUE, Double.MAX_VALUE);
	}

	public ShadedElevationImageReaderDelegate(String pixelType, String byteOrder, double missingDataSignal,
			double exaggeration, Vec4 sunPosition, double minElevation, double maxElevation)
	{
		super(pixelType, byteOrder, missingDataSignal);
		this.exaggeration = exaggeration;
		this.sunPosition = sunPosition;
		this.minElevation = minElevation;
		this.maxElevation = maxElevation;
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			String optionalMinMaxGroup = "(?:,\\(" + doublePattern + "," + doublePattern + "\\))?";
			Pattern pattern =
					Pattern.compile("(?:\\((\\w+),(\\w+)," + doublePattern + ",\\(" + doublePattern + ","
							+ doublePattern + "," + doublePattern + "\\)," + doublePattern + optionalMinMaxGroup
							+ "\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				String pixelType = matcher.group(1);
				String byteOrder = matcher.group(2);
				double missingDataSignal = Double.parseDouble(matcher.group(3));
				double sunPositionX = Double.parseDouble(matcher.group(4));
				double sunPositionY = Double.parseDouble(matcher.group(5));
				double sunPositionZ = Double.parseDouble(matcher.group(6));
				double exaggeration = Double.parseDouble(matcher.group(7));
				Vec4 sunPosition = new Vec4(sunPositionX, sunPositionY, sunPositionZ).normalize3();

				double minElevation = -Double.MAX_VALUE;
				double maxElevation = Double.MAX_VALUE;
				if (matcher.groupCount() >= 9 && matcher.group(8) != null && matcher.group(9) != null)
				{
					minElevation = Double.parseDouble(matcher.group(8));
					maxElevation = Double.parseDouble(matcher.group(9));
				}

				return new ShadedElevationImageReaderDelegate(WWXML.parseDataType(pixelType),
						WWXML.parseByteOrder(byteOrder), missingDataSignal, exaggeration, sunPosition, minElevation,
						maxElevation);
			}
		}
		return null;
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING + "(" + WWXML.dataTypeAsText(pixelType) + "," + WWXML.byteOrderAsText(byteOrder) + ","
				+ missingDataSignal + ",(" + sunPosition.x + "," + sunPosition.y + "," + sunPosition.z + "),"
				+ exaggeration + ")";
	}

	@Override
	protected BufferedImage generateImage(BufferWrapper elevations, int width, int height, Globe globe, Sector sector)
	{
		//image has one less in width and height than verts array, because normals are calculated using neighbors
		//it would be optimal to read the neighboring tiles for normals on tile edges; this would fix visible tile edges

		BufferedImage image = new BufferedImage(width - 1, height - 1, BufferedImage.TYPE_INT_ARGB);

		Vec4[] verts =
				calculateTileVerts(width, height, sector, elevations, missingDataSignal, exaggeration * 0.000005);
		Vec4[] normals = calculateNormals(width, height, verts);

		for (int y = 0, i = 0; y < height - 1; y++)
		{
			for (int x = 0; x < width - 1; x++, i++)
			{
				int argb = 0;
				Vec4 normal = normals[i];
				if (normal != null)
				{
					double light = Math.max(0d, normal.dot3(sunPosition));

					int r = (int) (255.0 * light);
					int g = (int) (255.0 * light);
					int b = (int) (255.0 * light);

					argb = (0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
				}
				image.setRGB(x, y, argb);
			}
		}

		return image;
	}

	protected Vec4[] calculateTileVerts(int width, int height, Sector sector, BufferWrapper elevations,
			double missingDataSignal, double exaggeration)
	{
		Vec4[] verts = new Vec4[width * height];
		double dlon = sector.getDeltaLonDegrees() / width;
		double dlat = sector.getDeltaLatDegrees() / height;

		for (int y = 0, i = 0; y < height; y++)
		{
			Angle lat = sector.getMaxLatitude().subtractDegrees(dlat * y);
			for (int x = 0; x < width; x++, i++)
			{
				Angle lon = sector.getMinLongitude().addDegrees(dlon * x);
				double elevation = elevations.getDouble(i);
				if (elevation != missingDataSignal && minElevation <= elevation && elevation <= maxElevation)
				{
					verts[i] = new Vec4(lat.degrees, lon.degrees, elevation * exaggeration);
				}
			}
		}

		return verts;
	}

	protected Vec4[] calculateNormals(int width, int height, Vec4[] verts)
	{
		Vec4[] norms = new Vec4[(width - 1) * (height - 1)];
		for (int y = 0, i = 0; y < height - 1; y++)
		{
			for (int x = 0; x < width - 1; x++, i++)
			{
				//v0-v1
				//|
				//v2

				int vertIndex = width * y + x;
				Vec4 v0 = verts[vertIndex];
				if (v0 != null)
				{
					Vec4 v1 = verts[vertIndex + 1];
					Vec4 v2 = verts[vertIndex + width];

					norms[i] = v1 != null && v2 != null ? v1.subtract3(v0).cross3(v0.subtract3(v2)).normalize3() : null;
				}
			}
		}
		return norms;
	}

	protected double[] getMinMax(BufferWrapper elevations, double missingDataSignal)
	{
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (int i = 0; i < elevations.length(); i++)
		{
			double value = elevations.getDouble(i);
			if (value != missingDataSignal)
			{
				min = Math.min(min, value);
				max = Math.max(max, value);
			}
		}

		return new double[] { min, max };
	}
}
