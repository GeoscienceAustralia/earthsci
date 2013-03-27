/**
 * Contains components that allow for the editing of {@link au.gov.ga.earthsci.bookmark.model.IBookmark}
 * instances and their associated {@link au.gov.ga.earthsci.bookmark.model.IBookmarkProperty}s.
 * 
 * <p/>
 * 
 * The primary component is the {@link au.gov.ga.earthsci.bookmark.ui.editor.BookmarkEditorDialog},
 * which provides a dialog box similar in nature to the Eclipse preferences dialog.
 * 
 * This dialog is associated with a single {@link au.gov.ga.earthsci.bookmark.model.IBookmark} instance,
 * and gives access to all available {@link au.gov.ga.earthsci.bookmark.model.IBookmarkProperty}s.
 * 
 * To launch the editor dialog, first create a new instance with an 
 * {@link au.gov.ga.earthsci.bookmark.model.IBookmark} to edit, and then open the dialog
 * with {@link au.gov.ga.earthsci.bookmark.ui.editor.BookmarkEditorDialog#open()}.
 * 
 * <pre>
 * Display.getDefault().asyncExec(new Runnable() {
 *  @Override
 *	public void run()
 *	{
 *	  BookmarkEditorDialog dialog = new BookmarkEditorDialog(bookmark, 
 *                                                           Display.getDefault().getActiveShell());
 *	  dialog.open();
 *	}
 * });
 * </pre>
 * 
 * <p/>
 * 
 * Individual {@link au.gov.ga.earthsci.bookmark.model.IBookmarkProperty}s are edited with associated
 * {@link au.gov.ga.earthsci.bookmark.ui.editor.IBookmarkPropertyEditor} instances. Subclasses should
 * consider extending the convenience base class 
 * {@link au.gov.ga.earthsci.bookmark.ui.editor.AbstractBookmarkPropertyEditor},
 * which has sensible default implementations for several methods.
 * 
 * <p/>
 * 
 * {@link au.gov.ga.earthsci.bookmark.ui.editor.IBookmarkPropertyEditor}s instances can be 
 * retrieved from the {@link au.gov.ga.earthsci.bookmark.ui.editor.BookmarkPropertyEditorFactory}.
 * This factory has methods for creating an editor suitable for a given 
 * {@link au.gov.ga.earthsci.bookmark.model.IBookmarkProperty} type, and for retrieving all types
 * for which an editor exists.
 * 
 * Editors can be registered on the {@link au.gov.ga.earthsci.bookmark.ui.editor.BookmarkPropertyEditorFactory}
 * directly via the {@link au.gov.ga.earthsci.bookmark.ui.editor.BookmarkPropertyEditorFactory#registerEditor(String, Class)}
 * method, or (more flexibly) using the Eclipse extension point 
 * {@code au.gov.ga.earthsci.application.bookmarkPropertyEditor}
 * 
 */
package au.gov.ga.earthsci.bookmark.ui.editor;

