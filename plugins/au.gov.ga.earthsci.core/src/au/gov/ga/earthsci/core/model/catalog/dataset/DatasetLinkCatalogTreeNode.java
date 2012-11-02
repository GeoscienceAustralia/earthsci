package au.gov.ga.earthsci.core.model.catalog.dataset;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.RetrievalJob;
import au.gov.ga.earthsci.core.retrieve.RetrievalServiceAccessor;
import au.gov.ga.earthsci.core.tree.ITreeNode;

/**
 * An {@link ICatalogTreeNode} that represents a {@code Link} element from the legacy
 * {@code dataset.xml} 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DatasetLinkCatalogTreeNode extends DatasetCatalogTreeNode
{
	private static final String PLUGIN_ID = "au.gov.ga.earthsci.core"; //$NON-NLS-1$
	
	private final AtomicBoolean loaded = new AtomicBoolean(false);
	private final URL linkURL;

	public DatasetLinkCatalogTreeNode(String name, URL linkURL, URL infoURL, URL iconURL, boolean base)
	{
		super(name, infoURL, iconURL, base);
		this.linkURL = linkURL;
	}

	@Override
	public boolean isLoaded()
	{
		return loaded.get();
	}
	
	@Override
	protected IStatus doLoad(IProgressMonitor monitor)
	{
		monitor.beginTask(NLS.bind(Messages.DatasetLinkCatalogTreeNode_DownloadingLinkMessage, linkURL.toExternalForm()), IProgressMonitor.UNKNOWN);
		
		RetrievalJob retrievalJob = RetrievalServiceAccessor.get().retrieve(linkURL);
		
		try
		{
			IRetrievalResult retrievalResult = retrievalJob.waitAndGetRetrievalResult();
			if (retrievalResult == null)
			{
				return new Status(Status.ERROR, PLUGIN_ID, NLS.bind(Messages.DatasetLinkCatalogTreeNode_NoRetrieverFoundMessage, linkURL));
			}
			if (!retrievalResult.isSuccessful())
			{
				return new Status(Status.ERROR, PLUGIN_ID, retrievalResult.getMessage(), retrievalResult.getException());
			}
			
			try
			{
				ICatalogTreeNode root = DatasetReader.read(retrievalResult.getAsInputStream(), linkURL);
				for (ITreeNode<ICatalogTreeNode> child : root.getChildren())
				{
					add(child);
				}
			}
			catch (Exception e)
			{
				return new Status(Status.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e);
			}
		}
		catch (InterruptedException e)
		{
			return Status.CANCEL_STATUS;
		}
		finally
		{
			monitor.done();
			loaded.set(true);
		}
		
		return Status.OK_STATUS;
	}
	
}
