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
package au.gov.ga.earthsci.catalog.dataset;

import java.net.URL;

import org.w3c.dom.Document;

import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.xml.IXmlLoader;
import au.gov.ga.earthsci.core.xml.IXmlLoaderCallback;
import au.gov.ga.earthsci.core.xml.IXmlLoaderFilter;
import au.gov.ga.earthsci.intent.Intent;

/**
 * {@link IXmlLoader} for dataset XML files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DatasetXmlLoader implements IXmlLoader, IXmlLoaderFilter
{
	@Override
	public boolean canLoad(Document document, Intent intent)
	{
		return "DatasetList".equalsIgnoreCase(document.getDocumentElement().getNodeName()); //$NON-NLS-1$
	}

	@Override
	public void load(Document document, URL url, Intent intent, IXmlLoaderCallback callback)
	{
		try
		{
			ICatalogTreeNode catalogTreeNode = DatasetReader.read(document, url);
			callback.completed(catalogTreeNode, document, url, intent);
		}
		catch (Exception e)
		{
			callback.error(e, document, url, intent);
		}
	}
}
