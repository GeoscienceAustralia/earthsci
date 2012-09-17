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
package au.gov.ga.earthsci.core.model.layer;

import au.gov.ga.earthsci.core.tree.AbstractTreeNode;

/**
 * Abstract implementation of the {@link ILayerTreeNode} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractLayerTreeNode extends AbstractTreeNode<ILayerTreeNode> implements ILayerTreeNode
{
	private String name;

	protected AbstractLayerTreeNode()
	{
		super();
		setValue(this);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		String oldValue = getName();
		this.name = name;
		firePropertyChange("name", oldValue, name); //$NON-NLS-1$
	}
}
