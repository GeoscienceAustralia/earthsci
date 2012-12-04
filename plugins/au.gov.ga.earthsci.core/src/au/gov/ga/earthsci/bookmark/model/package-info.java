/**
 * Contains the core bookmark model for the platform.
 * <p/>
 * The central component of this model is the {@link au.gov.ga.earthsci.bookmark.model.IBookmark}. 
 * This represents a single bookmark, which is capable of capturing system state that can be persisted 
 * and/or re-applied at a later point in time. Bookmarks may be created by the user, or supplied with a 
 * data bundle to provide information about the data to the user.
 * <p/>
 * Bookmarks are composed of zero or more {@link au.gov.ga.earthsci.bookmark.model.IBookmarkProperty}s, which
 * capture a specific piece of state (e.g. camera position information). These are used to re-apply the state.
 * Plugin authors may extend the bookmark mechanism by adding additional 
 * {@link au.gov.ga.earthsci.bookmark.model.IBookmarkProperty} implementations which capture additional state.
 * <p/>
 * Additional metadata can be stored on a bookmark using the {@link au.gov.ga.earthsci.bookmark.model.IBookmarkMetadata}
 * which is a simple mapping of keys to (potentially) HTML-formatted values. These values may be presented to the user
 * in a nicely formatted way, or edited as appropriate. A number of standard keys are provided that can be used
 * to store/retrieve commonly used metadata elements.
 */
package au.gov.ga.earthsci.bookmark.model;

