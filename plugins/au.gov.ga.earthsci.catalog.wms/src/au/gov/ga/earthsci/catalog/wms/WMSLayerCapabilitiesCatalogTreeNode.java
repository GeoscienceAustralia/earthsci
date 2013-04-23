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

import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

import au.gov.ga.earthsci.catalog.AbstractCatalogTreeNode;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;

/**
 * {@link ICatalogTreeNode} representing layers from a WMS server.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WMSLayerCapabilitiesCatalogTreeNode extends AbstractCatalogTreeNode
{
	protected final URI capabilitiesURI;
	protected final WMSLayerCapabilities layer;
	protected final WMSLayerStyle style;
	protected final boolean useStyleTitleInName;

	public WMSLayerCapabilitiesCatalogTreeNode(URI nodeURI, URI capabilitiesURI, WMSLayerCapabilities layer)
	{
		this(nodeURI, capabilitiesURI, layer, null, false);
	}

	protected WMSLayerCapabilitiesCatalogTreeNode(URI nodeURI, URI capabilitiesURI, WMSLayerCapabilities layer,
			WMSLayerStyle style, boolean useStyleTitleInName)
	{
		super(nodeURI);
		this.capabilitiesURI = capabilitiesURI;
		this.layer = layer;
		this.style = style;
		this.useStyleTitleInName = useStyleTitleInName;
		initChildren();
	}

	protected void initChildren()
	{
		for (WMSLayerCapabilities childLayer : layer.getLayers())
		{
			URI childURI = WMSHelper.uriSubpath(getURI(), childLayer.getName());

			//if the child layer only has a single style, use it
			WMSLayerStyle style = null;
			if (childLayer.getStyles() != null && childLayer.getStyles().size() == 1)
			{
				style = childLayer.getStyles().iterator().next();
			}

			WMSLayerCapabilitiesCatalogTreeNode childNode =
					new WMSLayerCapabilitiesCatalogTreeNode(childURI, capabilitiesURI, childLayer, style, false);
			addChild(childNode);
		}

		//if this layer has multiple styles, add them as children
		if (layer.getStyles() != null && layer.getStyles().size() > 1)
		{
			for (WMSLayerStyle style : layer.getStyles())
			{
				URI childURI = WMSHelper.uriSubpath(getURI(), style.getName());
				WMSLayerCapabilitiesCatalogTreeNode childNode =
						new WMSLayerCapabilitiesCatalogTreeNode(childURI, capabilitiesURI, layer, style, true);
				addChild(childNode);
			}
		}
	}

	@Override
	public boolean isRemoveable()
	{
		return false;
	}

	@Override
	public boolean isLayerNode()
	{
		return getChildCount() == 0;
	}

	@Override
	public URI getLayerURI()
	{
		try
		{
			return WMSHelper.generateLayerURI(capabilitiesURI, layer, style);
		}
		catch (URISyntaxException e)
		{
			return null;
		}
	}

	@Override
	public IContentType getLayerContentType()
	{
		return Platform.getContentTypeManager().getContentType(WMSHelper.WMS_LAYER_CONTENT_TYPE_ID);
	}

	@Override
	public String getName()
	{
		String name = layer.getTitle();
		if (style != null && useStyleTitleInName)
		{
			name += " - " + style.getTitle(); //$NON-NLS-1$
		}
		return name;
	}

	@Override
	public URL getInformationURL()
	{
		//TODO
		return null;
	}

	@Override
	public String getInformationString()
	{
		//TODO
		return null;
	}
}
