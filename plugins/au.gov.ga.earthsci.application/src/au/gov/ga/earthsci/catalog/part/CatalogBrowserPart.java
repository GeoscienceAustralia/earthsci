package au.gov.ga.earthsci.catalog.part;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.application.parts.layer.LayerTreeDragSourceListener;
import au.gov.ga.earthsci.core.model.catalog.ICatalogModel;
import au.gov.ga.earthsci.viewers.ControlTreeViewer;

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
	
	@Inject
	private CatalogTreeLabelProvider labelProvider;
	
	@Inject
	private IEclipseContext context;
	
	@Inject
	private ICatalogBrowserController controller;
	
	@PostConstruct
	public void init(Composite parent, MPart part)
	{
		initViewer(parent);
	}
	
	private void initViewer(Composite parent)
	{
		viewer = new ControlTreeViewer(parent, SWT.VIRTUAL);
		viewer.setContentProvider(new CatalogContentProvider(viewer));
		viewer.setLabelProvider(new DecoratingLabelProvider(labelProvider, labelProvider));
		viewer.setSorter(null);
		viewer.setInput(model);
		viewer.getTree().setItemCount(1);
		
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] {FileTransfer.getInstance(), CatalogTransfer.getInstance()}, new CatalogTreeDropAdapter(viewer, model));
		viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { CatalogTransfer.getInstance() }, new LayerTreeDragSourceListener(viewer));
	}
}
