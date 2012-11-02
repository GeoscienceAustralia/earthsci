package au.gov.ga.earthsci.core.model.catalog.dataset;

import java.net.URL;

import au.gov.ga.earthsci.core.model.catalog.AbstractCatalogTreeNode;
import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;

/**
 * An {@link ICatalogTreeNode} that represents a {@code Dataset} element from the legacy
 * {@code dataset.xml} 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DatasetCatalogTreeNode extends AbstractCatalogTreeNode
{

	private final String name;
	private final URL infoURL;
	private final URL iconURL;
	private final boolean base;
	
	public DatasetCatalogTreeNode(String name, URL infoURL, URL iconURL, boolean base)
	{
		this.name = name;
		this.infoURL = infoURL;
		this.iconURL = iconURL;
		this.base = base;
	}

	@Override
	public boolean isRemoveable()
	{
		return true;
	}

	@Override
	public boolean isReloadable()
	{
		return true;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean isLoaded()
	{
		return true;
	}
	
	public URL getIconURL()
	{
		return iconURL;
	}
	
	public URL getInfoURL()
	{
		return infoURL;
	}

	public boolean isBase()
	{
		return base;
	}
	
}
