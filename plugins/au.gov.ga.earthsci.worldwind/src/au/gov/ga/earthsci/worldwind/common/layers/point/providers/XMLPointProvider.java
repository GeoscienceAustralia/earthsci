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
package au.gov.ga.earthsci.worldwind.common.layers.point.providers;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.gov.ga.earthsci.worldwind.common.layers.Bounds;
import au.gov.ga.earthsci.worldwind.common.layers.point.PointLayer;
import au.gov.ga.earthsci.worldwind.common.layers.point.PointProvider;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * {@link PointProvider} implementation which loads points from an XML element.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class XMLPointProvider implements PointProvider
{
	private List<Position> points = new ArrayList<Position>();
	private List<AVList> attributes = new ArrayList<AVList>();
	private Bounds bounds = null;
	private boolean added = false;

	public XMLPointProvider(Element element)
	{
		if (element == null)
		{
			return;
		}
		
		XPath xpath = XMLUtil.makeXPath();
		Element[] pointElements = XMLUtil.getElements(element, "Points/Point", xpath);
		if (pointElements != null)
		{
			for (Element pointElement : pointElements)
			{
				Position position = XMLUtil.getPosition(pointElement, null, xpath);
				if (position != null)
				{
					points.add(position);
					bounds = Bounds.union(bounds, position);

					AVList attributes = new AVListImpl();
					this.attributes.add(attributes);
					NamedNodeMap elementAttributes = pointElement.getAttributes();
					for (int i = 0; i < elementAttributes.getLength(); i++)
					{
						Node child = elementAttributes.item(i);
						attributes.setValue(child.getNodeName(), child.getTextContent());
					}
					NodeList children = pointElement.getChildNodes();
					for (int i = 0; i < children.getLength(); i++)
					{
						Node child = children.item(i);
						attributes.setValue(child.getNodeName(), child.getTextContent());
					}
				}
			}
		}
	}

	@Override
	public Bounds getBounds()
	{
		return bounds;
	}

	@Override
	public boolean isFollowTerrain()
	{
		return true;
	}

	@Override
	public void requestData(PointLayer layer)
	{
		if (added)
		{
			return;
		}

		added = true;
		for (int i = 0; i < points.size(); i++)
		{
			layer.addPoint(points.get(i), attributes.get(i));
		}
		layer.loadComplete();
	}

	@Override
	public boolean isLoading()
	{
		return false;
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		//do nothing, as this provider is never loading
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		//do nothing, as this provider is never loading
	}
}
