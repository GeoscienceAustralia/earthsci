package au.gov.ga.earthsci.catalog.part;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;

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

	private ControlTreeViewer viewer;
	
	@Inject
	private ICatalogModel model;
	
	@Inject
	private CatalogTreeControlProvider controlProvider;
	
	@PostConstruct
	public void init(Composite parent, MPart part)
	{
		initViewer(parent);
	}
	
	private void initViewer(Composite parent)
	{
		viewer = new ControlTreeViewer(parent, SWT.VIRTUAL);
		viewer.setContentProvider(new CatalogContentProvider(viewer));
		viewer.setControlProvider(controlProvider);
		viewer.setLabelProvider(controlProvider);
		viewer.setSorter(null);
		viewer.setInput(model);
		viewer.getTree().setItemCount(1);
		
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] {FileTransfer.getInstance()}, new CatalogTreeDropAdapter(viewer, model));
	}
}
