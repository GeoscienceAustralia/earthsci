/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
 * Represents a Heads-Up-Display layer, defined by a hud extension point.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HudLayer
{
	private final String id;
	private final Class<? extends Layer> layerClass;
	private final String iconURI;
	private final String label;
	private final boolean enabled;

	public HudLayer(String id, Class<? extends Layer> layerClass, String iconURI, String label, boolean enabled)
	{
		this.id = id;
		this.layerClass = layerClass;
		this.iconURI = iconURI;
		this.label = label;
		this.enabled = enabled;
	}

	public String getId()
	{
		return id;
	}

	public Class<? extends Layer> getLayerClass()
	{
		return layerClass;
	}

	public String getIconURI()
	{
		return iconURI;
	}

	public String getLabel()
	{
		return label;
	}

	public boolean isEnabled()
	{
		return enabled;
	}
}
