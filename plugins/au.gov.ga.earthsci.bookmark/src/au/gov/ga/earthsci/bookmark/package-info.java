/**
 * The root package for the bookmark feature.
 * 
 * <p/>
 * 
 * A <em>bookmark</em> is a concept used to capture and store application state in a manner
 * that can be persisted and later retrieved, edited and/or re-applied.
 * 
 * The domain model for the bookmark feature is provided in the {@link au.gov.ga.earthsci.bookmark.model}
 * package.
 * 
 * <p/>
 * 
 * This package provides a number of interfaces for classes that can perform actions on components
 * of the bookmark feature; and a number of factory classes used to access these components.
 * 
 * <p/>
 * 
 * New {@link au.gov.ga.earthsci.bookmark.model.IBookmark} instances can be created with the
 * {@link au.gov.ga.earthsci.bookmark.BookmarkFactory#createBookmark createBookmark} method.
 * 
 * <p/>
 * 
 * The creation of {@link au.gov.ga.earthsci.bookmark.model.IBookmarkProperty}s is done through
 * the {@link au.gov.ga.earthsci.bookmark.BookmarkPropertyFactory} class, which contains methods for
 * creating instances from XML and from the current world state.
 * 
 * The {@link au.gov.ga.earthsci.bookmark.BookmarkPropertyFactory} delegates actual creation of
 * {@link au.gov.ga.earthsci.bookmark.model.IBookmarkProperty}s to registered 
 * {@link au.gov.ga.earthsci.bookmark.IBookmarkPropertyCreator} instances. These can be registered
 * directly on the factory using {@link au.gov.ga.earthsci.bookmark.BookmarkPropertyFactory#registerCreator registerCreator},
 * or (more flexibly) via the Eclipse extension point {@code au.gov.ga.earthsci.bookmark.creators}.
 * 
 * <p/>
 * 
 * The {@link au.gov.ga.earthsci.bookmark.BookmarkPropertyFactory} is also able to export 
 * {@link au.gov.ga.earthsci.bookmark.model.IBookmarkProperty} instances to XML using the 
 * {@link au.gov.ga.earthsci.bookmark.BookmarkPropertyFactory#exportProperty exportProperty} method.
 * 
 * In this case, the factory delegates to registered {@link au.gov.ga.earthsci.bookmark.IBookmarkPropertyExporter}
 * instances which are able to perform the property-specific export. Exporters can be registered
 * directly on the factory using {@link au.gov.ga.earthsci.bookmark.BookmarkPropertyFactory#registerExporter registerExporter},
 * or (more flexibly) via the Eclipse extension point {@code au.gov.ga.earthsci.bookmark.exporters}.
 * 
 * <p/>
 * 
 * The task of actually applying the state stored in an {@link au.gov.ga.earthsci.bookmark.model.IBookmarkProperty}
 * to the world is handled by {@link au.gov.ga.earthsci.bookmark.IBookmarkPropertyApplicator}s. These
 * are registered on the {@link au.gov.ga.earthsci.bookmark.BookmarkPropertyApplicatorRegistry} either directly
 * using {@link au.gov.ga.earthsci.bookmark.BookmarkPropertyApplicatorRegistry#registerApplicator registerApplicator},
 * or (more flexibly) via the Eclipse extension point {@code au.gov.ga.earthsci.bookmark.applicators}.
 * 
 * @see au.gov.ga.earthsci.bookmark.model
 * @see au.gov.ga.earthsci.bookmark.io
 */
package au.gov.ga.earthsci.bookmark;

