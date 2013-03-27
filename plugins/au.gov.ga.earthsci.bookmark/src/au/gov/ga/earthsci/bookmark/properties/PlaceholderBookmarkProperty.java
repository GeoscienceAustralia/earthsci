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

import au.gov.ga.earthsci.bookmark.IBookmarkPropertyCreator;
import au.gov.ga.earthsci.bookmark.io.BookmarkPropertyPersistentAdapter;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;

/**
 * A special placeholder property which serves to hold the XML representation of
 * an {@link IBookmarkProperty} in the case where no
 * {@link IBookmarkPropertyCreator} has been registered for the property type.
 * <p/>
 * This is used in the edge case where bookmarks have been persisted with a
 * plugin loaded that provides additional bookmark properties, then on next load
 * that plugin is no longer available. It serves to ensure that the property
 * information is not lost between invocations of the application.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class PlaceholderBookmarkProperty implements IBookmarkProperty
{
	public static final String TYPE = "au.gov.ga.earthsci.bookmark.properties.placeholder"; //$NON-NLS-1$

	private Element propertyXml;

	public PlaceholderBookmarkProperty(Element xml)
	{
		this.propertyXml = xml;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public String getName()
	{
		// Will (should) never be displayed to the user
		return "Placeholder"; //$NON-NLS-1$
	}

	/**
	 * Return the type of the wrapped property this placeholder is representing
	 */
	public String getWrappedType()
	{
		return propertyXml.getAttribute(BookmarkPropertyPersistentAdapter.PROPERTY_TYPE_ATTRIBUTE);
	}

	/**
	 * Return the XML structure for the property this placeholder is
	 * representing
	 */
	public Element getXML()
	{
		return propertyXml;
	}

}
