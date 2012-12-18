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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.workbench.swt.internal.copy.FilteredTree;
import org.eclipse.e4.ui.workbench.swt.internal.copy.PatternFilter;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import au.gov.ga.earthsci.application.ImageRegistry;
import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;

/**
 * A dialog that allows the editing of an {@link IBookmark} and it's 
 * properties.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarkEditorDialog extends TrayDialog
{
	private static final int[] SASH_WEIGHTS = new int[] {30, 70}; 
	private static final Point DEFAULT_MIN_PAGE_SIZE = new Point(400, 400);
	
	private final IBookmark bookmark;
	
	private Tree propertyTree;
	private FilteredTree propertyFilteredTree;
	private List<Object> root = new ArrayList<Object>();
	
	protected BookmarkEditorDialog(IBookmark bookmark, Shell shell)
	{
		super(shell);
		setShellStyle(SWT.RESIZE | SWT.SHELL_TRIM);
		this.bookmark = bookmark;
	}
	
	@Override
	protected Control createDialogArea(Composite parent)
	{
		getShell().setText("Edit bookmark");
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
		
		return composite;
	}
	
	/**
	 * Create the property tree control (left side) used to display available {@link IBookmarkProperty}s
	 * <p/>
	 * Expects the {@code parent} to have a {@link GridLayout}.
	 */
	private Control createPropertyTree(Composite parent)
	{
		PatternFilter filter = new PatternFilter();
		
		propertyFilteredTree = new FilteredTree(parent, SWT.FULL_SELECTION, filter, true);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.verticalSpan = 1;
		propertyFilteredTree.setLayoutData(gd);
		
		if (propertyFilteredTree.getFilterControl() != null) {
			Composite filterComposite = propertyFilteredTree.getFilterControl().getParent();
			gd = (GridData) filterComposite.getLayoutData();
			gd.verticalIndent = 2;
			gd.horizontalIndent = 1;
		}
		
		propertyTree = propertyFilteredTree.getViewer().getTree();
		
		return propertyFilteredTree;
	}
	
	/**
	 * Create the property edit area (right side) used to contain the {@link IBookmarkProperty} editors.
	 * <p/>
	 * Expects the {@code parent} to have a {@link GridLayout}.
	 */
	protected Control createPropertyEditArea(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		
		Label bottomSeparator = new Label(container, SWT.VERTICAL | SWT.SEPARATOR);
		bottomSeparator.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
		
		ScrolledComposite scrolled = new ScrolledComposite(container, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolled.setShowFocusedControl(true);
		scrolled.setExpandHorizontal(true);
		scrolled.setExpandVertical(true);
		scrolled.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		scrolled.setMinSize(DEFAULT_MIN_PAGE_SIZE);
		
		Composite inner = new Composite(scrolled, SWT.NONE);
		inner.setLayoutData(new GridData(GridData.FILL_BOTH));
		inner.setLayout(new GridLayout());
		
		scrolled.setContent(inner);
		
		Label l = new Label(inner, SWT.NONE);
		l.setText(bookmark.getName());
		
		return container;
	}
	
	@Deprecated
	private void debugControl(Control c)
	{
		c.setBackground(new Color(getShell().getDisplay(), new RGB(255, 0, 0)));
	}
}
