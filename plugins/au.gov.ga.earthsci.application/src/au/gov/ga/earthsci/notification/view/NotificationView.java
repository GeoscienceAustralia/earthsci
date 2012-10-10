package au.gov.ga.earthsci.notification.view;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.workbench.swt.internal.copy.FilteredTree;
import org.eclipse.e4.ui.workbench.swt.internal.copy.PatternFilter;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import au.gov.ga.earthsci.notification.INotification;
import au.gov.ga.earthsci.notification.NotificationCategory;
import au.gov.ga.earthsci.notification.NotificationLevel;

/**
 * A view that displays a filtered and/or grouped table of historic {@link INotification}s
 * <p/>
 * The model for this view is maintained by an injected {@link NotificationViewReceiver} instance, which
 * can be changed at any time using the {@link #setReceiver(NotificationViewReceiver)} method
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class NotificationView
{
	/**
	 * A simple enumeration of possible groupings for content in the {@link NotificationView}
	 */
	public enum Grouping
	{
		NONE,
		LEVEL,
		CATEGORY
	}
	
	private static ImageRegistry imageRegistry;
	private NotificationViewReceiver receiver;
	
	private Grouping groupBy = Grouping.NONE;
	
	private Tree tree;
	private FilteredTree filteredTree;
	private List<Object> root = new ArrayList<Object>();
	
	private static final int TITLE_COLUMN_INDEX = 0;
	private static final int DESCRIPTION_COLUMN_INDEX = 1;
	private static final int CATEGORY_COLUMN_INDEX = 2;
	private static final int CREATED_COLUMN_INDEX = 3;
	private static final int ACKNOWLEDGED_COLUMN_INDEX = 4;
	
	private TreeColumn titleColumn;
	private TreeColumn descriptionColumn;
	private TreeColumn categoryColumn;
	private TreeColumn createdColumn;
	private TreeColumn acknowledgedColumn;
	
	/**
	 * Refresh the view upon receiving a new (given) notification
	 * <p/>
	 * This is intended to be only executed from the associated receiver.
	 */
	void refresh(INotification n)
	{
		// TODO: Just add the given notification to the correct place in the tree
		reloadNotificationTree();
	}
	
	/**
	 * Initialise this view with the given parent component
	 */
	@Inject
	public void init(Composite parent)
	{
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);
		
		createViewer(parent);
	}

	/**
	 * Set the handler to use when receiving user menu events to control grouping in this view
	 */
	@Inject
	public void setNotificationViewGroupByHandler(NotificationViewGroupByHandler handler)
	{
		handler.setView(this);
	}
	
	/**
	 * Set the receiver that this view should listen to for notifications 
	 */
	@Inject
	public void setReceiver(NotificationViewReceiver receiver)
	{
		this.receiver = receiver;
		this.receiver.setView(this);
		
		// The order of dependency injection is indeterminate here, so init
		// may be called before/after the receiver is set
		if (filteredTree != null)
		{
			reloadNotificationTree();
		}
	}
	
	/**
	 * Set the grouping to use for this view.
	 * <p/>
	 * Note that this will usually cause the view to reload all historic notifications
	 * from the attached receiver.
	 */
	public void setGrouping(Grouping g)
	{
		if (g == groupBy)
		{
			return;
		}
		
		this.groupBy = g;
		reloadNotificationTree();
	}
	
	private void createViewer(Composite parent)
	{
		PatternFilter filter = new PatternFilter() {
			@Override
			protected boolean isLeafMatch(Viewer viewer, Object element) {
				if (element instanceof INotification) {
					INotification n = (INotification) element;
					return wordMatches(n.getLevel().name()) || 
						   wordMatches(n.getTitle()) || 
						   wordMatches(n.getText()) ||
						   wordMatches(n.getCategory().getLabel());
				}
				return false;
			}
		};
		filter.setIncludeLeadingWildcard(true);
		
		filteredTree = new FilteredTree(parent, SWT.FULL_SELECTION, filter, true);
		if (filteredTree.getFilterControl() != null) {
			Composite filterComposite = filteredTree.getFilterControl().getParent(); // FilteredTree new look lays filter Text on additional composite
			GridData gd = (GridData) filterComposite.getLayoutData();
			gd.verticalIndent = 2;
			gd.horizontalIndent = 1;
		}
		filteredTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		filteredTree.setInitialText("Type filter text");
		tree = filteredTree.getViewer().getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		createColumns(tree);
		
		filteredTree.getViewer().setAutoExpandLevel(2);
		filteredTree.getViewer().setContentProvider(new NotificationViewContentProvider(this));
		filteredTree.getViewer().setLabelProvider(new NotificationViewTreeLabelProvider());
		filteredTree.getViewer().setInput(this);
	}

	private void createColumns(Tree tree)
	{
		String[] titles = { NotificationViewMessages.NotificationView_TitleColumnLabel, 
							NotificationViewMessages.NotificationView_DescriptionColumnLabel, 
							NotificationViewMessages.NotificationView_CategoryColumnLabel, 
							NotificationViewMessages.NotificationView_CreateColumnLabel, 
							NotificationViewMessages.NotificationView_AcknowledgedColumnLabel };
		
		int[] widths = {300, 300, 100, 100, 100};

		titleColumn = createTreeColumn(titles[TITLE_COLUMN_INDEX], widths[TITLE_COLUMN_INDEX]);
		descriptionColumn = createTreeColumn(titles[DESCRIPTION_COLUMN_INDEX], widths[DESCRIPTION_COLUMN_INDEX]);
		categoryColumn = createTreeColumn(titles[CATEGORY_COLUMN_INDEX], widths[CATEGORY_COLUMN_INDEX]);
		createdColumn = createTreeColumn(titles[CREATED_COLUMN_INDEX], widths[CREATED_COLUMN_INDEX]);
		acknowledgedColumn = createTreeColumn(titles[ACKNOWLEDGED_COLUMN_INDEX], widths[ACKNOWLEDGED_COLUMN_INDEX]);
	}

	private TreeColumn createTreeColumn(String title, int width)
	{
		TreeColumn c = new TreeColumn(tree, SWT.LEFT);
		c.setText(title);
		c.setWidth(width);
		return c;
	}
	
	/**
	 * Re-build the notification tree from the notification list on the attached receiver
	 * using the current grouping/filtering settings.
	 */
	private void reloadNotificationTree()
	{
		if (receiver == null)
		{
			return;
		}
		
		final IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) {
				monitor.beginTask("Building notification view...", IProgressMonitor.UNKNOWN);
				
				root.clear();
				
				// Build a flat list - easy!
				if (groupBy == Grouping.NONE)
				{
					root.addAll(receiver.getNotifications());
				}
				else if (groupBy == Grouping.LEVEL)
				{
					Map<NotificationLevel, Group> groups = new EnumMap<NotificationLevel, Group>(NotificationLevel.class);
					for (INotification n : receiver.getNotifications())
					{
						Group g = groups.get(n.getLevel());
						if (g == null)
						{
							g = new Group(n.getLevel(), n.getLevel().getLabel());
							groups.put(n.getLevel(), g);
							root.add(g);
						}
						g.children.add(n);
					}
					Collections.sort(root, new Comparator<Object>()
					{
						@Override
						public int compare(Object o1, Object o2)
						{
							return NotificationLevel.SEVERITY_DESCENDING.compare((NotificationLevel)((Group)o1).grouping, 
																				 (NotificationLevel)((Group)o2).grouping);
						}
					});
				}
				else if (groupBy == Grouping.CATEGORY)
				{
					Map<NotificationCategory, Group> groups = new HashMap<NotificationCategory, Group>();
					for (INotification n : receiver.getNotifications())
					{
						Group g = groups.get(n.getCategory());
						if (g == null)
						{
							g = new Group(n.getCategory(), n.getCategory().getLabel());
							groups.put(n.getCategory(), g);
							root.add(g);
						}
						g.children.add(n);
					}
					Collections.sort(root, new Comparator<Object>()
					{
						@Override
						public int compare(Object o1, Object o2)
						{
							return String.CASE_INSENSITIVE_ORDER.compare(((Group)o1).label, ((Group)o2).label); 
						}
					});
				}
			}
		};
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run()
			{
				ProgressMonitorDialog pmd = new ProgressMonitorDialog(tree.getShell());
				try
				{
					pmd.run(true, true, op);
				}
				catch (InvocationTargetException e)
				{ 
					// do nothing
				}
				catch (InterruptedException e)
				{ 
					// do nothing
				}
				finally
				{
					asyncRefresh();
				}
			}
		});
	}
	
	/**
	 * Refresh the tree view on the appropriate display thread
	 */
	private void asyncRefresh() {
		if (tree.isDisposed())
		{
			return;
		}
		
		Display display = tree.getDisplay();
		if (display == null)
		{
			return;
		}
		
		display.asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (!tree.isDisposed())
				{
					TreeViewer viewer = filteredTree.getViewer();
					viewer.refresh();
					viewer.expandToLevel(2);
				}
			}
		});
	}
	
	/**
	 * @return the imageRegistry
	 */
	public static ImageRegistry getImageRegistry()
	{
		if (imageRegistry == null)
		{
			imageRegistry = new ImageRegistry();
			imageRegistry.put(NotificationLevel.INFORMATION.name(), ImageDescriptor.createFromURL(NotificationView.class.getResource("/icons/information.gif"))); //$NON-NLS-1$
			imageRegistry.put(NotificationLevel.WARNING.name(), ImageDescriptor.createFromURL(NotificationView.class.getResource("/icons/warning.gif"))); //$NON-NLS-1$
			imageRegistry.put(NotificationLevel.ERROR.name(), ImageDescriptor.createFromURL(NotificationView.class.getResource("/icons/error.gif"))); //$NON-NLS-1$
		}
		return imageRegistry;
	}

	/**
	 * A simple container class that represents a group of notifications
	 * <p/>
	 * Used to construct the simple 2-level tree used for this view
	 */
	private static class Group
	{
		private String label;
		private Object grouping;
		
		private List<INotification> children = new ArrayList<INotification>();
		
		public Group(Object grouping, String label)
		{
			this.label = label;
			this.grouping = grouping;
		}
	}
	
	/**
	 * A label provider that renders appropriate labels for the notification tree
	 */
	private static class NotificationViewTreeLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss"); //$NON-NLS-1$
		
		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			if (element instanceof INotification && columnIndex == TITLE_COLUMN_INDEX) 
			{
				return getImageRegistry().get(((INotification)element).getLevel().name());
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex)
		{
			if (element instanceof INotification)
			{
				INotification n = (INotification)element;
				switch (columnIndex)
				{
					case TITLE_COLUMN_INDEX: return n.getTitle();
					case DESCRIPTION_COLUMN_INDEX: return n.getText();
					case CATEGORY_COLUMN_INDEX: return n.getCategory().getLabel();
					case CREATED_COLUMN_INDEX: return sdf.format(n.getCreationTimestamp());
					case ACKNOWLEDGED_COLUMN_INDEX: return n.isAcknowledged() ? sdf.format(n.getAcknowledgementTimestamp()) : ""; //$NON-NLS-1$
				}
			}
			if (element instanceof Group && columnIndex == 0)
			{
				return ((Group)element).label;
			}
			return ""; //$NON-NLS-1$
		}
	}
	
	private static class NotificationViewContentProvider implements ITreeContentProvider
	{
		private final NotificationView view;
		
		public NotificationViewContentProvider(NotificationView view)
		{
			this.view = view;
		}
		
		@Override
		public void dispose()
		{
			// DO nothing
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
			// Do nothing
		}

		@Override
		public Object[] getElements(Object inputElement)
		{
			return view.root.toArray();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object[] getChildren(Object parentElement)
		{
			if (parentElement instanceof Group)
			{
				return ((Group)parentElement).children.toArray();
			}
			else if (parentElement instanceof List)
			{
				return ((List<Object>)parentElement).toArray();
			}
			return null;
		}

		@Override
		public Object getParent(Object element)
		{
			if (element instanceof List)
			{
				return null;
			}
			if (element instanceof Group)
			{
				return view.root;
			}
			if (element instanceof INotification && view.groupBy == Grouping.NONE)
			{
				return view.root;
			}
			
			INotification n = (INotification) element;
			for (Object o : view.root)
			{
				if (view.groupBy == Grouping.LEVEL && ((Group)o).grouping.equals(n.getLevel()))
				{
					return o;
				}
				if (view.groupBy == Grouping.CATEGORY && ((Group)o).grouping.equals(n.getCategory()))
				{
					return o;
				}
			}
			
			return null;
		}

		@Override
		public boolean hasChildren(Object element)
		{
			return !(element instanceof INotification);
		}
		
	}
}
