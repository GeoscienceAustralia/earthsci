package au.gov.ga.earthsci.core.model.catalog.dataset;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import au.gov.ga.earthsci.core.model.catalog.ErrorCatalogTreeNode;
import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.model.catalog.LoadingCatalogTreeNode;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.tree.ILazyTreeNodeCallback;
import au.gov.ga.earthsci.core.tree.ITreeNode;
import au.gov.ga.earthsci.core.tree.lazy.IRetrievalLazyTreeNode;
import au.gov.ga.earthsci.core.tree.lazy.RetrievalLazyTreeNodeHelper;

/**
 * An {@link ICatalogTreeNode} that represents a {@code Link} element from the
 * legacy {@code dataset.xml}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DatasetLinkCatalogTreeNode extends DatasetCatalogTreeNode implements
		IRetrievalLazyTreeNode<ICatalogTreeNode>
{
	private final URL linkURL;
	private final RetrievalLazyTreeNodeHelper<ICatalogTreeNode> helper =
			new RetrievalLazyTreeNodeHelper<ICatalogTreeNode>(this);

	public DatasetLinkCatalogTreeNode(URI nodeURI, String name, URL linkURL, URL infoURL, URL iconURL, boolean base)
	{
		super(nodeURI, name, infoURL, iconURL, base);
		this.linkURL = linkURL;
	}

	@Override
	public void load(ILazyTreeNodeCallback callback)
	{
		helper.load(callback);
	}

	@Override
	public boolean isLoaded()
	{
		return helper.isLoaded();
	}

	@Override
	public ITreeNode<ICatalogTreeNode>[] getDisplayChildren()
	{
		return helper.getDisplayChildren();
	}

	@Override
	public URL getRetrievalURL()
	{
		return linkURL;
	}

	@Override
	public ITreeNode<ICatalogTreeNode>[] handleRetrieval(IRetrievalData data, URL url) throws Exception
	{
		InputStream is = data.getInputStream();
		try
		{
			ICatalogTreeNode root = DatasetReader.read(is, url);
			return root.getChildren();
		}
		finally
		{
			is.close();
		}
	}

	@Override
	public ITreeNode<ICatalogTreeNode> getLoadingNode()
	{
		return new LoadingCatalogTreeNode();
	}

	@Override
	public ITreeNode<ICatalogTreeNode> getErrorNode(Exception error)
	{
		return new ErrorCatalogTreeNode(error);
	}
}
