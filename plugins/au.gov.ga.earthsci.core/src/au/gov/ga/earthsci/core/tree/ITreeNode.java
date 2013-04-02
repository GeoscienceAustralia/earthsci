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
package au.gov.ga.earthsci.core.tree;

import java.util.List;

import au.gov.ga.earthsci.common.util.ITreePropertyChangeBean;

/**
 * Represents a tree node, with children and a parent (unless root).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <E>
 *            Type of this tree node. All nodes in the tree hierarchy must be
 *            instances of this type, as well as instances of ITreeNode.
 */
public interface ITreeNode<E extends ITreeNode<E>> extends ITreePropertyChangeBean
{
	/**
	 * Genericised value of <code>this</code>.
	 * <p/>
	 * To get this value, either the generic class must be passed to an
	 * implementor's constructor so that <code>this</code> can be cast using the
	 * {@link Class#cast(Object)} method, or an unchecked cast must be used.
	 * This is unfortunate but unavoidable due to the CRTP nature of ITreeNode.
	 * <p/>
	 * Example constructor implementation:
	 * 
	 * <pre>
	 * public class TreeNode&lt;E extends ITreeNode&lt;E&gt;&gt; implements ITreeNode&lt;E&gt;
	 * {
	 * 	protected final E me;
	 * ...
	 * 	protected TreeNode(Class&lt;E&gt; genericClass)
	 * 	{
	 * 		if (!genericClass.isInstance(this))
	 * 		{
	 * 			throw new IllegalStateException();
	 * 		}
	 * 		me = genericClass.cast(this);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @return <code>this</code>
	 */
	E me();

	/**
	 * @return Is this node the root node (has no parent)?
	 */
	boolean isRoot();

	/**
	 * @return Parent of this node (returns null if this node is the root).
	 */
	@Override
	E getParent();

	/**
	 * @return True if this element has the given parent somewhere in it's path
	 *         to root.
	 */
	boolean hasParentInPathToRoot(ITreeNode<?> parent);

	/**
	 * Set the parent of this node.
	 * <p/>
	 * DO NOT CALL THIS METHOD. Should only be called by the {@link ITreeNode}
	 * implementation, when added/removing children.
	 * 
	 * @param parent
	 * @param indexInParent
	 */
	void setParent(E parent, int indexInParent);

	/**
	 * @return Does this node have children?
	 */
	boolean hasChildren();

	/**
	 * @return This node's children.
	 */
	List<E> getChildren();

	/**
	 * @return Number of children of this node.
	 */
	int getChildCount();

	/**
	 * @param index
	 * @return This node's child at <code>index</code>.
	 */
	E getChild(int index);

	/**
	 * @return Index of this node in it's parent (-1 if this node is the root).
	 */
	int index();

	/**
	 * @return Depth of this node in the tree hierarchy (0 if this node is the
	 *         root).
	 */
	int depth();

	/**
	 * Add a child to this node. If the child is already a child of this node,
	 * it is moved to the end of the child array.
	 * 
	 * @param child
	 *            Child to add.
	 */
	void addChild(E child);

	/**
	 * Add a child to this node at the specified index. If the specified child
	 * is already a child of this node, it is moved to the given index.
	 * 
	 * @param index
	 *            Index at which to add the child.
	 * @param child
	 *            Child to add.
	 */
	void addChild(int index, E child);

	/**
	 * Remove the specified child from this node.
	 * 
	 * @param child
	 *            Child to remove.
	 * @return True if the child was found and removed, false otherwise.
	 */
	boolean removeChild(E child);

	/**
	 * Remove the child at the specified index.
	 * 
	 * @param index
	 *            Index of the child to remove.
	 * @return Child removed.
	 */
	E removeChild(int index);

	/**
	 * Remove all children from this node
	 */
	void clearChildren();

	/**
	 * Remove this node from its parent (no-op if this node is the root).
	 */
	void removeFromParent();

	/**
	 * Move the child node from the given old index to the new index and trigger
	 * a single children changed property event.
	 * 
	 * @param child
	 *            The child to move
	 * @param newIndex
	 *            The new index of the child
	 */
	void moveChild(E child, int newIndex);

	/**
	 * Replace the child node with a new child node at the same index.
	 * 
	 * @param child
	 *            Child node to replace
	 * @param newChild
	 *            New child node to insert
	 * @return True if the child was found and replaced, false otherwise.
	 */
	boolean replaceChild(E child, E newChild);

	/**
	 * @return List containing all nodes from the root (at index 0) to this
	 *         node.
	 */
	List<E> pathToRoot();

	/**
	 * @return Array containing the index of each node from the root to this
	 *         node (not including the root).
	 */
	int[] indicesToRoot();

	/**
	 * @return The root node of this node.
	 */
	E getRoot();
}
