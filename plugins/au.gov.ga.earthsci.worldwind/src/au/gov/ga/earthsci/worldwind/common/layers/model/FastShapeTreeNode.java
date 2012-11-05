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

import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;
import gov.nasa.worldwind.util.tree.BasicTreeNode;
import gov.nasa.worldwind.util.tree.TreeNode;

/**
 * {@link TreeNode} used to enable/disable a FastShape. Used for displaying
 * {@link FastShape}s associated with a {@link ModelLayer} in a hierarchical
 * tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FastShapeTreeNode extends BasicTreeNode
{
	private final FastShape shape;

	public FastShapeTreeNode(FastShape shape)
	{
		super(shape.getName());
		this.shape = shape;
	}

	@Override
	public boolean isSelected()
	{
		return shape.isEnabled();
	}

	@Override
	public void setSelected(boolean selected)
	{
		super.setSelected(selected);
		shape.setEnabled(selected);
	}

	public FastShape getShape()
	{
		return shape;
	}
}
