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
package au.gov.ga.earthsci.worldwind.common.layers.geonames;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.gov.ga.earthsci.worldwind.common.util.ColorFont;

/**
 * Represents a single GeoName from the geoname.org database.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GeoName implements GeographicText
{
	/**
	 * GeoName name
	 */
	public final String name;
	/**
	 * GeoName id (unique)
	 */
	public final int geonameId;
	/**
	 * GeoName location
	 */
	public final LatLon latlon;
	/**
	 * GeoName feature class
	 */
	public final String featureClass;
	/**
	 * GeoName feature code
	 */
	public final String featureCode;
	/**
	 * Level in the GeoName hierarchy (globe is -1, continent is 0, country is
	 * 1, etc)
	 */
	public final int level;

	private final VisibilityCalculator visibilityCalculator;
	private final Position position;
	private final ColorFont font;
	private final ColorFontProvider fontProvider;

	private GeoName parent;
	private Collection<GeoName> children;

	private static Object fileLock = new Object();
	private static Map<Integer, GeoName> geonameMap = new HashMap<Integer, GeoName>();
	private static Object mapLock = new Object();

	public GeoName(String name, int geonameId, LatLon latlon, String featureClass, String featureCode, int level,
			ColorFontProvider fontProvider, VisibilityCalculator visibilityCalculator)
	{
		this.name = name;
		this.geonameId = geonameId;
		this.latlon = latlon;
		this.position = new Position(latlon, 0);
		this.featureClass = featureClass;
		this.featureCode = featureCode;
		this.level = level;
		this.fontProvider = fontProvider;
		this.font = fontProvider.get(featureCode);
		this.visibilityCalculator = visibilityCalculator;
	}

	private String cacheFilename()
	{
		return "GeoNames/" + (level + 1) + "/" + geonameId + ".xml";
	}

	private URL findCacheFile()
	{
		return WorldWind.getDataFileStore().findFile(cacheFilename(), true);
	}

	private File newCacheFile()
	{
		return WorldWind.getDataFileStore().newFile(cacheFilename());
	}

	public boolean cacheFileExists()
	{
		return findCacheFile() != null;
	}

	public void saveChildren(ByteBuffer buffer) throws IOException
	{
		synchronized (fileLock)
		{
			WWIO.saveBuffer(buffer, newCacheFile());
		}
	}

	public void loadChildren()
	{
		children = null;

		URL url = findCacheFile();
		if (url != null)
		{
			try
			{
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				documentBuilderFactory.setNamespaceAware(false);
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

				Document document = null;
				synchronized (fileLock)
				{
					document = documentBuilder.parse(url.openStream());
				}

				Collection<GeoName> geonames = parseDocument(document);
				if (geonames != null)
				{
					children = new HashSet<GeoName>();
					for (GeoName geoname : geonames)
					{
						synchronized (mapLock)
						{
							GeoName mapped = geonameMap.get(geoname.geonameId);
							if (mapped == null || mapped.parent == this)
							{
								geoname.parent = this;
								children.add(geoname);
								geonameMap.put(geoname.geonameId, geoname);
							}
							else
							{
								geoname = mapped;
								double distance1 =
										VisibilityCalculatorImpl.latlonDistanceSquared(latlon, geoname.latlon);
								double distance2 =
										VisibilityCalculatorImpl.latlonDistanceSquared(geoname.parent.latlon,
												geoname.latlon);
								if (distance1 < distance2)
								{
									geoname.parent.getChildren().remove(geoname);
									children.add(geoname);
									geoname.parent = this;
								}
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				Logging.logger().log(java.util.logging.Level.SEVERE, "Deleting corrupt GeoNames .xml file " + url, e);
				WorldWind.getDataFileStore().removeFile(url);
			}
		}
	}

	private Collection<GeoName> parseDocument(Document document)
	{
		NodeList resultsCount = document.getElementsByTagName("totalResultsCount");
		if (resultsCount.getLength() <= 0)
		{
			return null;
		}
		Collection<GeoName> geonames = new ArrayList<GeoName>();
		NodeList nodes = document.getElementsByTagName("geoname");
		if (nodes != null)
		{
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				NodeList children = node.getChildNodes();

				String name = null;
				String featureClass = null;
				String featureCode = null;
				Integer geonameId = null;
				Double lat = null;
				Double lon = null;

				for (int j = 0; j < children.getLength(); j++)
				{
					try
					{
						Node child = children.item(j);
						if (child.getNodeName().equals("name"))
						{
							name = child.getTextContent();
						}
						else if (child.getNodeName().equals("fcl"))
						{
							featureClass = child.getTextContent();
						}
						else if (child.getNodeName().equals("fcode"))
						{
							featureCode = child.getTextContent();
						}
						else if (child.getNodeName().equals("lat"))
						{
							lat = Double.valueOf(child.getTextContent());
						}
						else if (child.getNodeName().equals("lng"))
						{
							lon = Double.valueOf(child.getTextContent());
						}
						else if (child.getNodeName().equals("geonameId"))
						{
							geonameId = Integer.valueOf(child.getTextContent());
						}
					}
					catch (Exception e)
					{
					}
				}

				if (lat != null && lon != null && geonameId != null && name != null && name.length() > 0)
				{
					LatLon latlon = new LatLon(Angle.fromDegreesLatitude(lat), Angle.fromDegreesLongitude(lon));
					GeoName geoname =
							new GeoName(name, geonameId, latlon, featureClass, featureCode, level + 1, fontProvider,
									visibilityCalculator);
					geonames.add(geoname);
				}
			}
		}
		return geonames;
	}

	public boolean loadedChildren()
	{
		return children != null;
	}

	public Collection<GeoName> getChildren()
	{
		return children;
	}

	@Override
	public Color getBackgroundColor()
	{
		return font.backgroundColor;
	}

	@Override
	public Color getColor()
	{
		return font.color;
	}

	@Override
	public Font getFont()
	{
		return font.font;
	}

	@Override
	public Position getPosition()
	{
		return position;
	}

	@Override
	public CharSequence getText()
	{
		return name;
	}

	@Override
	public boolean isVisible()
	{
		return visibilityCalculator.isVisible(this);
	}

	@Override
	public double getPriority()
	{
		return 0;
	}

	@Override
	@Deprecated
	public void setBackgroundColor(Color background)
	{
	}

	@Override
	@Deprecated
	public void setColor(Color color)
	{
	}

	@Override
	@Deprecated
	public void setFont(Font font)
	{
	}

	@Override
	@Deprecated
	public void setPosition(Position position)
	{
	}

	@Override
	@Deprecated
	public void setText(CharSequence text)
	{
	}

	@Override
	@Deprecated
	public void setVisible(boolean visible)
	{
	}

	@Override
	@Deprecated
	public void setPriority(double d)
	{
	}

	@Override
	public String toString()
	{
		return name + " (" + geonameId + ") " + position;
	}

	@Override
	public int hashCode()
	{
		return geonameId ^ 1234321;
	}
}
