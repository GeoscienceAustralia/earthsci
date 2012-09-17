package au.gov.ga.earthsci.core.tree;

import java.util.Arrays;

import au.gov.ga.earthsci.core.util.AbstractPropertyChangeBean;

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
	public void setParent(ITreeNode<E> parent)
	{
		this.parent = parent;
	}

	@Override
	public boolean hasChildren()
	{
		return children != null && children.length > 0;
	}

	@Override
	public ITreeNode<E>[] getChildren()
	{
		return children;
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
		int index = Arrays.binarySearch(getParent().getChildren(), this);
		return index < 0 ? -1 : index;
	}

	@Override
	public int depth()
	{
		return recurseDepth(getParent(), 0);
	}

	private int recurseDepth(ITreeNode<?> node, int depth)
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
		children = newChildren;
		child.setParent(this);
	}

	@Override
	public boolean remove(ITreeNode<E> child)
	{
		int index = child.index();
		if (index < 0)
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
			throw new IndexOutOfBoundsException();

		@SuppressWarnings("unchecked")
		ITreeNode<E>[] newChildren = new ITreeNode[children.length - 1];

		ITreeNode<E> node = children[index];
		if (node.getParent() == this)
		{
			node.setParent(null);
		}

		if (index > 0)
		{
			System.arraycopy(children, 0, newChildren, 0, index);
		}
		if (index < children.length - 1)
		{
			System.arraycopy(children, index + 1, newChildren, index, children.length - index - 1);
		}
		children = newChildren;
		return node;
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
}
