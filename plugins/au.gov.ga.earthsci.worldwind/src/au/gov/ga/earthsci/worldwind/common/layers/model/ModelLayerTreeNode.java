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
package au.gov.ga.earthsci.worldwind.common.layers.model;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.layertree.LayerTreeNode;
import gov.nasa.worldwind.util.tree.TreeNode;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;

/**
 * {@link TreeNode} representing a {@link ModelLayer}, for displaying the
 * {@link ModelLayer} in a hierarchical tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ModelLayerTreeNode extends LayerTreeNode
{
	public ModelLayerTreeNode(Layer layer)
	{
		super(layer);
	}

	public void addChild(FastShape shape)
	{
		addChild(new FastShapeTreeNode(shape));
	}

	public void removeChild(FastShape shape)
	{
		TreeNode childToRemove = null;
		for (TreeNode node : children)
		{
			if (node instanceof FastShapeTreeNode)
			{
				if (((FastShapeTreeNode) node).getShape() == shape)
				{
					childToRemove = node;
				}
			}
		}
		if (childToRemove != null)
		{
			removeChild(childToRemove);
		}
	}
}
