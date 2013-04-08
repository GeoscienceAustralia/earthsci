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

import org.eclipse.core.runtime.content.IContentType;

import au.gov.ga.earthsci.common.util.IInformationed;
import au.gov.ga.earthsci.common.util.ILabelable;
import au.gov.ga.earthsci.common.util.INamed;
import au.gov.ga.earthsci.common.util.IPropertyChangeBean;
import au.gov.ga.earthsci.core.tree.ITreeNode;

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
	 *         added nodes should also be removeable.
	 */
	boolean isRemoveable();

	/**
	 * @return A URI that uniquely identifies this node in a catalog model. One
	 *         should be able to re-create the node from this URI.
	 */
	URI getURI();

	/**
	 * Return whether this node represents a layer that can be applied to the
	 * layer model
	 * 
	 * @return whether this node represents a layer that can be applied to the
	 *         layer model
	 */
	boolean isLayerNode();

	/**
	 * @return the URI of the layer this node represents if
	 *         {@link #isLayerNode()} returns <code>true</code>
	 */
	URI getLayerURI();

	/**
	 * @return the content type of the layer this node represents if
	 *         {@link #isLayerNode()} returns <code>true</code>
	 */
	IContentType getLayerContentType();

	/**
	 * @return URL pointing to the icon to display for this node
	 */
	URL getIconURL();
}
