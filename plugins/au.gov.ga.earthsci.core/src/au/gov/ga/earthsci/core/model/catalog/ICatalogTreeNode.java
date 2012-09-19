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
package au.gov.ga.earthsci.core.model.catalog;

import au.gov.ga.earthsci.core.tree.ITreeNode;
import au.gov.ga.earthsci.core.util.INamed;
import au.gov.ga.earthsci.core.util.IPropertyChangeBean;

/**
 * Represents tree nodes in the Catalog tree. Implementations of this interface
 * could be used for creating catalogs from different sources, such as XML
 * datasets, GOCAD project files, WMS catalogs, etc.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ICatalogTreeNode extends ITreeNode<ICatalogTreeNode>, IPropertyChangeBean, INamed
{
	/**
	 * @return Is this catalog node removeable from the Catalog tree? All user
	 *         added nodes should also be removeable.
	 */
	boolean isRemoveable();

	/**
	 * @return Is this catalog node's data able to be reloaded?
	 */
	boolean isReloadable();

	/**
	 * Reload this node's data.
	 */
	void reload();
}
