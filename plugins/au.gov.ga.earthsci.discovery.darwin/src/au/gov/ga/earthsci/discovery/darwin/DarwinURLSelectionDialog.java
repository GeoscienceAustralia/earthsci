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
package au.gov.ga.earthsci.discovery.darwin;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Dialog used to select a URL to open from a Darwin discovery result.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DarwinURLSelectionDialog extends StatusDialog
{
	private final List<DarwinDiscoveryResultURL> urls;
	private DarwinDiscoveryResultURL selected;
	private Button okButton;
	private TableViewer viewer;
	private CLabel urlLabel;

	public DarwinURLSelectionDialog(Shell parent, List<DarwinDiscoveryResultURL> urls)
	{
		super(parent);
		this.urls = urls;
	}

	public DarwinDiscoveryResultURL getSelected()
	{
		return selected;
	}

	public void setSelected(DarwinDiscoveryResultURL selected)
	{
		this.selected = selected;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		validate();
	}

	@Override
	protected void setShellStyle(int newShellStyle)
	{
		super.setShellStyle(newShellStyle | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridLayout layout = new GridLayout();
		layout.marginTop = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		comp.setLayout(layout);
		initializeDialogUnits(comp);

		viewer = new TableViewer(comp, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer.getTable().setLayoutData(gd);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProvider());
		viewer.setInput(urls);

		DarwinDiscoveryResultURL longestUrl = urlWithLongestName(urls);
		if (longestUrl != null)
		{
			GC gc = new GC(viewer.getTable());
			Point point = gc.textExtent(longestUrl.getName());
			gc.dispose();
			gd.widthHint = point.x;
		}

		//keep the column width in sync with the table width
		final TableColumn resultsColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
		Listener resizeListener = new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				resultsColumn.setWidth(viewer.getTable().getClientArea().width);
			}
		};
		viewer.getControl().addListener(SWT.Resize, resizeListener);
		viewer.getControl().addListener(SWT.Paint, resizeListener);

		if (selected != null)
		{
			viewer.setSelection(new StructuredSelection(selected));
		}
		else if (urls.size() == 1)
		{
			viewer.getTable().select(0);
		}
		else
		{
			//if there is a WMS url, select it by default
			for (DarwinDiscoveryResultURL url : urls)
			{
				if (url.getProtocol().toLowerCase().contains("wms")) //$NON-NLS-1$
				{
					viewer.setSelection(new StructuredSelection(url));
					break;
				}
			}
		}

		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				validate();
			}
		});

		ColumnViewerToolTipSupport.enableFor(viewer);

		urlLabel = new CLabel(comp, SWT.NONE);
		urlLabel.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		Dialog.applyDialogFont(comp);
		return comp;
	}

	private void validate()
	{
		selected = (DarwinDiscoveryResultURL) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		urlLabel.setText(selected == null ? "" : selected.getUrl().toString().replace("&", "&&")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		boolean valid = selected != null;
		okButton.setEnabled(valid);
	}

	private static DarwinDiscoveryResultURL urlWithLongestName(Collection<DarwinDiscoveryResultURL> urls)
	{
		DarwinDiscoveryResultURL longest = null;
		int maxLength = 0;
		for (DarwinDiscoveryResultURL url : urls)
		{
			int length = url.getName().length();
			if (length > maxLength)
			{
				maxLength = length;
				longest = url;
			}
		}
		return longest;
	}

	private static class LabelProvider extends ColumnLabelProvider
	{
		@Override
		public String getText(Object element)
		{
			return ((DarwinDiscoveryResultURL) element).getName();
		}

		@Override
		public Image getImage(Object element)
		{
			return super.getImage(element);
		}

		@Override
		public String getToolTipText(Object element)
		{
			return ((DarwinDiscoveryResultURL) element).getUrl().toString();
		}
	}
}
