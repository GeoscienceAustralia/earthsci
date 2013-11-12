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
package au.gov.ga.earthsci.catalog.directory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.eclipse.e4.core.contexts.IEclipseContext;

import au.gov.ga.earthsci.catalog.AbstractCatalogTreeNode;
import au.gov.ga.earthsci.core.url.SystemIconURLStreamHandlerService;
import au.gov.ga.earthsci.layer.intent.IntentLayerLoader;
import au.gov.ga.earthsci.layer.tree.ILayerNode;

/**
 * Catalog tree node that represents a file in a local filesystem.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FileCatalogTreeNode extends AbstractCatalogTreeNode
{
	private boolean iconURLLoaded = false;
	private URL iconURL;

	public FileCatalogTreeNode(URI fileURI)
	{
		super(fileURI);
	}

	@Override
	public boolean isRemoveable()
	{
		return false;
	}

	@Override
	public boolean isLayerNode()
	{
		return true;
	}

	@Override
	public void loadLayer(ILayerNode node, IEclipseContext context) throws Exception
	{
		IntentLayerLoader.load(getURI(), node, context);
	}

	@Override
	public String getName()
	{
		return new File(getURI()).getName();
	}

	@Override
	public URL getInformationURL()
	{
		return null;
	}

	@Override
	public String getInformationString()
	{
		//TODO
		return null;
	}

	@Override
	public URL getIconURL()
	{
		if (!iconURLLoaded)
		{
			try
			{
				iconURL = SystemIconURLStreamHandlerService.createURL(getURI());
			}
			catch (MalformedURLException e)
			{
			}
			iconURLLoaded = true;
		}
		return iconURL;
	}
}
