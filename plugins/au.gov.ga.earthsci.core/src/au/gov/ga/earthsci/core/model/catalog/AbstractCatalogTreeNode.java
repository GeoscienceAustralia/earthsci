package au.gov.ga.earthsci.core.model.catalog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import au.gov.ga.earthsci.core.tree.AbstractLazyTreeNode;
import au.gov.ga.earthsci.core.tree.LazyTreeJob;

/**
 * An abstract base implementation of the {@link ICatalogTreeNode} 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class AbstractCatalogTreeNode extends AbstractLazyTreeNode<ICatalogTreeNode> implements ICatalogTreeNode
{
	
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

}
