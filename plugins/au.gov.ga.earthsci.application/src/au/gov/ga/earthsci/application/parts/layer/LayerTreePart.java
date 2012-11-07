package au.gov.ga.earthsci.application.parts.layer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.list.MultiListProperty;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.core.worldwind.ITreeModel;

public class LayerTreePart
{
	@Inject
	private ITreeModel model;

	@Inject
	private IEclipseContext context;

	private CheckboxTreeViewer viewer;
	private Clipboard clipboard;

	@PostConstruct
	public void init(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, Composite parent, EMenuService menuService)
	{
		viewer = new CheckboxTreeViewer(parent, SWT.MULTI);
		context.set(TreeViewer.class, viewer);

		clipboard = new Clipboard(shell.getDisplay());
		context.set(Clipboard.class, clipboard);

		IListProperty childrenProperty = new MultiListProperty(new IListProperty[] { BeanProperties.list("children") }); //$NON-NLS-1$

		ObservableListTreeContentProvider contentProvider =
				new ObservableListTreeContentProvider(childrenProperty.listFactory(), null);
		viewer.setContentProvider(contentProvider);

		IObservableSet knownElements = contentProvider.getKnownElements();
		final IObservableMap enableds = BeanProperties.value("enabled").observeDetail(knownElements); //$NON-NLS-1$
		final IObservableMap opacities = BeanProperties.value("opacity").observeDetail(knownElements); //$NON-NLS-1$
		final IObservableMap names = BeanProperties.value("name").observeDetail(knownElements); //$NON-NLS-1$
		final IObservableMap anyChildrenEnableds =
				BeanProperties.value("anyChildrenEnabled").observeDetail(knownElements); //$NON-NLS-1$
		final IObservableMap allChildrenEnableds =
				BeanProperties.value("allChildrenEnabled").observeDetail(knownElements); //$NON-NLS-1$

		final IObservableMap[] attributeMap =
				new IObservableMap[] { enableds, opacities, names, anyChildrenEnableds, allChildrenEnableds };
		ILabelProvider labelProvider = new ObservableMapLabelProvider(attributeMap)
		{
			@Override
			public String getColumnText(Object element, int columnIndex)
			{
				ILayerTreeNode layerTreeNode = (ILayerTreeNode) element;
				String label;
				if (element instanceof LayerNode)
				{
					LayerNode layer = (LayerNode) element;
					label = layerTreeNode.getName();
					if (layer.getOpacity() < 1)
					{
						label += String.format(" (%d%%)", (int) (layer.getOpacity() * 100));
					}
				}
				else if (element instanceof FolderNode)
				{
					FolderNode folder = (FolderNode) element;
					label = folder.getName();
				}
				else
				{
					label = element.toString();
				}
				return label;
			}
		};
		viewer.setLabelProvider(labelProvider);

		viewer.setInput(model.getRootNode());
		viewer.expandAll();

		viewer.setCheckStateProvider(new ICheckStateProvider()
		{
			@Override
			public boolean isGrayed(Object element)
			{
				if (element instanceof ILayerTreeNode)
				{
					ILayerTreeNode node = (ILayerTreeNode) element;
					return !node.isAllChildrenEnabled();
				}
				return false;
			}

			@Override
			public boolean isChecked(Object element)
			{
				if (element instanceof ILayerTreeNode)
				{
					ILayerTreeNode node = (ILayerTreeNode) element;
					return node.isAnyChildrenEnabled();
				}
				return false;
			}
		});

		viewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				Object element = event.getElement();
				if (element instanceof ILayerTreeNode)
				{
					ILayerTreeNode node = (ILayerTreeNode) element;
					node.enableChildren(event.getChecked());
				}
			}
		});

		//add drag and drop support
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		viewer.addDragSupport(ops, new Transfer[] { LayerTransfer.getInstance() }, new LayerTreeDragSourceListener(
				viewer));
		viewer.addDropSupport(ops, new Transfer[] { LayerTransfer.getInstance(), FileTransfer.getInstance() },
				new LayerTreeDropAdapter(viewer, model));

		//add context menu
		menuService.registerContextMenu(viewer.getTree(), "au.gov.ga.earthsci.application.layertree.popupmenu"); //$NON-NLS-1$
	}

	@Focus
	private void setFocus()
	{
		viewer.getTree().setFocus();
	}
}
