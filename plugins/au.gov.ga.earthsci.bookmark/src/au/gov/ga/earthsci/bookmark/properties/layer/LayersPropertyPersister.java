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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.bookmark.IBookmarkPropertyCreator;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyExporter;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.common.util.UTF8URLEncoder;
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
	private static final String OPACITY_ATTRIBUTE_NAME = "opacity"; //$NON-NLS-1$
	private static final String OPACITY_ATTRIBUTE_XPATH = "@" + OPACITY_ATTRIBUTE_NAME; //$NON-NLS-1$
	private static final String URI_ATTRIBUTE_NAME = "uri"; //$NON-NLS-1$
	private static final String URI_ATTRIBUTE_XPATH = "@" + URI_ATTRIBUTE_NAME; //$NON-NLS-1$
	private static final String LAYER_ELEMENT_NAME = "layer"; //$NON-NLS-1$
	private static final String LAYER_XPATH = "./layerState/layer"; //$NON-NLS-1$

	private static final String INVALID_TYPE_MSG = "LayersPropertyPersister can only be used for layers properties"; //$NON-NLS-1$

	private static Logger logger = LoggerFactory.getLogger(LayersPropertyPersister.class);

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

			result.addLayer(((ILayerTreeNode) l).getURI(), l.getOpacity());
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

			URI uri = null;
			try
			{
				uri = new URI(UTF8URLEncoder.decode(XMLUtil.getText(state, URI_ATTRIBUTE_XPATH, ""))); //$NON-NLS-1$
			}
			catch (URISyntaxException e)
			{
				logger.error("Layer URI {} is not a valid URI", XMLUtil.getText(state, URI_ATTRIBUTE_XPATH, "")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			Double opacity = XMLUtil.getDouble(state, OPACITY_ATTRIBUTE_XPATH, 1.0d);

			result.addLayer(uri, opacity);
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
		for (Entry<URI, Double> e : ((LayersProperty) property).getLayerState().entrySet())
		{
			Element state = XMLUtil.appendElement(layerStateElement, LAYER_ELEMENT_NAME);
			state.setAttribute(URI_ATTRIBUTE_NAME, UTF8URLEncoder.encode(e.getKey().toString()));
			state.setAttribute(OPACITY_ATTRIBUTE_NAME, Double.toString(e.getValue()));
		}
	}
}
