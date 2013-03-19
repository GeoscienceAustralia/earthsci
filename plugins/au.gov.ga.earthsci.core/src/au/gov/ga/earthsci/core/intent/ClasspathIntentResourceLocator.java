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
package au.gov.ga.earthsci.core.intent;

import java.net.URI;
import java.net.URL;

import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.locator.IIntentResourceLocator;

/**
 * {@link IIntentResourceLocator} for classpath:// URIs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ClasspathIntentResourceLocator implements IIntentResourceLocator
{
	@Override
	public URL locate(Intent intent)
	{
		URI uri = intent.getURI();
		if (uri == null || !"classpath".equalsIgnoreCase(uri.getScheme())) //$NON-NLS-1$
		{
			return null;
		}
		String path = blankNullString(uri.getAuthority()) + blankNullString(uri.getPath());
		if (!path.startsWith("/")) //$NON-NLS-1$
		{
			path = "/" + path; //$NON-NLS-1$
		}
		return getClass().getResource(path);
	}

	private static String blankNullString(String s)
	{
		return s == null ? "" : s; //$NON-NLS-1$
	}
}
