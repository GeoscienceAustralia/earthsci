package au.gov.ga.earthsci.catalog.part;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.application.parts.layer.LayerTransfer;
import au.gov.ga.earthsci.core.model.catalog.ICatalogModel;
import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
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
	
	@Inject
	private ESelectionService selectionService;
	
	@PostConstruct
	public void init(final Composite parent, final MPart part, final EMenuService menuService)
	{
		controller.setCatalogBrowserPart(this);
		
		initViewer(parent, part, menuService);
		
	}
	
	private void initViewer(final Composite parent, final MPart part, final EMenuService menuService)
	{
		viewer = new ControlTreeViewer(parent, SWT.VIRTUAL);
		viewer.setContentProvider(new CatalogContentProvider(viewer));
		viewer.setLabelProvider(new DecoratingLabelProvider(labelProvider, labelProvider));
		viewer.setSorter(null);
		viewer.setInput(model);
		
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] {FileTransfer.getInstance()}, new CatalogTreeDropAdapter(viewer, model));
		viewer.addDragSupport(DND.DROP_DEFAULT | DND.DROP_COPY, new Transfer[] {LayerTransfer.getInstance()}, new CatalogTreeDragSourceListener(viewer, controller));
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				List<?> list = selection.toList();
				ICatalogTreeNode[] array;
				try
				{
					array = list.toArray(new ICatalogTreeNode[list.size()]);
				}
				catch (ArrayStoreException e)
				{
					//occurs when the selection contains a loading node, which is not an ICatalogTreeNode
					array = new ICatalogTreeNode[0];
				}
				selectionService.setSelection(array);
			}
		});
		
		menuService.registerContextMenu(viewer.getTree(), "au.gov.ga.earthsci.application.catalogbrowser.popupmenu"); //$NON-NLS-1$
	}
	
	public TreeViewer getTreeViewer()
	{
		return viewer;
	}
	
	public CatalogTreeLabelProvider getLabelProvider()
	{
		return labelProvider;
	}
}
