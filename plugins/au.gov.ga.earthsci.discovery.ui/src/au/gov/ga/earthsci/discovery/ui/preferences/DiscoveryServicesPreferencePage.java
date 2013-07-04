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
package au.gov.ga.earthsci.discovery.ui.preferences;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import au.gov.ga.earthsci.common.ui.dialogs.StackTraceDialog;
import au.gov.ga.earthsci.common.ui.preferences.PreferencePage;
import au.gov.ga.earthsci.discovery.DiscoveryServiceManager;
import au.gov.ga.earthsci.discovery.IDiscoveryProvider;
import au.gov.ga.earthsci.discovery.IDiscoveryService;
import au.gov.ga.earthsci.discovery.ui.Activator;
import au.gov.ga.earthsci.discovery.ui.ILayoutConstants;
import au.gov.ga.earthsci.discovery.ui.Messages;

/**
 * PreferencePage for editing discovery services.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryServicesPreferencePage extends PreferencePage
{
	private Table table;
	private CheckboxTableViewer viewer;
	private DiscoveryServiceComparator comparator = new DiscoveryServiceComparator();
	private final Set<IDiscoveryService> stagingSet = new HashSet<IDiscoveryService>();
	private final Map<IDiscoveryService, Boolean> originalEnablement = new HashMap<IDiscoveryService, Boolean>();

	private Button addButton;
	private Button editButton;
	private Button removeButton;
	private Button disableButton;
	private Button selectAllButton;
	private Button exportButton;

	public DiscoveryServicesPreferencePage()
	{
		noDefaultAndApplyButton();
		setTitle(Messages.DiscoveryServicesPreferencePage_Title);
		setDescription(Messages.DiscoveryServicesPreferencePage_Description);
	}

	@Override
	protected Control createContents(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(gd);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);

		table =
				new Table(composite, SWT.CHECK | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL
						| SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = convertWidthInCharsToPixels(ILayoutConstants.DEFAULT_TABLE_WIDTH);
		data.heightHint = convertHeightInCharsToPixels(ILayoutConstants.DEFAULT_TABLE_HEIGHT);
		table.setLayoutData(data);

		viewer = new CheckboxTableViewer(table);

		// Key listener for delete
		table.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.keyCode == SWT.DEL)
				{
					removeSelected();
				}
			}
		});
		setTableColumns();

		viewer.setComparator(comparator);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new DiscoveryServiceLabelProvider());

		stagingSet.clear();
		stagingSet.addAll(DiscoveryServiceManager.getServices());
		viewer.setInput(stagingSet);

		viewer.setCellModifier(new ICellModifier()
		{
			@Override
			public boolean canModify(Object element, String property)
			{
				return element instanceof IDiscoveryService;
			}

			@Override
			public Object getValue(Object element, String property)
			{
				String name = ((IDiscoveryService) element).getName();
				return name != null ? name : ""; //$NON-NLS-1$
			}

			@Override
			public void modify(Object element, String property, Object value)
			{
				if (value != null && value.toString().length() >= 0)
				{
					IDiscoveryService service;
					if (element instanceof Item)
					{
						service = (IDiscoveryService) ((Item) element).getData();
					}
					else if (element instanceof IDiscoveryService)
					{
						service = (IDiscoveryService) element;
					}
					else
					{
						return;
					}
					if (!value.toString().equals(service.getName()))
					{
						IDiscoveryService replacement =
								service.getProvider().createService(value.toString(), service.getServiceURL());
						replacement.setEnabled(service.isEnabled());
						stagingSet.remove(service);
						stagingSet.add(replacement);
						viewer.refresh();
					}
				}
			}

		});
		viewer.setColumnProperties(new String[] { "name" }); //$NON-NLS-1$
		viewer.setCellEditors(new CellEditor[] { new TextCellEditor(table) });

		viewer.setCheckStateProvider(new ICheckStateProvider()
		{
			@Override
			public boolean isChecked(Object element)
			{
				return ((IDiscoveryService) element).isEnabled();
			}

			@Override
			public boolean isGrayed(Object element)
			{
				return false;
			}
		});

		viewer.addCheckStateListener(new ICheckStateListener()
		{
			@Override
			public void checkStateChanged(CheckStateChangedEvent event)
			{
				IDiscoveryService service = (IDiscoveryService) event.getElement();
				if (!originalEnablement.containsKey(service))
				{
					originalEnablement.put(service, service.isEnabled());
				}
				service.setEnabled(event.getChecked());
				viewer.refresh();
				validateButtons();
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				validateButtons();
			}
		});

		Composite verticalButtonBar = createVerticalButtonBar(composite);
		data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.verticalAlignment = SWT.TOP;
		data.verticalIndent = 0;
		verticalButtonBar.setLayoutData(data);
		validateButtons();

		return composite;
	}

	private void setTableColumns()
	{
		table.setHeaderVisible(true);

		DiscoveryServiceViewerColumn[] columns = DiscoveryServiceViewerColumn.values();
		for (int i = 0; i < columns.length; i++)
		{
			final DiscoveryServiceViewerColumn column = columns[i];
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(true);
			tc.setText(column.getLabel());
			if (column == DiscoveryServiceViewerColumn.ENABLED)
			{
				tc.setWidth(convertWidthInCharsToPixels(ILayoutConstants.DEFAULT_SMALL_COLUMN_WIDTH));
				tc.setAlignment(SWT.CENTER);
			}
			else if (column == DiscoveryServiceViewerColumn.URL)
			{
				tc.setWidth(convertWidthInCharsToPixels(ILayoutConstants.DEFAULT_PRIMARY_COLUMN_WIDTH));
			}
			else
			{
				tc.setWidth(convertWidthInCharsToPixels(ILayoutConstants.DEFAULT_COLUMN_WIDTH));
			}
			tc.addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetDefaultSelected(SelectionEvent e)
				{
					columnSelected((TableColumn) e.widget, column);
				}

				@Override
				public void widgetSelected(SelectionEvent e)
				{
					columnSelected((TableColumn) e.widget, column);
				}

			});
			// First column only
			if (i == 0)
			{
				table.setSortColumn(tc);
				table.setSortDirection(SWT.UP);
				comparator.setColumn(column);
			}
		}
	}

	private void columnSelected(TableColumn tc, DiscoveryServiceViewerColumn column)
	{
		if (comparator.getColumn() == column)
		{
			comparator.setDescending(!comparator.isDescending());
			table.setSortDirection(comparator.isDescending() ? SWT.DOWN : SWT.UP);
		}
		else
		{
			comparator.setColumn(column);
			comparator.setDescending(false);
			table.setSortColumn(tc);
			table.setSortDirection(SWT.UP);
		}
		viewer.refresh();
	}

	private IDiscoveryService[] getSelected()
	{
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		List<IDiscoveryService> list = new ArrayList<IDiscoveryService>();
		for (Object selected : selection.toList())
		{
			if (selected instanceof IDiscoveryService)
			{
				list.add((IDiscoveryService) selected);
			}
		}
		return list.toArray(new IDiscoveryService[list.size()]);
	}

	private Composite createVerticalButtonBar(Composite parent)
	{
		// Create composite.
		Composite composite = new Composite(parent, SWT.NONE);
		initializeDialogUnits(composite);

		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 5;
		layout.marginHeight = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);

		createVerticalButtons(composite);
		return composite;
	}

	private void createVerticalButtons(Composite parent)
	{
		addButton = createVerticalButton(parent, Messages.DiscoveryServicesPreferencePage_AddButton, false);
		addButton.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				addService();
			}
		});

		editButton = createVerticalButton(parent, Messages.DiscoveryServicesPreferencePage_EditButton, false);
		editButton.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				editSelected();
			}
		});

		removeButton = createVerticalButton(parent, Messages.DiscoveryServicesPreferencePage_RemoveButton, false);
		removeButton.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				removeSelected();
			}
		});

		disableButton = createVerticalButton(parent, Messages.DiscoveryServicesPreferencePage_DisableButton, false);
		disableButton.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				toggleSelected();
			}
		});

		selectAllButton = createVerticalButton(parent, Messages.DiscoveryServicesPreferencePage_SelectAllButton, false);
		selectAllButton.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				selectAll();
			}
		});

		Button button = createVerticalButton(parent, Messages.DiscoveryServicesPreferencePage_ImportButton, false);
		button.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				importServices();
			}
		});

		exportButton = createVerticalButton(parent, Messages.DiscoveryServicesPreferencePage_ExportButton, false);
		exportButton.addListener(SWT.Selection, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				exportServices();
			}
		});
	}

	private Button createVerticalButton(Composite parent, String label, boolean defaultButton)
	{
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);

		GridData data = setVerticalButtonLayoutData(button);
		data.horizontalAlignment = GridData.FILL;

		button.setToolTipText(label);
		if (defaultButton)
		{
			Shell shell = parent.getShell();
			if (shell != null)
			{
				shell.setDefaultButton(button);
			}
		}
		return button;
	}

	private GridData setVerticalButtonLayoutData(Button button)
	{
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(widthHint, minSize.x);
		button.setLayoutData(data);
		return data;
	}

	private void validateButtons()
	{
		IDiscoveryService[] selected = getSelected();
		exportButton.setEnabled(selected.length > 0);
		removeButton.setEnabled(selected.length > 0);
		disableButton.setEnabled(selected.length > 0);
		selectAllButton.setEnabled(table.getItemCount() > 0);
		editButton.setEnabled(selected.length == 1);
		if (selected.length >= 1 && toggleMeansDisable(selected))
		{
			disableButton.setText(Messages.DiscoveryServicesPreferencePage_Disable);
		}
		else
		{
			disableButton.setText(Messages.DiscoveryServicesPreferencePage_Enable);
		}
	}

	private boolean toggleMeansDisable(IDiscoveryService[] elements)
	{
		int count = 0;
		for (int i = 0; i < elements.length; i++)
		{
			if (elements[i].isEnabled())
			{
				count++;
			}
		}
		return count * 2 >= elements.length;
	}

	private void addService()
	{
		EditDiscoveryServiceDialog dialog = new EditDiscoveryServiceDialog(getShell());
		dialog.setTitle(Messages.DiscoveryServicesPreferencePage_AddDialogTitle);
		if (dialog.open() == Window.OK)
		{
			IDiscoveryProvider provider = dialog.getProvider();
			String name = dialog.getName();
			URL url = dialog.getURL();
			if (provider != null && url != null)
			{
				IDiscoveryService service = provider.createService(name, url);
				stagingSet.add(service);

				viewer.refresh();
				validateButtons();
			}
		}
	}

	private void editSelected()
	{
		IDiscoveryService[] selected = getSelected();
		if (selected.length != 1)
		{
			return;
		}

		IDiscoveryService oldService = selected[0];
		EditDiscoveryServiceDialog dialog = new EditDiscoveryServiceDialog(getShell());
		dialog.setTitle(Messages.DiscoveryServicesPreferencePage_EditDialogTitle);
		dialog.setProvider(oldService.getProvider());
		dialog.setName(oldService.getName());
		dialog.setURL(oldService.getServiceURL());
		if (dialog.open() == Window.OK)
		{
			IDiscoveryProvider provider = dialog.getProvider();
			String name = dialog.getName();
			URL url = dialog.getURL();
			if (provider != null && url != null)
			{
				IDiscoveryService newService = provider.createService(name, url);
				newService.setEnabled(oldService.isEnabled());

				stagingSet.remove(oldService);
				stagingSet.add(newService);

				viewer.refresh();
				viewer.setSelection(new StructuredSelection(newService));
				validateButtons();
			}
		}
	}

	private void removeSelected()
	{
		IDiscoveryService[] selected = getSelected();
		for (IDiscoveryService s : selected)
		{
			stagingSet.remove(s);
		}
		viewer.refresh();
		validateButtons();
	}

	private void toggleSelected()
	{
		IDiscoveryService[] selected = getSelected();
		boolean enabled = !toggleMeansDisable(selected);
		for (IDiscoveryService s : selected)
		{
			s.setEnabled(enabled);
		}
		viewer.refresh();
		validateButtons();
	}

	private void selectAll()
	{
		table.setSelection(0, table.getItemCount() - 1);
		validateButtons();
	}

	private void importServices()
	{
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		dialog.setFilterExtensions(new String[] { "*.xml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
		String filename = dialog.open();
		if (filename != null)
		{
			File file = new File(filename);
			try
			{
				List<IDiscoveryService> services = DiscoveryServiceManager.loadServices(file);
				stagingSet.addAll(services);
			}
			catch (Exception e)
			{
				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
				StackTraceDialog.openError(getShell(), Messages.DiscoveryServicesPreferencePage_Error,
						Messages.DiscoveryServicesPreferencePage_ImportError, status);
			}

			viewer.refresh();
			validateButtons();
		}
	}

	private void exportServices()
	{
		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] { "*.xml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
		String filename = dialog.open();
		if (filename != null)
		{
			File file = new File(filename);
			try
			{
				DiscoveryServiceManager.saveServices(stagingSet, file);
			}
			catch (Exception e)
			{
				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
				StackTraceDialog.openError(getShell(), Messages.DiscoveryServicesPreferencePage_Error,
						Messages.DiscoveryServicesPreferencePage_ExportError, status);
			}
		}
	}

	@Override
	public boolean performOk()
	{
		//create a copy to prevent modification during iteration:
		Set<IDiscoveryService> services = new HashSet<IDiscoveryService>(DiscoveryServiceManager.getServices());
		for (IDiscoveryService existingService : services)
		{
			if (!stagingSet.contains(existingService))
			{
				DiscoveryServiceManager.removeService(existingService);
			}
		}
		for (IDiscoveryService newService : stagingSet)
		{
			if (!services.contains(newService))
			{
				DiscoveryServiceManager.addService(newService);
			}
		}
		return super.performOk();
	}

	@Override
	public boolean performCancel()
	{
		//revert enablement changes:
		for (Entry<IDiscoveryService, Boolean> entry : originalEnablement.entrySet())
		{
			entry.getKey().setEnabled(entry.getValue());
		}
		return super.performCancel();
	}
}
