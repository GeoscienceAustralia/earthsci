package au.gov.ga.earthsci.core.tree;

public interface ITreeNode<E>
{
	E getValue();

	boolean isRoot();

	ITreeNode<E> getParent();

	void setParent(ITreeNode<E> parent);

	boolean hasChildren();

	ITreeNode<E>[] getChildren();

	ITreeNode<E> getChild(int index);

	int index();

	int depth();

	void add(ITreeNode<E> child);

	void add(int index, ITreeNode<E> child);

	boolean remove(ITreeNode<E> child);

	ITreeNode<E> remove(int index);

	void removeFromParent();
}
