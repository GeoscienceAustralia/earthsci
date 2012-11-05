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
package au.gov.ga.earthsci.worldwind.common.layers.delegate;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;

import java.net.URL;

/**
 * Instances of {@link IRetrieverFactoryDelegate} are used to create
 * {@link Retriever}s when downloading tiles.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IRetrieverFactoryDelegate extends IDelegate
{
	/**
	 * Create a new {@link Retriever} for downloading from url.
	 * 
	 * @param url
	 *            URL to pass to {@link Retriever}'s constuctor
	 * @param postProcessor
	 *            Post processor to pass to {@link Retriever}'s constuctor
	 * @return New {@link Retriever}
	 */
	Retriever createRetriever(URL url, RetrievalPostProcessor postProcessor);
}
