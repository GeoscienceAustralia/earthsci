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

	private String name;
	private URL infoURL;
	private URL iconURL;

	public DatasetCatalogTreeNode(String name, URL infoURL, URL iconURL)
	{
		this.name = name;
		this.infoURL = infoURL;
		this.iconURL = iconURL;
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

}
