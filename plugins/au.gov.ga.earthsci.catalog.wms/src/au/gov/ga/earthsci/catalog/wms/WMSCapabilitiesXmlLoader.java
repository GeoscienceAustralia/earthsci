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
package au.gov.ga.earthsci.catalog.wms;

import gov.nasa.worldwind.ogc.wms.WMSCapabilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.core.xml.IXmlLoader;
import au.gov.ga.earthsci.core.xml.IXmlLoaderCallback;
import au.gov.ga.earthsci.core.xml.IXmlLoaderFilter;
import au.gov.ga.earthsci.intent.Intent;

/**
 * {@link IXmlLoader} for loading WMS_Capabilities documents.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WMSCapabilitiesXmlLoader implements IXmlLoader, IXmlLoaderFilter
{
	@Override
	public boolean canLoad(Document document, Intent intent)
	{
		Element element = document.getDocumentElement();
		return "WMS_Capabilities".equals(element.getNodeName()) || "WMT_MS_Capabilities".equals(element.getNodeName()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void load(Document document, URL url, Intent intent, IXmlLoaderCallback callback)
	{
		try
		{
			//save the document into a byte array, so we can pass it as an InputStream to the WMSCapabilities constructor
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Source xmlSource = new DOMSource(document);
			Result outputTarget = new StreamResult(outputStream);
			TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
			InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

			WMSCapabilities wmsCapabilities = new WMSCapabilities(is).parse();
			if (wmsCapabilities == null)
			{
				throw new Exception("Error parsing WMS_Capabilities document from URL: " + url); //$NON-NLS-1$
			}
			WMSCapabilitiesCatalogTreeNode catalogTreeNode =
					new WMSCapabilitiesCatalogTreeNode(intent.getURI(), wmsCapabilities);
			callback.completed(catalogTreeNode, document, url, intent);
		}
		catch (Exception e)
		{
			callback.error(e, document, url, intent);
		}
	}
}
