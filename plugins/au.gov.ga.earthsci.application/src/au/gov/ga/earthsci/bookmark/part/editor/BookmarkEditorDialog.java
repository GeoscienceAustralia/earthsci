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
package au.gov.ga.earthsci.bookmark.part.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.workbench.swt.internal.copy.FilteredTree;
import org.eclipse.e4.ui.workbench.swt.internal.copy.PatternFilter;
import org.eclipse.jface.dialogs.DialogMessageArea;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import au.gov.ga.earthsci.application.ImageRegistry;
import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.bookmark.part.Messages;
import au.gov.ga.earthsci.bookmark.part.editor.IBookmarkEditorMessage.Level;
import au.gov.ga.earthsci.core.util.SWTUtil;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * A dialog that allows the editing of an {@link IBookmark} and it's 
 * properties.
 * <p/>
 * The dialog presents a list view which gives access to available bookmark property editors
 * (as available via the {@link BookmarkPropertyEditorFactory}), and a main
 * editor view which is populated with the fields of the selected editor.
 * <p/>
 * A special "General" editor is provided for editing the top-level bookmark fields
 * (name etc.).
 * <p/>
 * Edits made to fields are only applied when the user presses "OK". If the dialog
 * is dismissed in any other way no edits are applied to the bookmark.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarkEditorDialog extends TrayDialog implements IBookmarkEditorListener
{
	private static final int[] SASH_WEIGHTS = new int[] {30, 70}; 
	private static final Point DEFAULT_MIN_PAGE_SIZE = new Point(400, 400);
	
	/** The current bookmark being edited */
	private final IBookmark bookmark;
	
	/** The tree menu containing available editors */
	private FilteredTree editorFilteredTree;
	
	/** The list of available editors */
	private List<IBookmarkEditor> editors = new ArrayList<IBookmarkEditor>();
	
	/** The current editor */
	private IBookmarkEditor currentEditor;
	
	/** The primary scroller that holds the editor and message area etc. */
	private ScrolledComposite editorScroller;
	
	/** The editor message area, for display of title and errors etc. */
	private DialogMessageArea messageArea;

	/** The 'include in bookmark' question area */
	private Composite editorInclusionArea;
	
	/** The 'include in bookmark' checkbox */
	private Button editorInclusionCheck;
	
	/** The container that holds the controls of the editors */
	private Composite editorContainer;
	
	/** The secondary button bar */
	private Composite editorButtonBar;

	/** The 'fill from current' button */
	private Button fillFromCurrentButton;
	
	/** The 'restore to originals' button */
	private Button resetButton;
	
	/**
	 * Create a new dialog for the editing of the given {@link IBookmark}
	 */
	public BookmarkEditorDialog(IBookmark bookmark, Shell shell)
	{
		super(shell);
		
		setShellStyle(SWT.RESIZE | SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		this.bookmark = bookmark;
		
		GeneralBookmarkEditor generalBookmarkEditor = new GeneralBookmarkEditor();
		generalBookmarkEditor.addListener(this);
		editors.add(generalBookmarkEditor);
		
		for (String type : BookmarkPropertyEditorFactory.getSupportedTypes())
		{
			IBookmarkPropertyEditor editor = BookmarkPropertyEditorFactory.createEditor(type);
			
			editor.addListener(this);
			if (bookmark.hasProperty(type))
			{
				editor.setProperty(bookmark.getProperty(type));
				editor.setIncludedInBookmark(true);
			}
			editors.add(editor);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent)
	{
		getShell().setText(Messages.BookmarkEditorDialog_DialogTitle);
		getShell().setImage(ImageRegistry.getInstance().get(ImageRegistry.ICON_EDIT));
		
		// Remove margins etc. from the parent composite
		final Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout parentLayout = ((GridLayout) composite.getLayout());
		parentLayout.numColumns = 1;
		parentLayout.marginHeight = 0;
		parentLayout.marginWidth = 0;
		parentLayout.verticalSpacing = 0;
		
		// Use a sash form to hold the left (property tree) and right (property editor) areas
		SashForm form = new SashForm(composite, SWT.HORIZONTAL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		form.setLayoutData(gd);
		GridLayout formLayout = new GridLayout();
		formLayout.horizontalSpacing = 0;
		formLayout.marginHeight = 0;
		formLayout.marginWidth = 0;
		form.setLayout(formLayout);
		
		createPropertyTree(form);
		createPropertyEditArea(form);
		
		form.setWeights(SASH_WEIGHTS);
		
		// Separate the button bar from the dialog area
		Label bottomSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		bottomSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		showEditor(editors.get(0));
		
		return composite;
	}
	
	/**
	 * Create the property tree control (left side) used to display available {@link IBookmarkProperty}s
	 * <p/>
	 * Expects the {@code parent} to have a {@link GridLayout}.
	 */
	private Control createPropertyTree(Composite parent)
	{
		PatternFilter filter = new BookmarkPropertyTreeFilter();
		filter.setIncludeLeadingWildcard(true);
		
		editorFilteredTree = new FilteredTree(parent, SWT.FULL_SELECTION, filter, true);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		editorFilteredTree.setLayoutData(gd);
		
		if (editorFilteredTree.getFilterControl() != null) {
			Composite filterComposite = editorFilteredTree.getFilterControl().getParent();
			gd = (GridData) filterComposite.getLayoutData();
			gd.verticalIndent = 2;
			gd.horizontalIndent = 1;
		}
		
		ColumnViewerToolTipSupport.enableFor(editorFilteredTree.getViewer());
		editorFilteredTree.getViewer().setContentProvider(new BookmarkPropertyTreeContentProvider());
		editorFilteredTree.getViewer().setLabelProvider(new BookmarkPropertyTreeLabelProvider());
		editorFilteredTree.getViewer().setComparator(new BookmarkPropertyTreeViewerComparator());
		editorFilteredTree.getViewer().setInput(editors);
		editorFilteredTree.getViewer().setAutoExpandLevel(2);
		
		editorFilteredTree.getViewer().setSelection(new StructuredSelection(editors.get(0)));
		
		editorFilteredTree.getViewer().addPostSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if (event.getSelection().isEmpty())
				{
					return;
				}
				if (!showEditor((IBookmarkEditor)((IStructuredSelection)event.getSelection()).getFirstElement()))
				{
					handleEditorChangeError();
				}
			}

			private void handleEditorChangeError()
			{
				try
				{
					editorFilteredTree.getViewer().removePostSelectionChangedListener(this);
					editorFilteredTree.getViewer().setSelection(new StructuredSelection(currentEditor));
				}
				finally
				{
					editorFilteredTree.getViewer().addPostSelectionChangedListener(this);
				}
			}
		});
		
		return editorFilteredTree;
	}
	
	/**
	 * Create the property edit area (right side) used to contain the {@link IBookmarkProperty} editors.
	 * <p/>
	 * Expects the {@code parent} to have a {@link GridLayout}.
	 */
	protected Control createPropertyEditArea(Composite parent)
	{
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		
		final Label leftSeparator = new Label(container, SWT.VERTICAL | SWT.SEPARATOR);
		leftSeparator.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
		
		editorScroller = new ScrolledComposite(container, SWT.V_SCROLL | SWT.H_SCROLL);
		editorScroller.setShowFocusedControl(true);
		editorScroller.setExpandHorizontal(true);
		editorScroller.setExpandVertical(true);
		editorScroller.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		editorScroller.setMinSize(DEFAULT_MIN_PAGE_SIZE);
		
		final Composite inner = new Composite(editorScroller, SWT.NONE);
		inner.setLayoutData(new GridData(GridData.FILL_BOTH));
		inner.setLayout(new GridLayout());
		
		editorScroller.setContent(inner);
		editorScroller.addControlListener(new ControlAdapter()
		{
			@Override
			public void controlResized(ControlEvent e)
			{
				updateScrollerMinSize();
			}
		});
		
		messageArea = new DialogMessageArea();
		messageArea.createContents(inner);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		messageArea.setTitleLayoutData(gd);
		messageArea.setMessageLayoutData(gd);
		
		Label separator = new Label(inner, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		editorInclusionArea = new Composite(inner, SWT.NONE);
		editorInclusionArea.setLayout(new GridLayout());
		editorInclusionArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		editorInclusionCheck = new Button(editorInclusionArea, SWT.CHECK);
		editorInclusionCheck.setText(Messages.BookmarkEditorDialog_IncludeInBookmarkLabel);
		editorInclusionCheck.setSelection(true);
		editorInclusionCheck.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				updateEditorIncluded(editorInclusionCheck.getSelection());
			}
		});
		
		editorContainer = new Composite(inner, SWT.NONE);
		editorContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		editorContainer.setLayout(new GridLayout());
		
		editorButtonBar = new Composite(inner, SWT.RIGHT_TO_LEFT);
		editorButtonBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		editorButtonBar.setLayout(new RowLayout());
		
		resetButton = new Button(editorButtonBar, SWT.NONE);
		resetButton.setText(Messages.BookmarkEditorDialog_ResetValuesLabel);
		resetButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (currentEditor == null)
				{
					return;
				}
				currentEditor.restoreOriginalValues();
			}
		});
		
		fillFromCurrentButton = new Button(editorButtonBar, SWT.NONE);
		fillFromCurrentButton.setText(Messages.BookmarkEditorDialog_FillFromCurrentLabel);
		fillFromCurrentButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (currentEditor == null || !(currentEditor instanceof IBookmarkPropertyEditor))
				{
					return;
				}
				((IBookmarkPropertyEditor)currentEditor).fillFromCurrent();
			}
		});
		
		return container;
	}
	
	/**
	 * Set the current editor in the editor area, if there are no validation errors present in the 
	 * currently visible editor.
	 *  
	 * @param selectedEditor The editor to show
	 * 
	 * @return <code>true</code> if the editor was successfully changed; <code>false</code> if 
	 * the editor was unable to be changed (e.g. there is a validation error present on 
	 * the current page)
	 */
	private boolean showEditor(IBookmarkEditor selectedEditor)
	{
		if (selectedEditor == null)
		{
			return false;
		}
		if (selectedEditor == currentEditor)
		{
			return true;
		}
		
		// Check validity and hide current editor as appropriate
		if (currentEditor != null)
		{
			updateValidityIndicatorsForCurrent();
			if (!currentEditor.isValid() && currentEditorIncludedInBookmark())
			{
				return false;
			}
			if (currentEditor.getControl() != null)
			{
				currentEditor.getControl().setVisible(false);
				((GridData)currentEditor.getControl().getLayoutData()).exclude = true;
			}
		}
		
		// Change editors
		currentEditor = selectedEditor;

		// Update the common components based on the current editor
		messageArea.showTitle(currentEditor.getName(), null);
		fillFromCurrentButton.setVisible(currentEditor instanceof IBookmarkPropertyEditor);
		
		// Instantiate / show editor control as appropriate
		if (currentEditor.getControl() == null)
		{
			Control control = currentEditor.createControl(editorContainer);
			control.setLayoutData(new GridData(GridData.FILL_BOTH));
		}
		else
		{
			currentEditor.getControl().setVisible(true);
			((GridData)currentEditor.getControl().getLayoutData()).exclude = false;
		}
		
		// Update the "include in bookmark" indicator
		updateEditorIncluded(currentEditorIncludedInBookmark());

		// Refresh the layout and scrollbar activation based on new content
		editorScroller.layout(true, true);
		updateScrollerMinSize();
		
		return true;
	}

	private void updateScrollerMinSize()
	{
		Rectangle r = editorScroller.getClientArea();
		Point computeSize = editorScroller.getContent().computeSize(r.width, SWT.DEFAULT);
		
		editorScroller.setMinSize(Math.max(DEFAULT_MIN_PAGE_SIZE.x, computeSize.x),
								  Math.max(DEFAULT_MIN_PAGE_SIZE.y, computeSize.y));
	}
	
	private boolean currentEditorIncludedInBookmark()
	{
		return editorIncludedInBookmark(currentEditor);
	}
	
	private boolean editorIncludedInBookmark(IBookmarkEditor editor)
	{
		if (editor == null)
		{
			return false;
		}
		if (!(editor instanceof IBookmarkPropertyEditor))
		{
			return true;
		}
		return ((IBookmarkPropertyEditor)editor).isIncludedInBookmark();
	}
	
	private void updateEditorIncluded(boolean included)
	{
		SWTUtil.setEnabled(editorContainer, included);
		SWTUtil.setEnabled(editorButtonBar, included);
		
		if (currentEditor instanceof IBookmarkPropertyEditor)
		{
			editorInclusionArea.setVisible(true);
			((GridData)editorInclusionArea.getLayoutData()).exclude = false;
			
			editorInclusionCheck.setSelection(included);
			
			((IBookmarkPropertyEditor)currentEditor).setIncludedInBookmark(included);
			
			updateValidityIndicatorsForCurrent();
			
			editorFilteredTree.getViewer().refresh(currentEditor, true);
		}
		else
		{
			editorInclusionArea.setVisible(false);
			((GridData)editorInclusionArea.getLayoutData()).exclude = true;
		}
	}
	
	@Override
	protected void okPressed()
	{
		if (!currentEditor.isValid())
		{
			updateValidityIndicatorsForCurrent();
			return;
		}
		
		for (IBookmarkEditor e : editors)
		{
			if (editorIncludedInBookmark(e))
			{
				e.okPressed();
				if (e instanceof IBookmarkPropertyEditor)
				{
					if (!bookmark.hasProperty(((IBookmarkPropertyEditor)e).getProperty().getType()))
					{
						bookmark.addProperty(((IBookmarkPropertyEditor)e).getProperty());
					}
				}
			}
			else
			{
				bookmark.removeProperty(((IBookmarkPropertyEditor)e).getProperty());
			}
		}
		
		setReturnCode(OK);
		close();
	}
	
	@Override
	protected void cancelPressed()
	{
		for (IBookmarkEditor e : editors)
		{
			e.cancelPressed();
		}
		
		setReturnCode(CANCEL);
		close();
	}
	
	@Override
	public void editorInvalid(IBookmarkEditor editor, IBookmarkEditorMessage[] messages)
	{
		updateValidityIndicators(editor, false, messages);
	}
	
	@Override
	public void editorValid(IBookmarkEditor editor)
	{
		updateValidityIndicators(editor, true, null);
	}
	
	private void updateValidityIndicatorsForCurrent()
	{
		if (currentEditor == null)
		{
			return;
		}
		updateValidityIndicators(currentEditor, currentEditor.isValid(), currentEditor.getMessages());
	}

	private void updateValidityIndicators(IBookmarkEditor editor, boolean valid, IBookmarkEditorMessage[] messages)
	{
		if (editor == null)
		{
			return;
		}
		if (valid || !currentEditorIncludedInBookmark())
		{
			messageArea.restoreTitle();
			if (getButton(IDialogConstants.OK_ID) != null)
			{
				getButton(IDialogConstants.OK_ID).setEnabled(true);
			}
		}
		else if (!valid)
		{
			if (messages != null && messages.length > 0)
			{
				messageArea.updateText(messages[0].getMessage(), 
									   messages[0].getLevel() == Level.ERROR ? IMessageProvider.ERROR : messages[0].getLevel() == Level.WARNING ? IMessageProvider.WARNING : IMessageProvider.INFORMATION);
			}
			if (getButton(IDialogConstants.OK_ID) != null)
			{
				getButton(IDialogConstants.OK_ID).setEnabled(false);
			}
		}
	}
	
	/**
	 * A class used to provide the editor fields for top-level bookmark members (name etc.)
	 */
	private class GeneralBookmarkEditor extends AbstractBookmarkEditor
	{
		private final String BOOKMARK_NAME_ID = "general.bookmark.name"; //$NON-NLS-1$
		private final IBookmarkEditorMessage BOOKMARK_NAME_EMPTY_MESSAGE = new BookmarkEditorMessage(Level.ERROR, 
																									"general.bookmark.name.empty",  //$NON-NLS-1$
																									Messages.BookmarkEditorDialog_EmptyBookmarkNameMessage); 
		
		private Composite container;
		private Text bookmarkNameField;
		
		@Override
		public void okPressed()
		{
			bookmark.setName(bookmarkNameField.getText());
		}

		@Override
		public void cancelPressed()
		{
			bookmarkNameField.setText(""); //$NON-NLS-1$
		}

		@Override
		public void restoreOriginalValues()
		{
			bookmarkNameField.setText(bookmark.getName());
		}

		@Override
		public Control createControl(Composite parent)
		{
			container = new Composite(parent, SWT.NONE);
			container.setLayoutData(new GridData(GridData.FILL_BOTH));
			container.setLayout(new GridLayout(2, false));
			
			Label nameFieldLabel = new Label(container, SWT.NONE);
			nameFieldLabel.setText(Messages.BookmarkEditorDialog_BookmarkNameFieldLabel);
			nameFieldLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true));
			
			bookmarkNameField = new Text(container, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
			bookmarkNameField.setText(bookmark.getName());
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.verticalAlignment = SWT.TOP;
			bookmarkNameField.setLayoutData(gd);
			bookmarkNameField.addModifyListener(new ModifyListener()
			{
				@Override
				public void modifyText(ModifyEvent e)
				{
					validate(BOOKMARK_NAME_ID, !Util.isBlank(bookmarkNameField.getText()), BOOKMARK_NAME_EMPTY_MESSAGE);
				}
			});
			
			return container;
		}

		@Override
		public Control getControl()
		{
			return container;
		}
		
		@Override
		public String getName()
		{
			return Messages.BookmarkEditorDialog_GeneralEditorTitle;
		}
		
		@Override
		public String getDescription()
		{
			return Messages.BookmarkEditorDialog_GeneralEditorDescription;
		}
	}
	
	private final class BookmarkPropertyTreeLabelProvider extends CellLabelProvider
	{
		@Override
		public String getToolTipText(Object element)
		{
			return ((IBookmarkEditor)element).getDescription();
		}
		
		@Override
		public int getToolTipDisplayDelayTime(Object object)
		{
			return 1000;
		}
		
		@Override
		public void update(ViewerCell cell)
		{
			IBookmarkEditor editor = (IBookmarkEditor)cell.getElement();
			cell.setText(editor.getName());
			if (editorIncludedInBookmark(editor))
			{
				cell.setFont(JFaceResources.getFontRegistry().getBold("default")); //$NON-NLS-1$
			}
			else
			{
				cell.setFont(JFaceResources.getDefaultFont());
			}
		}
	}
	
	private final class BookmarkPropertyTreeFilter extends PatternFilter
	{
		@Override
		protected boolean isLeafMatch(Viewer viewer, Object element)
		{
			if (element instanceof IBookmarkEditor) 
			{
				return wordMatches(((IBookmarkEditor)element).getName());
			}
			return false;
		}
	}
	
	private final class BookmarkPropertyTreeContentProvider implements ITreeContentProvider
	{
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
			return editors.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement)
		{
			return null;
		}

		@Override
		public Object getParent(Object element)
		{
			return null;
		}

		@Override
		public boolean hasChildren(Object element)
		{
			return false;
		}
	}
	
	private final class BookmarkPropertyTreeViewerComparator extends ViewerComparator
	{
		@Override
		public int compare(Viewer viewer, Object e1, Object e2)
		{
			if (e1 instanceof IBookmarkPropertyEditor && e2 instanceof IBookmarkPropertyEditor)
			{
				return String.CASE_INSENSITIVE_ORDER.compare(((IBookmarkPropertyEditor)e1).getName(), ((IBookmarkPropertyEditor)e2).getName());
			}
			return (e1 instanceof GeneralBookmarkEditor) ? -1 : 1;
		}
	}
}
