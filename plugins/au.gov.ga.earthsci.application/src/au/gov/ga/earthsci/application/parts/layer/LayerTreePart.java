package au.gov.ga.earthsci.application.parts.layer;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.list.MultiListProperty;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.application.ImageRegistry;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.tree.ITreeNode;
import au.gov.ga.earthsci.core.worldwind.ITreeModel;
import au.gov.ga.earthsci.core.worldwind.WorldWindView;
import au.gov.ga.earthsci.worldwind.common.layers.Bounded;
import au.gov.ga.earthsci.worldwind.common.util.FlyToSectorAnimator;
import au.gov.ga.earthsci.worldwind.common.util.Util;

public class LayerTreePart
{
	@Inject
	private ITreeModel model;

	@Inject
	private WorldWindView view;

	@Inject
	private IEclipseContext context;

	@Inject
	private ESelectionService selectionService;

	private CheckboxTreeViewer viewer;
	private LayerTreeLabelProvider labelProvider;
	private Clipboard clipboard;

	@PostConstruct
	public void init(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, Composite parent, EMenuService menuService)
	{
		LayerOpacityToolControl.setPartContext(context);

		viewer = new CheckboxTreeViewer(parent, SWT.MULTI);
		viewer.getTree().setBackgroundImage(ImageRegistry.getInstance().get(ImageRegistry.ICON_TRANSPARENT));
		context.set(TreeViewer.class, viewer);

		clipboard = new Clipboard(shell.getDisplay());
		context.set(Clipboard.class, clipboard);

		IListProperty childrenProperty = new MultiListProperty(new IListProperty[] { BeanProperties.list("children") }); //$NON-NLS-1$

		ObservableListTreeContentProvider contentProvider =
				new ObservableListTreeContentProvider(childrenProperty.listFactory(), null);
		viewer.setContentProvider(contentProvider);

		//TreeViewerEditor.create(viewer, new SecondClickColumnViewerEditorActivationStrategy(viewer), ColumnViewerEditor.DEFAULT);

		IObservableSet knownElements = contentProvider.getKnownElements();
		IObservableMap enabledMap = BeanProperties.value("enabled").observeDetail(knownElements); //$NON-NLS-1$
		IObservableMap opacityMap = BeanProperties.value("opacity").observeDetail(knownElements); //$NON-NLS-1$
		IObservableMap nameMap = BeanProperties.value("name").observeDetail(knownElements); //$NON-NLS-1$
		IObservableMap labelMap = BeanProperties.value("label").observeDetail(knownElements); //$NON-NLS-1$
		IObservableMap anyChildrenEnabledMap = BeanProperties.value("anyChildrenEnabled").observeDetail(knownElements); //$NON-NLS-1$
		IObservableMap allChildrenEnabledMap = BeanProperties.value("allChildrenEnabled").observeDetail(knownElements); //$NON-NLS-1$
		IObservableMap childrenMap = BeanProperties.value("children").observeDetail(knownElements); //$NON-NLS-1$
		IObservableMap expandedMap = BeanProperties.value("expanded").observeDetail(knownElements); //$NON-NLS-1$

		IObservableMap[] labelAttributeMaps =
				new IObservableMap[] { enabledMap, opacityMap, nameMap, labelMap, anyChildrenEnabledMap,
						allChildrenEnabledMap };

		labelProvider = new LayerTreeLabelProvider(labelAttributeMaps);
		viewer.setLabelProvider(labelProvider);
		viewer.setCheckStateProvider(new LayerTreeCheckStateProvider());

		viewer.setInput(model.getRootNode());
		viewer.setExpandedElements(getExpandedNodes());

		IMapChangeListener childrenListener = new IMapChangeListener()
		{
			@Override
			public void handleMapChange(MapChangeEvent event)
			{
				//for any children added, expand the nodes
				Set<?> addedKeys = event.diff.getAddedKeys();
				for (Object o : addedKeys)
				{
					if (o instanceof ILayerTreeNode)
					{
						((ILayerTreeNode) o).getParent().getValue().setExpanded(true);
					}
				}
			}
		};
		IMapChangeListener expandedListener = new IMapChangeListener()
		{
			@Override
			public void handleMapChange(MapChangeEvent event)
			{
				//ensure the expanded elements are kept in sync with the model
				viewer.getTree().getDisplay().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						if (!viewer.getTree().isDisposed())
						{
							viewer.setExpandedElements(getExpandedNodes());
						}
					}
				});
			}
		};
		childrenMap.addMapChangeListener(childrenListener);
		childrenMap.addMapChangeListener(expandedListener);
		expandedMap.addMapChangeListener(expandedListener);

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

		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				List<?> list = selection.toList();
				ILayerTreeNode[] array = list.toArray(new ILayerTreeNode[list.size()]);
				selectionService.setSelection(selection.size() == 1 ? selection.getFirstElement() : array);
			}
		});

		viewer.addTreeListener(new ITreeViewerListener()
		{
			@Override
			public void treeExpanded(TreeExpansionEvent event)
			{
				ILayerTreeNode layerNode = (ILayerTreeNode) event.getElement();
				layerNode.setExpanded(true);
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event)
			{
				ILayerTreeNode layerNode = (ILayerTreeNode) event.getElement();
				layerNode.setExpanded(false);
			}
		});

		viewer.getTree().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
				ViewerCell cell = viewer.getCell(new Point(e.x, e.y));
				if (cell == null)
					return;

				ILayerTreeNode layer = (ILayerTreeNode) cell.getElement();
				flyToLayer(layer);
			}

			@Override
			public void mouseDown(MouseEvent e)
			{
				ViewerCell cell = viewer.getCell(new Point(e.x, e.y));
				if (cell == null)
				{
					viewer.setSelection(StructuredSelection.EMPTY);
				}
			}
		});

		viewer.getTree().addTraverseListener(new TraverseListener()
		{
			@Override
			public void keyTraversed(TraverseEvent e)
			{
				if (e.detail == SWT.TRAVERSE_RETURN)
				{
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					if (selection.size() == 1)
					{
						ILayerTreeNode layer = (ILayerTreeNode) selection.getFirstElement();
						flyToLayer(layer);
					}
				}
			}
		});

		viewer.setCellEditors(new CellEditor[] { new TextCellEditor(viewer.getTree(), SWT.BORDER) });
		viewer.setColumnProperties(new String[] { "layer" }); //$NON-NLS-1$

		viewer.setCellModifier(new ICellModifier()
		{
			@Override
			public void modify(Object element, String property, Object value)
			{
				if (element instanceof Item)
				{
					element = ((Item) element).getData();
				}
				((ILayerTreeNode) element).setLabel((String) value);
			}

			@Override
			public Object getValue(Object element, String property)
			{
				if (element instanceof Item)
				{
					element = ((Item) element).getData();
				}
				return ((ILayerTreeNode) element).getLabelOrName();
			}

			@Override
			public boolean canModify(Object element, String property)
			{
				return true;
			}
		});

		ColumnViewerEditorActivationStrategy activationStrategy = new ColumnViewerEditorActivationStrategy(viewer)
		{
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event)
			{
				return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}
		};
		TreeViewerEditor.create(viewer, activationStrategy, ColumnViewerEditor.KEYBOARD_ACTIVATION);

		//add drag and drop support
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		viewer.addDragSupport(ops, new Transfer[] { LayerTransfer.getInstance() }, new LayerTreeDragSourceListener(
				viewer));
		viewer.addDropSupport(ops, new Transfer[] { LayerTransfer.getInstance(), FileTransfer.getInstance() },
				new LayerTreeDropAdapter(viewer, model));

		//add context menu
		menuService.registerContextMenu(viewer.getTree(), "au.gov.ga.earthsci.application.layertree.popupmenu"); //$NON-NLS-1$
	}

	@PreDestroy
	private void packup()
	{
		context.remove(TreeViewer.class);
		context.remove(Clipboard.class);
		labelProvider.packup();
	}

	@Focus
	private void setFocus()
	{
		viewer.getTree().setFocus();
	}

	private ILayerTreeNode[] getExpandedNodes()
	{
		List<ILayerTreeNode> list = new ArrayList<ILayerTreeNode>();
		addExpandedChildrenToList(model.getRootNode(), list);
		return list.toArray(new ILayerTreeNode[list.size()]);
	}

	private void addExpandedChildrenToList(ILayerTreeNode parent, List<ILayerTreeNode> list)
	{
		if (parent.isExpanded())
		{
			list.add(parent);
		}
		for (ITreeNode<ILayerTreeNode> child : parent.getChildren())
		{
			addExpandedChildrenToList(child.getValue(), list);
		}
	}

	public void flyToLayer(ILayerTreeNode layer)
	{
		//first check if the tree node is pointing to a KML feature; if so, goto the feature 
		/*if (layer instanceof TreeNodeLayerNode)
		{
			TreeNode treeNode = ((TreeNodeLayerNode) layer).getTreeNode();
			if (treeNode instanceof KMLFeatureTreeNode)
			{
				KMLAbstractFeature feature = ((KMLFeatureTreeNode) treeNode).getFeature();

				KMLViewController viewController = CustomKMLViewControllerFactory.create(wwd);
				if (viewController != null)
				{
					viewController.goTo(feature);
					wwd.redraw();
					return;
				}
			}
		}*/

		Sector sector = Bounded.Reader.getSector(layer);
		if (sector == null || !(view instanceof OrbitView))
		{
			return;
		}

		OrbitView orbitView = view;
		Position center = orbitView.getCenterPosition();
		Position newCenter;
		if (sector.contains(center) && sector.getDeltaLatDegrees() > 90 && sector.getDeltaLonDegrees() > 90)
		{
			newCenter = center;
		}
		else
		{
			newCenter = new Position(sector.getCentroid(), 0);
		}

		LatLon endVisibleDelta = new LatLon(sector.getDeltaLat(), sector.getDeltaLon());
		long lengthMillis = Util.getScaledLengthMillis(1, center, newCenter);

		FlyToOrbitViewAnimator animator =
				FlyToSectorAnimator.createFlyToSectorAnimator(orbitView, center, newCenter, orbitView.getHeading(),
						orbitView.getPitch(), orbitView.getZoom(), endVisibleDelta, lengthMillis);
		orbitView.stopAnimations();
		orbitView.stopMovement();
		orbitView.addAnimator(animator);
		orbitView.firePropertyChange(AVKey.VIEW, null, orbitView);
	}
}
