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
package au.gov.ga.earthsci.intent.xml;

import java.net.URL;

import org.w3c.dom.Document;

import au.gov.ga.earthsci.intent.Intent;

/**
 * Callback used to get a result from the handling of an XML load. This called
 * by an {@link IXmlLoader} to notify the caller of the result.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IXmlLoaderCallback
{
	/**
	 * Called by the XML loader when it has completed successfully. If the
	 * loader didn't produce a result, result will be null.
	 * 
	 * @param result
	 *            Result of the XML load
	 * @param document
	 *            Document that was loaded
	 * @param url
	 *            URL of the document that was loaded
	 * @param intent
	 *            Intent that completed
	 */
	void completed(Object result, Document document, URL url, Intent intent);

	/**
	 * Called when the Intent handler failed with an error.
	 * 
	 * @param e
	 *            Error generated from handling the Intent
	 * @param document
	 *            Document that failed
	 * @param url
	 *            URL of the document that failed
	 * @param intent
	 *            Intent that failed
	 */
	void error(Exception e, Document document, URL url, Intent intent);
}
