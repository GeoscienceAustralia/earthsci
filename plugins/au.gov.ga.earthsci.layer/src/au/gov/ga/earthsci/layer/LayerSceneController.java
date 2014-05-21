/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.layer;

import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.DrawContext;

import java.util.SortedSet;
import java.util.TreeSet;

import au.gov.ga.earthsci.layer.tree.ILayerNode;
import au.gov.ga.earthsci.worldwind.common.render.DrawContextDelegate;
import au.gov.ga.earthsci.worldwind.common.render.ExtendedSceneController;

/**
 * {@link SceneController} that supports {@link ILayerNode} draw order.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerSceneController extends ExtendedSceneController
{
	protected final LayerList sortedLayers = new LayerList();

	@Override
	public void doRepaint(DrawContext dc)
	{
		sortLayers(dc);
		super.doRepaint(dc);
	}

	protected void sortLayers(DrawContext dc)
	{
		//clear the sorted list
		sortedLayers.clear();
		LayerList layers = dc.getLayers();
		if (layers == null)
		{
			return;
		}

		//find a sorted set of all draw order values
		SortedSet<Integer> drawOrders = new TreeSet<Integer>();
		for (Layer layer : layers)
		{
			if (layer instanceof ILayerNode)
			{
				drawOrders.add(((ILayerNode) layer).getDrawOrder());
			}
		}

		//for each draw order, add the the layers with that draw order to the sorted list
		for (Integer drawOrder : drawOrders)
		{
			for (Layer layer : layers)
			{
				if (layer instanceof ILayerNode)
				{
					if (drawOrder == ((ILayerNode) layer).getDrawOrder())
					{
						sortedLayers.add(layer);
					}
				}
			}
		}

		//add any layers without draw orders to the end to be rendered last
		for (Layer layer : layers)
		{
			if (!(layer instanceof ILayerNode))
			{
				sortedLayers.add(layer);
			}
		}
	}

	@Override
	protected void pick(DrawContext dc)
	{
		super.pick(new DrawContextDelegate(dc)
		{
			@Override
			public LayerList getLayers()
			{
				return sortedLayers;
			}
		});
	}

	@Override
	protected void preRender(DrawContext dc)
	{
		super.preRender(new DrawContextDelegate(dc)
		{
			@Override
			public LayerList getLayers()
			{
				return sortedLayers;
			}
		});
	}

	@Override
	public void draw(DrawContext dc)
	{
		super.draw(new DrawContextDelegate(dc)
		{
			@Override
			public LayerList getLayers()
			{
				return sortedLayers;
			}
		});
	}
}
