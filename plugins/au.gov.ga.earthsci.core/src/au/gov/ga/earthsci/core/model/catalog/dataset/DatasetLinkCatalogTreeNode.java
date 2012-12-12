package au.gov.ga.earthsci.core.model.catalog.dataset;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.retrieve.IRetrieval;
import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.IRetrievalService;
import au.gov.ga.earthsci.core.retrieve.RetrievalServiceFactory;
import au.gov.ga.earthsci.core.tree.ITreeNode;
import au.gov.ga.earthsci.core.util.Util;

/**
 * An {@link ICatalogTreeNode} that represents a {@code Link} element from the
 * legacy {@code dataset.xml}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DatasetLinkCatalogTreeNode extends DatasetCatalogTreeNode
{
	private static final String LINK_DOWNLOAD_FAILED =
			Messages.DatasetLinkCatalogTreeNode_GenericLinkDownloadFailedMessage;

	private final URL linkURL;

	public DatasetLinkCatalogTreeNode(URI nodeURI, String name, URL linkURL, URL infoURL, URL iconURL, boolean base)
	{
		super(nodeURI, name, infoURL, iconURL, base);

		this.linkURL = linkURL;

		setLoaded(false);
	}

	@Override
	protected IStatus doLoad(IProgressMonitor monitor)
	{
		try
		{
			monitor.beginTask(
					NLS.bind(Messages.DatasetLinkCatalogTreeNode_DownloadingLinkMessage, linkURL.toExternalForm()),
					IProgressMonitor.UNKNOWN);


			IRetrievalService retrievalService = RetrievalServiceFactory.getServiceInstance();
			if (retrievalService == null)
			{
				throw new IllegalStateException(Messages.DatasetLinkCatalogTreeNode_NoRetrievalServiceMessage);
			}

			IRetrieval retrieval = retrievalService.retrieve(this, linkURL);
			retrieval.start();

			IRetrievalResult retrievalResult = retrieval.waitAndGetResult();
			if (retrievalResult == null)
			{
				return createErrorStatus(
						NLS.bind(Messages.DatasetLinkCatalogTreeNode_NoRetrieverFoundMessage, linkURL), null);
			}
			Exception error = retrievalResult.getError();
			if (error != null)
			{
				return createErrorStatus(error.getLocalizedMessage(), error);
			}

			InputStream is = retrievalResult.getData().getInputStream();
			try
			{
				ICatalogTreeNode root = DatasetReader.read(is, linkURL);
				for (ITreeNode<ICatalogTreeNode> child : root.getChildren())
				{
					add(child);
				}
			}
			finally
			{
				is.close();
			}
		}
		catch (InterruptedException e)
		{
			return Status.CANCEL_STATUS;
		}
		catch (Exception e)
		{
			return createErrorStatus(
					Util.isEmpty(e.getLocalizedMessage()) ? LINK_DOWNLOAD_FAILED : e.getLocalizedMessage(), e);
		}
		finally
		{
			monitor.done();
		}

		return Status.OK_STATUS;
	}

}
