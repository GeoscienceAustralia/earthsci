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

import gov.nasa.worldwind.layers.Layer;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import au.gov.ga.earthsci.layer.tree.ILayerNode;

/**
 * Layer draw order constants.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public enum DrawOrder
{
	Pre(-20, Messages.DrawOrder_PreLabel),
	Below(-10, Messages.DrawOrder_BelowLabel),
	Surface(0, Messages.DrawOrder_SurfaceLabel),
	Above(10, Messages.DrawOrder_AboveLabel),
	Post(20, Messages.DrawOrder_PostLabel);

	public final int value;
	public final String label;

	private DrawOrder(int value, String label)
	{
		this.value = value;
		this.label = label;
	}

	public static String getLabel(Integer value)
	{
		if (value != null)
		{
			for (DrawOrder order : DrawOrder.values())
			{
				if (order.value == value)
				{
					return order.label;
				}
			}
		}
		return Messages.DrawOrder_UnknownLabel;
	}

	public static int getDefaultDrawOrder(Class<? extends Layer> layerClass)
	{
		Integer value = ExtensionManager.getInstance().getDrawOrderFor(layerClass);
		if (value != null)
		{
			return value;
		}
		return Surface.value;
	}

	/**
	 * Sort the unsorted list of layers according to their {@link DrawOrder}
	 * value. Layers have a draw order value associated with them if they
	 * implement {@link ILayerNode}. Any non-{@link ILayerNode} layers are
	 * inserted at the end of the list.
	 * 
	 * @param unsorted
	 *            List of layers to sort
	 * @param sorted
	 *            List in which the sorted layers get added to
	 * @see ILayerNode#getDrawOrder()
	 */
	public static void sortLayers(List<Layer> unsorted, List<Layer> sorted)
	{
		//return empty if no layers
		if (unsorted == null)
		{
			return;
		}

		//find a sorted set of all draw order values
		SortedSet<Integer> drawOrders = new TreeSet<Integer>();
		for (Layer layer : unsorted)
		{
			if (layer instanceof ILayerNode)
			{
				drawOrders.add(((ILayerNode) layer).getDrawOrder());
			}
		}

		//for each draw order, add the the layers with that draw order to the sorted list
		for (Integer drawOrder : drawOrders)
		{
			for (Layer layer : unsorted)
			{
				if (layer instanceof ILayerNode)
				{
					if (drawOrder == ((ILayerNode) layer).getDrawOrder())
					{
						sorted.add(layer);
					}
				}
			}
		}

		//add any layers without draw orders to the end to be rendered last
		for (Layer layer : unsorted)
		{
			if (!(layer instanceof ILayerNode))
			{
				sorted.add(layer);
			}
		}
	}
}
