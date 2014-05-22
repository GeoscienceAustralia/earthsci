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
package au.gov.ga.earthsci.layer.tree;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import au.gov.ga.earthsci.common.collection.ArrayListHashMap;
import au.gov.ga.earthsci.common.collection.ListMap;
import au.gov.ga.earthsci.common.persistence.Exportable;
import au.gov.ga.earthsci.common.persistence.Persistent;
import au.gov.ga.earthsci.common.util.IEnableable;
import au.gov.ga.earthsci.common.util.IInformationed;
import au.gov.ga.earthsci.core.model.IModelStatus;
import au.gov.ga.earthsci.core.model.ModelStatus;
import au.gov.ga.earthsci.core.tree.AbstractTreeNode;
import au.gov.ga.earthsci.core.worldwind.WorldWindCompoundElevationModel;
import au.gov.ga.earthsci.layer.DrawOrder;
import au.gov.ga.earthsci.worldwind.common.layers.Bounds;

/**
 * Abstract implementation of the {@link ILayerTreeNode} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Exportable
public abstract class AbstractLayerTreeNode extends AbstractTreeNode<ILayerTreeNode> implements ILayerTreeNode
{
	private String id;
	private String name;
	private LayerList layerList;
	private List<Layer> unsortedLayers;
	private List<Layer> sortedLayers;
	private WorldWindCompoundElevationModel elevationModels;
	private ListMap<URI, ILayerTreeNode> catalogUriMap;
	private boolean lastAnyChildrenEnabled, lastAllChildrenEnabled;
	private String label;
	private URI catalogURI;
	private URL nodeInformationURL;
	private URL legendURL;
	private URL iconURL;
	private boolean expanded;
	private final Object semaphore = new Object();
	private IModelStatus status = ModelStatus.ok(null);

	protected AbstractLayerTreeNode()
	{
		super(ILayerTreeNode.class);
		id = UUID.randomUUID().toString();
		addPropertyChangeListener("enabled", new EnabledChangeListener()); //$NON-NLS-1$
	}

	@Persistent
	@Override
	public String getId()
	{
		return id;
	}

	private void setId(String id)
	{
		this.id = id;
	}

	@Override
	public void setIdFrom(ILayerNode node)
	{
		setId(node.getId());
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
		firePropertyChange("name", getName(), this.name = name); //$NON-NLS-1$
	}

	@Persistent(attribute = true)
	@Override
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
		firePropertyChange("label", getLabel(), this.label = label); //$NON-NLS-1$
	}

	@Override
	public String getLabelOrName()
	{
		return getLabel() == null ? getName() : getLabel();
	}

	@Persistent(name = "catalogURI")
	@Override
	public URI getCatalogURI()
	{
		return catalogURI;
	}

	@Override
	public void setCatalogURI(URI catalogURI)
	{
		firePropertyChange("catalogURI", getCatalogURI(), this.catalogURI = catalogURI); //$NON-NLS-1$
	}

	/**
	 * The URL pointing to this node's information page.
	 * <p/>
	 * If this node is a layer node and the associated layer implements
	 * {@link IInformationed}, then the layer's information URL should be used
	 * for information instead of this one.
	 * 
	 * @return The URL pointing to this node's information page.
	 */
	@Persistent(name = "infoURL")
	public URL getNodeInformationURL()
	{
		return nodeInformationURL;
	}

	public void setNodeInformationURL(URL nodeInformationURL)
	{
		firePropertyChange("nodeInformationURL", getNodeInformationURL(), this.nodeInformationURL = nodeInformationURL); //$NON-NLS-1$
	}

	@Override
	public URL getInformationURL()
	{
		return getNodeInformationURL();
	}

	@Override
	public String getInformationString()
	{
		return LayerNodeDescriber.describe(this);
	}

	@Override
	public Bounds getBounds()
	{
		Bounds bounds = null;
		for (ILayerTreeNode child : getChildren())
		{
			bounds = Bounds.union(bounds, child.getBounds());
		}
		return bounds;
	}

	@Override
	public boolean isFollowTerrain()
	{
		//if any layers don't follow the terrain, then return false
		for (ILayerTreeNode child : getChildren())
		{
			if (!child.isFollowTerrain())
			{
				return false;
			}
		}
		return true;
	}

	@Persistent
	@Override
	public URL getLegendURL()
	{
		return legendURL;
	}

	public void setLegendURL(URL legendURL)
	{
		firePropertyChange("legendURL", getLegendURL(), this.legendURL = legendURL); //$NON-NLS-1$
	}

	@Persistent
	@Override
	public URL getIconURL()
	{
		return iconURL;
	}

	public void setIconURL(URL iconURL)
	{
		firePropertyChange("iconURL", getIconURL(), this.iconURL = iconURL); //$NON-NLS-1$
	}

	@Persistent(name = "children")
	public ILayerTreeNode[] getChildrenAsArray()
	{
		List<ILayerTreeNode> children = getChildren();
		return getChildren().toArray(new ILayerTreeNode[children.size()]);
	}

	public void setChildrenAsArray(ILayerTreeNode[] children)
	{
		setChildren(Arrays.asList(children));
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
			{
				return true;
			}
		}
		if (hasChildren())
		{
			for (ILayerTreeNode child : getChildren())
			{
				if (child.anyChildrenEnabledEquals(enabled))
				{
					return true;
				}
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
			for (ILayerTreeNode child : getChildren())
			{
				child.enableChildren(enabled);
			}
		}
	}

	@Override
	public LayerList getLayers()
	{
		synchronized (semaphore)
		{
			if (layerList == null)
			{
				layerList = new LayerList();
				unsortedLayers = new ArrayList<Layer>();
				sortedLayers = new ArrayList<Layer>();
				updateLayers();
			}
			return layerList;
		}
	}

	@Override
	public void updateLayers()
	{
		synchronized (semaphore)
		{
			if (layerList != null)
			{
				layerList.removeAll();
				unsortedLayers.clear();
				sortedLayers.clear();
				addLayerNodesToList(this, unsortedLayers);
				DrawOrder.sortLayers(unsortedLayers, sortedLayers);
				layerList.addAll(sortedLayers);
			}
		}
		if (!isRoot())
		{
			getParent().updateLayers();
		}
	}

	private static void addLayerNodesToList(ILayerTreeNode node, List<Layer> list)
	{
		if (node instanceof Layer)
		{
			list.add((Layer) node);
		}
		for (ILayerTreeNode child : node.getChildren())
		{
			addLayerNodesToList(child, list);
		}
	}

	@Override
	public ILayerTreeNode getNodeForCatalogURI(URI catalogURI)
	{
		synchronized (semaphore)
		{
			if (catalogUriMap == null)
			{
				catalogUriMap = new ArrayListHashMap<URI, ILayerTreeNode>();
				updateCatalogURIMap();
			}
			List<ILayerTreeNode> nodes = catalogUriMap.get(catalogURI);
			if (nodes != null && nodes.size() > 0)
			{
				return nodes.get(0);
			}
			return null;
		}
	}

	@Override
	public CompoundElevationModel getElevationModels()
	{
		synchronized (semaphore)
		{
			if (elevationModels == null)
			{
				elevationModels = new WorldWindCompoundElevationModel();
				updateElevationModels();
			}
			return elevationModels;
		}
	}

	@Override
	public void updateElevationModels()
	{
		synchronized (semaphore)
		{
			if (elevationModels != null)
			{
				elevationModels.removeAll();
				addElevationModelNodesToCompoundElevationModel(this, elevationModels);
			}
		}
		if (!isRoot())
		{
			getParent().updateElevationModels();
		}
	}

	private static void addElevationModelNodesToCompoundElevationModel(ILayerTreeNode node,
			CompoundElevationModel compound)
	{
		if (node instanceof ILayerNode)
		{
			ILayerNode layerNode = (ILayerNode) node;
			ElevationModel elevationModel = layerNode.getElevationModel();
			if (elevationModel != null)
			{
				compound.addElevationModel(elevationModel);
			}
		}
		for (ILayerTreeNode child : node.getChildren())
		{
			addElevationModelNodesToCompoundElevationModel(child, compound);
		}
	}

	private void updateCatalogURIMap()
	{
		synchronized (semaphore)
		{
			if (catalogUriMap != null)
			{
				catalogUriMap.clear();
				addNodesToCatalogURIMap(this);
			}
		}
	}

	private void addNodesToCatalogURIMap(ILayerTreeNode node)
	{
		URI catalogUri = node.getCatalogURI();
		if (catalogUri != null)
		{
			catalogUriMap.putSingle(node.getCatalogURI(), node);
		}
		for (ILayerTreeNode child : node.getChildren())
		{
			addNodesToCatalogURIMap(child);
		}
	}

	@Override
	protected void fireChildrenPropertyChange(List<ILayerTreeNode> oldChildren, List<ILayerTreeNode> newChildren)
	{
		childrenChanged(oldChildren, children);
		super.fireChildrenPropertyChange(oldChildren, newChildren);
	}

	@Override
	public void childrenChanged(List<ILayerTreeNode> oldChildren, List<ILayerTreeNode> newChildren)
	{
		//TODO should we implement a (more efficient?) modification of these collections according to changed children?
		//update the collections if they exist
		updateLayers();
		updateElevationModels();
		updateCatalogURIMap();

		//fire property changes
		fireAnyAllChildrenEnabledChanged();

		//recurse up to the root node
		if (!isRoot())
		{
			getParent().childrenChanged(oldChildren, newChildren);
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
			getParent().enabledChanged();
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
		firePropertyChange("expanded", isExpanded(), this.expanded = expanded); //$NON-NLS-1$
	}

	@Override
	public IModelStatus getStatus()
	{
		return status;
	}

	@Override
	public void setStatus(IModelStatus status)
	{
		firePropertyChange("status", getStatus(), this.status = status); //$NON-NLS-1$
	}
}
