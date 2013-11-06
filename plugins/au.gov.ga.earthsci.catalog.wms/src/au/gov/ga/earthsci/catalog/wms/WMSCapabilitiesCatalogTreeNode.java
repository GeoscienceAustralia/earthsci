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
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;

import java.net.URI;
import java.net.URL;
import java.util.List;

import au.gov.ga.earthsci.catalog.AbstractCatalogTreeNode;
import au.gov.ga.earthsci.catalog.ErrorCatalogTreeNode;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.common.util.UTF8URLEncoder;

/**
 * {@link ICatalogTreeNode} for the root node of a WMS server. Contains the root
 * WMSCapabilities object, and the layers as child nodes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WMSCapabilitiesCatalogTreeNode extends AbstractCatalogTreeNode
{
	protected final WMSCapabilities capabilities;

	public WMSCapabilitiesCatalogTreeNode(URI capabilitiesURI, WMSCapabilities capabilities)
	{
		super(capabilitiesURI);
		this.capabilities = capabilities;
		initChildren();
	}

	protected void initChildren()
	{
		List<WMSLayerCapabilities> layerCapabilitiesList =
				capabilities.getCapabilityInformation() == null ? null : capabilities.getCapabilityInformation()
						.getLayerCapabilities();
		if (layerCapabilitiesList == null || layerCapabilitiesList.isEmpty())
		{
			addChild(new ErrorCatalogTreeNode(new Exception("No layers found")));
		}
		else
		{
			for (WMSLayerCapabilities layerCapabilities : layerCapabilitiesList)
			{
				URI childURI = WMSHelper.uriSubpath(getURI(), layerCapabilities.getName());
				WMSLayerCapabilitiesCatalogTreeNode childNode =
						new WMSLayerCapabilitiesCatalogTreeNode(childURI, getURI(), layerCapabilities);
				addChild(childNode);
			}
		}
	}

	@Override
	public boolean isRemoveable()
	{
		return true;
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
	public String getName()
	{
		return UTF8URLEncoder.decode(getURI().toASCIIString());
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

	/**
	 * @return If this WMS server only publishes a single layer, return the
	 *         catalog tree node that represents the layer
	 */
	public WMSLayerCapabilitiesCatalogTreeNode getSingleLayer()
	{
		try
		{
			return getSingleLayer(this, null);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private static WMSLayerCapabilitiesCatalogTreeNode getSingleLayer(ICatalogTreeNode parent,
			WMSLayerCapabilitiesCatalogTreeNode found) throws Exception
	{
		List<ICatalogTreeNode> children = parent.getChildren();
		for (ICatalogTreeNode child : children)
		{
			if (child instanceof WMSLayerCapabilitiesCatalogTreeNode)
			{
				WMSLayerCapabilitiesCatalogTreeNode layerChild = (WMSLayerCapabilitiesCatalogTreeNode) child;
				if (layerChild.isLayerNode())
				{
					if (found != null)
					{
						throw new Exception();
					}
					found = layerChild;
				}
			}
			//recurse
			WMSLayerCapabilitiesCatalogTreeNode childFound = getSingleLayer(child, found);
			if (childFound != null)
			{
				found = childFound;
			}
		}
		return found;
	}
}
