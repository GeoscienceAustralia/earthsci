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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.w3c.dom.Document;

import au.gov.ga.earthsci.intent.Intent;

/**
 * Loads an XML document to an object. Only called with documents that match
 * this loader's {@link IXmlLoaderFilter}.
 * <p/>
 * Upon instatiation, an {@link IEclipseContext} is used to inject any annotated
 * methods/fields into this object.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IXmlLoader
{
	/**
	 * Load the given XML document.
	 * 
	 * @param document
	 *            Document to load
	 * @param context
	 *            Context to use for any relative URLs in the XML
	 * @param intent
	 *            Intent associated with this load
	 * @return Object loaded from XML
	 * @throws XmlLoaderException
	 *             If the load from XML fails
	 */
	Object load(Document document, URL context, Intent intent) throws XmlLoaderException;
}
