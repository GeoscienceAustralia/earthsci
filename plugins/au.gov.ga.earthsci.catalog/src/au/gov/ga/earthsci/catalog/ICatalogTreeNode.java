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
package au.gov.ga.earthsci.catalog;

import java.net.URI;
import java.net.URL;

import org.eclipse.e4.core.contexts.IEclipseContext;

import au.gov.ga.earthsci.common.util.IInformationed;
import au.gov.ga.earthsci.common.util.ILabelable;
import au.gov.ga.earthsci.common.util.INamed;
import au.gov.ga.earthsci.common.util.IPropertyChangeBean;
import au.gov.ga.earthsci.core.tree.ITreeNode;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.layer.IPersistentLayer;
import au.gov.ga.earthsci.layer.intent.IntentLayerLoader;
import au.gov.ga.earthsci.layer.tree.ILayerNode;

/**
 * Represents tree nodes in the Catalog tree. Implementations of this interface
 * could be used for creating catalogs from different sources, such as XML
 * datasets, GOCAD project files, WMS catalogs, etc.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ICatalogTreeNode extends ITreeNode<ICatalogTreeNode>, IPropertyChangeBean, INamed, ILabelable,
		IInformationed
{
	/**
	 * @return Is this catalog node removeable from the Catalog tree? All user
	 *         added nodes should also be removeable (ie the root level of the
	 *         catalog).
	 */
	boolean isRemoveable();

	/**
	 * @return A URI that uniquely identifies this node in a catalog model. One
	 *         should be able to re-create the node from this URI.
	 */
	URI getURI();

	/**
	 * This method can be called many times during icon load, and therefore the
	 * result should be cached.
	 * 
	 * @return URL pointing to the icon to display for this node
	 */
	URL getIconURL();

	/**
	 * Return whether this node represents a layer that can be applied to the
	 * layer model
	 * 
	 * @return whether this node represents a layer that can be applied to the
	 *         layer model
	 */
	boolean isLayerNode();

	/**
	 * Load the layer associated with this node to the given layer tree node. If
	 * this catalog node represents a layer node (and therefore
	 * {@link #isLayerNode()} returns true), this method should load the
	 * associated layer and set it on the provided layer tree node. It can do
	 * this asynchronously.
	 * <p/>
	 * If the layer associated with this node is loaded from a resource that has
	 * a URI, the simple implementation of this method is as follows:
	 * 
	 * <pre>
	 * public void loadLayer(ILayerNode node, IEclipseContext context)
	 * {
	 * 	IntentLayerLoader.load(node, layerUri, context);
	 * }
	 * </pre>
	 * 
	 * The {@link IntentLayerLoader} will handle firing an {@link Intent} to
	 * load the layer from the URI, and once loaded will set it on the
	 * {@link ILayerNode}.
	 * <p/>
	 * Catalogs can choose to create their own layers. The layer must implement
	 * {@link IPersistentLayer}, and be set on the layer tree node using the
	 * {@link ILayerNode#setLayer(IPersistentLayer)} method once loaded. The
	 * catalog can also choose to remove the layer tree node if the user aborts
	 * the layer loading (eg from an iteractive dialog) by calling
	 * {@link ILayerNode#removeFromParent()}.
	 * 
	 * @param node
	 *            Layer tree node to add the loaded catalog layer to, using
	 *            {@link ILayerNode#setLayer(IPersistentLayer)}
	 * @param context
	 *            Eclipse context
	 * @throws Exception
	 *             If an error occurs during layer load. This will be displayed
	 *             as an error on the layer node.
	 */
	void loadLayer(ILayerNode node, IEclipseContext context) throws Exception;
}
