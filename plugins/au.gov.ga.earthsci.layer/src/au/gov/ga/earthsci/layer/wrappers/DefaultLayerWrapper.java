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
package au.gov.ga.earthsci.layer.wrappers;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWXML;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.xpath.XPath;

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.layer.LayerFactory;
import au.gov.ga.earthsci.layer.delegator.LayerDelegator;
import au.gov.ga.earthsci.layer.intent.IntentLayerLoader;
import au.gov.ga.earthsci.layer.tree.ILayerNode;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * Basic {@link ILayerWrapper} implementation for any {@link Layer} created from
 * an XML layer definition, or from a {@link URI} using the
 * {@link IntentLayerLoader}.
 * <p/>
 * If the layer from created by the {@link LayerFactory} from an XML element,
 * the factory saves the XML element used to create the layer as part of the
 * layer's value map ({@link AVList}). This wrapper pulls out the element, and
 * saves it when persisting the layer. When loading the layer, it uses the same
 * XML definition to load the layer.
 * <p/>
 * Subclasses can edit elements/attributes within the XML element during the
 * editing process; this is to support editing legacy layers.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DefaultLayerWrapper extends LayerDelegator implements ILayerWrapper
{
	private static final Logger logger = LoggerFactory.getLogger(DefaultLayerWrapper.class);
	public final static String CLASS_ELEMENT = "class"; //$NON-NLS-1$
	public final static String BUNDLE_ATTRIBUTE = "bundle"; //$NON-NLS-1$
	public final static String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
	public final static String LEGACY_ELEMENT = "legacy"; //$NON-NLS-1$
	public final static String URI_ELEMENT = "uri"; //$NON-NLS-1$
	public final static String URL_ELEMENT = "url"; //$NON-NLS-1$

	protected String classBundle;
	protected String className;
	protected Element element;
	protected URI uri;
	protected URL url;

	private boolean reloadingLayer;

	@Override
	public boolean supports(Layer layer)
	{
		//supports all Layer implementations:
		return true;
	}

	@Override
	public void setLayer(Layer layer)
	{
		super.setLayer(layer);

		if (reloadingLayer)
		{
			return;
		}

		classBundle = FrameworkUtil.getBundle(layer.getClass()).getSymbolicName();
		className = layer.getClass().getName();
		element = (Element) layer.getValue(LayerFactory.LAYER_ELEMENT);
		uri = (URI) layer.getValue(IntentLayerLoader.LAYER_URI_KEY);

		url = (URL) layer.getValue(AVKeyMore.CONTEXT_URL);
		if (url == null)
		{
			AVList params = (AVList) layer.getValue(AVKey.CONSTRUCTION_PARAMETERS);
			if (params != null)
			{
				url = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
			}
		}
	}

	@Override
	public boolean isLoading()
	{
		return reloadingLayer;
	}

	@Override
	public void load(Element parent)
	{
		XPath xpath = WWXML.makeXPath();

		element = null;
		uri = null;
		url = null;

		classBundle = XmlUtil.getText(parent, CLASS_ELEMENT + "/@" + BUNDLE_ATTRIBUTE, null, xpath); //$NON-NLS-1$
		className = XmlUtil.getText(parent, CLASS_ELEMENT + "/@" + NAME_ATTRIBUTE, null, xpath); //$NON-NLS-1$

		Element definitionElement = WWXML.getElement(parent, LEGACY_ELEMENT, xpath);
		if (definitionElement != null)
		{
			element = XmlUtil.getFirstChildElement(definitionElement);
		}

		String uriText = XmlUtil.getText(parent, URI_ELEMENT, null, xpath);
		if (uriText != null)
		{
			try
			{
				uri = new URI(uriText);
			}
			catch (URISyntaxException e)
			{
				logger.error("Error converting text to URI: " + uriText); //$NON-NLS-1$
			}
		}

		String urlText = XmlUtil.getText(parent, URL_ELEMENT, null, xpath);
		if (urlText != null)
		{
			try
			{
				url = new URL(urlText);
			}
			catch (MalformedURLException e)
			{
				logger.error("Error converting text to URL: " + urlText); //$NON-NLS-1$
			}
		}
	}

	@Override
	public void save(Element parent)
	{
		if (uri != null)
		{
			XmlUtil.setTextElement(parent, URI_ELEMENT, uri.toString());
		}

		if (element != null)
		{
			Element legacyElement = parent.getOwnerDocument().createElement(LEGACY_ELEMENT);
			parent.appendChild(legacyElement);
			Node imported = legacyElement.getOwnerDocument().importNode(element, true);
			legacyElement.appendChild(imported);

			if (url != null)
			{
				XmlUtil.setTextElement(parent, URL_ELEMENT, url.toString());
			}
		}
		else if (className != null && classBundle != null && uri == null)
		{
			Element element = XmlUtil.createElement(parent, CLASS_ELEMENT, null);
			element.setAttribute(BUNDLE_ATTRIBUTE, classBundle);
			element.setAttribute(NAME_ATTRIBUTE, className);
		}
	}

	@Override
	public void initialize(ILayerNode node, IEclipseContext context)
	{
		try
		{
			reloadingLayer = true;

			if (element != null)
			{
				reloadFromElement();
			}
			else if (uri != null)
			{
				reloadFromUri(node, context);
			}
			else if (classBundle != null && className != null)
			{
				reloadFromClassName();
			}
			else
			{
				logger.error("Error loading wrapped layer, no definition/url/class defined"); //$NON-NLS-1$
			}
		}
		finally
		{
			reloadingLayer = false;
		}
	}

	/**
	 * Reload this layer from the XML element.
	 */
	protected void reloadFromElement()
	{
		URL url = this.url;
		if (uri != null)
		{
			try
			{
				url = uri.toURL();
			}
			catch (MalformedURLException e)
			{
				//ignore;
			}
		}

		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.CONTEXT_URL, url);
		Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
		Object result = factory.createFromConfigSource(element, params);
		if (!(result instanceof Layer))
		{
			if (result == null)
			{
				logger.error("Error loading layer from element, layer factory returned null"); //$NON-NLS-1$
			}
			else
			{
				logger.error("Object loaded is not a Layer: " + result.getClass()); //$NON-NLS-1$
			}
			return;
		}

		Layer layer = (Layer) result;
		setLayer(layer);
	}

	/**
	 * Reload this layer from the URI, using the {@link IntentLayerLoader}.
	 * 
	 * @param node
	 * @param context
	 */
	protected void reloadFromUri(ILayerNode node, IEclipseContext context)
	{
		try
		{
			IntentLayerLoader.load(uri, node, context);
		}
		catch (Exception e)
		{
			logger.error("Error loading layer from URL: " + uri, e); //$NON-NLS-1$
		}
	}

	/**
	 * Reload this layer from a class name.
	 */
	protected void reloadFromClassName()
	{
		try
		{
			Bundle bundle = Platform.getBundle(classBundle);
			@SuppressWarnings("unchecked")
			Class<? extends Layer> layerClass = (Class<? extends Layer>) bundle.loadClass(className);
			Layer layer = layerClass.newInstance();
			setLayer(layer);
		}
		catch (Exception e)
		{
			logger.error("Error instantiating layer class", e); //$NON-NLS-1$
		}
	}
}
