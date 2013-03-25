/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.model.intent;

import java.net.URL;

import javax.inject.Inject;

import au.gov.ga.earthsci.core.retrieve.IRetrieval;
import au.gov.ga.earthsci.core.retrieve.IRetrievalService;
import au.gov.ga.earthsci.core.retrieve.RetrievalAdapter;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.IIntentHandler;
import au.gov.ga.earthsci.intent.Intent;

/**
 * @author u09145
 *
 */
public class GDALRasterModelIntentHandler implements IIntentHandler
{

	@Inject
	private IRetrievalService retrievalService;
	
	@Override
	public void handle(final Intent intent, final IIntentCallback callback)
	{
		try
		{
			final URL url = intent.getURL();
			if (url == null)
			{
				throw new IllegalArgumentException("Intent URL is null"); //$NON-NLS-1$
			}
			
			IRetrieval retrieval = retrievalService.retrieve(this, url);
			retrieval.addListener(new RetrievalAdapter () {
				@Override
				public void complete(IRetrieval retrieval)
				{
					System.out.println("Booya " + url);
					callback.completed(retrieval.getData(), intent);
				}
			});
			retrieval.start();
			
		}
		catch (Exception e)
		{
			callback.error(e, intent);
		}
	}

}
