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
package au.gov.ga.earthsci.core.bookmark;

import au.gov.ga.earthsci.core.bookmark.model.Bookmark;
import au.gov.ga.earthsci.core.bookmark.model.IBookmark;
import au.gov.ga.earthsci.core.bookmark.model.IBookmarkProperty;

/**
 * A factory class for creating {@link IBookmark} instances
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarkFactory
{

	/**
	 * Create a new {@link IBookmark} instance from the current world state containing the provided property types.
	 * <p/>
	 * If no types are provided, a bookmark instance will be created with all known property types.
	 *  
	 * @param propertyTypes The types of properties to include in the new bookmark
	 * 
	 * @return The newly created bookmark
	 */
	public static IBookmark createBookmark(String... propertyTypes)
	{
		if (propertyTypes == null || propertyTypes.length == 0)
		{
			propertyTypes = BookmarkPropertyFactory.getKnownPropertyTypes();
		}
		
		Bookmark result = new Bookmark();
		for (String propertyType : propertyTypes)
		{
			IBookmarkProperty p = BookmarkPropertyFactory.createProperty(propertyType);
			if (p != null)
			{
				result.addProperty(p);
			}
		}
		return result;
	}
	
}
