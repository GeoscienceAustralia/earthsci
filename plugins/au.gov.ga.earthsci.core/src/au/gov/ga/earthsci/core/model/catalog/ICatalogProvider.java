package au.gov.ga.earthsci.core.model.catalog;

import java.net.URI;


/**
 * A service interface for classes that can load an {@link ICatalogTreeNode} tree structure
 * from a provided source URI.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ICatalogProvider
{

	/**
	 * Return whether or not this provider can load a catalog from the provided source.
	 * 
	 * @param source The source to test
	 * 
	 * @return <code>true</code> if this provider supports the provided source, <code>false</code> otherwise.
	 */
	boolean supports(URI source);
	
	/**
	 * Load the catalog from the provided URI and return the root node
	 * for the catalog tree.
	 * 
	 * @param source The source to use to load the catalog from. See {@link #supports(URI)}
	 * 
	 * @return The tree node at the root of the catalog tree
	 * 
	 * @throws IllegalArgumentException if this provider cannot load a catalog from the provided source. 
	 * See {@link #supports(URI)} to determine ahead of time if the provider can load from a given source.
	 */
	ICatalogTreeNode loadCatalog(URI source);
	
}
