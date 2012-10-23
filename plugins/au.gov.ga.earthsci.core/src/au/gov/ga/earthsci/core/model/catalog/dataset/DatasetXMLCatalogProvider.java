package au.gov.ga.earthsci.core.model.catalog.dataset;

import java.net.URI;

import org.w3c.dom.Document;

import au.gov.ga.earthsci.core.model.catalog.ICatalogProvider;
import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.util.XmlUtil;

/**
 * An {@link ICatalogProvider} that can load a catalog from a legacy {@code dataset.xml} file
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DatasetXMLCatalogProvider implements ICatalogProvider
{

	private static final String DATASET_ROOT_ELEMENT = "datasetlist"; //$NON-NLS-1$

	@Override
	public boolean supports(URI source)
	{
		if (source == null)
		{
			return false;
		}
		
		Document datasetDocument = XmlUtil.openDocument(source);
		if (datasetDocument != null && datasetDocument.getDocumentElement().getNodeName().equalsIgnoreCase(DATASET_ROOT_ELEMENT))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public ICatalogTreeNode loadCatalog(URI source)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
