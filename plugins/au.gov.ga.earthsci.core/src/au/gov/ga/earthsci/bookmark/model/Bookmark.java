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
package au.gov.ga.earthsci.bookmark.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import au.gov.ga.earthsci.bookmark.Messages;
import au.gov.ga.earthsci.bookmark.io.BookmarkPropertyPersistentAdapter;
import au.gov.ga.earthsci.core.persistence.Adapter;
import au.gov.ga.earthsci.core.persistence.Exportable;
import au.gov.ga.earthsci.core.persistence.Persistent;
import au.gov.ga.earthsci.core.util.AbstractPropertyChangeBean;

/**
 * The default {@link IBookmark} implementation
 *
 * @author James Navin (james.navin@ga.gov.au)
 */
@Exportable
public class Bookmark extends AbstractPropertyChangeBean implements IBookmark
{

	private String name;
	private IBookmarkMetadata metadata = new BookmarkMetadata();
	
	private Long transitionDuration = null;
	
	private Map<String, IBookmarkProperty> properties = new ConcurrentHashMap<String, IBookmarkProperty>();
	
	public Bookmark()
	{
		this.name = Messages.Bookmark_DefaultBookmarkName;
	}
	
	@Override
	@Persistent
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		firePropertyChange("name", this.name, this.name = name); //$NON-NLS-1$
	}
	
	@Override
	public IBookmarkMetadata getMetadata()
	{
		return metadata;
	}

	@Persistent(elementName="property")
	@Override
	@Adapter(BookmarkPropertyPersistentAdapter.class)
	public IBookmarkProperty[] getProperties()
	{
		return properties.values().toArray(new IBookmarkProperty[properties.size()]);
	}

	public void setProperties(IBookmarkProperty[] properties)
	{
		IBookmarkProperty[] oldProperties = getProperties();
		
		this.properties.clear();
		for (IBookmarkProperty property : properties)
		{
			doAddProperty(property, false);
		}
		
		firePropertyChange("properties", oldProperties, getProperties()); //$NON-NLS-1$
	}
	
	@Override
	public IBookmarkProperty getProperty(String type)
	{
		return properties.get(type);
	}
	
	@Override
	public void addProperty(IBookmarkProperty property)
	{
		doAddProperty(property, true);
	}

	private void doAddProperty(IBookmarkProperty property, boolean fireEvent)
	{
		if (property == null)
		{
			return;
		}
		
		IBookmarkProperty[] oldProperties = null;
		if (fireEvent)
		{
			oldProperties = getProperties();
		}
		
		this.properties.put(property.getType(), property);
		
		if (fireEvent)
		{
			firePropertyChange("properties", oldProperties, getProperties()); //$NON-NLS-1$
		}
	}
	
	@Override
	public boolean hasProperty(String type)
	{
		return properties.containsKey(type);
	}

	@Persistent
	@Override
	public Long getTransitionDuration()
	{
		return transitionDuration;
	}

	public void setTransitionDuration(Long duration)
	{
		firePropertyChange("transitionDuration", this.transitionDuration, this.transitionDuration = duration); //$NON-NLS-1$
	}
	
}
