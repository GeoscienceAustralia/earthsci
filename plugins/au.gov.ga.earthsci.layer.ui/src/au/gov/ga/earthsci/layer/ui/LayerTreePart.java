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
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
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
import au.gov.ga.earthsci.worldwind.common.util.FlyToSectorAnimator;
import au.gov.ga.earthsci.worldwind.common.util.Util;

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

	private CheckboxTreeViewer viewer;
	private LayerTreeLabelProvider labelProvider;
	private Clipboard clipboard;
	private ObservableListTreeSupport<ILayerTreeNode> observableListTreeSupport;

	@PostConstruct
	public void init(Composite parent, EMenuService menuService)
	{
		viewer = new CheckboxTreeViewer(parent, SWT.MULTI);
		viewer.getTree().setBackgroundImage(ImageRegistry.getInstance().get(ImageRegistry.ICON_TRANSPARENT));
		context.set(TreeViewer.class, viewer);

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
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		viewer.setCheckStateProvider(new LayerTreeCheckStateProvider());

		//set the viewer and listener inputs
		viewer.setInput(model.getRootNode());
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

		//setup the selection tracking
		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				List<?> list = selection.toList();
				ILayerTreeNode[] array = list.toArray(new ILayerTreeNode[list.size()]);
				settingSelection = true;
				selectionService.setSelection(array.length == 1 ? array[0] : array);
				settingSelection = false;
			}
		});
		viewer.getTree().addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				ILayerTreeNode firstElement = (ILayerTreeNode) selection.getFirstElement();
				if (firstElement != null)
				{
					selectLayer(firstElement);
				}
			}
		});

		//setup tree expansion/collapse listening
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

		//make tree cells unselectable by selecting outside
		viewer.getTree().addMouseListener(new MouseAdapter()
		{
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

		//setup cell editing
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
		viewer.addDragSupport(ops, new Transfer[] { LocalLayerTransfer.getInstance(), LayerTransfer.getInstance() },
				new LayerTreeDragSourceListener(
						viewer));
		viewer.addDropSupport(ops, new Transfer[] { LocalLayerTransfer.getInstance(), LayerTransfer.getInstance(),
				FileTransfer.getInstance() },
				new LayerTreeDropAdapter(viewer, model, context));

		//add context menu
		menuService.registerContextMenu(viewer.getTree(), "au.gov.ga.earthsci.application.layertree.popupmenu"); //$NON-NLS-1$
	}

	@PreDestroy
	private void packup()
	{
		observableListTreeSupport.dispose();
		context.remove(TreeViewer.class);
		context.remove(Clipboard.class);
		labelProvider.packup();
	}

	@Focus
	private void setFocus()
	{
		viewer.getTree().setFocus();
	}

	private void updateElementLabel(final Object element)
	{
		if (!viewer.getControl().isDisposed())
		{
			viewer.getControl().getDisplay().asyncExec(new Runnable()
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
		viewer.getControl().getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (!viewer.getControl().isDisposed())
				{
					viewer.setExpandedElements(getExpandedNodes());
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

		View view = WorldWindowRegistry.INSTANCE.getActiveView();
		if (view == null)
		{
			return;
		}

		Sector sector = Bounded.Reader.getSector(layer);
		if (sector == null || !(view instanceof OrbitView))
		{
			return;
		}

		OrbitView orbitView = (OrbitView) view;
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

	protected void activateAndSelectElement(final ILayerTreeNode element)
	{
		if (!viewer.getControl().isDisposed())
		{
			viewer.getControl().getDisplay().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					MPart part = partService.findPart(PART_ID);
					if (part != null)
					{
						partService.activate(part);
						viewer.setSelection(new StructuredSelection(element), true);
					}
				}
			});
		}
	}

	@Inject
	private void select(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode[] nodes)
	{
		if (nodes == null || viewer == null || settingSelection)
		{
			return;
		}
		StructuredSelection selection = new StructuredSelection(nodes);
		viewer.setSelection(selection, true);
		for (ILayerTreeNode node : nodes)
		{
			viewer.expandToLevel(node, 1);
		}
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
