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
package au.gov.ga.earthsci.core.retrieve;

import java.net.URL;

/**
 * A service interface for classes that are able to retrieve resources from
 * provided URLs.
 * <p/>
 * Service implementations should yield a {@link RetrievalJob} that performs the actual resource
 * retrieval. This job should not be scheduled, but scheduling is left up to the user.
 * <p/>
 * The basic {@link #retrieve(URL)} method performs background, non-blocking retrieval and takes advantage
 * of any caching provided by implementations. If more control is required by clients, other forms of the method
 * allow these options to be modified.
 * <p/>
 * In general there will be a single service per application, and clients will access
 * the current instance through dependency injection or via the current Eclipse context.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IRetrievalService
{
	/**
	 * Retrieve a resource identified by the provided URL.
	 * Any caching provided by implementing classes will be used.
	 * <p/>
	 * The returned job is NOT scheduled, the user must schedule it manually. If the service is
	 * unable to handle the specified URL, <code>null</code> will be returned.
	 * <p/>
	 * Equivalent to:
	 * <pre>retrieve(url, false);</pre>
	 * 
	 * @param url The URL of the resource to retrieve.
	 *  
	 * @return The job used to retrieve the resource, or <code>null</code> if the provided URL is unsupported.
	 */
	RetrievalJob retrieve(URL url);
	
	/**
	 * Retrieve a resource from the provided URL. This flavour allows more control over how the retrieval is performed.
	 * <p/>
	 * The returned job is NOT scheduled, the user must schedule it manually.
	 * 
	 * @param url The URL of the resource to retrieve 
	 * @param forceRefresh Whether or not to force a refresh of any caching provided by implementations.
	 * 
	 * @return The job used to retrieve the resource, or <code>null</code> if the provided URL is unsupported.
	 */
	RetrievalJob retrieve(URL url, boolean forceRefresh);
}
