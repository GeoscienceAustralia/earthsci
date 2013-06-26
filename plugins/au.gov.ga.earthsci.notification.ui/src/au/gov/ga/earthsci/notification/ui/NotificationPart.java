package au.gov.ga.earthsci.notification.ui;

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
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.swt.internal.copy.FilteredTree;
import org.eclipse.e4.ui.workbench.swt.internal.copy.PatternFilter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import au.gov.ga.earthsci.common.ui.dialogs.StackTraceDialog;
import au.gov.ga.earthsci.notification.INotification;
import au.gov.ga.earthsci.notification.NotificationCategory;
import au.gov.ga.earthsci.notification.NotificationLevel;
import au.gov.ga.earthsci.notification.ui.handlers.GroupByHandler;

/**
 * A view that displays a filtered and/or grouped table of historic
 * {@link INotification}s
 * <p/>
 * The model for this view is maintained by an injected
 * {@link NotificationPartReceiver} instance, which can be changed at any time
 * using the {@link #setReceiver(NotificationPartReceiver)} method
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class NotificationPart
{

	private static final int TITLE_COLUMN_INDEX = 0;
	private static final int DESCRIPTION_COLUMN_INDEX = 1;
	private static final int CATEGORY_COLUMN_INDEX = 2;
	private static final int CREATED_COLUMN_INDEX = 3;
	private static final int ACKNOWLEDGED_COLUMN_INDEX = 4;

	private static final int ASCENDING = 1;
	private static final int DESCENDING = -1;

	private static ImageRegistry imageRegistry;

	/**
	 * A simple enumeration of possible groupings for content in the
	 * {@link NotificationPart}
	 */
	public enum Grouping
	{
		NONE,
		LEVEL,
		CATEGORY
	}

	private Grouping groupBy = Grouping.NONE;

	private NotificationPartReceiver receiver;

	private Tree tree;
	private FilteredTree filteredTree;
	private List<Object> root = new ArrayList<Object>();

	// Table columns
	private int titleOrder = ASCENDING;
	private TreeColumn titleColumn;

	private int descriptionOrder = ASCENDING;
	private TreeColumn descriptionColumn;

	private int categoryOrder = ASCENDING;
	private TreeColumn categoryColumn;

	private int createdOrder = DESCENDING;
	private TreeColumn createdColumn;

	private int acknowledgedOrder = DESCENDING;
	private TreeColumn acknowledgedColumn;

	@Inject
	@Optional
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	/**
	 * Initialise this view with the given parent component
	 */
	@Inject
	public void init(Composite parent, MPart part, NotificationPartReceiver receiver)
	{
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);

		createViewer(parent);
		initialiseCorrectGrouping(part);
		setReceiver(receiver);
	}

	/**
	 * Set the handler to use when receiving user menu events to control
	 * grouping in this view
	 */
	@Inject
	public void setNotificationViewGroupByHandler(GroupByHandler handler)
	{
		handler.setView(this);
	}

	/**
	 * Set the receiver that this view should listen to for notifications
	 */
	public void setReceiver(NotificationPartReceiver receiver)
	{
		this.receiver = receiver;
		this.receiver.setView(this);
		reloadNotificationTree();
	}

	/**
	 * Set the grouping to use for this view.
	 * <p/>
	 * Note that this will usually cause the view to reload all historic
	 * notifications from the attached receiver.
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
		PatternFilter filter = new PatternFilter()
		{
			@Override
			protected boolean isLeafMatch(Viewer viewer, Object element)
			{
				if (element instanceof INotification)
				{
					INotification n = (INotification) element;
					return wordMatches(n.getLevel().name()) || wordMatches(n.getTitle()) || wordMatches(n.getText())
							|| wordMatches(n.getCategory().getLabel());
				}
				return false;
			}
		};
		filter.setIncludeLeadingWildcard(true);

		filteredTree = new FilteredTree(parent, SWT.FULL_SELECTION, filter, true);
		if (filteredTree.getFilterControl() != null)
		{
			Composite filterComposite = filteredTree.getFilterControl().getParent();
			GridData gd = (GridData) filterComposite.getLayoutData();
			gd.verticalIndent = 2;
			gd.horizontalIndent = 1;
		}
		filteredTree.setLayoutData(new GridData(GridData.FILL_BOTH));
		filteredTree.setInitialText(Messages.NotificationView_FilterTextBoxLabel);
		tree = filteredTree.getViewer().getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		createColumns(tree);

		filteredTree.getViewer().setAutoExpandLevel(2);
		filteredTree.getViewer().setContentProvider(new NotificationViewContentProvider(this));
		filteredTree.getViewer().setLabelProvider(new NotificationViewTreeLabelProvider());
		filteredTree.getViewer().setInput(this);

		filteredTree.getViewer().getTree().addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				Object data = e.item.getData();
				if (data instanceof INotification)
				{
					INotification notification = (INotification) data;
					if (notification.getThrowable() != null)
					{
						IStatus status =
								new Status(notification.getLevel().getStatusSeverity(), Activator.PLUGIN_ID,
										notification.getThrowable().getLocalizedMessage(), notification.getThrowable());
						StackTraceDialog.openError(shell, notification.getTitle(), notification.getText(), status);
					}
					else
					{
						IStatus status =
								new Status(notification.getLevel().getStatusSeverity(), Activator.PLUGIN_ID,
										notification.getText());
						ErrorDialog.openError(shell, notification.getTitle(), null, status);
					}
				}
			}
		});
	}

	private void createColumns(Tree tree)
	{
		String[] titles =
		{ Messages.NotificationView_TitleColumnLabel, Messages.NotificationView_DescriptionColumnLabel,
				Messages.NotificationView_CategoryColumnLabel, Messages.NotificationView_CreateColumnLabel,
				Messages.NotificationView_AcknowledgedColumnLabel };

		int[] widths = { 300, 300, 100, 100, 100 };

		titleColumn = createTreeColumn(titles[TITLE_COLUMN_INDEX], widths[TITLE_COLUMN_INDEX]);
		titleColumn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				titleOrder *= -1;
				filteredTree.getViewer().setComparator(new ViewerComparator()
				{
					@Override
					public int compare(Viewer viewer, Object e1, Object e2)
					{
						if (!(e1 instanceof INotification) || !(e2 instanceof INotification))
						{
							return 0;
						}
						return String.CASE_INSENSITIVE_ORDER.compare(((INotification) e1).getTitle(),
								((INotification) e2).getTitle()) * titleOrder;
					}
				});
				setColumnSorting(TITLE_COLUMN_INDEX, titleOrder);
			}
		});

		descriptionColumn = createTreeColumn(titles[DESCRIPTION_COLUMN_INDEX], widths[DESCRIPTION_COLUMN_INDEX]);
		descriptionColumn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				descriptionOrder *= -1;
				filteredTree.getViewer().setComparator(new ViewerComparator()
				{
					@Override
					public int compare(Viewer viewer, Object e1, Object e2)
					{
						if (!(e1 instanceof INotification) || !(e2 instanceof INotification))
						{
							return 0;
						}
						return String.CASE_INSENSITIVE_ORDER.compare(((INotification) e1).getText(),
								((INotification) e2).getText()) * descriptionOrder;
					}
				});
				setColumnSorting(DESCRIPTION_COLUMN_INDEX, descriptionOrder);
			}
		});

		categoryColumn = createTreeColumn(titles[CATEGORY_COLUMN_INDEX], widths[CATEGORY_COLUMN_INDEX]);
		categoryColumn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				categoryOrder *= -1;
				filteredTree.getViewer().setComparator(new ViewerComparator()
				{
					@Override
					public int compare(Viewer viewer, Object e1, Object e2)
					{
						if (!(e1 instanceof INotification) || !(e2 instanceof INotification))
						{
							return 0;
						}
						return String.CASE_INSENSITIVE_ORDER.compare(((INotification) e1).getCategory().getLabel(),
								((INotification) e2).getCategory().getLabel()) * categoryOrder;
					}
				});
				setColumnSorting(CATEGORY_COLUMN_INDEX, categoryOrder);
			}
		});

		createdColumn = createTreeColumn(titles[CREATED_COLUMN_INDEX], widths[CREATED_COLUMN_INDEX]);
		createdColumn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				createdOrder *= -1;
				filteredTree.getViewer().setComparator(new ViewerComparator()
				{
					@Override
					public int compare(Viewer viewer, Object e1, Object e2)
					{
						if (!(e1 instanceof INotification) || !(e2 instanceof INotification))
						{
							return 0;
						}
						return ((INotification) e1).getCreationTimestamp().compareTo(
								((INotification) e2).getCreationTimestamp())
								* createdOrder;
					}
				});
				setColumnSorting(CREATED_COLUMN_INDEX, createdOrder);
			}
		});

		acknowledgedColumn = createTreeColumn(titles[ACKNOWLEDGED_COLUMN_INDEX], widths[ACKNOWLEDGED_COLUMN_INDEX]);
		acknowledgedColumn.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				acknowledgedOrder *= -1;
				filteredTree.getViewer().setComparator(new ViewerComparator()
				{
					@Override
					public int compare(Viewer viewer, Object e1, Object e2)
					{
						if (!(e1 instanceof INotification) || !(e2 instanceof INotification))
						{
							return 0;
						}
						INotification n1 = (INotification) e1;
						INotification n2 = (INotification) e2;
						if (n1.getAcknowledgementTimestamp() == null && n2.getAcknowledgementTimestamp() == null)
						{
							return 0;
						}
						if (n1.getAcknowledgementTimestamp() == null && n2.getAcknowledgementTimestamp() != null)
						{
							return ASCENDING;
						}
						if (n1.getAcknowledgementTimestamp() != null && n2.getAcknowledgementTimestamp() == null)
						{
							return DESCENDING;
						}
						return n1.getAcknowledgementTimestamp().compareTo(n2.getAcknowledgementTimestamp())
								* acknowledgedOrder;
					}
				});
				setColumnSorting(ACKNOWLEDGED_COLUMN_INDEX, acknowledgedOrder);
			}
		});
	}

	private TreeColumn createTreeColumn(String title, int width)
	{
		TreeColumn c = new TreeColumn(tree, SWT.LEFT);
		c.setText(title);
		c.setWidth(width);
		return c;
	}

	private void setColumnSorting(int index, int order)
	{
		tree.setSortColumn(tree.getColumn(index));
		tree.setSortDirection(order == ASCENDING ? SWT.UP : SWT.DOWN);
	}

	/**
	 * Re-build the notification tree from the notification list on the attached
	 * receiver using the current grouping/filtering settings.
	 */
	private void reloadNotificationTree()
	{
		if (receiver == null)
		{
			return;
		}

		final IRunnableWithProgress op = new IRunnableWithProgress()
		{
			@Override
			public void run(IProgressMonitor monitor)
			{
				monitor.beginTask(Messages.NotificationView_ProgressDescription, IProgressMonitor.UNKNOWN);

				root.clear();

				// Build a flat list - easy!
				if (groupBy == Grouping.NONE)
				{
					root.addAll(receiver.getNotifications());
				}
				else if (groupBy == Grouping.LEVEL)
				{
					Map<NotificationLevel, Group> groups =
							new EnumMap<NotificationLevel, Group>(NotificationLevel.class);
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
					Collections.sort(root, levelGroupComparator);
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
					Collections.sort(root, categoryGroupComparator);
				}
			}
		};
		Display.getDefault().asyncExec(new Runnable()
		{
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
	 * Refresh the view upon receiving a new (given) notification
	 * <p/>
	 * This is intended to be only executed from the associated receiver.
	 */
	void refresh(INotification n)
	{
		// Insert the new notification into the correct place in the tree model
		switch (groupBy)
		{
		case NONE:
		{
			root.add(n);
			break;
		}
		case LEVEL:
		{
			Group targetGroup = null;
			for (int i = 0; i < root.size(); i++)
			{
				Group g = (Group) root.get(i);
				if (g.grouping.equals(n.getLevel()))
				{
					targetGroup = g;
					break;
				}
			}
			if (targetGroup == null)
			{
				targetGroup = new Group(n.getLevel(), n.getLevel().getLabel());
				root.add(targetGroup);
				Collections.sort(root, levelGroupComparator);
			}
			targetGroup.children.add(n);
			break;
		}
		case CATEGORY:
		{
			Group targetGroup = null;
			for (int i = 0; i < root.size(); i++)
			{
				Group g = (Group) root.get(i);
				if (g.grouping.equals(n.getCategory()))
				{
					targetGroup = g;
					break;
				}
			}
			if (targetGroup == null)
			{
				targetGroup = new Group(n.getCategory(), n.getCategory().getLabel());
				root.add(targetGroup);
				Collections.sort(root, categoryGroupComparator);
			}
			targetGroup.children.add(n);
			break;
		}
		}

		// Refresh the tree
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				filteredTree.getViewer().refresh();
			}
		});
	}


	/**
	 * Refresh the tree view on the appropriate display thread
	 */
	private void asyncRefresh()
	{
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
	 * Initialise the grouping as per the persisted menu selection on startup
	 */
	private void initialiseCorrectGrouping(MPart part)
	{
		// Find the active grouping
		String selectedId = null;
		for (MMenu menu : part.getMenus())
		{
			if (menu.getTags().contains("ViewMenu")) //$NON-NLS-1$
			{
				for (MMenuElement element : menu.getChildren())
				{
					if (!element.getElementId().equals("au.gov.ga.earthsci.notification.part.NotificationPart.group")) //$NON-NLS-1$
					{
						continue;
					}
					for (MMenuElement child : ((MMenu) element).getChildren())
					{
						if (((MMenuItem) child).isSelected())
						{
							selectedId = child.getElementId();
						}
					}
				}
			}
		}

		Grouping grouping = GroupByHandler.getGroupingForMenuItemId(selectedId);
		setGrouping(grouping);
	}

	/**
	 * @return the imageRegistry
	 */
	public static ImageRegistry getImageRegistry()
	{
		if (imageRegistry == null)
		{
			ImageRegistry imageRegistry = new ImageRegistry();
			imageRegistry.put(NotificationLevel.INFORMATION.name(),
					ImageDescriptor.createFromURL(NotificationPart.class.getResource("/icons/info.gif"))); //$NON-NLS-1$
			imageRegistry.put(NotificationLevel.WARNING.name(),
					ImageDescriptor.createFromURL(NotificationPart.class.getResource("/icons/warning.gif"))); //$NON-NLS-1$
			imageRegistry.put(NotificationLevel.ERROR.name(),
					ImageDescriptor.createFromURL(NotificationPart.class.getResource("/icons/error.gif"))); //$NON-NLS-1$

			NotificationPart.imageRegistry = imageRegistry;
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

	/** A comparator for ordering level groups in the view */
	private static final Comparator<Object> levelGroupComparator = new Comparator<Object>()
	{
		@Override
		public int compare(Object o1, Object o2)
		{
			return NotificationLevel.SEVERITY_DESCENDING.compare((NotificationLevel) ((Group) o1).grouping,
					(NotificationLevel) ((Group) o2).grouping);
		}
	};

	/** A comparator for ordering category groups in the view */
	private static final Comparator<Object> categoryGroupComparator = new Comparator<Object>()
	{
		@Override
		public int compare(Object o1, Object o2)
		{
			return String.CASE_INSENSITIVE_ORDER.compare(((Group) o1).label, ((Group) o2).label);
		}
	};

	/**
	 * A label provider that renders appropriate labels for the notification
	 * tree
	 */
	private static class NotificationViewTreeLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		private static final String DATE_FORMAT = "dd MMM yyyy HH:mm:ss"; //$NON-NLS-1$

		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			if (element instanceof INotification && columnIndex == TITLE_COLUMN_INDEX)
			{
				return getImageRegistry().get(((INotification) element).getLevel().name());
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex)
		{
			if (element instanceof INotification)
			{
				INotification n = (INotification) element;
				switch (columnIndex)
				{
				case TITLE_COLUMN_INDEX:
					return n.getTitle();
				case DESCRIPTION_COLUMN_INDEX:
					return n.getText();
				case CATEGORY_COLUMN_INDEX:
					return n.getCategory().getLabel();
				case CREATED_COLUMN_INDEX:
					return new SimpleDateFormat(DATE_FORMAT).format(n.getCreationTimestamp());
				case ACKNOWLEDGED_COLUMN_INDEX:
					return n.isAcknowledged() ? new SimpleDateFormat(DATE_FORMAT).format(n
							.getAcknowledgementTimestamp()) : ""; //$NON-NLS-1$
				}
			}
			if (element instanceof Group && columnIndex == 0)
			{
				return ((Group) element).label;
			}
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * An {@link ITreeContentProvider} that understands grouping within the
	 * notification view
	 */
	private static class NotificationViewContentProvider implements ITreeContentProvider
	{
		private final NotificationPart view;

		public NotificationViewContentProvider(NotificationPart view)
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
				return ((Group) parentElement).children.toArray();
			}
			else if (parentElement instanceof List)
			{
				return ((List<Object>) parentElement).toArray();
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
				if (view.groupBy == Grouping.LEVEL && ((Group) o).grouping.equals(n.getLevel()))
				{
					return o;
				}
				if (view.groupBy == Grouping.CATEGORY && ((Group) o).grouping.equals(n.getCategory()))
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
