package au.gov.ga.earthsci.catalog.ui;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanListProperty;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.application.Activator;
import au.gov.ga.earthsci.application.tree.LazyObservableListTreeContentProvider;
import au.gov.ga.earthsci.catalog.ErrorCatalogTreeNode;
import au.gov.ga.earthsci.catalog.ICatalogModel;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.common.databinding.ObservableListTreeSupport;
import au.gov.ga.earthsci.common.databinding.TreeChangeAdapter;
import au.gov.ga.earthsci.common.ui.dialogs.StackTraceDialog;
import au.gov.ga.earthsci.common.ui.viewers.ControlTreeViewer;
import au.gov.ga.earthsci.layer.ui.dnd.LayerTransfer;

/**
 * A part that renders a tree-view of the current {@link ICatalogModel} and
 * allows the user to browse and interact with elements in the model.
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

	private boolean settingSelection = false;
	private ObservableListTreeSupport<ICatalogTreeNode> observableListTreeSupport;

	@PostConstruct
	public void init(final Composite parent, final MPart part, final EMenuService menuService)
	{
		initViewer(parent, part, menuService);
	}

	@PreDestroy
	private void packup()
	{
		observableListTreeSupport.dispose();
		labelProvider.dispose();
	}

	private void initViewer(final Composite parent, final MPart part, final EMenuService menuService)
	{
		viewer = new ControlTreeViewer(parent, SWT.MULTI);
		viewer.setLabelProvider(new DecoratingStyledCellLabelProvider(labelProvider, labelProvider, null));
		viewer.setSorter(null);

		IBeanListProperty childrenProperty =
				BeanProperties.list(ICatalogTreeNode.class, "children", ICatalogTreeNode.class); //$NON-NLS-1$
		IObservableFactory<ICatalogTreeNode, IObservableList<ICatalogTreeNode>> listFactory =
				childrenProperty.listFactory();
		LazyObservableListTreeContentProvider<ICatalogTreeNode> contentProvider =
				new LazyObservableListTreeContentProvider<ICatalogTreeNode>(listFactory, null);
		viewer.setContentProvider((IContentProvider) contentProvider);

		observableListTreeSupport = new ObservableListTreeSupport<ICatalogTreeNode>(listFactory);
		observableListTreeSupport.addListener(new TreeChangeAdapter<ICatalogTreeNode>()
		{
			@Override
			public void elementAdded(ICatalogTreeNode element)
			{
				if (element.isRoot() || element.getParent().isRoot())
				{
					viewer.expandToLevel(element, 1);
				}
			}
		});

		viewer.setInput(model.getRoot());
		observableListTreeSupport.setInput(model.getRoot());

		viewer.addDropSupport(DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_MOVE,
				new Transfer[] { FileTransfer.getInstance() },
				new CatalogTreeDropAdapter(viewer, model, context));
		viewer.addDragSupport(DND.DROP_DEFAULT | DND.DROP_COPY, new Transfer[] { LayerTransfer.getInstance() },
				new CatalogTreeDragSourceListener(viewer, controller));

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
				settingSelection = true;
				selectionService.setSelection(array);
				settingSelection = false;
			}
		});

		viewer.getTree().addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				ICatalogTreeNode firstElement = (ICatalogTreeNode) selection.getFirstElement();
				if (firstElement != null)
				{
					selectNode(firstElement);
				}
			}
		});

		menuService.registerContextMenu(viewer.getTree(), "au.gov.ga.earthsci.application.catalogbrowser.popupmenu"); //$NON-NLS-1$
	}

	private void selectNode(ICatalogTreeNode node)
	{
		if (node instanceof ErrorCatalogTreeNode)
		{
			ErrorCatalogTreeNode errorNode = (ErrorCatalogTreeNode) node;
			IStatus status = new Status(IStatus.ERROR,
					Activator.getBundleName(),
					errorNode.getMessage(),
					errorNode.getError());
			StackTraceDialog.openError(viewer.getTree().getShell(),
					Messages.CatalogBrowserPart_ErrorDialogTitle, null, status);
		}
		else if (node.isLayerNode())
		{
			controller.addToLayerModel(node);
		}
		viewer.expandToLevel(node, 1);
	}

	public TreeViewer getTreeViewer()
	{
		return viewer;
	}

	public CatalogTreeLabelProvider getLabelProvider()
	{
		return labelProvider;
	}

	@Inject
	private void select(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ICatalogTreeNode[] nodes)
	{
		if (nodes == null || viewer == null || settingSelection)
		{
			return;
		}
		StructuredSelection selection = new StructuredSelection(nodes);
		viewer.setSelection(selection, true);
		for (ICatalogTreeNode node : nodes)
		{
			viewer.expandToLevel(node, 1);
		}
	}

	@Inject
	private void select(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ICatalogTreeNode node)
	{
		if (node == null)
		{
			return;
		}
		select(new ICatalogTreeNode[] { node });
	}
}
