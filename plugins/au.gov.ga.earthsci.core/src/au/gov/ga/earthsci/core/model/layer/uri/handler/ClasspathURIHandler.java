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

import java.io.InputStream;
import java.net.URI;

import au.gov.ga.earthsci.core.util.Util;

/**
 * {@link ILayerURIHandler} implementation for the classpath:// URI scheme. Uses
 * the URI's authority and path parts as a resource name, which it then loads as
 * a resource using the class loader, and then creates a Layer from the resource
 * using the WW layer factory.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ClasspathURIHandler extends AbstractInputStreamURIHandler
{
	private static final String SCHEME = "classpath"; //$NON-NLS-1$

	@Override
	public String getSupportedScheme()
	{
		return SCHEME;
	}

	@Override
	public Layer createLayerFromURI(URI uri) throws LayerURIHandlerException
	{
		InputStream is;
		try
		{
			String path = Util.blankNullString(uri.getAuthority()) + Util.blankNullString(uri.getPath());
			if (!path.startsWith("/")) //$NON-NLS-1$
			{
				path = "/" + path; //$NON-NLS-1$
			}
			is = getClass().getResourceAsStream(path);
			//if standard class loader can't find it, assume it's available by the WWJ library class loader
			if (is == null)
			{
				is = Layer.class.getClassLoader().getResourceAsStream(path);
			}
		}
		catch (Exception e)
		{
			throw new LayerURIHandlerException(e);
		}
		return createLayer(is, uri);
	}
}
