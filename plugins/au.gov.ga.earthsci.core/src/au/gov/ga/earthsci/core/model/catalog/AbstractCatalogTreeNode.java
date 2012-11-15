package au.gov.ga.earthsci.core.model.catalog;

import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import au.gov.ga.earthsci.core.persistence.Exportable;
import au.gov.ga.earthsci.core.persistence.Persistent;
import au.gov.ga.earthsci.core.tree.AbstractLazyTreeNode;
import au.gov.ga.earthsci.core.tree.LazyTreeJob;

/**
 * An abstract base implementation of the {@link ICatalogTreeNode} 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Exportable
public abstract class AbstractCatalogTreeNode extends AbstractLazyTreeNode<ICatalogTreeNode> implements ICatalogTreeNode
{

	@Persistent
	private URI nodeURI;
	@Persistent
	private String label;
	
	public AbstractCatalogTreeNode(URI nodeURI)
	{
		this.nodeURI = nodeURI;
	}
	
	/** For persistence mechanism only */
	protected AbstractCatalogTreeNode() {};
	
	@Override
	protected IStatus doLoad(IProgressMonitor monitor)
	{
		monitor.done();
		return Status.OK_STATUS;
	}
	
	@Override
	public LazyTreeJob reload()
	{
		return load();
	}
	
	@Override
	public String getLabel()
	{
		return label;
	}
	
	@Override
	public void setLabel(String label)
	{
		String oldLabel = this.label;
		this.label = label;
		
		firePropertyChange("label", oldLabel, label); //$NON-NLS-1$
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
