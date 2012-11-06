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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import au.gov.ga.earthsci.core.persistence.Exportable;
import au.gov.ga.earthsci.core.persistence.Persistent;
import au.gov.ga.earthsci.core.tree.AbstractTreeNode;
import au.gov.ga.earthsci.core.tree.ITreeNode;
import au.gov.ga.earthsci.core.util.IEnableable;

/**
 * Abstract implementation of the {@link ILayerTreeNode} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Exportable
public abstract class AbstractLayerTreeNode extends AbstractTreeNode<ILayerTreeNode> implements ILayerTreeNode
{
	private String name;
	private LayerList layerList;
	private boolean lastAnyChildrenEnabled, lastAllChildrenEnabled;

	protected AbstractLayerTreeNode()
	{
		super();
		setValue(this);
		addPropertyChangeListener("enabled", new EnabledChangeListener()); //$NON-NLS-1$
	}

	@Persistent(attribute = true)
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

	@Persistent
	@Override
	public ITreeNode<ILayerTreeNode>[] getChildren()
	{
		return super.getChildren();
	}

	@Override
	public boolean isAnyChildrenEnabled()
	{
		return anyChildrenEnabledEquals(true);
	}

	@Override
	public boolean isAllChildrenEnabled()
	{
		return !anyChildrenEnabledEquals(false);
	}

	@Override
	public boolean anyChildrenEnabledEquals(boolean enabled)
	{
		if (this instanceof IEnableable)
		{
			IEnableable enableable = (IEnableable) this;
			if (enableable.isEnabled() == enabled)
				return true;
		}
		if (hasChildren())
		{
			for (ITreeNode<ILayerTreeNode> child : getChildren())
			{
				if (child.getValue().anyChildrenEnabledEquals(enabled))
					return true;
			}
		}
		return false;
	}

	@Override
	public void enableChildren(boolean enabled)
	{
		if (this instanceof IEnableable)
		{
			IEnableable enableable = (IEnableable) this;
			enableable.setEnabled(enabled);
		}
		if (hasChildren())
		{
			for (ITreeNode<ILayerTreeNode> child : getChildren())
			{
				child.getValue().enableChildren(enabled);
			}
		}
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

	private void addNodesToLayerList(ILayerTreeNode node)
	{
		if (node instanceof Layer)
		{
			layerList.add((Layer) node);
		}
		for (ITreeNode<ILayerTreeNode> child : node.getChildren())
		{
			addNodesToLayerList(child.getValue());
		}
	}

	@Override
	protected void setChildren(ITreeNode<ILayerTreeNode>[] children)
	{
		ITreeNode<ILayerTreeNode>[] oldChildren = getChildren();
		super.setChildren(children);
		childrenChanged(oldChildren, children);
	}

	@Override
	public void childrenChanged(ITreeNode<ILayerTreeNode>[] oldChildren, ITreeNode<ILayerTreeNode>[] newChildren)
	{
		if (layerList != null)
		{
			//update any nodes that actually have layer lists
			fillLayerList();
			//TODO should we implement a (more efficient?) modification of the list according to changed children?
		}
		fireAnyAllChildrenEnabledChanged();
		if (!isRoot())
		{
			//recurse up to the root node
			getParent().getValue().childrenChanged(oldChildren, newChildren);
		}
	}

	@Override
	public void enabledChanged()
	{
		fireAnyAllChildrenEnabledChanged();
		if (!isRoot())
		{
			//recurse up to the root node
			getParent().getValue().enabledChanged();
		}
	}

	private void fireAnyAllChildrenEnabledChanged()
	{
		firePropertyChange(
				"allChildrenEnabled", lastAllChildrenEnabled, lastAllChildrenEnabled = isAllChildrenEnabled()); //$NON-NLS-1$
		firePropertyChange(
				"anyChildrenEnabled", lastAnyChildrenEnabled, lastAnyChildrenEnabled = isAnyChildrenEnabled()); //$NON-NLS-1$
	}

	protected class EnabledChangeListener implements PropertyChangeListener
	{
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			enabledChanged();
		}
	}
}
