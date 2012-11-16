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

import java.util.Arrays;

import au.gov.ga.earthsci.core.util.AbstractPropertyChangeBean;

/**
 * Abstract implementation of the {@link ITreeNode} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <E>
 *            Type wrapped by this node.
 */
public abstract class AbstractTreeNode<E> extends AbstractPropertyChangeBean implements ITreeNode<E>
{
	protected E value;
	protected ITreeNode<E> parent;
	@SuppressWarnings("unchecked")
	protected ITreeNode<E>[] children = new ITreeNode[0];

	@Override
	public E getValue()
	{
		return value;
	}

	public void setValue(E value)
	{
		this.value = value;
	}

	@Override
	public boolean isRoot()
	{
		return getParent() == null;
	}

	@Override
	public ITreeNode<E> getParent()
	{
		return parent;
	}

	@Override
	public void setParent(ITreeNode<E> parent, int indexInParent)
	{
		if (parent != null && parent.getChild(indexInParent) != this)
		{
			throw new IllegalArgumentException("Node is not a child of the given parent"); //$NON-NLS-1$
		}
		ITreeNode<E> oldValue = getParent();
		this.parent = parent;
		firePropertyChange("parent", oldValue, parent); //$NON-NLS-1$
	}

	@Override
	public boolean hasChildren()
	{
		return getChildCount() > 0;
	}

	@Override
	public ITreeNode<E>[] getChildren()
	{
		return children;
	}

	@Override
	public int getChildCount()
	{
		return children == null ? 0 : children.length;
	}

	protected void setChildren(ITreeNode<E>[] children)
	{
		ITreeNode<E>[] oldValue = getChildren();
		this.children = children == null ? null : Arrays.copyOf(children, children.length);

		//set children's parent to this
		if (children != null)
		{
			for (int i = 0; i < children.length; i++)
			{
				ITreeNode<E> child = children[i];
				if (child.getParent() != this)
				{
					child.setParent(this, i);
				}
			}
		}

		firePropertyChange("children", oldValue, children); //$NON-NLS-1$
	}

	@Override
	public ITreeNode<E> getChild(int index)
	{
		return children[index];
	}

	@Override
	public int index()
	{
		if (isRoot())
		{
			return -1;
		}

		int i = 0;
		for (ITreeNode<E> sibling : getParent().getChildren())
		{
			if (sibling == this)
			{
				return i;
			}
			i++;
		}
		return -1;
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
	public void add(ITreeNode<E> child)
	{
		add(-1, child);
	}

	@Override
	public void add(int index, ITreeNode<E> child)
	{
		// Handle the edge case of a child node being added that already exists
		// Note - the rest of the tree API expects child arrays to act as sets
		if (child.getParent() == this)
		{
			moveChild(child, child.index(), index);
			return;
		}
		
		if (child.getParent() != null)
		{
			child.getParent().remove(child);
		}
		@SuppressWarnings("unchecked")
		ITreeNode<E>[] newChildren = new ITreeNode[children.length + 1];
		if (index < 0 || index > children.length)
		{
			index = children.length;
		}
		if (index > 0)
		{
			System.arraycopy(children, 0, newChildren, 0, index);
		}
		newChildren[index] = child;
		if (index < children.length)
		{
			System.arraycopy(children, index, newChildren, index + 1, children.length - index);
		}
		setChildren(newChildren);
	}

	/**
	 * Move the child node from the given old index to the new index and 
	 * trigger a single children changed property event.
	 * 
	 * @param child The child to move
	 * @param oldIndex The old index of the child
	 * @param newIndex The new index of the child
	 * 
	 */
	// TODO: Should this be promoted to the public API? 
	private void moveChild(ITreeNode<E> child, int oldIndex, int newIndex)
	{
		if (newIndex < 0  || newIndex > children.length)
		{
			newIndex = children.length - 1;
		}
		
		if (oldIndex == newIndex)
		{
			return;
		}
		
		@SuppressWarnings("unchecked")
		ITreeNode<E>[] newChildren = new ITreeNode[children.length];
		System.arraycopy(children, 0, newChildren, 0, Math.min(oldIndex, newIndex));
		
		if (oldIndex < newIndex)
		{
			System.arraycopy(children, oldIndex+1, newChildren, oldIndex, newIndex - oldIndex);
			if (newIndex < children.length - 1)
			{
				System.arraycopy(children, newIndex+1, newChildren, newIndex+1, children.length - newIndex - 1);
			}
		}
		else
		{
			System.arraycopy(children, newIndex, newChildren, newIndex+1, oldIndex - newIndex);
			if (oldIndex < children.length - 1)
			{
				System.arraycopy(children, oldIndex+1, newChildren, oldIndex+1, children.length - oldIndex - 1);
			}
		}
		newChildren[newIndex] = child;
		
		setChildren(newChildren);
	}
	
	@Override
	public boolean remove(ITreeNode<E> child)
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
	public ITreeNode<E> remove(int index)
	{
		if (index < 0 || index >= children.length)
		{
			throw new IndexOutOfBoundsException();
		}

		@SuppressWarnings("unchecked")
		ITreeNode<E>[] newChildren = new ITreeNode[children.length - 1];
		ITreeNode<E> node = children[index];
		if (node.getParent() == this)
		{
			node.setParent(null, -1);
		}
		if (index > 0)
		{
			System.arraycopy(children, 0, newChildren, 0, index);
		}
		if (index < children.length - 1)
		{
			System.arraycopy(children, index + 1, newChildren, index, children.length - index - 1);
		}
		setChildren(newChildren);
		return node;
	}

	@Override
	public void removeAll()
	{
		if (children.length == 0)
		{
			return;
		}
		
		@SuppressWarnings("unchecked")
		ITreeNode<E>[] newChildren = new ITreeNode[0];
		
		for (ITreeNode<E> child : children)
		{
			if (child.getParent() == this)
			{
				child.setParent(null, -1);
			}
		}
		
		setChildren(newChildren);
	}
	
	@Override
	public void removeFromParent()
	{
		if (isRoot())
		{
			return;
		}
		getParent().remove(this);
	}

	@Override
	public ITreeNode<E>[] pathToRoot()
	{
		int count = depth() + 1;
		@SuppressWarnings("unchecked")
		ITreeNode<E>[] path = new ITreeNode[count];
		ITreeNode<E> node = this;
		for (int i = count - 1; i >= 0; i--)
		{
			path[i] = node;
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
	public ITreeNode<E> getRoot()
	{
		ITreeNode<E> node = this;
		while (!node.isRoot())
		{
			node = node.getParent();
		}
		return node;
	}
}
