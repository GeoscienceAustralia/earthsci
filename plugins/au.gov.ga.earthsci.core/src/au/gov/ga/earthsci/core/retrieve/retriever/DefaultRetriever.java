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
package au.gov.ga.earthsci.core.retrieve.retriever;

import java.net.URL;

import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.IRetrievalProperties;
import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.IRetriever;
import au.gov.ga.earthsci.core.retrieve.IRetrieverMonitor;
import au.gov.ga.earthsci.core.retrieve.RetrievalStatus;
import au.gov.ga.earthsci.core.retrieve.RetrieverResult;
import au.gov.ga.earthsci.core.retrieve.RetrieverResultStatus;
import au.gov.ga.earthsci.core.retrieve.result.LocalURLRetrievalResult;

/**
 * Abstract {@link IRetriever} that reads a resource from a URL naively using
 * the {@link URL#openStream()} method. Can be used for local URLs, such as file
 * and resource URLs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DefaultRetriever implements IRetriever
{
	@Override
	public RetrieverResult retrieve(URL url, IRetrieverMonitor monitor, IRetrievalProperties retrievalProperties,
			IRetrievalData cachedData) throws Exception
	{
		monitor.updateStatus(RetrievalStatus.READING);
		IRetrievalResult result = new LocalURLRetrievalResult(url);
		return new RetrieverResult(result, result.isSuccessful() ? RetrieverResultStatus.COMPLETE
				: RetrieverResultStatus.ERROR);
	}

	@Override
	public IRetrievalData checkCache(URL url)
	{
		//caching is unsupported for these retrievers
		return null;
	}

	@Override
	public boolean supports(URL url)
	{
		return true;
	}
}
