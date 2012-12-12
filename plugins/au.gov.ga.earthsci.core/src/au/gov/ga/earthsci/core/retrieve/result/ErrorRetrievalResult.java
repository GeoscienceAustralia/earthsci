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
package au.gov.ga.earthsci.core.retrieve.result;

import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;

/**
 * An {@link IRetrievalResult} that can be used when an error has occurred
 * during resource retrieval.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ErrorRetrievalResult implements IRetrievalResult
{
	private final Exception error;

	public ErrorRetrievalResult(Exception error)
	{
		this.error = error;
	}

	@Override
	public boolean isSuccessful()
	{
		return false;
	}

	@Override
	public Exception getError()
	{
		return error;
	}

	@Override
	public boolean isFromCache()
	{
		return false;
	}

	@Override
	public IRetrievalData getData()
	{
		return null;
	}
}
