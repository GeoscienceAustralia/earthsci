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
package au.gov.ga.earthsci.common.util;

import gov.nasa.worldwind.util.WWXML;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Utility methods for XML handling.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class XmlUtil
{
	/**
	 * Get the first {@link Text} child of the given parent.
	 * 
	 * @param parent
	 *            Parent to search
	 * @return First {@link Text} child of parent
	 */
	public static Text getFirstChildText(Node parent)
	{
		return getFirstChildImplementing(parent, Text.class);
	}

	/**
	 * Get the first {@link Element} child of the given parent.
	 * 
	 * @param parent
	 *            Parent to search
	 * @return First {@link Element} child of parent
	 */
	public static Element getFirstChildElement(Node parent)
	{
		return getFirstChildImplementing(parent, Element.class);
	}

	/**
	 * Get all children {@link Element}s of parent.
	 * 
	 * @param parent
	 *            Parent to search
	 * @return Array of {@link Element} children of parent
	 */
	public static Element[] getElements(Node parent)
	{
		return getChildrenImplementing(parent, Element.class);
	}

	/**
	 * Get the first child node of parent that implements/subclasses the given
	 * type.
	 * 
	 * @param parent
	 *            Parent to search
	 * @param nodeType
	 *            Type of child to search for
	 * @return First child of parent that conforms to the given nodeType
	 */
	public static <N extends Node> N getFirstChildImplementing(Node parent, Class<N> nodeType)
	{
		return getNthChildImplementing(0, parent, nodeType);
	}

	/**
	 * Get the index'th child node of parent that implements/subclasses the
	 * given type.
	 * 
	 * @param index
	 *            Child node index
	 * @param parent
	 *            Parent to search
	 * @param nodeType
	 *            Type of child to search for
	 * @return index'th child of parent that conforms to the given nodeType
	 */
	public static <N extends Node> N getNthChildImplementing(int index, Node parent, Class<N> nodeType)
	{
		if (parent == null)
		{
			return null;
		}
		NodeList children = parent.getChildNodes();
		if (children == null)
		{
			return null;
		}
		int count = 0;
		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			if (nodeType.isAssignableFrom(node.getClass()))
			{
				if (count++ == index)
				{
					@SuppressWarnings("unchecked")
					N n = (N) node;
					return n;
				}
			}
		}
		return null;
	}

	/**
	 * Get all child nodes of parent that implement/subclass the given type.
	 * 
	 * @param parent
	 *            Parent to search
	 * @param nodeType
	 *            Type of child to search for
	 * @return Array of children of parent that conform to the given nodeType
	 */
	public static <N extends Node> N[] getChildrenImplementing(Node parent, Class<N> nodeType)
	{
		if (parent == null)
		{
			return null;
		}
		NodeList children = parent.getChildNodes();
		if (children == null)
		{
			return null;
		}
		int count = 0;
		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			if (nodeType.isAssignableFrom(node.getClass()))
			{
				count++;
			}
		}
		@SuppressWarnings("unchecked")
		N[] array = (N[]) Array.newInstance(nodeType, count);
		for (int i = 0, pos = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			if (nodeType.isAssignableFrom(node.getClass()))
			{
				@SuppressWarnings("unchecked")
				N n = (N) node;
				Array.set(array, pos++, n);
			}
		}
		return array;
	}

	/**
	 * Returns the number of child elements of parent that have the given
	 * element tag name.
	 * 
	 * @param name
	 *            Element tag name to search for
	 * @param parent
	 *            Parent to search
	 * @return Count of child elements that have the given name
	 */
	public static int getCountChildElementsByTagName(String name, Element parent)
	{
		if (parent == null)
		{
			return 0;
		}
		NodeList children = parent.getChildNodes();
		if (children == null)
		{
			return 0;
		}
		int count = 0;
		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			if (node instanceof Element)
			{
				Element e = (Element) node;
				if (e.getTagName().equals(name))
				{
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * Return the index'th child element of parent that has the given element
	 * tag name.
	 * 
	 * @param index
	 *            Index of the child to return
	 * @param name
	 *            Element tag name to search for
	 * @param parent
	 *            Parent to search
	 * @return index'th child element of parent that has the given name
	 */
	public static Element getChildElementByTagName(int index, String name, Element parent)
	{
		if (parent == null)
		{
			return null;
		}
		NodeList children = parent.getChildNodes();
		if (children == null)
		{
			return null;
		}
		int count = 0;
		for (int i = 0; i < children.getLength(); i++)
		{
			Node node = children.item(i);
			if (node instanceof Element)
			{
				Element e = (Element) node;
				if (e.getTagName().equals(name) && (count++ == index))
				{
					return e;
				}
			}
		}
		return null;
	}

	/**
	 * Return an array of direct child elements that have the given element tag
	 * name.
	 * 
	 * @param name
	 *            Element tag name to search for
	 * @param parent
	 *            Parent to search
	 * @return Array of child elements with the given name
	 */
	public static Element[] getChildElementsByTagName(String name, Element parent)
	{
		List<Element> elements = new ArrayList<Element>();
		if (parent != null)
		{
			NodeList children = parent.getChildNodes();
			if (children != null)
			{
				for (int i = 0; i < children.getLength(); i++)
				{
					Node node = children.item(i);
					if (node instanceof Element)
					{
						Element e = (Element) node;
						if (e.getTagName().equals(name))
						{
							elements.add(e);
						}
					}
				}
			}
		}
		return elements.toArray(new Element[elements.size()]);
	}

	/**
	 * Save the given document to an output stream. Output is nicely formatted,
	 * with child elements indented by 4 spaces.
	 * 
	 * @param doc
	 *            Document to save
	 * @param outputStream
	 *            OutputStream to save to
	 * @throws TransformerException
	 * @throws IOException
	 */
	public static void saveDocumentToFormattedStream(Document doc, OutputStream outputStream)
			throws TransformerException, IOException
	{
		Source source = new DOMSource(doc);
		Result result = new StreamResult(outputStream);
		Transformer transformer = createTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4"); //$NON-NLS-1$ //$NON-NLS-2$
		transformer.transform(source, result);
	}

	/**
	 * Create a new {@link Transformer}.
	 * 
	 * @throws TransformerConfigurationException
	 */
	public static Transformer createTransformer() throws TransformerConfigurationException
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		return transformerFactory.newTransformer();
	}

	/**
	 * Open the XML document referenced in the given source
	 * <p/>
	 * Supports:
	 * <ul>
	 * <li> {@link Document}
	 * <li> {@link URI} (for supported protocols - see {@link URI#toURL()})
	 * <li>and all formats supported by {@link WWXML#openDocument(Object)}
	 * </ul>
	 */
	public static Document openDocument(Object source)
	{
		if (source == null)
		{
			return null;
		}

		if (source instanceof Document)
		{
			return (Document) source;
		}

		if (source instanceof URI)
		{
			try
			{
				return WWXML.openDocument(((URI) source).toURL());
			}
			catch (Exception e)
			{
				return null;
			}
		}

		try
		{
			return WWXML.openDocument(source);
		}
		catch (Exception e)
		{
			return null;
		}
	}

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

	/**
	 * Return the text at the given path relative to the given context.
	 * 
	 * @param context
	 *            The element from which the path is relative to
	 * @param path
	 *            The path to the text value to return
	 * 
	 * @return the text at the given path relative to the given context, or
	 *         <code>null</code> if none is found
	 */
	public static String getText(Element context, String path)
	{
		return WWXML.getText(context, path);
	}

	/**
	 * Return the text at the given path relative to the given context,
	 * returning the provided default if none is found.
	 * 
	 * @param context
	 *            The element from which the path is relative to
	 * @param path
	 *            The path to the text value to return
	 * @param def
	 *            The default value to return if none is found
	 * 
	 * @return the text at the given path relative to the given context, or the
	 *         provided default if none is found.
	 */
	public static String getText(Element context, String path, String def)
	{
		return getText(context, path, def, null);
	}

	/**
	 * Return the text at the given path relative to the given context,
	 * returning the provided default if none is found.
	 * 
	 * @param context
	 *            The element from which the path is relative to
	 * @param path
	 *            The path to the text value to return
	 * @param def
	 *            The default value to return if none is found
	 * @param xpath
	 *            An {@link XPath} instance that can be reused across calls
	 * 
	 * @return the text at the given path relative to the given context, or the
	 *         provided default if none is found.
	 */
	public static String getText(Element context, String path, String def, XPath xpath)
	{
		String s = WWXML.getText(context, path, xpath);
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
		Boolean b = WWXML.getBoolean(context, path, xpath);
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
		Double d = WWXML.getDouble(context, path, xpath);
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
		Integer i = WWXML.getInteger(context, path, xpath);
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
		Long i = WWXML.getLong(context, path, xpath);
		if (i == null)
		{
			return def;
		}
		return i;
	}

	/**
	 * Return a URL created from the text identified at the given path relative
	 * to the provided XML element.
	 * 
	 * @param element
	 *            The XML element the path is relative to
	 * @param path
	 *            The xpath expression identifying the text to use to generate
	 *            the URL
	 * @param context
	 *            The URL context to use for construction of relative URLs
	 * 
	 * @return URL created from the text identified at the given path
	 * 
	 * @throws MalformedURLException
	 *             If the identified text cannot be converted to a valid URL
	 */
	public static URL getURL(Element element, String path, URL context) throws MalformedURLException
	{
		String text = getText(element, path);
		return textToURL(text, context);
	}

	/**
	 * Return a URL created from the text identified at the given path relative
	 * to the provided XML element.
	 * 
	 * @param element
	 *            The XML element the path is relative to
	 * @param path
	 *            The xpath expression identifying the text to use to generate
	 *            the URL
	 * @param context
	 *            The URL context to use for construction of relative URLs
	 * @param xpath
	 *            An {@link XPath} instance that can be reused between calls
	 * 
	 * @return URL created from the text identified at the given path
	 * 
	 * @throws MalformedURLException
	 *             If the identified text cannot be converted to a valid URL
	 */
	public static URL getURL(Element element, String path, URL context, XPath xpath) throws MalformedURLException
	{
		String text = WWXML.getText(element, path, xpath);
		return textToURL(text, context);
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

	/**
	 * Returns all elements identified by the given XPath expression relative to
	 * the provided element.
	 * 
	 * @param context
	 *            The element from which the xpath expression is relative
	 * @param path
	 *            The xpath expression to use for locating elements
	 * @param xpath
	 *            An {@link XPath} instance that can be reused between calls
	 * 
	 * @return All elements identified by the given XPath expression, or the
	 *         empty array if none are found.
	 */
	public static Element[] getElements(Element context, String path, XPath xpath)
	{
		Element[] elements = WWXML.getElements(context, path, xpath);
		return elements == null ? new Element[0] : elements;
	}

	/**
	 * Create and return a new document builder
	 */
	public static DocumentBuilder createDocumentBuilder()
	{
		return WWXML.createDocumentBuilder(false);
	}
}
