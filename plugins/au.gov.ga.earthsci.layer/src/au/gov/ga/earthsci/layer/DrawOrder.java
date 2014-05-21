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

	public static String getLabel(int value)
	{
		for (DrawOrder order : DrawOrder.values())
		{
			if (order.value == value)
			{
				return order.label;
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
}
