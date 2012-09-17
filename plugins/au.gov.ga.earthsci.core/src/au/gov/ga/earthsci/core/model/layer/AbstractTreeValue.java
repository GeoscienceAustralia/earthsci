package au.gov.ga.earthsci.core.model.layer;

import au.gov.ga.earthsci.core.tree.AbstractTreeNode;

public class AbstractTreeValue extends AbstractTreeNode<AbstractTreeValue> implements ITreeValue
{
	private String name;

	protected AbstractTreeValue()
	{
		super();
		setValue(this);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		String oldValue = getName();
		this.name = name;
		firePropertyChange("name", oldValue, name); //$NON-NLS-1$
	}
}
