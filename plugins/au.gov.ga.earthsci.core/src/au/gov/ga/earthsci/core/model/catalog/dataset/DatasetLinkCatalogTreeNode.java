package au.gov.ga.earthsci.core.model.catalog.dataset;

import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;

/**
 * An {@link ICatalogTreeNode} that represents a {@code Link} element from the legacy
 * {@code dataset.xml} 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DatasetLinkCatalogTreeNode extends DatasetCatalogTreeNode
{
	private boolean loaded;
	
	private URL linkURL;

	public DatasetLinkCatalogTreeNode(String name, URL linkURL, URL infoURL, URL iconURL)
	{
		super(name, infoURL, iconURL);
		this.linkURL = linkURL;
	}

	@Override
	public boolean isLoaded()
	{
		// TODO Auto-generated method stub
		return super.isLoaded();
	}
	
	@Override
	protected IStatus doLoad(IProgressMonitor monitor)
	{
		monitor.beginTask("Downloading link " + linkURL.toExternalForm(), IProgressMonitor.UNKNOWN);
		
		
	}
	
	
	
}
