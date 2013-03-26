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
package au.gov.ga.earthsci.bookmark;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;

/**
 * An interface for classes that are able to create {@link IBookmarkProperty}
 * instances
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarkPropertyCreator
{
	/**
	 * Returns the property types supported by this creator
	 * 
	 * @return The property types supported by this creator
	 */
	String[] getSupportedTypes();

	/**
	 * Creates and returns a new {@link IBookmarkProperty} from the provided
	 * context.
	 * 
	 * @param type
	 *            The type of property to create
	 * @param propertyElement
	 *            The root element of an XML tree that contains the property
	 *            information
	 * 
	 * @return The {@link IBookmarkProperty} created from the given XML
	 * 
	 * @throws IllegalArgumentException
	 *             If the context is <code>null</code> or does not contain the
	 *             required keys for this creator
	 */
	IBookmarkProperty createFromXML(String type, Element propertyElement);

	/**
	 * Creates and returns a new {@link IBookmarkProperty} from the current
	 * world state.
	 * 
	 * @param type
	 *            The type of property to create
	 * 
	 * @return The {@link IBookmarkProperty} created from the given context
	 */
	IBookmarkProperty createFromCurrentState(String type);
}
