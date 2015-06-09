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
package au.gov.ga.earthsci.bookmark.properties.layer;

import gov.nasa.worldwind.layers.Layer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import au.gov.ga.earthsci.bookmark.IBookmarkPropertyCreator;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyExporter;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.common.util.Validate;
import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.layer.tree.ILayerTreeNode;
import au.gov.ga.earthsci.layer.worldwind.ITreeModel;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * A {@link IBookmarkPropertyCreator} and {@link IBookmarkPropertyExporter} for
 * the {@link LayersProperty}. Handles creation and persistence of the property.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LayersPropertyPersister implements IBookmarkPropertyCreator, IBookmarkPropertyExporter
{
	public static final String OPACITY_ATTRIBUTE_NAME = "opacity"; //$NON-NLS-1$
	private static final String OPACITY_ATTRIBUTE_XPATH = "@" + OPACITY_ATTRIBUTE_NAME; //$NON-NLS-1$
	private static final String ID_ATTRIBUTE_NAME = "id"; //$NON-NLS-1$
	private static final String ID_ATTRIBUTE_XPATH = "@" + ID_ATTRIBUTE_NAME; //$NON-NLS-1$
	private static final String LAYER_ELEMENT_NAME = "layer"; //$NON-NLS-1$
	private static final String LAYER_XPATH = "./layerState/layer"; //$NON-NLS-1$

	private static final String INVALID_TYPE_MSG = "LayersPropertyPersister can only be used for layers properties"; //$NON-NLS-1$

	@Inject
	private ITreeModel currentLayersModel;

	@Override
	public String[] getSupportedTypes()
	{
		return new String[] { LayersProperty.TYPE };
	}

	@Override
	public IBookmarkProperty createFromCurrentState(String type)
	{
		Validate.isTrue(LayersProperty.TYPE.equals(type), INVALID_TYPE_MSG);
		LayersProperty result = new LayersProperty();
		for (Layer l : currentLayersModel.getLayers())
		{
			if (!(l instanceof ILayerTreeNode) || !l.isEnabled())
			{
				continue;
			}

			result.addLayer(((ILayerTreeNode) l).getId(), l.getOpacity(), l.getName());
		}

		return result;
	}

	@Override
	public IBookmarkProperty createFromXML(String type, Element propertyElement)
	{
		Validate.isTrue(LayersProperty.TYPE.equals(type), INVALID_TYPE_MSG);
		Validate.notNull(propertyElement, "A property element is required"); //$NON-NLS-1$

		LayersProperty result = new LayersProperty();
		for (Element state : XmlUtil.getElements(propertyElement, LAYER_XPATH, null))
		{
			//			state.getAttributes()
			String id = XMLUtil.getText(state, ID_ATTRIBUTE_XPATH);
			Double opacity = XMLUtil.getDouble(state, OPACITY_ATTRIBUTE_XPATH, 1.0d);
			NamedNodeMap attrs = state.getAttributes();
			if (id != null)
			{
				List<Pair<String, String>> pairs = new ArrayList<>();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					String key = attrs.item(i).getNodeName();
					String value = attrs.item(i).getNodeValue();
					if (!key.equals(ID_ATTRIBUTE_NAME))
					{
						pairs.add(new ImmutablePair<String, String>(key, value));
					}
				}
				result.addLayer(id, pairs.toArray(new Pair[0]));
			}
		}

		return result;
	}

	@Override
	public void exportToXML(IBookmarkProperty property, Element parent)
	{
		if (property == null)
		{
			return;
		}
		Validate.isTrue(property.getType().equals(LayersProperty.TYPE), INVALID_TYPE_MSG);
		Validate.notNull(parent, "A property element is required"); //$NON-NLS-1$

		Element layerStateElement = XMLUtil.appendElement(parent, "layerState"); //$NON-NLS-1$

		for (Entry<String, Map<String, String>> layerInfo : ((LayersProperty) property).getLayerStateInfo().entrySet())
		{
			Element state = XMLUtil.appendElement(layerStateElement, LAYER_ELEMENT_NAME);
			state.setAttribute(ID_ATTRIBUTE_NAME, layerInfo.getKey());
			for (Entry<String, String> stateInfo : layerInfo.getValue().entrySet())
			{
				state.setAttribute(stateInfo.getKey(), stateInfo.getValue());
			}

		}

		//		for (Entry<String, Double> e : ((LayersProperty) property).getLayerState().entrySet())
		//		{
		//			Element state = XMLUtil.appendElement(layerStateElement, LAYER_ELEMENT_NAME);
		//			state.setAttribute(ID_ATTRIBUTE_NAME, e.getKey());
		//			state.setAttribute(OPACITY_ATTRIBUTE_NAME, Double.toString(e.getValue()));
		//		}
	}
}
