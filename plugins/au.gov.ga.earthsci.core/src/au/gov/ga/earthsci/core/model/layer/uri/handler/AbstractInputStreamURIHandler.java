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

import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.Layer;

import java.io.InputStream;
import java.net.URI;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * Abstract {@link ILayerURIHandler} implementation that uses the configured
 * World Wind Layer Factory to create Layers from an InputStream.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractInputStreamURIHandler extends AbstractURIHandler
{
	/**
	 * Create a Layer from the given {@link InputStream}. Intended to be called
	 * by subclasses.
	 * 
	 * @param is
	 *            InputStream to create a Layer from
	 * @param uri
	 *            Original URI used to create the InputStream
	 * @return a new Layer created by the WW layer factory from the InputStream
	 * @throws LayerURIHandlerException
	 */
	protected Layer createLayer(InputStream is, URI uri) throws LayerURIHandlerException
	{
		Object o;
		try
		{
			AVList params = new AVListImpl();
			try
			{
				params.setValue(AVKeyMore.CONTEXT_URL, uri.toURL());
			}
			catch (Exception e)
			{
				//ignore
			}
			o = BasicFactory.create(AVKey.LAYER_FACTORY, is, params);
		}
		catch (Exception e)
		{
			throw new LayerURIHandlerException(e);
		}
		if (!(o instanceof Layer))
		{
			throw new LayerURIHandlerException("Not a Layer: " + uri); //$NON-NLS-1$
		}
		return (Layer) o;
	}
}
