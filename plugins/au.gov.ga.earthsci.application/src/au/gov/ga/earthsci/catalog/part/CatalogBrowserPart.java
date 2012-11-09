package au.gov.ga.earthsci.catalog.part;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.core.model.catalog.CatalogFactory;
import au.gov.ga.earthsci.core.model.catalog.ICatalogModel;
import au.gov.ga.earthsci.core.tree.ILazyTreeNode;

/**
 * A part that renders a tree-view of the current {@link ICatalogModel} and allows
 * the user to browse and interact with elements in the model.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CatalogBrowserPart
{

	private TreeViewer viewer;
	
	@Inject
	private ICatalogModel model;
	
	@PostConstruct
	public void init(Composite parent, MPart part)
	{
		
		initTree();
		initViewer(parent);
		
	}
	
	@Deprecated
	private void initTree()
	{
		model.addTopLevelCatalog(CatalogFactory.loadCatalog(new File("V:/projects/data/12-6205 - IGC Common Earth Model/Viewer - Full version/data/Dataset/dataset.xml").toURI()));
	}

	private void initViewer(Composite parent)
	{
		viewer = new TreeViewer(parent, SWT.VIRTUAL);
		viewer.setContentProvider(new CatalogContentProvider(viewer));
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element)
			{
				return ((ILazyTreeNode<?>)element).getName();
			}
		});
		viewer.setSorter(null);
		viewer.setInput(model);
		viewer.getTree().setItemCount(1);
	}
}
