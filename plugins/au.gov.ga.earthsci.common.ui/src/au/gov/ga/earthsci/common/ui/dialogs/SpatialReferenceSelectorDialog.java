/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.common.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.gdal.osr.SpatialReference;

import au.gov.ga.earthsci.common.spatial.SpatialReferences;
import au.gov.ga.earthsci.common.spatial.SpatialReferences.SpatialReferenceSummary;
import au.gov.ga.earthsci.common.util.Util;

/**
 * A dialog that allows a user to select a spatial reference system (SRS) from a
 * list of known reference systems.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public final class SpatialReferenceSelectorDialog extends TitleAreaDialog
{

	private static final Point DIALOG_SIZE = new Point(400, 500);

	private SpatialReferenceSummary selectedSummary = null;

	private Text filterText;
	private TableViewer referencesView;

	public SpatialReferenceSelectorDialog(Shell parentShell)
	{
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);

		newShell.setText(Messages.getString("SpatialReferenceSelectorDialog.DialogTitle")); //$NON-NLS-1$

		newShell.setSize(DIALOG_SIZE);
		newShell.setMinimumSize(DIALOG_SIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		setTitle(Messages.getString("SpatialReferenceSelectorDialog.DialogTitle")); //$NON-NLS-1$
		setMessage(Messages.getString("SpatialReferenceSelectorDialog.DialogDescription")); //$NON-NLS-1$

		Composite container = (Composite) super.createDialogArea(parent);

		addFilterText(container);
		addReferencesTableViewer(container);
		wireReferenceSelectionListener();
		wireFilterTextToViewer();

		return container;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	private void wireReferenceSelectionListener()
	{
		referencesView.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.isEmpty())
				{
					getButton(IDialogConstants.OK_ID).setEnabled(false);
					selectedSummary = null;
				}
				else
				{
					getButton(IDialogConstants.OK_ID).setEnabled(true);
					selectedSummary = (SpatialReferenceSummary) selection.getFirstElement();
				}
			}
		});
	}

	private void wireFilterTextToViewer()
	{
		referencesView.addFilter(new ViewerFilter()
		{

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element)
			{
				// TODO: This filter is quite naive. Might be nicer to some wildcard matching etc.
				String search = Util.removeWhitespace(filterText.getText()).toUpperCase();
				if (Util.isEmpty(search))
				{
					return true;
				}

				SpatialReferenceSummary summary = (SpatialReferenceSummary) element;
				String candidate = Util.removeWhitespace(summary.getEpsg() + summary.getName()).toUpperCase();
				return candidate.contains(search);
			}

		});

		filterText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				referencesView.refresh();
			}
		});
	}

	private void addFilterText(Composite container)
	{
		filterText = new Text(container, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_ERROR);
		filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
	}

	private void addReferencesTableViewer(Composite container)
	{
		Composite tableContainer = new Composite(container, SWT.NONE);
		tableContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableContainer.setLayout(tableColumnLayout);

		referencesView = new TableViewer(tableContainer, SWT.V_SCROLL | SWT.H_SCROLL |
				SWT.BORDER | SWT.SINGLE | SWT.VIRTUAL | SWT.FULL_SELECTION);
		referencesView.setContentProvider(ArrayContentProvider.getInstance());
		referencesView.setInput(SpatialReferences.get());
		referencesView.getTable().setLinesVisible(true);
		referencesView.getTable().setHeaderVisible(true);

		TableViewerColumn epsgColumn = new TableViewerColumn(referencesView, SWT.NONE);
		epsgColumn.getColumn().setText(Messages.getString("SpatialReferenceSelectorDialog.CodeColumnTitle")); //$NON-NLS-1$
		tableColumnLayout.setColumnData(epsgColumn.getColumn(), new ColumnPixelData(80));
		epsgColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return ((SpatialReferenceSummary) element).getEpsg();
			}
		});

		TableViewerColumn nameColumn = new TableViewerColumn(referencesView, SWT.NONE);
		nameColumn.getColumn().setText(Messages.getString("SpatialReferenceSelectorDialog.NameColumnTitle")); //$NON-NLS-1$
		tableColumnLayout.setColumnData(nameColumn.getColumn(), new ColumnWeightData(100));
		nameColumn.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return ((SpatialReferenceSummary) element).getName();
			}
		});
	}

	@Override
	protected void cancelPressed()
	{
		selectedSummary = null;

		super.cancelPressed();
	}

	/**
	 * Get the user-selected {@link SpatialReference}, or <code>null</code> if
	 * none was selected or the dialog was cancelled.
	 */
	public SpatialReference getSelectedSpatialReference()
	{
		return selectedSummary == null ? null : selectedSummary.createReference();
	}

	public SpatialReferenceSummary getSelected()
	{
		return selectedSummary;
	}

}
