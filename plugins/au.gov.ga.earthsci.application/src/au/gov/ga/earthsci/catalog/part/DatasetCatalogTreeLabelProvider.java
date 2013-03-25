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
package au.gov.ga.earthsci.catalog.part;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.catalog.model.ICatalogTreeNode;
import au.gov.ga.earthsci.catalog.model.dataset.DatasetCatalogTreeNode;

/**
 * An {@link ICatalogTreeLabelProvider} that supports {@link DatasetCatalogTreeNode}s
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DatasetCatalogTreeLabelProvider implements ICatalogTreeLabelProvider
{

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(DatasetCatalogTreeLabelProvider.class);

	@Override
	public boolean supports(ICatalogTreeNode node)
	{
		return node instanceof DatasetCatalogTreeNode;
	}

	@Override
	public URL getIconURL(ICatalogTreeNode node)
	{
		DatasetCatalogTreeNode datasetNode = (DatasetCatalogTreeNode)node;
		
		return datasetNode.getIconURL() == null ? CatalogTreeLabelProviderRegistry.getDefaultProvider().getIconURL(datasetNode) : datasetNode.getIconURL();
	}

	@Override
	public String getLabel(ICatalogTreeNode node)
	{
		return node.getLabelOrName();
	}

	@Override
	public URL getInfoURL(ICatalogTreeNode node)
	{
		return ((DatasetCatalogTreeNode)node).getInfoURL();
	}

	@Override
	public void dispose()
	{
	}
	
}
