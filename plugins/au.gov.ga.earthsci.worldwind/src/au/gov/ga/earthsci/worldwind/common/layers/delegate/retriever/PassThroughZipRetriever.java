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
package au.gov.ga.earthsci.worldwind.common.layers.delegate.retriever;

import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * Extended {@link HTTPRetriever} which simply stores incoming zip files
 * directly to the ByteBuffer, instead of decompressing them.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PassThroughZipRetriever extends HTTPRetriever
{
	public PassThroughZipRetriever(URL url, RetrievalPostProcessor postProcessor)
	{
		super(url, postProcessor);
	}

	@Override
	protected ByteBuffer readZipStream(InputStream inputStream, URL url) throws IOException
	{
		return readNonSpecificStream(inputStream, getConnection());
	}
}
