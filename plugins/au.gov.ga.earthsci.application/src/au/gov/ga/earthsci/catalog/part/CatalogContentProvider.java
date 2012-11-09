package au.gov.ga.earthsci.catalog.part;

import org.eclipse.jface.viewers.TreeViewer;

import au.gov.ga.earthsci.application.tree.LazyTreeNodeContentProvider;
import au.gov.ga.earthsci.core.model.catalog.ICatalogModel;

/**
 * An extension of the {@link LazyTreeNodeContentProvider} that returns the appropriate
 * root level nodes for the current {@link ICatalogModel} 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CatalogContentProvider extends LazyTreeNodeContentProvider
{

	public CatalogContentProvider(final TreeViewer viewer)
	{
		super(viewer);
	}
	
	@Override
	public Object[] getElements(final Object inputElement)
	{
		return ((ICatalogModel)inputElement).getTopLevelCatalogs();
	}

}
