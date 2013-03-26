/**
 * Contains the core catalog model for the platform.
 * <p/>
 * The catalog model is an abstraction of sources of data and layers in the platform.
 * In this model, a catalog is a tree structure represented by {@link au.gov.ga.earthsci.catalog.model.ICatalogTreeNode}s. 
 * The nodes in this tree know how to load their own children as appropriate, and can supply layer information to the layer loading
 * mechanism as appropriate.
 * <p/>
 * Catalogs can be provided by {@link au.gov.ga.earthsci.catalog.model.ICatalogProvider} instances, which can be registered on a central
 * {@link au.gov.ga.earthsci.catalog.model.CatalogFactory} either directly or via the {@code au.gov.ga.earthsci.core.model.catalog.provider}
 * extension point. Clients can then easily load a catalog from a source using {@link au.gov.ga.earthsci.catalog.model.CatalogFactory#loadCatalog(java.net.URI)}.
 * <p/>
 * Multiple catalogs can be available at any one time. These are accessed through the current {@link au.gov.ga.earthsci.catalog.model.ICatalogModel} instance.
 * 
 */
package au.gov.ga.earthsci.catalog.model;

