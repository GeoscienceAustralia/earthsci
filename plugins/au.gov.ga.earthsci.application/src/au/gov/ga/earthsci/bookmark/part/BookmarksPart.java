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
package au.gov.ga.earthsci.bookmark.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.application.ImageRegistry;
import au.gov.ga.earthsci.bookmark.model.Bookmarks;
import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmarkList;
import au.gov.ga.earthsci.bookmark.part.handlers.CopyToListHandler;
import au.gov.ga.earthsci.bookmark.part.handlers.MoveToListHandler;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * A part used to display current bookmarks, and to allow the user to interact
 * with them.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarksPart
{
	private static final String POPUP_MENU_ID = "au.gov.ga.earthsci.application.bookmarks.popupmenu"; //$NON-NLS-1$
	private static final String MOVE_TO_MENU_ID = "au.gov.ga.earthsci.application.bookmarks.movetomenu"; //$NON-NLS-1$
	private static final String COPY_TO_MENU_ID = "au.gov.ga.earthsci.application.bookmarks.copytomenu"; //$NON-NLS-1$

	@Inject
	private Bookmarks bookmarks;

	@Inject
	private ESelectionService selectionService;

	@Inject
	private EModelService modelService;

	@Inject
	private EMenuService menuService;

	@Inject
	private MApplication application;

	@Inject
	private IEclipseContext context;

	@Inject
	private IBookmarksController controller;

	private TableViewer bookmarkListTableViewer;
	private ComboViewer bookmarkListsComboViewer;
	private MPopupMenu popupMenu;
	private MMenu copyToMenu;
	private MMenu moveToMenu;

	@PostConstruct
	public void init(final Composite parent, final MPart part)
	{
		controller.setView(this);

		parent.setLayout(new GridLayout(1, true));

		initCombo(parent);
		initList(parent);

		context.set(TableViewer.class, bookmarkListTableViewer);

		setupClipboardDnD();
		setupBookmarkListInput();
		setupPopupMenus();
	}


	/**
	 * Highlight the given bookmark in the part
	 * 
	 * @param bookmark
	 *            The bookmark to highlight. If <code>null</code>, clears any
	 *            highlighting
	 */
	public void highlight(final IBookmark bookmark)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				bookmarkListTableViewer.setSelection(bookmark == null ? null : new StructuredSelection(bookmark));
			}
		});
	}

	public void refreshDropdown()
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (controller.getCurrentList() == getSelectedBookmarkList())
				{
					return;
				}
				bookmarkListsComboViewer.setSelection(new StructuredSelection(controller.getCurrentList()), true);
			}
		});
	}

	private void setupClipboardDnD()
	{
		bookmarkListTableViewer.addDropSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { BookmarkTransfer
				.getInstance() }, new BookmarksDropAdapter(bookmarkListTableViewer, controller));

		bookmarkListTableViewer.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { BookmarkTransfer
				.getInstance() }, new BookmarksDragSourceListener(bookmarkListTableViewer));

		Clipboard clipboard = new Clipboard(Display.getCurrent());
		context.set(Clipboard.class, clipboard);
	}

	private void setupBookmarkListInput()
	{
		bookmarkListTableViewer.setInput(BeanProperties.list("bookmarks").observe(getSelectedBookmarkList())); //$NON-NLS-1$
		bookmarkListsComboViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				bookmarkListTableViewer.setInput(BeanProperties.list("bookmarks").observe(getSelectedBookmarkList())); //$NON-NLS-1$
			}
		});
	}

	private void initCombo(Composite parent)
	{
		bookmarkListsComboViewer = new ComboViewer(parent, SWT.READ_ONLY | SWT.DROP_DOWN);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = false;
		bookmarkListsComboViewer.getCombo().setLayoutData(gd);

		bookmarkListsComboViewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (!(element instanceof IBookmarkList))
				{
					return super.getText(element);
				}
				return ((IBookmarkList) element).getName();
			}
		});

		// Trigger a label refresh if a list name changes etc.
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		IObservableMap nameMap = BeanProperties.value("name").observeDetail(contentProvider.getKnownElements()); //$NON-NLS-1$
		nameMap.addMapChangeListener(new IMapChangeListener()
		{
			@Override
			public void handleMapChange(MapChangeEvent event)
			{
				for (Object key : event.diff.getChangedKeys())
				{
					bookmarkListsComboViewer.refresh(key, true);
				}
			}
		});

		bookmarkListsComboViewer.setContentProvider(contentProvider);
		bookmarkListsComboViewer.setInput(BeanProperties.list("lists").observe(bookmarks)); //$NON-NLS-1$
		bookmarkListsComboViewer.setSelection(new StructuredSelection(bookmarks.getDefaultList()));

		bookmarkListsComboViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				controller.setCurrentList(getSelectedBookmarkList());
			}
		});
	}

	private void initList(Composite parent)
	{
		Composite tableHolder = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		tableHolder.setLayoutData(gd);

		TableColumnLayout layout = new TableColumnLayout();
		tableHolder.setLayout(layout);

		bookmarkListTableViewer =
				new TableViewer(tableHolder, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		bookmarkListTableViewer.getTable().setHeaderVisible(false);
		bookmarkListTableViewer.getTable().setLinesVisible(false);

		bookmarkListTableViewer.getTable().setLayoutData(gd);

		ObservableListContentProvider contentProvider = new ObservableListContentProvider();
		bookmarkListTableViewer.setContentProvider(contentProvider);

		IObservableMap labelMap = BeanProperties.value("name").observeDetail(contentProvider.getKnownElements()); //$NON-NLS-1$

		TableViewerColumn column = new TableViewerColumn(bookmarkListTableViewer, SWT.LEFT);
		column.setEditingSupport(new BookmarkNameEditingSupport(bookmarkListTableViewer));
		column.setLabelProvider(new ObservableMapCellLabelProvider(labelMap)
		{
			@Override
			public void update(ViewerCell cell)
			{
				super.update(cell);
				cell.setText(" " + ((IBookmark) cell.getElement()).getName()); //$NON-NLS-1$
				cell.setImage(ImageRegistry.getInstance().get(ImageRegistry.ICON_BOOKMARKS));
			}
		});
		ColumnLayoutData cld = new ColumnWeightData(12);
		layout.setColumnData(column.getColumn(), cld);

		// Allow edit (rename) only via programmatic access (rename command) 
		ColumnViewerEditorActivationStrategy activationStrategy =
				new ColumnViewerEditorActivationStrategy(bookmarkListTableViewer)
				{
					@Override
					protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event)
					{
						return event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
					}
				};
		TableViewerEditor.create(bookmarkListTableViewer, activationStrategy, ColumnViewerEditor.KEYBOARD_ACTIVATION);

		// Populate the current selection with the actual bookmark items
		bookmarkListTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection selection = (IStructuredSelection) bookmarkListTableViewer.getSelection();
				List<?> list = selection.toList();
				selectionService.setSelection(list.toArray(new IBookmark[list.size()]));
			}
		});

		// Apply the bookmark on double click
		bookmarkListTableViewer.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				controller.apply((IBookmark) ((IStructuredSelection) event.getSelection()).getFirstElement());
			}
		});

		// Deselect when clicking outside a valid row
		bookmarkListTableViewer.getTable().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseUp(MouseEvent e)
			{
				if (bookmarkListTableViewer.getTable().getItem(new Point(e.x, e.y)) == null)
				{
					bookmarkListTableViewer.setSelection(null);
				}
			}
		});

	}

	private void setupPopupMenus()
	{
		popupMenu = menuService.registerContextMenu(bookmarkListTableViewer.getTable(), POPUP_MENU_ID);

		copyToMenu = (MMenu) modelService.find(COPY_TO_MENU_ID, popupMenu);
		moveToMenu = (MMenu) modelService.find(MOVE_TO_MENU_ID, popupMenu);

		updateCopyToMenu();
		updateMoveToMenu();

		bookmarks.addPropertyChangeListener("lists", new PropertyChangeListener() //$NON-NLS-1$
				{
					@Override
					public void propertyChange(PropertyChangeEvent evt)
					{
						updateCopyToMenu();
						updateMoveToMenu();
					}
				});
	}

	private void updateCopyToMenu()
	{
		updateMenu(copyToMenu, CopyToListHandler.COMMAND_ID, CopyToListHandler.LIST_PARAMETER_ID);
	}

	private void updateMoveToMenu()
	{
		updateMenu(moveToMenu, MoveToListHandler.COMMAND_ID, MoveToListHandler.LIST_PARAMETER_ID);
	}

	@SuppressWarnings("unchecked")
	private void updateMenu(MMenu menu, String commandId, String paramId)
	{
		// Remove existing menu items
		for (MMenuElement child : menu.getChildren())
		{
			child.setToBeRendered(false);
			child.setVisible(false);
		}
		menu.getChildren().clear();

		// Find the appropriate command
		MCommand command = null;
		for (MCommand c : application.getCommands())
		{
			if (commandId.equals(c.getElementId()))
			{
				command = c;
				break;
			}
		}

		// Add a menu item for each available list
		for (IBookmarkList b : bookmarks.getLists())
		{
			MHandledMenuItem menuItem = MMenuFactory.INSTANCE.createHandledMenuItem();
			menuItem.setLabel(b.getName());
			menuItem.setCommand(command);
			menuItem.setElementId(menu.getElementId() + "." + b.getId()); //$NON-NLS-1$
			menuItem.setType(ItemType.PUSH);
			menuItem.setToBeRendered(true);
			menuItem.setVisible(true);

			MParameter parameter = MCommandsFactory.INSTANCE.createParameter();
			parameter.setName(paramId);
			parameter.setValue(b.getId());

			menuItem.getParameters().add(parameter);

			menu.getChildren().add(menuItem);
		}

		menu.setToBeRendered(true);
		menu.setVisible(true);

		// This is a workaround for Bug 391340 to force the UI to re-sync with the application model
		IRendererFactory rendererFactory = context.get(IRendererFactory.class);
		AbstractPartRenderer renderer = rendererFactory.getRenderer(popupMenu, bookmarkListTableViewer.getTable());
		renderer.processContents((MElementContainer<MUIElement>) (Object) popupMenu);
	}

	/**
	 * @return The currently selected bookmark list from the combo box
	 */
	private IBookmarkList getSelectedBookmarkList()
	{
		return (IBookmarkList) ((StructuredSelection) bookmarkListsComboViewer.getSelection()).getFirstElement();
	}

	/**
	 * A simple {@link EditingSupport} implementation that provides in-place
	 * editing of bookmark names within the list
	 */
	private static class BookmarkNameEditingSupport extends EditingSupport
	{
		private TableViewer viewer;

		public BookmarkNameEditingSupport(TableViewer viewer)
		{
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected CellEditor getCellEditor(Object element)
		{
			return new TextCellEditor(viewer.getTable(), SWT.BORDER);
		}

		@Override
		protected boolean canEdit(Object element)
		{
			return true;
		}

		@Override
		protected Object getValue(Object element)
		{
			return ((IBookmark) element).getName();
		}

		@Override
		protected void setValue(Object element, Object value)
		{
			String stringValue = (String) value;
			if (!Util.isBlank(stringValue))
			{
				((IBookmark) element).setName(stringValue);
			}
			viewer.update(element, null);
		}

	}
}
