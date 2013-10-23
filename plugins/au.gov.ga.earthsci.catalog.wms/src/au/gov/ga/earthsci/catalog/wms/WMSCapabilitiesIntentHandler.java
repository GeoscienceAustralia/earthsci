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
import gov.nasa.worldwind.util.WWXML;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.w3c.dom.Document;

import au.gov.ga.earthsci.catalog.CatalogLayerHelper;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.intent.AbstractRetrieveIntentHandler;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.layer.FolderNode;
import au.gov.ga.earthsci.layer.LayerNode;
import au.gov.ga.earthsci.layer.intent.IntentLayerLoader;

/**
 * Intent handler that handles WMS capabilities documents.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WMSCapabilitiesIntentHandler extends AbstractRetrieveIntentHandler
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
			load(document, url, intent, callback);
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

	protected void load(Document document, URL url, Intent intent, IIntentCallback callback)
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

			if (!ICatalogTreeNode.class.equals(intent.getExpectedReturnType()))
			{
				WMSLayerCapabilitiesCatalogTreeNode singleLayer = catalogTreeNode.getSingleLayer();
				if (singleLayer != null)
				{
					FolderNode folder = CatalogLayerHelper.createFolderNode(catalogTreeNode);
					LayerNode layer = CatalogLayerHelper.createLayerNode(singleLayer);
					folder.addChild(layer);
					IntentLayerLoader.load(layer, context);
					callback.completed(folder, intent);
					return;
				}
			}

			callback.completed(catalogTreeNode, intent);
		}
		catch (Exception e)
		{
			callback.error(e, intent);
		}
	}
}
