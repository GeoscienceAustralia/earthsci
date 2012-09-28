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
package au.gov.ga.earthsci.core.persistence;

import java.net.URI;

import org.w3c.dom.Element;

/**
 * Represents an adapter object that provides support for persisting the given
 * parameter type to and from XML.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <E>
 *            Type that this adapter supports.
 */
public interface IPersistentAdapter<E>
{
	/**
	 * Convert the given object to XML. The developer can set attributes
	 * directly on the provided element.
	 * 
	 * @param object
	 *            Object to convert
	 * @param element
	 *            XML element to save the object in
	 * @param context
	 */
	void toXML(E object, Element element, URI context);

	/**
	 * Convert the given XML to a new object. The element provided is at the
	 * same level as the element provided to
	 * {@link #toXML(Object, Element, URI)}.
	 * 
	 * @param element
	 *            XML element to load the object's properties from.
	 * @param context
	 * @return New object created from XML
	 */
	E fromXML(Element element, URI context);
}
