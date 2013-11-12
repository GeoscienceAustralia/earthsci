package au.gov.ga.earthsci.catalog;

import java.net.URI;
import java.net.URL;

import org.eclipse.e4.core.contexts.IEclipseContext;

import au.gov.ga.earthsci.layer.tree.ILayerNode;

public class DummyCatalogNode extends AbstractCatalogTreeNode
{
	public DummyCatalogNode(URI nodeURI)
	{
		super(nodeURI);
	}

	@Override
	public boolean isRemoveable()
	{
		return false;
	}

	@Override
	public boolean isLayerNode()
	{
		return false;
	}

	@Override
	public void loadLayer(ILayerNode node, IEclipseContext context) throws Exception
	{
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public URL getInformationURL()
	{
		return null;
	}

	@Override
	public String getInformationString()
	{
		return null;
	}
}
