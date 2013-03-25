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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import au.gov.ga.earthsci.bookmark.Messages;
import au.gov.ga.earthsci.bookmark.io.BookmarkPropertyPersistentAdapter;
import au.gov.ga.earthsci.common.util.AbstractPropertyChangeBean;
import au.gov.ga.earthsci.core.persistence.Adapter;
import au.gov.ga.earthsci.core.persistence.Exportable;
import au.gov.ga.earthsci.core.persistence.Persistent;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * The default {@link IBookmark} implementation
 *
 * @author James Navin (james.navin@ga.gov.au)
 */
@Exportable
public class Bookmark extends AbstractPropertyChangeBean implements IBookmark
{

	@Persistent
	private String id;
	
	private String name;
	private IBookmarkMetadata metadata = new BookmarkMetadata();
	
	private Long transitionDuration = null;
	
	private Map<String, IBookmarkProperty> properties = new ConcurrentHashMap<String, IBookmarkProperty>();
	
	public Bookmark()
	{
		this.name = Messages.Bookmark_DefaultBookmarkName;
		this.id = UUID.randomUUID().toString();
	}
	
	/**
	 * Copy constructor.
	 * <p/>
	 * Performs a shallow copy, with a new ID
	 * 
	 * @param other The bookmark to copy
	 */
	public Bookmark(IBookmark other)
	{
		this.id = UUID.randomUUID().toString();
		this.name = other.getName();
		this.transitionDuration = other.getTransitionDuration();
		for (IBookmarkProperty p : other.getProperties())
		{
			doAddProperty(p, false);
		}
		this.metadata = other.getMetadata();
	}
	
	@Override
	public String getId()
	{
		return id;
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
	public boolean removeProperty(IBookmarkProperty p)
	{
		if (p == null)
		{
			return false;
		}
		
		return removeProperty(p.getType()) != null;
	}
	
	@Override
	public IBookmarkProperty removeProperty(String type)
	{
		if (Util.isBlank(type))
		{
			return null;
		}
		
		IBookmarkProperty[] oldProperties = getProperties();
		
		IBookmarkProperty p = this.properties.remove(type);
		
		if (p != null)
		{
			firePropertyChange("properties", oldProperties, getProperties()); //$NON-NLS-1$
		}
		
		return p;
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
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof IBookmark))
		{
			return false;
		}
		
		IBookmark other = (IBookmark)obj;
		
		return id.equals(other.getId());
	}
	
	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
	
	@Override
	public String toString()
	{
		return "Bookmark{" + id + ", " + name + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
