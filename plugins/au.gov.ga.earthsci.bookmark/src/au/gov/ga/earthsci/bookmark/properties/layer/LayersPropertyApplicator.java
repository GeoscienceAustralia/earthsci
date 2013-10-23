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

import javax.inject.Inject;

import au.gov.ga.earthsci.bookmark.IBookmarkPropertyAnimator;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyApplicator;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.layer.LayerNode;
import au.gov.ga.earthsci.layer.worldwind.ITreeModel;

/**
 * An {@link IBookmarkPropertyApplicator} used to apply layer state captured in
 * a {@link LayersProperty} instance.
 * <p/>
 * The semantics of the application are as follows:
 * <ol>
 * <li>Layers are matched purely on URI
 * <li>All layers that are in both the current layer model and the bookmark
 * property will be enabled, and the opacity from the property applied
 * <li>Any layer in the current layer model that does not appear in the bookmark
 * property will be disabled
 * <li>Any layer in the bookmark property that does not exist in the current
 * layer model will be ignored
 * </ol>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LayersPropertyApplicator implements IBookmarkPropertyApplicator
{

	private static final String[] SUPPORTED_TYPES = new String[] { LayersProperty.TYPE };

	@Inject
	private ITreeModel currentModel;

	@Override
	public String[] getSupportedTypes()
	{
		return SUPPORTED_TYPES;
	}

	@Override
	public void apply(IBookmarkProperty property)
	{
		if (property == null)
		{
			return;
		}

		LayersProperty layersProperty = (LayersProperty) property;

		for (Layer l : currentModel.getLayers())
		{
			if (!(l instanceof LayerNode))
			{
				continue;
			}

			LayerNode layerNode = (LayerNode) l;
			if (layersProperty.getLayerState().containsKey(layerNode.getURI()))
			{
				layerNode.setEnabled(true);
				layerNode.setOpacity(layersProperty.getLayerState().get(layerNode.getURI()));
			}
			else
			{
				layerNode.setEnabled(false);
			}
		}
	}

	@Override
	public IBookmarkPropertyAnimator createAnimator(IBookmarkProperty start, IBookmarkProperty end, long duration)
	{
		return new LayersPropertyAnimator(currentModel, (LayersProperty) start, (LayersProperty) end, duration);
	}

}
