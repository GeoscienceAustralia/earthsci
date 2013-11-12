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
package au.gov.ga.earthsci.catalog.dataset;

import java.net.URI;
import java.net.URL;

import org.eclipse.e4.core.contexts.IEclipseContext;

import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.common.persistence.Exportable;
import au.gov.ga.earthsci.common.persistence.Persistent;
import au.gov.ga.earthsci.layer.intent.IntentLayerLoader;
import au.gov.ga.earthsci.layer.tree.ILayerNode;

/**
 * An {@link ICatalogTreeNode} that represents a {@code Layer} element from the
 * legacy {@code dataset.xml}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Exportable
public class DatasetLayerCatalogTreeNode extends DatasetCatalogTreeNode
{
	@Persistent
	private URL layerURL;

	private boolean def;
	private boolean enabled;

	public DatasetLayerCatalogTreeNode(final URI nodeURI, final String name, final URL url, final URL infoURL,
			final URL iconURL, final boolean base, final boolean def, final boolean enabled)
	{
		super(nodeURI, name, infoURL, iconURL, base);

		this.layerURL = url;
		this.def = def;
		this.enabled = enabled;
	}

	@Override
	public boolean hasChildren()
	{
		return false;
	}

	@Override
	public int getChildCount()
	{
		return 0;
	}

	public boolean isDefault()
	{
		return def;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public URL getLayerUrl()
	{
		return layerURL;
	}

	@Override
	public boolean isLayerNode()
	{
		return true;
	}

	@Override
	public void loadLayer(ILayerNode node, IEclipseContext context) throws Exception
	{
		IntentLayerLoader.load(layerURL.toURI(), node, context);
	}
}
