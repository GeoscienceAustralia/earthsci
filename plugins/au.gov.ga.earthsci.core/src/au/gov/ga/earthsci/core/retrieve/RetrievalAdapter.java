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
 * Empty implementation of the {@link IRetrievalListener}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RetrievalAdapter implements IRetrievalListener
{
	@Override
	public void statusChanged(IRetrieval retrieval)
	{
	}

	@Override
	public void progress(IRetrieval retrieval)
	{
	}

	@Override
	public void complete(IRetrieval retrieval)
	{
	}

	@Override
	public void paused(IRetrieval retrieval)
	{
	}
}