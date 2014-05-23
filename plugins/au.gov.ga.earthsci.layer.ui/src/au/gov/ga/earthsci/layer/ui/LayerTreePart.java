/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.layer.ui;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.OrbitView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
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
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.application.Activator;
import au.gov.ga.earthsci.application.ImageRegistry;
import au.gov.ga.earthsci.application.parts.globe.handlers.TargetModeSwitcher;
import au.gov.ga.earthsci.common.databinding.ITreeChangeListener;
import au.gov.ga.earthsci.common.databinding.ObservableListTreeSupport;
import au.gov.ga.earthsci.common.databinding.TreeChangeAdapter;
import au.gov.ga.earthsci.common.ui.dialogs.StackTraceDialog;
import au.gov.ga.earthsci.layer.tree.ILayerTreeNode;
import au.gov.ga.earthsci.layer.ui.dnd.LayerTransfer;
import au.gov.ga.earthsci.layer.ui.dnd.LocalLayerTransfer;
import au.gov.ga.earthsci.layer.worldwind.ITreeModel;
import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;
import au.gov.ga.earthsci.worldwind.common.layers.Bounded;
import au.gov.ga.earthsci.worldwind.common.layers.Bounds;
import au.gov.ga.earthsci.worldwind.common.util.Util;
import au.gov.ga.earthsci.worldwind.common.view.orbit.FlyToOrbitViewAnimator;
import au.gov.ga.earthsci.worldwind.common.view.orbit.FlyToSectorAnimator;
import au.gov.ga.earthsci.worldwind.common.view.target.TargetOrbitView;

