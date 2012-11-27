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
package au.gov.ga.earthsci.core.bookmark.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The default {@link IBookmark} implementation
 *
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Bookmark implements IBookmark
{

	private String name;
	private IBookmarkMetadata metadata = new BookmarkMetadata();
	private Map<String, IBookmarkProperty> properties = new HashMap<String, IBookmarkProperty>();
	
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public IBookmarkMetadata getMetadata()
	{
		return metadata;
	}

	@Override
	public Collection<IBookmarkProperty> getProperties()
	{
		return properties.values();
	}

	@Override
	public IBookmarkProperty getProperty(String type)
	{
		return properties.get(type);
	}
	
	@Override
	public void addProperty(IBookmarkProperty property)
	{
		if (property == null)
		{
			return;
		}
		this.properties.put(property.getType(), property);
	}

	@Override
	public boolean hasProperty(String type)
	{
		return properties.containsKey(type);
	}

}
