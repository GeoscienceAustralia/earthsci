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
package au.gov.ga.earthsci.core.mime;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;

/**
 * Provides the ability to lookup an Eclipse content type that is associated
 * with an IANA MIME type. Associations are created using the
 * <code>au.gov.ga.earthsci.core.mimeTypes</code> extension point.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MIMEHelper
{
	private final static Map<String, String> map = new HashMap<String, String>();
	private static boolean initialized = false;
	private final static String EXTENSION_POINT_ID = "au.gov.ga.earthsci.core.mimeTypes"; //$NON-NLS-1$

	private static void initialize()
	{
		if (initialized)
		{
			return;
		}

		IConfigurationElement[] config = RegistryFactory.getRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
		for (IConfigurationElement element : config)
		{
			String mimeType = element.getAttribute("name"); //$NON-NLS-1$
			String contentType = element.getAttribute("content-type-id"); //$NON-NLS-1$
			map.put(mimeType, contentType);
		}
		initialized = true;
	}

	public static IContentType getContentTypeForMIMEType(String mimeType)
	{
		initialize();
		String contentTypeId = map.get(mimeType);
		if (contentTypeId == null)
		{
			return null;
		}
		return Platform.getContentTypeManager().getContentType(contentTypeId);
	}
}
