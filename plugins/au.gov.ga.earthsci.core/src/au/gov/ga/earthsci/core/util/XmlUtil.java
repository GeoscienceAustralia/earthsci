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
package au.gov.ga.earthsci.core.util;

import gov.nasa.worldwind.util.WWXML;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
	 * 	<li> {@link Document}
	 *  <li> {@link URI} (for supported protocols - see {@link URI#toURL()})
	 *  <li> and all formats supported by {@link WWXML#openDocument(Object)}
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
			return (Document)source;
		}
		
		if (source instanceof URI)
		{
			try
			{
				return WWXML.openDocument(((URI)source).toURL());
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
}
