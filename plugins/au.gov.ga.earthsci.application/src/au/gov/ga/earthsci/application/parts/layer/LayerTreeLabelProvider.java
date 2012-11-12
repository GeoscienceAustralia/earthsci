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
package au.gov.ga.earthsci.application.parts.layer;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;

import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;

/**
 * Label provider for the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTreeLabelProvider extends ObservableMapLabelProvider
{
	public LayerTreeLabelProvider(IObservableMap attributeMap)
	{
		super(attributeMap);
	}

	public LayerTreeLabelProvider(IObservableMap[] attributeMaps)
	{
		super(attributeMaps);
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		ILayerTreeNode layerTreeNode = (ILayerTreeNode) element;
		String label;
		if (element instanceof LayerNode)
		{
			LayerNode layer = (LayerNode) element;
			label = layerTreeNode.getLabelOrName();
			if (layer.getOpacity() < 1)
			{
				label += String.format(" (%d%%)", (int) (layer.getOpacity() * 100)); //$NON-NLS-1$
			}
		}
		else if (element instanceof FolderNode)
		{
			FolderNode folder = (FolderNode) element;
			label = folder.getName();
		}
		else
		{
			label = element.toString();
		}
		return label;
	}
}
