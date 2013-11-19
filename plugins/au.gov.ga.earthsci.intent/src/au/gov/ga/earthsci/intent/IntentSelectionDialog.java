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
package au.gov.ga.earthsci.intent;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog shown to the user if multiple intent filters match an intent, allowing
 * the user to select the desired handler to handle the intent.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class IntentSelectionDialog extends Dialog
{
	private final static int ROW_HEIGHT = 32;

	private final List<IntentFilter> filters;
	private int selectedIndex = -1;
	private TableViewer viewer;
	private Text text;
	private int textHeight;

	protected IntentSelectionDialog(Shell parent, Intent intent, List<IntentFilter> filters)
	{
		super(parent);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		this.filters = filters;
	}

	@Override
	protected Point getInitialSize()
	{
		return getShell().computeSize(SWT.DEFAULT, filters.size() * ROW_HEIGHT + textHeight + 100, true);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);

		getShell().setText("Select action");

		viewer = new TableViewer(composite, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer.getTable().setLayoutData(gridData);

		text = new Text(composite, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
		gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
		GC gc = new GC(text);
		try
		{
			gc.setFont(text.getFont());
			FontMetrics fm = gc.getFontMetrics();
			textHeight = 4 * fm.getHeight();
			gridData.heightHint = textHeight;
		}
		finally
		{
			gc.dispose();
		}
		text.setLayoutData(gridData);

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new IntentSelectionDialogTableLabelProvider());
		viewer.getTable().addListener(SWT.MeasureItem, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				event.height = ROW_HEIGHT;
			}
		});
		viewer.setInput(filters);
		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				updateSelection();
			}
		});
		viewer.getTable().select(0);
		updateSelection();

		return composite;
	}

	private void updateSelection()
	{
		selectedIndex = viewer.getTable().getSelectionIndex();
		IntentFilter filter = (IntentFilter) ((StructuredSelection) viewer.getSelection()).getFirstElement();
		if (filter != null)
		{
			text.setText(filter.getDescription());
		}
	}

	public int getSelectedIndex()
	{
		return selectedIndex;
	}

	public void setSelectedIndex(int selectedIndex)
	{
		this.selectedIndex = selectedIndex;
	}

	public static class Factory
	{
		@Inject
		@Named(IServiceConstants.ACTIVE_SHELL)
		private Shell shell;

		public IntentSelectionDialog create(Intent intent, List<IntentFilter> filters)
		{
			return new IntentSelectionDialog(shell, intent, filters);
		}
	}
}
