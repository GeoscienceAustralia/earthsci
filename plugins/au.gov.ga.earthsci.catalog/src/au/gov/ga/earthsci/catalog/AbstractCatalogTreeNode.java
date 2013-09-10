package au.gov.ga.earthsci.catalog;

import java.net.URI;
import java.net.URL;

import au.gov.ga.earthsci.core.tree.AbstractTreeNode;

/**
 * An abstract base implementation of the {@link ICatalogTreeNode}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractCatalogTreeNode extends AbstractTreeNode<ICatalogTreeNode> implements ICatalogTreeNode
{
	private final URI nodeURI;
	private String label;

	public AbstractCatalogTreeNode(URI nodeURI)
	{
		super(ICatalogTreeNode.class);
		this.nodeURI = nodeURI;
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

	@Override
	public URL getIconURL()
	{
		if (isRoot() || getParent().isRoot())
		{
			return Icons.REPO;
		}
		else if (isLayerNode())
		{
			return Icons.FILE;
		}
		else
		{
			return Icons.FOLDER;
		}
	}
}
