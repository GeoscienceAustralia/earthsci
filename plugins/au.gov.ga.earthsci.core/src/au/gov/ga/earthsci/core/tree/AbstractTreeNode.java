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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.gov.ga.earthsci.common.util.AbstractTreePropertyChangeBean;

/**
 * Abstract implementation of the {@link ITreeNode} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <E>
 *            Type wrapped by this node.
 */
public abstract class AbstractTreeNode<E extends ITreeNode<E>> extends AbstractTreePropertyChangeBean implements
		ITreeNode<E>
{
	protected final E me;
	protected E parent;
	protected List<E> children = Collections.unmodifiableList(new ArrayList<E>());

	protected AbstractTreeNode(Class<E> genericClass)
	{
		if (!genericClass.isInstance(this))
		{
			throw new IllegalStateException("Subclasses of " + AbstractTreeNode.class //$NON-NLS-1$
					+ " must be an instance of the class specified by the generic argument"); //$NON-NLS-1$
		}
		me = genericClass.cast(this);
	}

	@Override
	public E me()
	{
		return me;
	}

	@Override
	public boolean isRoot()
	{
		return getParent() == null;
	}

	@Override
	public E getParent()
	{
		return parent;
	}

	@Override
	public void setParent(E parent, int indexInParent)
	{
		if (parent != null && parent.getChild(indexInParent) != this)
		{
			throw new IllegalArgumentException("Node is not a child of the given parent"); //$NON-NLS-1$
		}
		firePropertyChange("parent", getParent(), this.parent = parent); //$NON-NLS-1$
	}

	@Override
	public boolean hasChildren()
	{
		return getChildCount() > 0;
	}

	@Override
	public List<E> getChildren()
	{
		return children;
	}

	@Override
	public int getChildCount()
	{
		return children.size();
	}

	/**
	 * Set this node's children.
	 * 
	 * @param children
	 *            Node's children, cannot be null
	 */
	public void setChildren(List<E> children)
	{
		List<E> oldValue = getChildren();
		this.children = Collections.unmodifiableList(children);

		//set children's parent to this
		int i = 0;
		for (E child : children)
		{
			child.setParent(me(), i++);
		}

		fireChildrenPropertyChange(oldValue, children);
	}

	/**
	 * Fire the "children" property change.
	 * 
	 * @param oldChildren
	 * @param newChildren
	 */
	protected void fireChildrenPropertyChange(List<E> oldChildren, List<E> newChildren)
	{
		firePropertyChange("children", oldChildren, newChildren); //$NON-NLS-1$
	}

	@Override
	public E getChild(int index)
	{
		return children.get(index);
	}

	@Override
	public int index()
	{
		if (isRoot())
		{
			return -1;
		}
		return getParent().getChildren().indexOf(this);
	}

	@Override
	public int depth()
	{
		return recurseDepth(getParent(), 0);
	}

	protected int recurseDepth(ITreeNode<?> node, int depth)
	{
		if (node == null)
		{
			return depth;
		}
		return recurseDepth(node.getParent(), depth + 1);
	}

	@Override
	public void add(E child)
	{
		add(-1, child);
	}

	@Override
	public void add(int index, E child)
	{
		// Handle the edge case of a child node being added that already exists
		// Note - the rest of the tree API expects child arrays to act as sets
		if (child.getParent() == this)
		{
			moveChild(child, index);
			return;
		}
		if (child.getParent() != null)
		{
			child.getParent().remove(child);
		}
		if (index < 0 || index > children.size())
		{
			index = children.size();
		}
		List<E> newChildren = new ArrayList<E>(children);
		newChildren.add(index, child);
		setChildren(newChildren);
	}

	@Override
	public void moveChild(E child, int newIndex)
	{
		if (newIndex < 0 || newIndex >= children.size())
		{
			newIndex = children.size() - 1;
		}
		int oldIndex = child.index();
		if (oldIndex == newIndex)
		{
			return;
		}
		List<E> newChildren = new ArrayList<E>(children);
		newChildren.remove(oldIndex);
		newChildren.add(newIndex, child);
		setChildren(newChildren);
	}

	@Override
	public boolean remove(E child)
	{
		int index = child.index();
		if (index < 0)
		{
			return false;
		}
		if (getChild(index) != child)
		{
			return false;
		}
		remove(index);
		return true;
	}

	@Override
	public E remove(int index)
	{
		if (index < 0 || index >= children.size())
		{
			throw new IndexOutOfBoundsException();
		}
		List<E> newChildren = new ArrayList<E>(children);
		E node = newChildren.remove(index);
		if (node.getParent() == this)
		{
			node.setParent(null, -1);
		}
		setChildren(newChildren);
		return node;
	}

	@Override
	public void removeAll()
	{
		if (children.isEmpty())
		{
			return;
		}
		for (E child : children)
		{
			if (child.getParent() == this)
			{
				child.setParent(null, -1);
			}
		}
		setChildren(new ArrayList<E>());
	}

	@Override
	public void removeFromParent()
	{
		if (isRoot())
		{
			return;
		}
		@SuppressWarnings("unchecked")
		E e = (E) this;
		getParent().remove(e);
	}

	@Override
	public boolean replaceChild(E child, E newChild)
	{
		int index = child.index();
		if (index < 0)
		{
			return false;
		}
		if (getChild(index) != child)
		{
			return false;
		}
		List<E> newChildren = new ArrayList<E>(children);
		newChildren.remove(index);
		if (child.getParent() == this)
		{
			child.setParent(null, -1);
		}
		newChildren.add(index, newChild);
		setChildren(newChildren);
		return true;
	}

	@Override
	public List<E> pathToRoot()
	{
		List<E> path = new ArrayList<E>();
		E node = me();
		while (node != null)
		{
			path.add(0, node);
			node = node.getParent();
		}
		return path;
	}

	@Override
	public int[] indicesToRoot()
	{
		int count = depth();
		int[] indices = new int[count];
		ITreeNode<E> node = this;
		for (int i = count - 1; i >= 0; i--)
		{
			indices[i] = node.index();
			node = node.getParent();
		}
		return indices;
	}

	@Override
	public boolean hasParentInPathToRoot(ITreeNode<?> parent)
	{
		ITreeNode<E> node = this;
		while (node != null)
		{
			if (node == parent)
			{
				return true;
			}
			node = node.getParent();
		}
		return false;
	}

	@Override
	public E getRoot()
	{
		E node = me();
		while (!node.isRoot())
		{
			node = node.getParent();
		}
		return node;
	}
}
