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
import java.util.List;

/**
 * Represents the model of the available catalogs.
 * <p/>
 * The catalog model contains a single root node which acts as a
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ICatalogModel
{
	/**
	 * Return the root node of this model.
	 * <p/>
	 * The root node contains each of the top-level catalogs in the model. It is
	 * a special-case node that will not usually be displayed to the user,
	 * instead acting as a container for other catalog trees.
	 * 
	 * @return The root node of the current model.
	 * 
	 * @see #getTopLevelCatalogs()
	 */
	ICatalogTreeNode getRoot();

	/**
	 * Return the top-level catalogs present in this model. May be empty if no
	 * catalogs are present.
	 * <p/>
	 * In most cases this will be equivalent to calling
	 * {@code getRoot().getChildren()}
	 * 
	 * @return The ordered list of top-level catalogs in this model
	 */
	List<ICatalogTreeNode> getTopLevelCatalogs();

	/**
	 * Add the provided catalog tree as a top-level catalog to this model.
	 * 
	 * @param catalog
	 *            The root node of the catalog tree to add to this model.
	 */
	void addTopLevelCatalog(ICatalogTreeNode catalog);

	/**
	 * Add the provided catalog tree node at the given index. If index is out of
	 * bounds, insertion will occur at the start/end of the child list as
	 * appropriate.
	 * 
	 * @param index
	 *            The index at which to insert the catalog
	 * @param catalog
	 *            The catalog to insert
	 */
	void addTopLevelCatalog(int index, ICatalogTreeNode catalog);

	/**
	 * Does this catalog model contain a top level catalog pointing to the given
	 * URI?
	 * 
	 * @param uri
	 * @return True if this catalog contains a catalog for this URI
	 */
	boolean containsTopLevelCatalogURI(URI uri);

	/**
	 * Get the catalog tree node for the catalog loaded from the given URI. If
	 * multiple catalog nodes exist for the URI, the last one is returned.
	 * 
	 * @param uri
	 * @return Catalog tree node for URI, null if one doesn't exist
	 */
	ICatalogTreeNode getTopLevelCatalogForURI(URI uri);

	/**
	 * Remove all top level catalogs loaded from the given URI.
	 * 
	 * @param uri
	 */
	void removeTopLevelCatalogsForURI(URI uri);
}
