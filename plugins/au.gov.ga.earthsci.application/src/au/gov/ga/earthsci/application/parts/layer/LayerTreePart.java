package au.gov.ga.earthsci.application.parts.layer;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.view.orbit.FlyToOrbitViewAnimator;
import gov.nasa.worldwind.view.orbit.OrbitView;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.worldwind.ITreeModel;
import au.gov.ga.earthsci.core.worldwind.WorldWindView;
import au.gov.ga.earthsci.viewers.ControlCheckboxTreeViewer;
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

	@Inject
	private LayerTreeControlProvider controlProvider;

	private ControlCheckboxTreeViewer viewer;
	private Clipboard clipboard;

	@PostConstruct
	public void init(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, Composite parent, EMenuService menuService)
	{
		viewer = new ControlCheckboxTreeViewer(parent, SWT.MULTI);
		context.set(TreeViewer.class, viewer);

		clipboard = new Clipboard(shell.getDisplay());
		context.set(Clipboard.class, clipboard);

		IListProperty childrenProperty = new MultiListProperty(new IListProperty[] { BeanProperties.list("children") }); //$NON-NLS-1$

		ObservableListTreeContentProvider contentProvider =
				new ObservableListTreeContentProvider(childrenProperty.listFactory(), null);
		viewer.setContentProvider(contentProvider);

		//TreeViewerEditor.create(viewer, new SecondClickColumnViewerEditorActivationStrategy(viewer), ColumnViewerEditor.DEFAULT);

		IObservableSet knownElements = contentProvider.getKnownElements();
		final IObservableMap enableds = BeanProperties.value("enabled").observeDetail(knownElements); //$NON-NLS-1$
		final IObservableMap opacities = BeanProperties.value("opacity").observeDetail(knownElements); //$NON-NLS-1$
		final IObservableMap names = BeanProperties.value("name").observeDetail(knownElements); //$NON-NLS-1$
		final IObservableMap labels = BeanProperties.value("label").observeDetail(knownElements); //$NON-NLS-1$
		final IObservableMap anyChildrenEnableds =
				BeanProperties.value("anyChildrenEnabled").observeDetail(knownElements); //$NON-NLS-1$
		final IObservableMap allChildrenEnableds =
				BeanProperties.value("allChildrenEnabled").observeDetail(knownElements); //$NON-NLS-1$

		final IObservableMap[] attributeMap =
				new IObservableMap[] { enableds, opacities, names, labels, anyChildrenEnableds, allChildrenEnableds };

		viewer.setControlProvider(controlProvider);
		viewer.setLabelProvider(new LayerTreeLabelProvider(attributeMap));
		viewer.setCheckStateProvider(new LayerTreeCheckStateProvider());

		viewer.setInput(model.getRootNode());
		viewer.expandAll(); //TODO save expanded state, and restore

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
				selectionService.setSelection(selection.getFirstElement());
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
	}

	@Focus
	private void setFocus()
	{
		viewer.getTree().setFocus();
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
