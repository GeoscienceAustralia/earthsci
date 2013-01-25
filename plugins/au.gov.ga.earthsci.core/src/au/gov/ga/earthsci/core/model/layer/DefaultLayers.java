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
package au.gov.ga.earthsci.core.model.layer.uri;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.WWXML;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.core.util.Util;

/**
 * Helper class which reads the "LayerList" element from the World Wind
 * {@link Configuration}, and then generates {@link URI}s for each layer
 * specified.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DefaultLayers
{
	private final static Logger logger = LoggerFactory.getLogger(DefaultLayers.class);

	/**
	 * @return {@link FolderNode} containing the {@link LayerNode}s representing
	 *         the default layers defined in the WW configuration
	 */
	public static FolderNode getLayers()
	{
		FolderNode folder = new FolderNode();
		folder.setName("Default");
		LayerNode[] nodes = getLayerNodes();
		for (LayerNode node : nodes)
		{
			folder.add(node);
		}
		return folder;
	}

	/**
	 * Generate an array of {@link LayerNode}s containing the default layers
	 * defined in the WW configuration.
	 * 
	 * @return Array of LayerNodes that represent the default layers defined in
	 *         the WW configuration
	 */
	public static LayerNode[] getLayerNodes()
	{
		List<LayerNode> nodes = new ArrayList<LayerNode>();

		//first add the elevation model
		String elevationModelResource =
				Configuration.getStringValue(AVKey.EARTH_ELEVATION_MODEL_CONFIG_FILE,
						"config/Earth/EarthElevationModelAsBil16.xml"); //$NON-NLS-1$
		String elevationModelURIString = "classpath://" + elevationModelResource; //$NON-NLS-1$
		try
		{
			LayerNode node = new LayerNode();
			node.setURI(new URI(elevationModelURIString));
			nodes.add(node);
		}
		catch (URISyntaxException e)
		{
			logger.error("Error creating layer URI from string: " + elevationModelURIString, e); //$NON-NLS-1$
		}

		//now add the default layers
		Element element = Configuration.getElement("./LayerList"); //$NON-NLS-1$
		LayerList layers = createLayersFromElement(element);
		for (Layer layer : layers)
		{
			if (layer instanceof LayerNode)
			{
				nodes.add((LayerNode) layer);
			}
		}

		return nodes.toArray(new LayerNode[nodes.size()]);
	}

	/**
	 * Create a LayerList of {@link URILayer}s from the given xml element.
	 * 
	 * @param element
	 *            Element to create LayerList from
	 * @return LayerList containing URILayers specified in the xml element
	 */
	protected static LayerList createLayersFromElement(Element element)
	{
		LayerNodeFactory factory = new LayerNodeFactory();
		Object o = factory.createFromConfigSource(element, null);

		if (o instanceof LayerList)
			return (LayerList) o;

		if (o instanceof Layer)
			return new LayerList(new Layer[] { (Layer) o });

		if (o instanceof LayerList[])
		{
			LayerList[] lists = (LayerList[]) o;
			if (lists.length > 0)
				return LayerList.collapseLists((LayerList[]) o);
		}

		return null;
	}

	/**
	 * {@link BasicLayerFactory} subclass which overrides the standard Layer
	 * creation by creating {@link LayerNode}s.
	 */
	protected static class LayerNodeFactory extends BasicLayerFactory
	{
		//adapted from superclass, but doesn't actually create any real layers, only URILayers
		@Override
		protected Layer createFromLayerDocument(Element domElement, AVList params)
		{
			String href = WWXML.getText(domElement, "@href"); //$NON-NLS-1$
			String className = WWXML.getText(domElement, "@className"); //$NON-NLS-1$
			boolean classNameSpecified = !Util.isEmpty(className);

			String actuate = WWXML.getText(domElement, "@actuate"); //$NON-NLS-1$
			boolean enabled = actuate != null && actuate.equals("onLoad"); //$NON-NLS-1$
			//for some reason, in BasicLayerFactory, actuate behaves slightly differently for className layers vs href layers:
			if (classNameSpecified)
			{
				enabled = WWUtil.isEmpty(actuate) || actuate.equals("onLoad"); //$NON-NLS-1$
			}

			String uriString = null;
			if (classNameSpecified)
			{
				uriString = "class://" + className; //$NON-NLS-1$
			}
			else if (!Util.isEmpty(href))
			{
				uriString = "classpath://" + href; //$NON-NLS-1$
			}

			if (uriString == null)
			{
				//cannot create a URI for the layer element, return a dummy layer
				return new InvalidLayer();
			}

			URI uri;
			try
			{
				uri = new URI(uriString);
			}
			catch (URISyntaxException e)
			{
				logger.error("Error creating layer URI from string: " + uriString, e); //$NON-NLS-1$
				return null;
			}

			LayerNode node = new LayerNode();
			node.setEnabled(enabled);
			node.setURI(uri);

			Map<String, String> properties = createPropertyMap(domElement);
			for (Entry<String, String> entry : properties.entrySet())
			{
				try
				{
					Util.setPropertyOn(node, entry.getKey(), entry.getValue());
				}
				catch (InvocationTargetException e)
				{
					logger.warn("Error setting property on layer: " + entry.getKey(), e); //$NON-NLS-1$
				}
			}

			return node;
		}

		protected Map<String, String> createPropertyMap(Element domElement)
		{
			Map<String, String> map = new HashMap<String, String>();

			Element[] elements = WWXML.getElements(domElement, "Property", null); //$NON-NLS-1$
			if (elements != null)
			{
				for (Element element : elements)
				{
					String propertyName = element.getAttribute("name"); //$NON-NLS-1$
					if (Util.isEmpty(propertyName))
						continue;

					String propertyValue = element.getAttribute("value"); //$NON-NLS-1$
					if (Util.isEmpty(propertyValue))
						continue;

					map.put(propertyName, propertyValue);
				}
			}

			return map;
		}
	}

	/**
	 * {@link Layer} implementation that does nothing except stores a URI.
	 */
	protected static class URILayer extends AbstractLayer
	{
		public final URI uri;

		public URILayer(URI uri)
		{
			this.uri = uri;
		}

		@Override
		protected void doRender(DrawContext dc)
		{
		}
	}

	/**
	 * {@link Layer} implementation that does nothing.
	 */
	protected static class InvalidLayer extends AbstractLayer
	{
		@Override
		protected void doRender(DrawContext dc)
		{
		}
	}
}
