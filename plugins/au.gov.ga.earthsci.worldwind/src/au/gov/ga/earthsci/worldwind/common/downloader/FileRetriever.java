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
package au.gov.ga.earthsci.worldwind.common.downloader;

import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.URLRetriever;

import java.net.URL;

/**
 * Retriever which supports retrieval from a URL with the file:// protocol.
 * Required because HTTPRetiever (the standard subclass of URLRetriever) doesn't
 * support the file protocol. Simply a non-abstract subclass of the abstract
 * {@link URLRetriever} class.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FileRetriever extends URLRetriever
{
	public FileRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		super(url, postProcessor);
	}
}
