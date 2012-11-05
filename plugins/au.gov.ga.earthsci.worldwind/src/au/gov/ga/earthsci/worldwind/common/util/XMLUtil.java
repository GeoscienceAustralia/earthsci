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
package au.gov.ga.earthsci.worldwind.common.util;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.util.transform.URLTransformer;

/**
 * An extension of the World-Wind provided {@link WWXML} utilities, provides
 * additional helper methods for dealing with XML documents.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public class XMLUtil extends WWXML
{
	private final static double EPSILON = 1e-10;

	/**
	 * @return The XML element from a generic source. If the source is an
	 *         {@link Element}, will return the provided source. Otherwise, will
	 *         attempt to open the source as an XML {@link Document} and will
	 *         return the document element.
	 * 
	 * @see {@link WWXML#openDocument(Object)}
	 */
	public static Element getElementFromSource(Object source)
	{
		if (source == null)
		{
			return null;
		}
		if (source instanceof Element)
		{
			return (Element) source;
		}
		else if (source instanceof Document)
		{
			return ((Document) source).getDocumentElement();
		}
		else
		{
			Document document = openDocument(source);
			if (document != null)
			{
				return document.getDocumentElement();
			}
		}
		return null;
	}

	public static String getText(Element context, String path, String def)
	{
		return getText(context, path, def, null);
	}

	public static String getText(Element context, String path, String def, XPath xpath)
	{
		String s = getText(context, path, xpath);
		if (s == null)
		{
			return def;
		}
		return s;
	}

	public static boolean getBoolean(Element context, String path, boolean def)
	{
		return getBoolean(context, path, def, null);
	}

	public static boolean getBoolean(Element context, String path, boolean def, XPath xpath)
	{
		Boolean b = getBoolean(context, path, xpath);
		if (b == null)
		{
			return def;
		}
		return b;
	}

	public static double getDouble(Element context, String path, double def)
	{
		return getDouble(context, path, def, null);
	}

	public static double getDouble(Element context, String path, double def, XPath xpath)
	{
		Double d = getDouble(context, path, xpath);
		if (d == null)
		{
			return def;
		}
		return d;
	}

	public static int getInteger(Element context, String path, int def)
	{
		return getInteger(context, path, def, null);
	}

	public static int getInteger(Element context, String path, int def, XPath xpath)
	{
		Integer i = getInteger(context, path, xpath);
		if (i == null)
		{
			return def;
		}
		return i;
	}

	public static long getLong(Element context, String path, long def)
	{
		return getLong(context, path, def, null);
	}

	public static long getLong(Element context, String path, long def, XPath xpath)
	{
		Long i = getLong(context, path, xpath);
		if (i == null)
		{
			return def;
		}
		return i;
	}

	public static MercatorSector getMercatorSector(Element context, String path, XPath xpath)
	{
		if (context == null)
		{
			String message = Logging.getMessage("nullValue.ContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Element el = path == null ? context : getElement(context, path, xpath);
		if (el == null)
			return null;

		LatLon sw = getMercatorLatLon(el, "SouthWest/LatLon", xpath);
		LatLon ne = getMercatorLatLon(el, "NorthEast/LatLon", xpath);

		if (sw == null || ne == null)
			return null;

		return new MercatorSector(MercatorSector.gudermannianInverse(sw.latitude),
				MercatorSector.gudermannianInverse(ne.latitude), sw.longitude, ne.longitude);
	}

	protected static LatLon getMercatorLatLon(Element context, String path, XPath xpath)
	{
		if (context == null)
		{
			String message = Logging.getMessage("nullValue.ContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		try
		{
			Element el = path == null ? context : getElement(context, path, xpath);
			if (el == null)
				return null;

			String units = getText(el, "@units", xpath);
			boolean degrees = units == null || "degrees".equals(units);
			boolean radians = "radians".equals(units);
			Double lat = getDouble(el, "@latitude", xpath);
			Double latPercent = getDouble(el, "@latitudePercent", xpath);
			Double lon = getDouble(el, "@longitude", xpath);

			if (lat == null && latPercent != null)
			{
				Angle latAngle = MercatorSector.gudermannian(latPercent);
				lat = radians ? latAngle.radians : latAngle.degrees;
			}

			if (lat == null || lon == null)
				return null;

			if (degrees)
				return LatLon.fromDegrees(lat, lon);

			if (radians)
				return LatLon.fromRadians(lat, lon);

			// Warn that units are not recognized
			String message = Logging.getMessage("XML.UnitsUnrecognized", units);
			Logging.logger().warning(message);

			return null;
		}
		catch (NumberFormatException e)
		{
			String message = Logging.getMessage("generic.ConversionError", path);
			Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
			return null;
		}
	}

	public static URL getURL(Element element, String path, URL context) throws MalformedURLException
	{
		String text = getText(element, path);
		return getURL(text, context);
	}

	public static URL getURL(Element element, String path, URL context, XPath xpath) throws MalformedURLException
	{
		String text = getText(element, path, xpath);
		return getURL(text, context);
	}

	protected static URL getURL(String text, URL context) throws MalformedURLException
	{
		URL url = textToURL(text, context);
		return URLTransformer.transform(url);
	}

	protected static URL textToURL(String text, URL context) throws MalformedURLException
	{
		if (text == null || text.length() == 0)
		{
			return null;
		}
		if (context == null)
		{
			return new URL(text);
		}
		return new URL(context, text);
	}

	public static File getFile(Element element, String path)
	{
		try
		{
			URL url = getURL(element, path, null);
			if (url == null)
			{
				return null;
			}
			return new File(url.toURI());
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static Element appendColor(Element context, String path, Color color)
	{
		Element element = WWXML.appendElement(context, path);
		WWXML.setIntegerAttribute(element, "red", color.getRed());
		WWXML.setIntegerAttribute(element, "green", color.getGreen());
		WWXML.setIntegerAttribute(element, "blue", color.getBlue());
		WWXML.setIntegerAttribute(element, "alpha", color.getAlpha());
		return element;
	}

	public static Position getPosition(Element context, String path, XPath xpath)
	{
		if (context == null)
		{
			String message = Logging.getMessage("nullValue.ContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		try
		{
			Element el = path == null ? context : getElement(context, path, xpath);
			if (el == null)
			{
				return null;
			}

			String units = getText(el, "@units", xpath);
			Double lat = getDouble(el, "@latitude", xpath);
			Double lon = getDouble(el, "@longitude", xpath);
			Double elev = getDouble(el, "@elevation", xpath);

			if (lat == null || lon == null)
			{
				return null;
			}

			if (elev == null)
			{
				elev = 0d;
			}

			if (units == null || units.length() == 0 || units.equals("degrees"))
			{
				return Position.fromDegrees(lat, lon, elev);
			}

			if (units.equals("radians"))
			{
				return Position.fromRadians(lat, lon, elev);
			}

			// Warn that units are not recognized
			String message = Logging.getMessage("XML.UnitsUnrecognized", units);
			Logging.logger().warning(message);

			return null;
		}
		catch (NumberFormatException e)
		{
			String message = Logging.getMessage("generic.ConversionError", path);
			Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
			return null;
		}
	}

	public static Element appendPosition(Element context, String path, Position pos)
	{
		if (context == null)
		{
			String message = Logging.getMessage("nullValue.ContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (pos == null)
		{
			String message = Logging.getMessage("nullValue.PositionIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Element el = appendElementPath(context, path);
		setTextAttribute(el, "units", "degrees");
		setDoubleAttribute(el, "latitude", pos.getLatitude().degrees);
		setDoubleAttribute(el, "longitude", pos.getLongitude().degrees);
		setDoubleAttribute(el, "elevation", pos.getElevation());

		return el;
	}

	public static Vec4 getVec4(Element context, String path, XPath xpath)
	{
		if (context == null)
		{
			String message = Logging.getMessage("nullValue.ContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		try
		{
			Element el = path == null ? context : getElement(context, path, xpath);
			if (el == null)
			{
				return null;
			}

			Double x = getDouble(el, "@x", xpath);
			Double y = getDouble(el, "@y", xpath);
			Double z = getDouble(el, "@z", xpath);
			Double w = getDouble(el, "@w", xpath);

			if (x == null || y == null || z == null)
			{
				return null;
			}

			if (w == null)
			{
				w = 1d;
			}

			return new Vec4(x, y, z, w);
		}
		catch (NumberFormatException e)
		{
			String message = Logging.getMessage("generic.ConversionError", path);
			Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
			return null;
		}
	}

	public static Element appendVec4(Element context, String path, Vec4 v)
	{
		if (context == null)
		{
			String message = Logging.getMessage("nullValue.ContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (v == null)
		{
			String message = Logging.getMessage("nullValue.Vec4IsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Element el = appendElementPath(context, path);
		setDoubleAttribute(el, "x", v.x);
		setDoubleAttribute(el, "y", v.y);
		setDoubleAttribute(el, "z", v.z);
		if (v.w != 1)
		{
			setDoubleAttribute(el, "w", v.w);
		}

		return el;
	}

	public static Long getFormattedDate(Element context, String path, XPath xpath)
	{
		if (context == null)
		{
			String message = Logging.getMessage("nullValue.ContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		try
		{
			Element el = path == null ? context : getElement(context, path, xpath);
			if (el == null)
			{
				return null;
			}

			String value = getText(el, "@value", xpath);
			String format = getText(el, "@format", xpath);

			DateFormat dateFormat = new SimpleDateFormat(format);
			Date date = dateFormat.parse(value);

			return date.getTime();
		}
		catch (ParseException e)
		{
			String message = Logging.getMessage("generic.ConversionError", path);
			Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
			return null;
		}
	}

	public static ColorMap getColorMap(Element context, String path, XPath xpath)
	{
		if (context == null)
		{
			String message = Logging.getMessage("nullValue.ContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (xpath == null)
		{
			xpath = makeXPath();
		}

		Element element = getElement(context, path, xpath);
		if (element == null)
		{
			return null;
		}

		ColorMap colorMap = new ColorMap();

		Boolean b = XMLUtil.getBoolean(element, "@interpolateHue", xpath);
		if (b != null)
		{
			colorMap.setInterpolateHue(b);
		}

		b = XMLUtil.getBoolean(element, "@percentages", xpath);
		if (b != null)
		{
			colorMap.setValuesPercentages(b);
		}

		Element[] mapEntries = WWXML.getElements(element, "Entry", xpath);
		if (mapEntries != null)
		{
			for (Element entry : mapEntries)
			{
				Double value = WWXML.getDouble(entry, "@value", xpath);
				if (value == null)
				{
					value = WWXML.getDouble(entry, "@elevation", xpath);
				}
				if (value == null)
				{
					continue;
				}

				//don't allow a duplicate key
				while (colorMap.containsKey(value))
				{
					value += EPSILON;
				}

				int red = XMLUtil.getInteger(entry, "@red", 0, xpath);
				int green = XMLUtil.getInteger(entry, "@green", 0, xpath);
				int blue = XMLUtil.getInteger(entry, "@blue", 0, xpath);
				int alpha = XMLUtil.getInteger(entry, "@alpha", 255, xpath);
				Color color = new Color(red, green, blue, alpha);
				colorMap.put(value, color);
			}
		}

		return colorMap;
	}

	public static void appendColorMap(Element element, String path, ColorMap colorMap)
	{
		Element colorMapElement = appendElement(element, path);
		setBooleanAttribute(colorMapElement, "interpolateHue", colorMap.isInterpolateHue());
		if (colorMap.isValuesPercentages())
		{
			setBooleanAttribute(colorMapElement, "percentages", colorMap.isValuesPercentages());
		}
		for (Entry<Double, Color> colorMapEntry : colorMap.entrySet())
		{
			Element entryElement = appendElement(colorMapElement, "Entry");
			setDoubleAttribute(entryElement, "value", colorMapEntry.getKey());
			setIntegerAttribute(entryElement, "red", colorMapEntry.getValue().getRed());
			setIntegerAttribute(entryElement, "green", colorMapEntry.getValue().getGreen());
			setIntegerAttribute(entryElement, "blue", colorMapEntry.getValue().getBlue());
			setIntegerAttribute(entryElement, "alpha", colorMapEntry.getValue().getAlpha());
		}
	}

	public static void checkAndSetFormattedDateParam(Element context, AVList params, String paramKey, String paramName,
			XPath xpath)
	{
		if (context == null)
		{
			String message = Logging.getMessage("nullValue.ElementIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (params == null)
		{
			String message = Logging.getMessage("nullValue.ParametersIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (paramKey == null)
		{
			String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (paramName == null)
		{
			String message = Logging.getMessage("nullValue.ParameterNameIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Long l = getFormattedDate(context, paramName, xpath);
		if (l != null)
		{
			params.setValue(paramKey, l);
		}
	}

	public static void checkAndSetURLParam(Element context, AVList params, String paramKey, String paramName,
			XPath xpath)
	{
		if (context == null)
		{
			String message = Logging.getMessage("nullValue.ElementIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (params == null)
		{
			String message = Logging.getMessage("nullValue.ParametersIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (paramKey == null)
		{
			String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (paramName == null)
		{
			String message = Logging.getMessage("nullValue.ParameterNameIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Object o = params.getValue(paramKey);
		if (o == null)
		{
			String s = getText(context, paramName, xpath);
			if (s != null && s.length() > 0)
			{
				try
				{
					URL url = new URL(s);
					params.setValue(paramKey, url);
				}
				catch (MalformedURLException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static void checkAndSetMercatorSectorParam(Element context, AVList params, String paramKey,
			String paramName, XPath xpath)
	{
		if (context == null)
		{
			String message = Logging.getMessage("nullValue.ElementIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (params == null)
		{
			String message = Logging.getMessage("nullValue.ParametersIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (paramKey == null)
		{
			String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (paramName == null)
		{
			String message = Logging.getMessage("nullValue.ParameterNameIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		Object o = params.getValue(paramKey);
		if (o == null)
		{
			Sector sector = getMercatorSector(context, paramName, xpath);
			if (sector != null)
				params.setValue(paramKey, sector);
		}
	}

	/**
	 * Saves the provided XML document to the provided file location in a
	 * pretty-printed, human readable, indented format.
	 * <p/>
	 * This method will overwrite any existing contents in the provided file
	 * location.
	 * 
	 * @param doc
	 *            The xml document to save
	 * @param filePath
	 *            The location to save to. Will overwrite any existing file.
	 */
	public static void saveDocumentToFormattedFile(Document doc, String filePath)
	{
		if (doc == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (filePath == null)
		{
			String message = Logging.getMessage("nullValue.FilePathIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}


		try
		{
			FileOutputStream outputStream = new FileOutputStream(filePath);
			saveDocumentToFormattedStream(doc, outputStream);
		}
		catch (Exception e)
		{
			String message = Logging.getMessage("generic.ExceptionAttemptingToWriteXml", filePath);
			Logging.logger().severe(message);
			throw new WWRuntimeException(e);
		}
	}

	/**
	 * Saves the provided XML document to the provided output stream in a
	 * pretty-printed, human readable, indented format.
	 * 
	 * @param doc
	 *            The xml document to save
	 * @param outputStream
	 *            The output stream to write the output to
	 */
	public static void saveDocumentToFormattedStream(Document doc, OutputStream outputStream)
	{
		if (doc == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}


		if (outputStream == null)
		{
			String message = Logging.getMessage("nullValue.FilePathIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		try
		{
			Source source = new DOMSource(doc);
			Result result = new StreamResult(outputStream);
			Transformer transformer = createTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");
			transformer.transform(source, result);
			outputStream.close();
		}
		catch (Exception e)
		{
			String message = Logging.getMessage("generic.ExceptionAttemptingToWriteXml");
			Logging.logger().severe(message);
			throw new WWRuntimeException(e);
		}

	}
}
