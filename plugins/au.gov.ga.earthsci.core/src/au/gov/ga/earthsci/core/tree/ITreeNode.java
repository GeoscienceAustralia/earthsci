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

/**
 * Represents a tree node, with children and a parent (unless root).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <E>
 *            Type wrapped by this tree node.
 */
public interface ITreeNode<E>
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
	ITreeNode<E> getParent();

	/**
	 * Set the parent of this node. Should only be called by the
	 * {@link ITreeNode} implementation, when added/removing children.
	 * 
	 * @param parent
	 */
	void setParent(ITreeNode<E> parent);

	/**
	 * @return Does this node have children?
	 */
	boolean hasChildren();

	/**
	 * @return This node's children.
	 */
	ITreeNode<E>[] getChildren();

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
	 * Add a child to this node.
	 * 
	 * @param child
	 *            Child to add.
	 */
	void add(ITreeNode<E> child);

	/**
	 * Add a child to this node at the specified index.
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
}
