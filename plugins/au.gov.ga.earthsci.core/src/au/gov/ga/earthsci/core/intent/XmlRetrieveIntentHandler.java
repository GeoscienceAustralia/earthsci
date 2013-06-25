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
package au.gov.ga.earthsci.core.intent;

import gov.nasa.worldwind.util.WWXML;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.w3c.dom.Document;

import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.xml.IXmlLoader;
import au.gov.ga.earthsci.core.xml.IXmlLoaderCallback;
import au.gov.ga.earthsci.core.xml.XmlLoaderManager;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;

/**
 * IntentHandler that uses the retrieval service to retrieve the Intent's data,
 * then loads the data as an XML document, and passes the document to the
 * {@link IXmlLoader} system.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class XmlRetrieveIntentHandler extends AbstractRetrieveIntentHandler
{
	@Inject
	private IEclipseContext context;

	@Override
	protected void handle(IRetrievalData data, URL url, Intent intent, final IIntentCallback callback)
	{
		InputStream is = null;
		try
		{
			is = data.getInputStream();

			DocumentBuilder builder = WWXML.createDocumentBuilder(true);
			Document document = builder.parse(is);
			IXmlLoaderCallback loaderCallback = new IXmlLoaderCallback()
			{
				@Override
				public void completed(Object result, Document document, URL url, Intent intent)
				{
					callback.completed(result, intent);
				}

				@Override
				public void error(Exception e, Document document, URL url, Intent intent)
				{
					callback.error(e, intent);
				}
			};
			XmlLoaderManager.getInstance().load(document, url, intent, loaderCallback, context);
		}
		catch (Exception e)
		{
			callback.error(e, intent);
		}
		finally
		{
			try
			{
				if (is != null)
				{
					is.close();
				}
			}
			catch (IOException e)
			{
				// Do nothing
			}
		}
	}
}
