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
import java.net.URI;
import java.net.URL;
import java.util.List;

import au.gov.ga.earthsci.core.persistence.Exportable;
import au.gov.ga.earthsci.core.persistence.Persistent;
import au.gov.ga.earthsci.core.tree.AbstractTreeNode;
import au.gov.ga.earthsci.core.tree.ITreeNode;
import au.gov.ga.earthsci.core.util.IEnableable;
import au.gov.ga.earthsci.core.util.MultiMap;

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
	private MultiMap<URI, ILayerTreeNode> uriMap;
	private boolean lastAnyChildrenEnabled, lastAllChildrenEnabled;
	private String label;
	private URI uri;
	private URL infoUrl;
	private URL legendUrl;
	private URL iconUrl;
	private boolean expanded;

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

	@Override
	@Persistent(attribute = true)
	public String getLabel()
	{
		return label;
	}

	@Override
	public void setLabel(String label)
	{
		if (getName() != null && getName().equals(label))
		{
			setLabel(null);
			return;
		}

		String oldValue = getLabel();
		this.label = label;
		firePropertyChange("label", oldValue, label); //$NON-NLS-1$
	}

	@Override
	public String getLabelOrName()
	{
		return getLabel() == null ? getName() : getLabel();
	}

	@Persistent(name = "uri")
	@Override
	public URI getURI()
	{
		return uri;
	}

	@Override
	public void setURI(URI uri)
	{
		this.uri = uri;
	}

	@Persistent
	@Override
	public URL getInfoURL()
	{
		return infoUrl;
	}

	public void setInfoURL(URL infoURL)
	{
		this.infoUrl = infoURL;
	}

	@Persistent
	@Override
	public URL getLegendURL()
	{
		return legendUrl;
	}

	public void setLegendURL(URL legendURL)
	{
		this.legendUrl = legendURL;
	}

	@Persistent
	@Override
	public URL getIconURL()
	{
		return iconUrl;
	}

	public void setIconURL(URL iconURL)
	{
		this.iconUrl = iconURL;
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

	private void fillLayerList()
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
	public ILayerTreeNode[] getNodesForURI(URI uri)
	{
		if (uriMap == null)
		{
			uriMap = new MultiMap<URI, ILayerTreeNode>();
			fillURIMap();
		}
		List<ILayerTreeNode> nodes = uriMap.get(uri);
		if (nodes != null)
		{
			return nodes.toArray(new ILayerTreeNode[nodes.size()]);
		}
		return new ILayerTreeNode[0];
	}

	private void fillURIMap()
	{
		synchronized (uriMap)
		{
			uriMap.clear();
			addNodesToURIMap(this);
		}
	}

	private void addNodesToURIMap(ILayerTreeNode node)
	{
		uriMap.putSingle(node.getURI(), node);
		for (ITreeNode<ILayerTreeNode> child : node.getChildren())
		{
			addNodesToURIMap(child.getValue());
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
		//TODO should we implement a (more efficient?) modification of these collections according to changed children?
		//update the collections if they exist
		if (layerList != null)
		{
			fillLayerList();
		}
		if (uriMap != null)
		{
			fillURIMap();
		}

		//fire property changes
		fireAnyAllChildrenEnabledChanged();
		firePropertyChange("descendants", null, this); //$NON-NLS-1$

		//recurse up to the root node
		if (!isRoot())
		{
			getParent().getValue().childrenChanged(oldChildren, newChildren);
		}
	}

	@Override
	public void enabledChanged()
	{
		//fire property changes
		fireAnyAllChildrenEnabledChanged();
		
		//recurse up to the root node
		if (!isRoot())
		{
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

	private class EnabledChangeListener implements PropertyChangeListener
	{
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			enabledChanged();
		}
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " {" + getLabelOrName() + "}"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Persistent(attribute = true)
	@Override
	public boolean isExpanded()
	{
		return expanded;
	}

	@Override
	public void setExpanded(boolean expanded)
	{
		boolean oldValue = isExpanded();
		this.expanded = expanded;
		firePropertyChange("expanded", oldValue, label); //$NON-NLS-1$
	}
}
