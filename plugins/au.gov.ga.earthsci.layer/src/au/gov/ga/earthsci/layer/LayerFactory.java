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
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * Factory for creating {@link Layer}s from XML.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerFactory extends au.gov.ga.earthsci.worldwind.common.layers.LayerFactory
{
	public static final String LAYER_ELEMENT = "au.gov.ga.earthsci.layer.LayerElement"; //$NON-NLS-1$

	private Logger logger = LoggerFactory.getLogger(LayerFactory.class);

	@Override
	protected Object doCreateFromElement(Element domElement, AVList params) throws Exception
	{
		//first set the legend url in the params
		try
		{
			URL context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
			URL legend = XMLUtil.getURL(domElement, "Legend", context); //$NON-NLS-1$
			params.setValue(AVKeyMore.LEGEND_URL, legend);
		}
		catch (Exception e)
		{
			logger.error("Error setting legend url", e); //$NON-NLS-1$
		}

		Exception exception = null;
		try
		{
			Object o = super.doCreateFromElement(domElement, params);
			if (o != null)
			{
				if (o instanceof Layer)
				{
					setElementAndUrlOnLayer((Layer) o, domElement, params);
				}
				return o;
			}
		}
		catch (Exception e)
		{
			exception = e;
		}

		//attempt creating an elevation model
		try
		{
			Object o = BasicFactory.create(AVKey.ELEVATION_MODEL_FACTORY, domElement, params);
			if (o instanceof ElevationModel)
			{
				Layer layer = new ElevationModelLayer((ElevationModel) o);
				setElementAndUrlOnLayer(layer, domElement, params);
				return layer;
			}
		}
		catch (Exception e)
		{
			if (exception == null)
			{
				exception = e;
			}
		}

		if (exception != null)
		{
			throw exception;
		}

		return null;
	}

	@Override
	protected Layer createFromLayerDocument(Element domElement, AVList params)
	{
		Layer layer = super.createFromLayerDocument(domElement, params);
		if (layer != null)
		{
			setElementAndUrlOnLayer(layer, domElement, params);
		}
		return layer;
	}

	private void setElementAndUrlOnLayer(Layer layer, Element element, AVList params)
	{
		URL context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
		layer.setValue(AVKeyMore.CONTEXT_URL, context);
		layer.setValue(LAYER_ELEMENT, element);
	}
}
