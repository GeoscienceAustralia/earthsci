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
package au.gov.ga.earthsci.worldwind.common.layers.geometry.types;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import au.gov.ga.earthsci.worldwind.common.layers.geometry.GeometryLayer;
import au.gov.ga.earthsci.worldwind.common.layers.geometry.Shape;
import au.gov.ga.earthsci.worldwind.common.layers.geometry.ShapeProvider;
import au.gov.ga.earthsci.worldwind.common.layers.styled.Attribute;
import au.gov.ga.earthsci.worldwind.common.layers.styled.BasicStyleProvider;
import au.gov.ga.earthsci.worldwind.common.layers.styled.Style;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StyleProvider;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * A base class for {@link GeometryLayer} implementations.
 * <p/>
 * Provides convenience methods to aid in implementation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class GeometryLayerBase extends AbstractLayer implements GeometryLayer
{
	private AVList avList = new AVListImpl();
	private final URL shapeSourceUrl;
	private final String dataCacheName;
	private final ShapeProvider shapeProvider;
	private final StyleProvider styleProvider;

	@SuppressWarnings("unchecked")
	public GeometryLayerBase(AVList params)
	{
		try
		{
			URL shapeSourceContext = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
			String url = params.getStringValue(AVKey.URL);
			shapeSourceUrl = new URL(shapeSourceContext, url);
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("Unable to parse shape source URL", e);
		}

		dataCacheName = params.getStringValue(AVKey.DATA_CACHE_NAME);

		shapeProvider = (ShapeProvider) params.getValue(AVKeyMore.DATA_LAYER_PROVIDER);

		styleProvider = new BasicStyleProvider();
		styleProvider.setStyles((List<Style>) params.getValue(AVKeyMore.DATA_LAYER_STYLES));
		styleProvider.setAttributes((List<Attribute>) params.getValue(AVKeyMore.DATA_LAYER_ATTRIBUTES));
		
		Validate.notBlank(dataCacheName, "Shape data cache name not set");

		Validate.notNull(shapeProvider, "Shape data provider is null");
		Validate.notNull(styleProvider.getStyles(), "Shape style list is null");
		Validate.notNull(styleProvider.getAttributes(), "Shape attribute list is null");

		setValues(params);
	}

	@Override
	public Object setValue(String key, Object value)
	{
		return avList.setValue(key, value);
	}

	@Override
	public AVList setValues(AVList avList)
	{
		return this.avList.setValues(avList);
	}

	@Override
	public Object getValue(String key)
	{
		return avList.getValue(key);
	}

	@Override
	public Collection<Object> getValues()
	{
		return avList.getValues();
	}

	@Override
	public String getStringValue(String key)
	{
		return avList.getStringValue(key);
	}

	@Override
	public Set<Entry<String, Object>> getEntries()
	{
		return avList.getEntries();
	}

	@Override
	public boolean hasKey(String key)
	{
		return avList.hasKey(key);
	}

	@Override
	public Object removeKey(String key)
	{
		return avList.removeKey(key);
	}

	@Override
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		avList.addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		avList.removePropertyChangeListener(propertyName, listener);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		avList.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		avList.removePropertyChangeListener(listener);
	}

	@Override
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		avList.firePropertyChange(propertyName, oldValue, newValue);
	}

	@Override
	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
	{
		avList.firePropertyChange(propertyChangeEvent);
	}

	@Override
	public AVList copy()
	{
		return avList.copy();
	}

	@Override
	public AVList clearList()
	{
		return avList.clearList();
	}

	@Override
	public Sector getSector()
	{
		// TODO: Cache the calculated sector
		List<Position> points = new ArrayList<Position>();
		for (Shape shape : getShapes())
		{
			points.addAll(shape.getPoints());
		}
		return Sector.boundingSector(points);
	}

	@Override
	public void setup(WorldWindow wwd)
	{
		// Subclasses may override to perform required setup
	}

	@Override
	public void loadComplete()
	{
		// Subclasses may override to perform required post-load processing
	}

	@Override
	public URL getUrl() throws MalformedURLException
	{
		return shapeSourceUrl;
	}

	@Override
	public String getDataCacheName()
	{
		return dataCacheName;
	}

	@Override
	protected final void doRender(DrawContext dc)
	{
		if (isEnabled())
		{
			getShapeProvider().requestData(this);
		}
		renderGeometry(dc);
	}

	protected ShapeProvider getShapeProvider()
	{
		return shapeProvider;
	}

	protected StyleProvider getStyleProvider()
	{
		return styleProvider;
	}
	
	@Override
	public boolean isLoading()
	{
		return shapeProvider.isLoading();
	}
	
	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		shapeProvider.addLoadingListener(listener);
	}
	
	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		shapeProvider.removeLoadingListener(listener);
	}
}
