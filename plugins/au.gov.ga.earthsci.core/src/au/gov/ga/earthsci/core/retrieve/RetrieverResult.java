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

/**
 * Result returned by an {@link IRetriever} implementation after retrieval is
 * complete or stopped.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RetrieverResult
{
	public final IRetrievalResult result;
	public final RetrieverResultStatus status;

	/**
	 * Create a new instance.
	 * 
	 * @param result
	 *            Result of the retrieval
	 * @param status
	 *            Status of the retrieval, cannot be null
	 * @throws NullPointerException
	 *             If status is null
	 */
	public RetrieverResult(IRetrievalResult result, RetrieverResultStatus status)
	{
		if (status == null)
		{
			throw new NullPointerException("Status is null"); //$NON-NLS-1$
		}
		this.result = result;
		this.status = status;
	}
}