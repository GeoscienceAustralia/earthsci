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

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import au.gov.ga.earthsci.core.tree.AbstractTreeNode;
import au.gov.ga.earthsci.core.tree.ITreeNode;

/**
 * Abstract implementation of the {@link ILayerTreeNode} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractLayerTreeNode extends AbstractTreeNode<AbstractLayerTreeNode> implements
		ILayerTreeNode<AbstractLayerTreeNode>
{
	private String name;
	private LayerList layerList;

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

	@Override
	public LayerList getLayerList()
	{
		if (layerList == null)
		{
			layerList = new LayerList();
			fillLayerList();
		}
		return layerList;
	}

	protected void fillLayerList()
	{
		synchronized (layerList)
		{
			layerList.removeAll();
			addNodesToLayerList(this);
		}
	}

	protected void addNodesToLayerList(AbstractLayerTreeNode node)
	{
		if (node instanceof Layer)
		{
			layerList.add((Layer) node);
		}
		for (ITreeNode<AbstractLayerTreeNode> child : node.getChildren())
		{
			addNodesToLayerList(child.getValue());
		}
	}

	@Override
	protected void setChildren(ITreeNode<AbstractLayerTreeNode>[] children)
	{
		ITreeNode<AbstractLayerTreeNode>[] oldChildren = getChildren();
		super.setChildren(children);
		childrenChanged(oldChildren, children);
	}

	protected void childrenChanged(ITreeNode<AbstractLayerTreeNode>[] oldChildren,
			ITreeNode<AbstractLayerTreeNode>[] newChildren)
	{
		if (layerList != null)
		{
			//update any nodes that actually have layer lists
			fillLayerList();
			//TODO should we implement a (more efficient?) modification of the list according to changed children?
		}
		if (!isRoot())
		{
			//recurse up to the root node
			getParent().getValue().childrenChanged(oldChildren, newChildren);
		}
	}
}
