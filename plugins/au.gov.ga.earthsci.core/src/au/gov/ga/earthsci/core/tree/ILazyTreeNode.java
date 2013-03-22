package au.gov.ga.earthsci.core.tree;


/**
 * An extension of the {@link ITreeNode} interface that supports lazy loading of
 * children.
 * <p/>
 * See {@link AsynchronousLazyTreeNodeHelper} for a simple helper that these
 * methods can be delegated to.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ILazyTreeNode<E> extends ITreeNode<E>
{
	/**
	 * Load this node's children. The callback should be notified once loading
	 * is complete.
	 * <p/>
	 * Implementations should intelligently handle the case where this is called
	 * more than once. For example, if called while a load is in progress, this
	 * shouldn't fire another load job.
	 * 
	 * @param callback
	 *            Callback to notify on loading completion
	 */
	void load(ILazyTreeNodeCallback callback);

	/**
	 * @return Whether this node's children have been loaded (or attempted with
	 *         an error)
	 */
	boolean isLoaded();

	/**
	 * Calculate a list of children that should be displayed to users.
	 * <p/>
	 * For example:
	 * <ul>
	 * <li>If this node is currently in the process of loading, this method
	 * could return a single node representing the loading state, or an array
	 * containing a loading node plus its current children.</li>
	 * <li>If the load failed for this node, this method could return a single
	 * node containing the error.</li>
	 * <li>Otherwise this method should return {@link #getChildren()}.
	 * </ul>
	 * 
	 * @return Array of child elements to display to the user
	 */
	ITreeNode<E>[] getDisplayChildren();
}
