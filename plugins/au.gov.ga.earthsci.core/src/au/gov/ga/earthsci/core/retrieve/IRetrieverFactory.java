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
 * Factory for returning {@link IRetriever}s that know how to retrieve resources
 * from supported URLs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IRetrieverFactory
{
	/**
	 * Find and return an {@link IRetriever} that can retrieve a resource from
	 * the given URL.
	 * 
	 * @param url
	 *            URL that needs to be retrieved
	 * @return {@link IRetriever} for retrieving the given URL
	 */
	IRetriever getRetriever(URL url);

	/**
	 * Register an {@link IRetriever}.
	 * 
	 * @param retriever
	 *            {@link IRetriever} to register
	 */
	void registerRetriever(IRetriever retriever);
}
