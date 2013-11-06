package au.gov.ga.earthsci.catalog.dataset;

import java.net.URI;
import java.net.URL;

import au.gov.ga.earthsci.catalog.AbstractCatalogTreeNode;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;

/**
 * An {@link ICatalogTreeNode} that represents a {@code Dataset} element from
 * the legacy {@code dataset.xml}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DatasetCatalogTreeNode extends AbstractCatalogTreeNode
{

	/** The name to use for this node */
	private final String name;

	/** The info URL (if applicable) for this node */
	private final URL infoURL;

	/** The icon URL (if applicable) for this node */
	private final URL iconURL;

	private final boolean base;

	private final boolean removeable;

	public DatasetCatalogTreeNode(final URI nodeURI, final String name, final URL infoURL, final URL iconURL,
			final boolean base)
	{
		this(nodeURI, name, infoURL, iconURL, base, false);
	}

	public DatasetCatalogTreeNode(final URI nodeURI, final String name, final URL infoURL, final URL iconURL,
			final boolean base, boolean removeable)
	{
		super(nodeURI);

		this.name = name;
		this.infoURL = infoURL;
		this.iconURL = iconURL;
		this.base = base;
		this.removeable = removeable;
	}

	@Override
	public boolean isRemoveable()
	{
		return removeable;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public URL getIconURL()
	{
		return iconURL != null ? iconURL : super.getIconURL();
	}

	public boolean isBase()
	{
		return base;
	}

	@Override
	public boolean isLayerNode()
	{
		return false;
	}

	@Override
	public URI getLayerURI()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return getLabelOrName();
	}

	@Override
	public URL getInformationURL()
	{
		return infoURL;
	}

	@Override
	public String getInformationString()
	{
		//TODO
		return null;
	}
}
