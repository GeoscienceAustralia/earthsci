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
package au.gov.ga.earthsci.core.retrieve.cache;

import java.io.InputStream;
import java.net.URL;

/**
 * Provides a simple interface for caching resources based on URLs. Provides simple get/put operations.
 * <p/>
 * Specific implementations should handle issues such as eviction, cache lifespan etc.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IURLResourceCache
{

	/**
	 * Returns whether or not a cached resource exists for the given URL.
	 * 
	 * @param url The URL of the source resource to check for in the cache
	 * 
	 * @return <code>true</code> if a cached resource exists, <code>false</code> otherwise
	 */
	boolean hasResource(URL url);
	
	/**
	 * Returns a URL to the locally cached resource for the given (possibly remote) URL, if one exists.
	 * 
	 * @param url The URL of the source resource for which a cached version is requested.
	 * 
	 * @return The URL of the cached version of the provided resource, or <code>null</code> if one does not exist.
	 * 
	 * @see #hasResource(URL)
	 */
	URL getResource(URL url);
	
	/**
	 * Puts the resource identified by the provided URL into the cache. 
	 * 
	 * @param url The URL of the resource to add to the cache
	 * @param stream An input stream connected to the resource
	 * 
	 * @return The URL of the locally cached version of the resource
	 */
	URL putResource(URL url, InputStream stream);
	
}
