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
package au.gov.ga.earthsci.layer.ui;

import org.eclipse.jface.viewers.ICheckStateProvider;

import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;

/**
 * {@link ICheckStateProvider} implementation for the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTreeCheckStateProvider implements ICheckStateProvider
{
	@Override
	public boolean isGrayed(Object element)
	{
		if (element instanceof ILayerTreeNode)
		{
			ILayerTreeNode node = (ILayerTreeNode) element;
			return !node.isAllChildrenEnabled();
		}
		return false;
	}

	@Override
	public boolean isChecked(Object element)
	{
		if (element instanceof ILayerTreeNode)
		{
			ILayerTreeNode node = (ILayerTreeNode) element;
			return node.isAnyChildrenEnabled();
		}
		return false;
	}
}
