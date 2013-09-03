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

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;

import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;

/**
 * IntentHandler that uses the retrieval service to retrieve the Intent's data,
 * then loads the data as an XML document, and passes the document an abstract
 * method for subclasses to handle.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractXmlRetrieveIntentHandler extends AbstractRetrieveIntentHandler
{
	/**
	 * Handle the XML document, notifying the callback of the result (or error).
	 * 
	 * @param document
	 *            XML document to handle
	 * @param url
	 *            URL that the document was retrieved from
	 * @param intent
	 *            Intent that started the retrieval
	 * @param callback
	 *            Intent callback to notify of success/failure
	 */
	protected abstract void handle(Document document, URL url, Intent intent, IIntentCallback callback);

	@Override
	protected void handle(IRetrievalData data, URL url, Intent intent, IIntentCallback callback)
	{
		InputStream is = null;
		try
		{
			is = data.getInputStream();

			DocumentBuilder builder = WWXML.createDocumentBuilder(true);
			Document document = builder.parse(is);
			handle(document, url, intent, callback);
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
