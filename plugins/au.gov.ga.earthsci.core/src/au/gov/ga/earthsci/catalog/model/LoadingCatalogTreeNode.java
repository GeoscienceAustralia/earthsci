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
package au.gov.ga.earthsci.catalog.model;

import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.content.IContentType;

/**
 * {@link ICatalogTreeNode} that represents a loading node as a child of a node
 * that loads its children lazily.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LoadingCatalogTreeNode extends AbstractCatalogTreeNode
{
	public LoadingCatalogTreeNode()
	{
		this(null);
	}

	public LoadingCatalogTreeNode(URI nodeURI)
	{
		super(nodeURI);
	}

	@Override
	public boolean isRemoveable()
	{
		return false;
	}

	@Override
	public boolean isLayerNode()
	{
		return false;
	}

	@Override
	public URI getLayerURI()
	{
		return null;
	}

	@Override
	public IContentType getLayerContentType()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return "Loading...";
	}

	@Override
	public URL getInformationURL()
	{
		return null;
	}

	@Override
	public String getInformationString()
	{
		return null;
	}
}
