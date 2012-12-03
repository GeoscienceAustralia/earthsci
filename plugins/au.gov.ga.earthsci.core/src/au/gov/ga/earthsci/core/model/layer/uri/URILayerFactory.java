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

import gov.nasa.worldwind.layers.Layer;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import au.gov.ga.earthsci.core.model.layer.uri.handler.ClassURIHandler;
import au.gov.ga.earthsci.core.model.layer.uri.handler.ClasspathURIHandler;
import au.gov.ga.earthsci.core.model.layer.uri.handler.FileURIHandler;
import au.gov.ga.earthsci.core.model.layer.uri.handler.ILayerURIHandler;
import au.gov.ga.earthsci.core.model.layer.uri.handler.LayerURIHandlerException;

/**
 * Factory used to create Layers from URIs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class URILayerFactory
{
	private final static List<ILayerURIHandler> handlers = new ArrayList<ILayerURIHandler>();

	static
	{
		registerLayerURIHandler(new ClasspathURIHandler());
		registerLayerURIHandler(new ClassURIHandler());
		registerLayerURIHandler(new FileURIHandler());
	}

	/**
	 * Register a handler that handles a particular URI scheme used to create
	 * Layers.
	 * 
	 * @param handler
	 */
	public static void registerLayerURIHandler(ILayerURIHandler handler)
	{
		handlers.add(handler);
	}

	/**
	 * Unregister a registered handler.
	 * 
	 * @param handler
	 * @see #registerLayerURIHandler(ILayerURIHandler)
	 */
	public static void unregisterLayerURIHandler(ILayerURIHandler handler)
	{
		handlers.remove(handler);
	}

	/**
	 * Create a layer from a URI. Uses the handler registered for the URI's
	 * scheme.
	 * 
	 * @param caller
	 *            Object requesting the layer
	 * @param uri
	 *            URI to create the layer from
	 * @param monitor
	 *            {@link IProgressMonitor} used to report layer creation
	 *            progress
	 * @return Layer created from the URI
	 * @throws URILayerFactoryException
	 */
	public static Layer createLayer(Object caller, URI uri, IProgressMonitor monitor) throws URILayerFactoryException
	{
		if (uri.getScheme() == null)
		{
			throw new URILayerFactoryException("URI scheme is blank"); //$NON-NLS-1$
		}
		for (ILayerURIHandler handler : handlers)
		{
			if (handler.isSchemeSupported(uri.getScheme()))
			{
				try
				{
					return handler.createLayer(caller, uri, monitor);
				}
				catch (LayerURIHandlerException e)
				{
					throw new URILayerFactoryException(e);
				}
			}
		}
		throw new URILayerFactoryException("Don't know how to create layer from URI with scheme: " + uri.getScheme()); //$NON-NLS-1$
	}
}
