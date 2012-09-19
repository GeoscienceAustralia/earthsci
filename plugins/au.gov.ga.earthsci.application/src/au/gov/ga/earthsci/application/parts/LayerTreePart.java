package au.gov.ga.earthsci.application.parts;

import javax.inject.Inject;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.list.MultiListProperty;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.core.worldwind.TreeModel;

public class LayerTreePart
{
	@Inject
	private TreeModel model;

	private CheckboxTreeViewer viewer;

	@Inject
	public void init(Composite parent)
	{
		viewer = new CheckboxTreeViewer(parent);

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
				ILayerTreeNode<?> layerTreeNode = (ILayerTreeNode<?>) element;
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
				if (element instanceof ILayerTreeNode<?>)
				{
					ILayerTreeNode<?> node = (ILayerTreeNode<?>) element;
					return !node.isAllChildrenEnabled();
				}
				return false;
			}

			@Override
			public boolean isChecked(Object element)
			{
				if (element instanceof ILayerTreeNode<?>)
				{
					ILayerTreeNode<?> node = (ILayerTreeNode<?>) element;
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
				if (element instanceof ILayerTreeNode<?>)
				{
					ILayerTreeNode<?> node = (ILayerTreeNode<?>) element;
					node.enableChildren(event.getChecked());
				}
			}
		});
	}

}
