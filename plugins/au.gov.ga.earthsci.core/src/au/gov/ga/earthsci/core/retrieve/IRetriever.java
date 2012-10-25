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
 * A strategy interface for classes able to retrieve resources from specific URL types.
 * <p/>
 * Normally clients will not interact with this class. Instead they will use an instance of {@link IRetrievalService} which
 * provides integration with the Jobs API etc.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IRetriever
{
	/**
	 * Returns whether or not this retriever supports retrieving resources from the provided URL.
	 * <p/>
	 * If this method returns <code>true</code>, {@link #retrieve(URL)} will return a result.
	 * 
	 * @param url The URL to test
	 * 
	 * @return <code>true</code> if this retriever supports the provided URL, <code>false</code> otherwise.
	 */
	boolean supports(URL url);
	
	/**
	 * Retrieves the resource from the provided URL on the current thread and returns the result.
	 * 
	 * @param url The URL to retrieve
	 * @param monitor A monitor to report progress on
	 * 
	 * @return The result of the retrieval
	 * 
	 * @throws IllegalArgumentException if this retriever does not support the provided URL. 
	 * Use {@link #supports(URL)} to determine if this retriever supports the provided URL.
	 */
	IRetrievalResult retrieve(URL url, IRetrievalMonitor monitor);
}
