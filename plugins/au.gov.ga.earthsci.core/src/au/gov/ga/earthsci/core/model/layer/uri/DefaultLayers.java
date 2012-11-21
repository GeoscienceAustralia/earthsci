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
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.WWXML;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.core.util.UTF8URLEncoder;
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
	/**
	 * @return {@link FolderNode} containing the {@link LayerNode}s representing
	 *         the default layers defined in the WW configuration
	 */
	public static FolderNode getLayers()
	{
		FolderNode folder = new FolderNode();
		folder.setName("Default");
		URI[] uris = getLayerURIs();
		for (URI uri : uris)
		{
			LayerNode node = new LayerNode();
			node.setUri(uri);
			folder.add(node);
		}
		return folder;
	}

	/**
	 * Generate an array of URIs which can be used for creating the default
	 * layers defined in the WW configuration. Returned URIs can be instantiated
	 * into Layer objects using the {@link URILayerFactory}.
	 * 
	 * @return Array of URIs that represent the default layers defined in the WW
	 *         configuration
	 */
	public static URI[] getLayerURIs()
	{
		Element element = Configuration.getElement("./LayerList"); //$NON-NLS-1$
		LayerList layers = createLayersFromElement(element);

		List<URI> uris = new ArrayList<URI>();
		for (Layer layer : layers)
		{
			if (layer instanceof URILayer)
			{
				uris.add(((URILayer) layer).uri);
			}
		}
		return uris.toArray(new URI[uris.size()]);
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
		URILayerFactory factory = new URILayerFactory();
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
	 * creation by creating a dummy {@link URILayer}. The {@link URILayer}
	 * contains a URI which can be used to instatiate the actual Layer.
	 */
	protected static class URILayerFactory extends BasicLayerFactory
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

			//add enabled, and any properties, to a query string
			String queryString = "?enabled=" + enabled; //$NON-NLS-1$
			String propertyQueryStringPart = createPropertyQueryStringPart(domElement);
			if (!Util.isEmpty(propertyQueryStringPart))
			{
				queryString += "&" + propertyQueryStringPart; //$NON-NLS-1$
			}

			String uriString = null;
			if (classNameSpecified)
			{
				uriString = "class://" + className + queryString; //$NON-NLS-1$
			}
			else if (!Util.isEmpty(href))
			{
				uriString = "classpath://" + href + queryString; //$NON-NLS-1$
			}

			if (uriString == null)
			{
				//cannot create a URI for the layer element, return a dummy layer
				return new InvalidLayer();
			}

			try
			{
				return new URILayer(new URI(uriString));
			}
			catch (URISyntaxException e)
			{
				//TODO
				e.printStackTrace();
				return null;
			}
		}

		protected String createPropertyQueryStringPart(Element domElement)
		{
			StringBuilder sb = new StringBuilder();

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

					propertyName = UTF8URLEncoder.encode(propertyName);
					propertyValue = UTF8URLEncoder.encode(propertyValue);
					sb.append("&" + propertyName + "=" + propertyValue); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			if (sb.length() == 0)
			{
				return ""; //$NON-NLS-1$
			}

			return sb.substring(1);
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
