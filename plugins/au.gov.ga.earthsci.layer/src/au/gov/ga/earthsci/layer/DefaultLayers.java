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
package au.gov.ga.earthsci.layer;

import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.WorldMapLayer;

import java.net.URI;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.layer.hud.HudLayers;
import au.gov.ga.earthsci.layer.tree.FolderNode;
import au.gov.ga.earthsci.layer.tree.LayerNode;

/**
 * Helper class which reads the "LayerList" element from the World Wind
 * {@link Configuration}, and then generates {@link URI}s for each layer
 * specified.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DefaultLayers
{
	/**
	 * @return {@link FolderNode} containing the {@link LayerNode}s representing
	 *         the default layers defined in the WW configuration
	 */
	public static FolderNode getLayers()
	{
		FolderNode folder = new FolderNode();
		folder.setName(Messages.DefaultLayers_DefaultLabel);

		Element el = Configuration.getElement("./LayerList"); //$NON-NLS-1$
		LayerList layers = createLayersFromElement(el);

		String elevationModelConfigSource =
				Configuration.getStringValue(AVKey.EARTH_ELEVATION_MODEL_CONFIG_FILE,
						"config/Earth/EarthElevationModelAsBil16.xml"); //$NON-NLS-1$
		Object elevationModelLayer = BasicFactory.create(AVKey.LAYER_FACTORY, elevationModelConfigSource);
		if (elevationModelLayer instanceof Layer)
		{
			layers.add(0, (Layer) elevationModelLayer);
		}

		for (Layer layer : layers)
		{
			if (HudLayers.containsLayerClass(layer.getClass()))
			{
				continue;
			}
			if (layer.getClass().equals(WorldMapLayer.class))
			{
				continue;
			}

			LayerNode layerNode = new LayerNode();
			IPersistentLayer persistentLayer = LegacyLayerHelper.wrap(layer);
			layerNode.setLayer(persistentLayer);
			folder.addChild(layerNode);
		}

		return folder;
	}

	/**
	 * Create the layer list from an XML configuration element.
	 * 
	 * @param element
	 *            the configuration description.
	 * 
	 * @return a new layer list matching the specified description.
	 */
	protected static LayerList createLayersFromElement(Element element)
	{
		Object o = BasicFactory.create(AVKey.LAYER_FACTORY, element);

		if (o instanceof LayerList)
		{
			return (LayerList) o;
		}

		if (o instanceof Layer)
		{
			return new LayerList(new Layer[] { (Layer) o });
		}

		if (o instanceof LayerList[])
		{
			LayerList[] lists = (LayerList[]) o;
			if (lists.length > 0)
			{
				return LayerList.collapseLists((LayerList[]) o);
			}
		}

		return null;
	}
}
