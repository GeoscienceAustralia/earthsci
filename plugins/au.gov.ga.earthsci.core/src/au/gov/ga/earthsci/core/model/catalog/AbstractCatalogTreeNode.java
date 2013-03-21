package au.gov.ga.earthsci.core.model.catalog;

import java.net.URI;

import au.gov.ga.earthsci.core.tree.AbstractTreeNode;

/**
 * An abstract base implementation of the {@link ICatalogTreeNode}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractCatalogTreeNode extends AbstractTreeNode<ICatalogTreeNode> implements ICatalogTreeNode
{
	private URI nodeURI;
	private String label;

	public AbstractCatalogTreeNode(URI nodeURI)
	{
		this.nodeURI = nodeURI;
		setValue(this);
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public void setLabel(String label)
	{
		firePropertyChange("label", this.label, this.label = label); //$NON-NLS-1$
	}

	@Override
	public String getLabelOrName()
	{
		return getLabel() == null ? getName() : getLabel();
	}

	@Override
	public URI getURI()
	{
		return nodeURI;
	}
}
