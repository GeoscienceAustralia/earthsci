package au.gov.ga.earthsci.core.model.catalog.dataset;

import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.content.IContentType;

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

	/** The name to use for this node */
	private String name;
	
	/** The info URL (if applicable) for this node */
	private URL infoURL;
	
	/** The icon URL (if applicable) for this node */
	private URL iconURL;
	
	private boolean base;
	
	public DatasetCatalogTreeNode(final URI nodeURI, final String name, final URL infoURL, final URL iconURL, final boolean base)
	{
		super(nodeURI);
		
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
	public String getName()
	{
		return name;
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
	public IContentType getLayerContentType()
	{
		return null;
	}
	
	@Override
	public String toString()
	{
		return getLabelOrName();
	}
}
