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

import au.gov.ga.earthsci.core.retrieve.IRetriever;
import au.gov.ga.earthsci.core.retrieve.IRetrieverMonitor;
import au.gov.ga.earthsci.core.retrieve.RetrievalStatus;
import au.gov.ga.earthsci.core.retrieve.RetrieverResult;
import au.gov.ga.earthsci.core.retrieve.RetrieverResultStatus;
import au.gov.ga.earthsci.core.retrieve.result.ErrorRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.result.URLRetrievalResult;

/**
 * Abstract {@link IRetriever} that reads a resource from a URL naively using
 * the {@link URL#openStream()} method.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractURLRetriever implements IRetriever
{
	@Override
	public RetrieverResult retrieve(URL url, IRetrieverMonitor monitor, boolean cache, boolean refresh)
			throws Exception
	{
		monitor.updateStatus(RetrievalStatus.READING);
		try
		{
			checkURL(url);
			return new RetrieverResult(new URLRetrievalResult(url), RetrieverResultStatus.COMPLETE);
		}
		catch (Exception e)
		{
			return new RetrieverResult(new ErrorRetrievalResult(e), RetrieverResultStatus.ERROR);
		}
	}

	public abstract void checkURL(URL url) throws Exception;
}
