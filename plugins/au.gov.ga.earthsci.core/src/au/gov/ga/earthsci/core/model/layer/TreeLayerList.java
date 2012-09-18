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
package au.gov.ga.earthsci.core.model.layer;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;

/**
 * {@link LayerList} subclass which supports a hierarchical tree structure, but
 * still can be used as the {@link LayerList} passed to the World Wind model for
 * rendering.
 * <p/>
 * Each layer added to this list is wrapped in a {@link LayerNode} wrapper.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TreeLayerList extends LayerList
{
	private boolean wrapping = false;

	public TreeLayerList()
	{
		super();
	}

	public TreeLayerList(Layer[] layers)
	{
		super(layers);
		wrapAllLayers();
	}

	public TreeLayerList(LayerList layerList)
	{
		this(layerList, true);
	}

	protected TreeLayerList(LayerList layerList, boolean wrap)
	{
		super(layerList);
		if (wrap)
		{
			wrapAllLayers();
		}
	}

	@Override
	protected LayerList makeShallowCopy(LayerList sourceList)
	{
		return new TreeLayerList(sourceList, false);
	}

	@Override
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		//don't re-fire the change when wrapping the layers
		if (wrapping)
			return;

		//firePropertyChange is called with AVKey.LAYERS whenever the layer list changes
		if (propertyName == AVKey.LAYERS)
		{
			wrapAllLayers();
		}

		super.firePropertyChange(propertyName, oldValue, newValue);
	}

	/**
	 * Wrap all layers in this list that are not instances of {@link LayerNode}.
	 */
	protected void wrapAllLayers()
	{
		wrapping = true;
		for (int i = size() - 1; i >= 0; i--)
		{
			Layer layer = get(i);
			if (!(layer instanceof LayerNode))
			{
				layer = new LayerNode(layer);
				set(i, layer);
			}
		}
		wrapping = false;
	}
}
