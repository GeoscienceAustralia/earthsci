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
package au.gov.ga.earthsci.catalog.part;

import java.net.URL;

import au.gov.ga.earthsci.catalog.model.ICatalogTreeNode;

/**
 * A pluggable strategy interface used to provide UI elements to display
 * {@link ICatalogTreeNode}s in the catalog browser tree.
 * <p/>
 * Implementations can be registered against the {@code au.gov.ga.earthsci.application.catalogNodeControlProvider} extension point. 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ICatalogTreeLabelProvider
{

	/**
	 * Determine whether this provider can be applied to the given node.
	 * <p/>
	 * Implementations should be as specific as appropriate to ensure they are not mistakenly used
	 * for nodes to which they do not apply.
	 * 
	 * @param node The node to test
	 * 
	 * @return <code>true</code> if this provider can be applied to the given node
	 */
	boolean supports(ICatalogTreeNode node);
	
	/**
	 * Return a URL that can be used to retrieve an icon to use for the node, if applicable.
	 * 
	 * @param node The node for which an icon is required
	 * 
	 * @return The URL of the image to use for the node icon, or <code>null</code> if none is available
	 */
	URL getIconURL(ICatalogTreeNode node);
	
	/**
	 * Return the label to use for the node
	 * 
	 * @param node The node for which a label is required
	 * 
	 * @return The label to use for the node
	 */
	String getLabel(ICatalogTreeNode node);
	
	/**
	 * Return a URL that can be used to retrieve more information about the node.
	 * 
	 * @param node The node for which an info url is required
	 * 
	 * @return the info URL, or <code>null</code> if none is available.
	 */
	URL getInfoURL(ICatalogTreeNode node);
	
	/**
	 * Dispose of any resources used by this provider.
	 * <p/>
	 * Implementations should be able to re-create any disposed resources if needed in the case
	 * where a provider is re-used.
	 */
	void dispose();
}
