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
package au.gov.ga.earthsci.intent;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;

/**
 * Central management point for the
 * <code>au.gov.ga.earthsci.intent.guessableContentTypeURIs</code> extension
 * point.
 * <p/>
 * Allows plugins to define URIs whose path can be used to guess the content
 * type. For example, guessable content type URIs may include file:// and jar://
 * URIs, but not http:// URIs (whose content type can be retrieved from the HTTP
 * response header).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GuessableContentTypeURIManager
{
	private static final String GUESSABLE_CONTENT_TYPE_URIS_ID = "au.gov.ga.earthsci.intent.guessableContentTypeURIs"; //$NON-NLS-1$

	private static final Set<URIFilter> filters = new HashSet<URIFilter>();

	static
	{
		IConfigurationElement[] config =
				RegistryFactory.getRegistry().getConfigurationElementsFor(GUESSABLE_CONTENT_TYPE_URIS_ID);
		for (IConfigurationElement element : config)
		{
			filters.add(new URIFilter(element));
		}
	}

	public static IContentType guessContentType(URI uri)
	{
		if (uri == null || !shouldGuessContentType(uri) || uri.getPath() == null)
		{
			return null;
		}
		return Platform.getContentTypeManager().findContentTypeFor(uri.getPath());
	}

	public static boolean shouldGuessContentType(URI uri)
	{
		for (URIFilter filter : filters)
		{
			if (filter.matches(uri))
			{
				return true;
			}
		}
		return false;
	}
}
