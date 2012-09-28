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
package au.gov.ga.earthsci.core.model.layer.uri.handler;

import gov.nasa.worldwind.layers.Layer;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;
import java.util.Map.Entry;

import au.gov.ga.earthsci.core.util.QueryString;
import au.gov.ga.earthsci.core.util.Util;

/**
 * Abstract implementation of the {@link ILayerURIHandler} interface. Concrete
 * subclasses are responsible to implement URI scheme specific loading.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractURIHandler implements ILayerURIHandler
{
	/**
	 * Create a Layer from the given URI. The URI's scheme has already been
	 * checked against {@link #getSupportedScheme()}.
	 * 
	 * @param uri
	 *            URI to create the layer from
	 * @return New Layer created form the URI
	 * @throws LayerURIHandlerException
	 */
	protected abstract Layer createLayerFromURI(URI uri) throws LayerURIHandlerException;

	/**
	 * Should the query string be used to extract and set properties on the
	 * created Layer? Intended to be overridden by subclasses if the URI's query
	 * parameters shouldn't be used to set properties on the layer.
	 * 
	 * @return True if the properties in the query string should be set on the
	 *         created Layer
	 */
	protected boolean shouldSetProperties()
	{
		return true;
	}

	@Override
	public Layer createLayer(URI uri) throws LayerURIHandlerException
	{
		if (!getSupportedScheme().equalsIgnoreCase(uri.getScheme()))
		{
			throw new LayerURIHandlerException("Invalid URI scheme: " + uri); //$NON-NLS-1$
		}
		Layer layer = createLayerFromURI(uri);
		if (shouldSetProperties())
		{
			setPropertiesFromQuery(layer, uri);
		}
		return layer;
	}

	/**
	 * Parse the URI's query string, and set the properties on the given Layer.
	 * 
	 * @param layer
	 *            Layer to set properties on
	 * @param uri
	 *            URI whose query string to use
	 * @throws LayerURIHandlerException
	 */
	protected static void setPropertiesFromQuery(Layer layer, URI uri) throws LayerURIHandlerException
	{
		QueryString query = new QueryString(uri.getQuery());
		for (Entry<String, List<String>> entry : query.entrySet())
		{
			List<String> values = entry.getValue();
			if (values == null)
				continue;

			for (String value : values)
			{
				try
				{
					Util.setPropertyOn(layer, entry.getKey(), value);
				}
				catch (InvocationTargetException e)
				{
					throw new LayerURIHandlerException(e);
				}
			}
		}
	}
}
