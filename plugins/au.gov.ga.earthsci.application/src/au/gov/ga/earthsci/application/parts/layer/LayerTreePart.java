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
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
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

	@PostConstruct
	public void init(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, Composite parent)
	{
		viewer = new CheckboxTreeViewer(parent);
		context.set(TreeViewer.class, viewer);

		IListProperty childrenProperty = new MultiListProperty(new IListProperty[] { BeanProperties.list("children") }); //$NON-NLS-1$

		ObservableListTreeContentProvider contentProvider =
				new ObservableListTreeContentProvider(childrenProperty.listFactory(), null);
		viewer.setContentProvider(contentProvider);

		IObservableSet knownElements = contentProvider.getKnownElements();
		final IObservableMap enableds = BeanProperties.value("enabled").observeDetail(knownElements); //$NON-NLS-1$
		final IObservableMap opacities = BeanProperties.value("opacity").observeDetail(knownElements); //$NON-NLS-1$
		final IObservableMap anyChildrenEnableds =
				BeanProperties.value("anyChildrenEnabled").observeDetail(knownElements); //$NON-NLS-1$
		final IObservableMap allChildrenEnableds =
				BeanProperties.value("allChildrenEnabled").observeDetail(knownElements); //$NON-NLS-1$

		final IObservableMap[] attributeMap =
				new IObservableMap[] { enableds, opacities, anyChildrenEnableds, allChildrenEnableds };
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
					label = String.format("%s (%d%%)", layerTreeNode.getName(), (int) (layer.getOpacity() * 100));
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
		Transfer[] transfers = new Transfer[] { LayerTransfer.getInstance() };
		viewer.addDragSupport(ops, transfers, new LayerTreeDragSourceListener(viewer));
		viewer.addDropSupport(ops, transfers, new LayerTreeDropAdapter(viewer, model));

		//add copy and paste support
		//Clipboard clipboard = new Clipboard(shell.getDisplay());
		
		//MPart part;
		//part.getToolbar().

		//IActionBars bars = getViewSite().getActionBars();
		//bars.setGlobalActionHandler(ActionFactory.CUT.getId(), new CutGadgetAction(viewer, clipboard));
		//bars.setGlobalActionHandler(ActionFactory.COPY.getId(), new CopyGadgetAction(viewer, clipboard));
		//bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), new PasteTreeGadgetAction(viewer, clipboard));
	}

	@Focus
	private void setFocus()
	{
		viewer.getTree().setFocus();
	}
}
