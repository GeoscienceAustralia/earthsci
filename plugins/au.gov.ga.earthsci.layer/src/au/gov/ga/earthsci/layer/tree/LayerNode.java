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
package au.gov.ga.earthsci.layer.tree;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.e4.core.contexts.IEclipseContext;

import au.gov.ga.earthsci.common.persistence.Adapter;
import au.gov.ga.earthsci.common.persistence.Persistent;
import au.gov.ga.earthsci.common.util.IEnableable;
import au.gov.ga.earthsci.common.util.IInformationed;
import au.gov.ga.earthsci.common.util.ILoader;
import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.layer.ExtensionManager;
import au.gov.ga.earthsci.layer.IElevationModelLayer;
import au.gov.ga.earthsci.layer.ILayer;
import au.gov.ga.earthsci.layer.ILayerDelegate;
import au.gov.ga.earthsci.layer.ILayerWrapper;
import au.gov.ga.earthsci.layer.LayerDelegate;
import au.gov.ga.earthsci.layer.LayerPersistentAdapter;
import au.gov.ga.earthsci.layer.intent.IntentLayerLoader;
import au.gov.ga.earthsci.layer.wrappers.LayerWrapper;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * Layer tree node implementation for layers. Implements the {@link Layer}
 * interface, and delegates all layer methods to a {@link LayerDelegate} object.
 * <p/>
 * Also fires a property change in all setter methods to comply with the Java
 * Bean specification.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerNode extends AbstractLayerTreeNode implements ILayerDelegate, IEnableable, ILoader
{
	protected final LayerDelegate delegate = new LayerDelegate();
	private boolean nameSet = false;
	private boolean loading = false;

	public LayerNode()
	{
		//propagate property changes from the delegate to the listeners of the node
		delegate.addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				firePropertyChange(new PropertyChangeEvent(LayerNode.this, evt.getPropertyName(), evt.getOldValue(),
						evt.getNewValue()));
			}
		});
	}

	@Override
	public boolean isLayerSet()
	{
		return delegate.isLayerSet();
	}

	@Override
	public void setURI(URI uri)
	{
		super.setURI(uri);
		if (!nameSet && !isLayerSet())
		{
			delegate.setName(uri.toString());
		}
	}

	@Persistent(name = "implementation")
	@Adapter(LayerPersistentAdapter.class)
	public ILayer getLayerImplementation()
	{
		if (delegate.getLayer() instanceof ILayer)
		{
			return (ILayer) delegate.getLayer();
		}
		return null;
	}

	//for unpersistence
	@SuppressWarnings("unused")
	private void setLayerImplementation(ILayer layer)
	{
		setLayer(layer);
	}

	/**
	 * @return The {@link Layer} that this node delegates to.
	 */
	@Override
	public Layer getLayer()
	{
		//TODO is this robust enough?
		if (delegate.getLayer() instanceof LayerWrapper)
		{
			return ((LayerWrapper) delegate.getLayer()).getLayer();
		}

		return delegate.getLayer();
	}

	/**
	 * Set the {@link Layer} that this node delegates to.
	 * 
	 * @param layer
	 */
	@Override
	public void setLayer(Layer layer)
	{
		boolean isElevationModel = layer instanceof IElevationModelLayer;

		if (!(layer instanceof ILayer))
		{
			//if the loaded layer is not an ILayer, then wrap it in the legacy wrapper
			ILayerWrapper wrapper = ExtensionManager.getInstance().wrapLayer(layer);
			if (wrapper != null)
			{
				layer = wrapper;
			}
		}

		//set the values from the layer on this node
		setName(layer.getName());

		//get the legend url if it exists (set by the LayerFactory)
		URL legendURL = (URL) layer.getValue(AVKeyMore.LEGEND_URL);
		if (legendURL == null)
		{
			AVList constructionParameters = (AVList) layer.getValue(AVKey.CONSTRUCTION_PARAMETERS);
			if (constructionParameters != null)
			{
				legendURL = (URL) constructionParameters.getValue(AVKeyMore.LEGEND_URL);
			}
		}
		if (legendURL != null)
		{
			setLegendURL(legendURL);
		}

		//set the actual layer (this will copy any changed properties on the old layer to the new layer)
		this.delegate.setLayer(layer);

		//TODO rethink this:
		//we need to update the root's elevation models if this layer is an elevation model layer
		if (isElevationModel)
		{
			childrenChanged(getChildren(), getChildren());
		}
	}

	@Override
	public URL getInformationURL()
	{
		//if the layer is IInformationed, use that instead
		if (delegate.getLayer() instanceof IInformationed)
		{
			URL url = ((IInformationed) delegate.getLayer()).getInformationURL();
			if (url != null)
			{
				return url;
			}
		}
		return super.getInformationURL();
	}

	@Override
	public String getInformationString()
	{
		//if the layer is IInformationed, use that instead
		if (delegate.getLayer() instanceof IInformationed)
		{
			String information = ((IInformationed) delegate.getLayer()).getInformationString();
			if (!Util.isEmpty(information))
			{
				return information;
			}
		}
		return super.getInformationString();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		firePropertyChange(evt);
	}

	public void loadLayer(IEclipseContext context)
	{
		IntentLayerLoader.load(this, context);
	}

	@Override
	public boolean isLoading()
	{
		if (loading)
		{
			return true;
		}
		if (getLayer() instanceof ILoader)
		{
			return ((ILoader) getLayer()).isLoading();
		}
		return false;
	}

	public void setLoading(boolean loading)
	{
		firePropertyChange("loading", this.loading, this.loading = loading); //$NON-NLS-1$
	}

	//////////////////////
	// Layer delegation //
	//////////////////////

	@Override
	public void dispose()
	{
		delegate.dispose();
	}

	@Override
	public void onMessage(Message msg)
	{
		delegate.onMessage(msg);
	}

	@Override
	public Object setValue(String key, Object value)
	{
		return delegate.setValue(key, value);
	}

	@Persistent(attribute = true)
	@Override
	public boolean isEnabled()
	{
		return delegate.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		boolean oldValue = isEnabled();
		delegate.setEnabled(enabled);
		firePropertyChange("enabled", oldValue, enabled); //$NON-NLS-1$
	}

	@Override
	public String getName()
	{
		return delegate.getName();
	}

	@Override
	public void setName(String name)
	{
		String oldValue = getName();
		delegate.setName(name);
		firePropertyChange("name", oldValue, name); //$NON-NLS-1$
		nameSet = true;
	}

	@Override
	public AVList setValues(AVList avList)
	{
		return delegate.setValues(avList);
	}

	@Override
	public String getRestorableState()
	{
		return delegate.getRestorableState();
	}

	@Persistent(attribute = true)
	@Override
	public double getOpacity()
	{
		return delegate.getOpacity();
	}

	@Override
	public void restoreState(String stateInXml)
	{
		delegate.restoreState(stateInXml);
	}

	@Override
	public Object getValue(String key)
	{
		return delegate.getValue(key);
	}

	@Override
	public void setOpacity(double opacity)
	{
		double oldValue = getOpacity();
		delegate.setOpacity(opacity);
		firePropertyChange("opacity", oldValue, opacity); //$NON-NLS-1$
	}

	@Override
	public Collection<Object> getValues()
	{
		return delegate.getValues();
	}

	@Override
	public String getStringValue(String key)
	{
		return delegate.getStringValue(key);
	}

	@Override
	public boolean isPickEnabled()
	{
		return delegate.isPickEnabled();
	}

	@Override
	public Set<Entry<String, Object>> getEntries()
	{
		return delegate.getEntries();
	}

	@Override
	public boolean hasKey(String key)
	{
		return delegate.hasKey(key);
	}

	@Override
	public Object removeKey(String key)
	{
		return delegate.removeKey(key);
	}

	@Override
	public void setPickEnabled(boolean isPickable)
	{
		boolean oldValue = isPickEnabled();
		delegate.setPickEnabled(isPickable);
		firePropertyChange("pickEnabled", oldValue, isPickable); //$NON-NLS-1$
	}

	@Override
	public void preRender(DrawContext dc)
	{
		delegate.preRender(dc);
	}

	@Override
	public void render(DrawContext dc)
	{
		delegate.render(dc);
	}

	@Override
	public void pick(DrawContext dc, Point pickPoint)
	{
		delegate.pick(dc, pickPoint);
	}

	@Override
	public boolean isAtMaxResolution()
	{
		return delegate.isAtMaxResolution();
	}

	@Override
	public boolean isMultiResolution()
	{
		return delegate.isMultiResolution();
	}

	@Override
	public double getScale()
	{
		return delegate.getScale();
	}

	@Override
	public boolean isNetworkRetrievalEnabled()
	{
		return delegate.isNetworkRetrievalEnabled();
	}

	@Override
	public void setNetworkRetrievalEnabled(boolean networkRetrievalEnabled)
	{
		boolean oldValue = isNetworkRetrievalEnabled();
		delegate.setNetworkRetrievalEnabled(networkRetrievalEnabled);
		firePropertyChange("networkRetrievalEnabled", oldValue, networkRetrievalEnabled); //$NON-NLS-1$
	}

	@Override
	public AVList copy()
	{
		return delegate.copy();
	}

	@Override
	public void setExpiryTime(long expiryTime)
	{
		long oldValue = getExpiryTime();
		delegate.setExpiryTime(expiryTime);
		firePropertyChange("expiryTime", oldValue, expiryTime); //$NON-NLS-1$
	}

	@Override
	public AVList clearList()
	{
		return delegate.clearList();
	}

	@Override
	public long getExpiryTime()
	{
		return delegate.getExpiryTime();
	}

	@Override
	public double getMinActiveAltitude()
	{
		return delegate.getMinActiveAltitude();
	}

	@Override
	public void setMinActiveAltitude(double minActiveAltitude)
	{
		double oldValue = getMinActiveAltitude();
		delegate.setMinActiveAltitude(minActiveAltitude);
		firePropertyChange("minActiveAltitude", oldValue, minActiveAltitude); //$NON-NLS-1$
	}

	@Override
	public double getMaxActiveAltitude()
	{
		return delegate.getMaxActiveAltitude();
	}

	@Override
	public void setMaxActiveAltitude(double maxActiveAltitude)
	{
		double oldValue = getMaxActiveAltitude();
		delegate.setMaxActiveAltitude(maxActiveAltitude);
		firePropertyChange("maxActiveAltitude", oldValue, maxActiveAltitude); //$NON-NLS-1$
	}

	@Override
	public boolean isLayerInView(DrawContext dc)
	{
		return delegate.isLayerInView(dc);
	}

	@Override
	public boolean isLayerActive(DrawContext dc)
	{
		return delegate.isLayerActive(dc);
	}

	@Override
	public Double getMaxEffectiveAltitude(Double radius)
	{
		return delegate.getMaxEffectiveAltitude(radius);
	}

	@Override
	public Double getMinEffectiveAltitude(Double radius)
	{
		return delegate.getMinEffectiveAltitude(radius);
	}
}
