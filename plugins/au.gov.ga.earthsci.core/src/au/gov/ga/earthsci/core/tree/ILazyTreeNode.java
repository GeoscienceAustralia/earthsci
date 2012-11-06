package au.gov.ga.earthsci.core.tree;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import au.gov.ga.earthsci.core.util.INamed;

/**
 * An extension of the {@link ITreeNode} interface that supports lazy loading of
 * children.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ILazyTreeNode<E> extends ITreeNode<E>, INamed
{
	/**
	 * Load this node's children.
	 * <p/>
	 * Loading will be performed in the returned {@link Job}, which takes the name of the node
	 */
	LazyTreeJob load();
	
	/**
	 * @return Whether this node's children have been loaded 
	 */
	boolean isLoaded();
	
	/**
	 * @return Whether an error has occurred while loading this node's children
	 */
	boolean hasError();
	
	/**
	 * @return The status associated with the last load of this node's children
	 */
	IStatus getStatus();
}
