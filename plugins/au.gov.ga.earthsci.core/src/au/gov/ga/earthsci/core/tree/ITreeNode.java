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

import au.gov.ga.earthsci.core.util.ITreePropertyChangeBean;

/**
 * Represents a tree node, with children and a parent (unless root).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <E>
 *            Type wrapped by this tree node.
 */
public interface ITreeNode<E> extends ITreePropertyChangeBean
{
	/**
	 * @return Value at this node.
	 */
	E getValue();

	/**
	 * @return Is this node the root node (has no parent)?
	 */
	boolean isRoot();

	/**
	 * @return Parent of this node (returns null if this node is the root).
	 */
	@Override
	ITreeNode<E> getParent();

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
	void setParent(ITreeNode<E> parent, int indexInParent);

	/**
	 * @return Does this node have children?
	 */
	boolean hasChildren();

	/**
	 * @return This node's children.
	 */
	ITreeNode<E>[] getChildren();

	/**
	 * @return Number of children of this node.
	 */
	int getChildCount();

	/**
	 * @param index
	 * @return This node's child at <code>index</code>.
	 */
	ITreeNode<E> getChild(int index);

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
	void add(ITreeNode<E> child);

	/**
	 * Add a child to this node at the specified index. If the specified child
	 * is already a child of this node, it is moved to the given index.
	 * 
	 * @param index
	 *            Index at which to add the child.
	 * @param child
	 *            Child to add.
	 */
	void add(int index, ITreeNode<E> child);

	/**
	 * Remove the specified child from this node.
	 * 
	 * @param child
	 *            Child to remove.
	 * @return True if the child was found and removed, false otherwise.
	 */
	boolean remove(ITreeNode<E> child);

	/**
	 * Remove all children from this node
	 */
	void removeAll();

	/**
	 * Remove the child at the specified index.
	 * 
	 * @param index
	 *            Index of the child to remove.
	 * @return Child removed.
	 */
	ITreeNode<E> remove(int index);

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
	void moveChild(ITreeNode<E> child, int newIndex);

	/**
	 * @return Array containing all nodes from the root (at index 0) to this
	 *         node.
	 */
	ITreeNode<E>[] pathToRoot();

	/**
	 * @return Array containing the index of each node from the root to this
	 *         node (not including the root).
	 */
	int[] indicesToRoot();

	/**
	 * @return The root node of this node.
	 */
	ITreeNode<E> getRoot();
}
