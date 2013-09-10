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
package au.gov.ga.earthsci.bookmark.io;

import java.net.URI;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.bookmark.BookmarkPropertyFactory;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyCreator;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyExporter;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.common.persistence.IPersistentAdapter;

/**
 * An {@link IPersistentAdapter} that uses registered
 * {@link IBookmarkPropertyCreator}s and {@link IBookmarkPropertyExporter}s to
 * store/load {@link IBookmarkProperty}s to/from XML.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarkPropertyPersistentAdapter implements IPersistentAdapter<IBookmarkProperty>
{
	public static final String PROPERTY_TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$

	@Override
	public void toXML(IBookmarkProperty property, Element propertyElement, URI context)
	{
		propertyElement.setAttribute(PROPERTY_TYPE_ATTRIBUTE, property.getType());
		BookmarkPropertyFactory.exportProperty(property, propertyElement);
	}

	@Override
	public IBookmarkProperty fromXML(Element root, URI context)
	{
		return BookmarkPropertyFactory.createProperty(root.getAttribute(PROPERTY_TYPE_ATTRIBUTE), root);
	}

}
