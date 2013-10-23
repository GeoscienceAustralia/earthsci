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
package au.gov.ga.earthsci.layer.hud;

import gov.nasa.worldwind.layers.Layer;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;

import au.gov.ga.earthsci.common.util.ExtensionPointHelper;

/**
 * Static accessor for the list of defined {@link HudLayer} extension points.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HudLayers
{
	public static final String HUD_EXTENSION_POINT_ID = "au.gov.ga.earthsci.layer.hudLayers"; //$NON-NLS-1$

	private static HudLayer[] layers;
	private static final Set<Class<?>> layerClasses = new HashSet<Class<?>>();

	public static HudLayer[] get()
	{
		if (layers == null)
		{
			layers = create();
			for (HudLayer layer : layers)
			{
				layerClasses.add(layer.getLayerClass());
			}
		}
		return layers;
	}

	protected static HudLayer[] create()
	{
		List<HudLayer> layers = new ArrayList<HudLayer>();

		IConfigurationElement[] config =
				RegistryFactory.getRegistry().getConfigurationElementsFor(HUD_EXTENSION_POINT_ID);
		for (IConfigurationElement element : config)
		{
			try
			{
				String id = element.getAttribute("id"); //$NON-NLS-1$
				@SuppressWarnings("unchecked")
				Class<? extends Layer> layerClass =
						(Class<? extends Layer>) ExtensionPointHelper.getClassForProperty(element, "class"); //$NON-NLS-1$
				URI iconURI = ExtensionPointHelper.getResourceURIForProperty(element, "icon"); //$NON-NLS-1$
				String iconURIString = iconURI == null ? null : iconURI.toString();
				String label = element.getAttribute("label"); //$NON-NLS-1$
				boolean enabled = Boolean.parseBoolean(element.getAttribute("enabled")); //$NON-NLS-1$
				layers.add(new HudLayer(id, layerClass, iconURIString, label, enabled));
			}
			catch (Exception e)
			{
			}
		}

		return layers.toArray(new HudLayer[layers.size()]);
	}

	public static boolean containsLayerClass(Class<?> c)
	{
		get(); //ensure the list is created
		return layerClasses.contains(c);
	}
}