/**
 * Part that shows the hierarchical tree of layers.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTreePart
{
	public static final String PART_ID = "au.gov.ga.earthsci.application.layertree.part"; //$NON-NLS-1$

	@Inject
	private ITreeModel model;

	@Inject
	private IEclipseContext context;

	@Inject
	private ESelectionService selectionService;
	private boolean settingSelection = false;

	@Inject
	private EPartService partService;

	@Inject
	@Optional
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	private CTabFolder tabFolder;
	private CTabItem structureTabItem;
	private CTabItem orderTabItem;
	private CheckboxTreeViewer structureViewer;
	private TreeViewer orderViewer;
	private LayerTreeLabelProvider labelProvider;
	private Clipboard clipboard;
	private ObservableListTreeSupport<ILayerTreeNode> observableListTreeSupport;
	private final DrawOrderModel drawOrderModel = new DrawOrderModel();

	@PostConstruct
	public void init(Composite parent, EMenuService menuService)
	{
		tabFolder = new CTabFolder(parent, SWT.BOTTOM);

		structureTabItem = new CTabItem(tabFolder, SWT.NONE);
		structureTabItem.setText("Structure");
		createStructureViewer(tabFolder, menuService, structureTabItem);

		orderTabItem = new CTabItem(tabFolder, SWT.NONE);
		orderTabItem.setText("Draw order");
		createOrderViewer(tabFolder, menuService, orderTabItem);

		tabFolder.pack();
		tabFolder.setSelection(structureTabItem);
	}

	protected void createStructureViewer(Composite parent, EMenuService menuService, CTabItem tabItem)
	{
		structureViewer = new CheckboxTreeViewer(parent, SWT.MULTI);
		tabItem.setControl(structureViewer.getControl());
		structureViewer.getTree().setBackgroundImage(ImageRegistry.getInstance().get(ImageRegistry.ICON_TRANSPARENT));
		context.set(TreeViewer.class, structureViewer);

		clipboard = new Clipboard(parent.getDisplay());
		context.set(Clipboard.class, clipboard);

		//create a property change listener for updating the labels whenever a property
		//changes on an ILayerTreeNode instance
		final PropertyChangeListener anyChangeListener = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(final PropertyChangeEvent evt)
			{
				updateElementLabel(evt.getSource());
			}
		};
		//create a property change listener that ensures the expanded state of the tree
		//is kept in sync with the value of the expanded property for each node
		final PropertyChangeListener expandedChangeListener = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				syncExpandedNodes();
			}
		};

		//setup the label provider
		labelProvider = new LayerTreeLabelProvider();

		//create a bean list property associated with ILayerTreeNode's children property
		IBeanListProperty<ILayerTreeNode, ILayerTreeNode> childrenProperty =
				BeanProperties.list(ILayerTreeNode.class, "children", ILayerTreeNode.class); //$NON-NLS-1$
		//setup a factory for creating observables observing ILayerTreeNodes
		IObservableFactory<ILayerTreeNode, IObservableList<ILayerTreeNode>> observableFactory =
				childrenProperty.listFactory();

		//listen for any changes (additions/removals) to any of the children in the tree
		observableListTreeSupport = new ObservableListTreeSupport<ILayerTreeNode>(observableFactory);
		observableListTreeSupport.addListener(new ITreeChangeListener<ILayerTreeNode>()
		{
			@Override
			public void elementAdded(ILayerTreeNode element)
			{
				element.addPropertyChangeListener(anyChangeListener);
				element.addPropertyChangeListener("expanded", expandedChangeListener); //$NON-NLS-1$
			}

			@Override
			public void elementRemoved(ILayerTreeNode element)
			{
				element.removePropertyChangeListener(anyChangeListener);
				element.removePropertyChangeListener("expanded", expandedChangeListener); //$NON-NLS-1$
			}
		});

		//create a content provider that listens for changes to any children in the tree
		@SuppressWarnings("rawtypes")
		IObservableFactory rawFactory = observableFactory;
		@SuppressWarnings("unchecked")
		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(rawFactory, null);

		//set the viewer's providers
		structureViewer.setContentProvider(contentProvider);
		structureViewer.setLabelProvider(labelProvider);
		structureViewer.setCheckStateProvider(new LayerTreeCheckStateProvider());

		//set the viewer and listener inputs
		structureViewer.setInput(model.getRootNode());
		observableListTreeSupport.setInput(model.getRootNode());

		//Listen for any additions to the tree, and expand added node's parent, so that
		//added nodes are always visible. This is done after the input is set up, so that
		//we don't expand all the nodes that are already in the tree.
		observableListTreeSupport.addListener(new TreeChangeAdapter<ILayerTreeNode>()
		{
			@Override
			public void elementAdded(ILayerTreeNode element)
			{
				//for any children added, expand the nodes
				if (!element.isRoot())
				{
					element.getParent().setExpanded(true);
				}

				//if the nodes were already expanded, the expanded property change event
				//is not fired, so we need to sync the expanded state anyway
				syncExpandedNodes();

				//when a layer is added, we should activate this part and select the added element
				activateAndSelectElement(element);
			}
		});

		//expand any nodes that should be expanded after unpersisting
		syncExpandedNodes();

		//enable/disable any nodes that are checked/unchecked
		structureViewer.addCheckStateListener(new ICheckStateListener()
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

		//setup the selection tracking
		structureViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection selection = (IStructuredSelection) structureViewer.getSelection();
				List<?> list = selection.toList();
				ILayerTreeNode[] array = list.toArray(new ILayerTreeNode[list.size()]);
				settingSelection = true;
				selectionService.setSelection(array.length == 1 ? array[0] : array);
				settingSelection = false;
			}
		});
		structureViewer.getTree().addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				IStructuredSelection selection = (IStructuredSelection) structureViewer.getSelection();
				ILayerTreeNode firstElement = (ILayerTreeNode) selection.getFirstElement();
				if (firstElement != null)
				{
					selectLayer(firstElement);
				}
			}
		});

		//setup tree expansion/collapse listening
		structureViewer.addTreeListener(new ITreeViewerListener()
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

		//make tree cells unselectable by selecting outside
		structureViewer.getTree().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDown(MouseEvent e)
			{
				ViewerCell cell = structureViewer.getCell(new Point(e.x, e.y));
				if (cell == null)
				{
					structureViewer.setSelection(StructuredSelection.EMPTY);
				}
			}
		});

		//setup cell editing
		structureViewer.setCellEditors(new CellEditor[] { new TextCellEditor(structureViewer.getTree(), SWT.BORDER) });
		structureViewer.setColumnProperties(new String[] { "layer" }); //$NON-NLS-1$
		structureViewer.setCellModifier(new ICellModifier()
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

		ColumnViewerEditorActivationStrategy activationStrategy =
				new ColumnViewerEditorActivationStrategy(structureViewer)
				{
					@Override
					protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event)
					{
						return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
					}
				};
		TreeViewerEditor.create(structureViewer, activationStrategy, ColumnViewerEditor.KEYBOARD_ACTIVATION);

		//add drag and drop support
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		structureViewer.addDragSupport(ops,
				new Transfer[] { LocalLayerTransfer.getInstance(), LayerTransfer.getInstance() },
				new LayerTreeDragSourceListener(structureViewer));
		structureViewer.addDropSupport(ops,
				new Transfer[] { LocalLayerTransfer.getInstance(), LayerTransfer.getInstance(),
						FileTransfer.getInstance() },
				new LayerTreeDropAdapter(structureViewer, model, context));

		//add context menu
		menuService
				.registerContextMenu(structureViewer.getTree(), "au.gov.ga.earthsci.application.layertree.popupmenu"); //$NON-NLS-1$
	}

	protected void createOrderViewer(Composite parent, EMenuService menuService, CTabItem tabItem)
	{
		orderViewer = new TreeViewer(parent, SWT.MULTI);
		tabItem.setControl(orderViewer.getControl());
		orderViewer.getTree().setBackgroundImage(ImageRegistry.getInstance().get(ImageRegistry.ICON_TRANSPARENT));
		orderViewer.setAutoExpandLevel(2);

		drawOrderModel.setInput(model.getRootNode());

		//create a bean list property associated with ILayerTreeNode's children property
		IBeanListProperty<DrawOrderModel.IDrawOrderModelElement, DrawOrderModel.IDrawOrderModelElement> childrenProperty =
				BeanProperties.list(DrawOrderModel.IDrawOrderModelElement.class,
						"children", DrawOrderModel.IDrawOrderModelElement.class); //$NON-NLS-1$
		//setup a factory for creating observables observing ILayerTreeNodes
		IObservableFactory<DrawOrderModel.IDrawOrderModelElement, IObservableList<DrawOrderModel.IDrawOrderModelElement>> observableFactory =
				childrenProperty.listFactory();

		//create a content provider that listens for changes to any children in the tree
		@SuppressWarnings("rawtypes")
		IObservableFactory rawFactory = observableFactory;
		@SuppressWarnings("unchecked")
		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(rawFactory, null);

		DrawOrderLabelProvider labelProvider = new DrawOrderLabelProvider();

		//set the viewer's providers
		orderViewer.setContentProvider(contentProvider);
		orderViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(labelProvider));

		//set the viewer and listener inputs
		orderViewer.setInput(drawOrderModel.getRoot());

		//add drag and drop support
		int ops = DND.DROP_MOVE;
		orderViewer.addDragSupport(ops, new Transfer[] { LocalLayerTransfer.getInstance() },
				new DrawOrderDragSourceListener(orderViewer));
		orderViewer.addDropSupport(ops, new Transfer[] { LocalLayerTransfer.getInstance() },
				new DrawOrderDropAdapter(orderViewer));
	}

	@PreDestroy
	private void packup()
	{
		observableListTreeSupport.dispose();
		context.remove(TreeViewer.class);
		context.remove(Clipboard.class);
		labelProvider.packup();
		drawOrderModel.dispose();
	}

	@Focus
	private void setFocus()
	{
		if (tabFolder.getSelection() == orderTabItem)
		{
			orderViewer.getTree().setFocus();
		}
		else
		{
			structureViewer.getTree().setFocus();
		}
	}

	private void updateElementLabel(final Object element)
	{
		if (!structureViewer.getControl().isDisposed())
		{
			structureViewer.getControl().getDisplay().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					LabelProviderChangedEvent event = new LabelProviderChangedEvent(labelProvider, element);
					labelProvider.fireLabelProviderChanged(event);
				}
			});
		}
	}

	private void syncExpandedNodes()
	{
		//ensure the expanded elements are kept in sync with the model
		structureViewer.getControl().getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (!structureViewer.getControl().isDisposed())
				{
					structureViewer.setExpandedElements(getExpandedNodes());
				}
			}
		});
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
		for (ILayerTreeNode child : parent.getChildren())
		{
			addExpandedChildrenToList(child, list);
		}
	}

	public void selectLayer(ILayerTreeNode layer)
	{
		if (layer.getStatus().isError())
		{
			Throwable e = layer.getStatus().getThrowable();
			IStatus status = new Status(IStatus.ERROR, Activator.getBundleName(), e.getLocalizedMessage(), e);
			StackTraceDialog.openError(shell, "Error", null, status);
		}
		else
		{
			flyToLayer(layer);
		}
	}

	public static void flyToLayer(ILayerTreeNode layer)
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

		View view = WorldWindowRegistry.INSTANCE.getActiveView();
		if (view == null)
		{
			return;
		}

		Bounds bounds = Bounded.Reader.getBounds(layer);
		if (bounds == null || !(view instanceof OrbitView))
		{
			return;
		}

		boolean targetMode = false;
		if (view instanceof TargetOrbitView)
		{
			targetMode = !(Bounded.Reader.isFollowTerrain(layer) &&
					bounds.minimum.elevation == 0 && bounds.maximum.elevation == 0);
			TargetModeSwitcher.setTargetMode((TargetOrbitView) view, targetMode);
		}

		OrbitView orbitView = (OrbitView) view;
		Position center = orbitView.getCenterPosition();
		Position newCenter;
		if (bounds.toSector().contains(center) &&
				bounds.deltaLatitude.degrees > 90 && bounds.deltaLongitude.degrees > 90)
		{
			//handles large datasets, like the blue marble imagery, and landsat
			newCenter = targetMode ? center : new Position(center, 0);
		}
		else
		{
			newCenter = bounds.center;
		}

		LatLon endVisibleDelta = new LatLon(bounds.deltaLatitude, bounds.deltaLongitude);
		long lengthMillis = Util.getScaledLengthMillis(1, center, newCenter);

		Angle newHeading = orbitView.getHeading();
		Angle newPitch = orbitView.getPitch();
		if (!targetMode && newPitch.degrees > 45)
		{
			newPitch = Angle.fromDegrees(45);
		}

		double newZoom = FlyToSectorAnimator.calculateEndZoom(orbitView, endVisibleDelta);
		FlyToOrbitViewAnimator animator = FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(orbitView,
				center, newCenter,
				orbitView.getHeading(), newHeading,
				orbitView.getPitch(), newPitch,
				orbitView.getZoom(), newZoom,
				lengthMillis, WorldWind.ABSOLUTE);
		orbitView.stopAnimations();
		orbitView.stopMovement();
		orbitView.addAnimator(animator);
		orbitView.firePropertyChange(AVKey.VIEW, null, orbitView);
	}

	protected void activateAndSelectElement(final ILayerTreeNode element)
	{
		if (!structureViewer.getControl().isDisposed())
		{
			structureViewer.getControl().getDisplay().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					MPart part = partService.findPart(PART_ID);
					if (part != null)
					{
						partService.activate(part);
						structureViewer.setSelection(new StructuredSelection(element), true);
						tabFolder.setSelection(structureTabItem);
					}
				}
			});
		}
	}

	@Inject
	private void select(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode[] nodes)
	{
		if (nodes == null || structureViewer == null || settingSelection)
		{
			return;
		}
		StructuredSelection selection = new StructuredSelection(nodes);
		structureViewer.setSelection(selection, true);
		for (ILayerTreeNode node : nodes)
		{
			structureViewer.expandToLevel(node, 1);
		}
		tabFolder.setSelection(structureTabItem);
	}

	@Inject
	private void select(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode node)
	{
		if (node == null)
		{
			return;
		}
		select(new ILayerTreeNode[] { node });
	}
}
