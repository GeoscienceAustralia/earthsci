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

import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents a handler that can create a {@link Layer} from a {@link URI}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILayerURIHandler
{
	/**
	 * @return True if the given URI scheme is supported by this handler.
	 */
	boolean isSchemeSupported(String scheme);

	/**
	 * Create a {@link Layer} from the given {@link URI}.
	 * 
	 * @param uri
	 *            URI to create the Layer from
	 * @param monitor
	 *            {@link IProgressMonitor} to report layer load progress to
	 * @return A Layer created from the given uri
	 * @throws LayerURIHandlerException
	 *             when Layer creation fails
	 */
	Layer createLayer(URI uri, IProgressMonitor monitor) throws LayerURIHandlerException;
}
