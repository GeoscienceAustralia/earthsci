/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.core.persistence;

import java.net.URI;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IPersistentAdapter} implementation that persists an XML element by
 * adding the child elements of the element to the destination XML element. Also
 * copies the attributes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ElementPersistentAdapter implements IPersistentAdapter<Element>
{
	@Override
	public void toXML(Element object, Element element, URI context)
	{
		NodeList children = object.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			if (child instanceof Element)
			{
				Element childElement = (Element) child;
				Node imported = element.getOwnerDocument().importNode(childElement, true);
				element.appendChild(imported);
			}
		}
		NamedNodeMap attributes = object.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++)
		{
			Node attribute = attributes.item(i);
			object.setAttribute(attribute.getNodeName(), attribute.getNodeValue());
		}
	}

	@Override
	public Element fromXML(Element element, URI context)
	{
		return element;
	}
}
