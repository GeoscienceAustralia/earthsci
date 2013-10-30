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

import java.net.URL;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.layer.ILayerWrapper;
import au.gov.ga.earthsci.layer.LayerDelegate;
import au.gov.ga.earthsci.layer.LayerFactory;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public class LayerWrapper extends LayerDelegate implements ILayerWrapper
{
	public final static String URL_ELEMENT = "url"; //$NON-NLS-1$
	public final static String DEFINITION_ELEMENT = "definition"; //$NON-NLS-1$

	protected URL url;
	protected Element element;
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

		URL url = (URL) layer.getValue(AVKeyMore.CONTEXT_URL);
		if (url == null)
		{
			AVList params = (AVList) layer.getValue(AVKey.CONSTRUCTION_PARAMETERS);
			if (params != null)
			{
				url = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
			}
		}
		if (url != null)
		{
			this.url = url;
		}

		Element element = (Element) layer.getValue(LayerFactory.LAYER_ELEMENT);
		if (element != null)
		{
			this.element = element;
		}
	}

	@Override
	public boolean isLoading()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void load(Element parent)
	{
		XPath xpath = WWXML.makeXPath();

		url = null;
		element = null;

		Element urlElement = WWXML.getElement(parent, URL_ELEMENT, xpath);
		if (urlElement != null)
		{
			try
			{
				url = new URL(urlElement.getTextContent());
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Element definitionElement = WWXML.getElement(parent, DEFINITION_ELEMENT, xpath);
		if (definitionElement != null)
		{
			element = XmlUtil.getFirstChildElement(definitionElement);
		}

		reload();
	}

	@Override
	public void save(Element parent)
	{
		if (url == null && element == null)
		{
			//TODO log error
		}

		if (url != null)
		{
			Element urlElement = parent.getOwnerDocument().createElement(URL_ELEMENT);
			parent.appendChild(urlElement);
			urlElement.setTextContent(url.toString());
		}

		if (element != null)
		{
			Element xmlElement = parent.getOwnerDocument().createElement(DEFINITION_ELEMENT);
			parent.appendChild(xmlElement);
			Node imported = xmlElement.getOwnerDocument().importNode(element, true);
			xmlElement.appendChild(imported);
		}
	}

	public void reload()
	{
		try
		{
			reloadingLayer = true;

			if (element != null)
			{
				AVList params = new AVListImpl();
				params.setValue(AVKeyMore.CONTEXT_URL, url);
				Factory factory = (Factory) WorldWind.createConfigurationComponent(AVKey.LAYER_FACTORY);
				Object result = factory.createFromConfigSource(element, params);
				if (!(result instanceof Layer))
				{
					//TODO log error
					return;
				}

				Layer layer = (Layer) result;
				setLayer(layer);
			}
			else if (url != null)
			{
				//TODO
			}
			else
			{
				//TODO log error
			}
		}
		finally
		{
			reloadingLayer = false;
		}
	}
}
