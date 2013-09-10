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
package au.gov.ga.earthsci.core.seed;

import java.net.URL;

import org.w3c.dom.Document;

import au.gov.ga.earthsci.core.intent.AbstractXmlRetrieveIntentHandler;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.seeder.SeederManager;

/**
 * Handles seed XML documents. Passes the document to the {@link SeederManager}.
 * 
 * @see SeederManager
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SeedXmlIntentHandler extends AbstractXmlRetrieveIntentHandler
{
	@Override
	protected void handle(Document document, URL url, Intent intent, IIntentCallback callback)
	{
		SeederManager.seed(document, url);
	}
}
