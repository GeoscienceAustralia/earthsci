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
 * An interface for classes that are able to export an IBookmarkProperty to a map
 * of key-value pairs that can then be used by a corresponding {@link IBookmarkPropertyCreator}
 * to re-create the property.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarkPropertyExporter
{

	/**
	 * Returns the property types supported by this creator
	 * 
	 * @return The property types supported by this creator
	 */
	String[] getSupportedTypes();
	
	/**
	 * Export the given property to XML
	 * 
	 * @param property The property to export
	 * @param parent A parent XML element onto which the property XML can be attached
	 */
	void exportToXML(IBookmarkProperty property, Element parent);
}
