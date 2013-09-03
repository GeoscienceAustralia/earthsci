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
package au.gov.ga.earthsci.intent.resolver;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.content.IContentType;

import au.gov.ga.earthsci.intent.Intent;

/**
 * Class that can resolve a content type from an Intent's URL.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IContentTypeResolver
{
	/**
	 * Check if this resolver supports the given URL/intent.
	 * 
	 * @param url
	 * @param intent
	 * @return True if this resolver supports the URL/intent
	 */
	boolean supports(URL url, Intent intent);

	/**
	 * Attempt to resolve the content type for the given URL/intent.
	 * 
	 * @param url
	 * @param intent
	 * @return The content type for the URL/intent, else <code>null</code>
	 */
	IContentType resolve(URL url, Intent intent) throws IOException;
}
