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
package au.gov.ga.earthsci.bookmark.properties;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import au.gov.ga.earthsci.bookmark.IBookmarkPropertyExporter;
import au.gov.ga.earthsci.bookmark.io.BookmarkPropertyPersistentAdapter;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;

/**
 * An exporter for the {@link PlaceholderBookmarkProperty}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class PlaceholderBookmarkPropertyExporter implements IBookmarkPropertyExporter
{

	@Override
	public String[] getSupportedTypes()
	{
		return new String[] {PlaceholderBookmarkProperty.TYPE};
	}

	@Override
	public void exportToXML(IBookmarkProperty property, Element parent)
	{
		PlaceholderBookmarkProperty placeholder = (PlaceholderBookmarkProperty)property;
		parent.setAttribute(BookmarkPropertyPersistentAdapter.PROPERTY_TYPE_ATTRIBUTE, placeholder.getWrappedType()); 
		for (int i = 0; i < placeholder.getXML().getChildNodes().getLength(); i++)
		{
			Node newChild = placeholder.getXML().getChildNodes().item(i).cloneNode(true);
			parent.appendChild(parent.getOwnerDocument().adoptNode(newChild));
		}
		return;
	}

}
