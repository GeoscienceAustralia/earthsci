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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import au.gov.ga.earthsci.common.ui.util.TextURLDropAdapter;
import au.gov.ga.earthsci.discovery.DiscoveryProviderRegistry;
import au.gov.ga.earthsci.discovery.IDiscoveryProvider;
import au.gov.ga.earthsci.discovery.IDiscoveryService;
import au.gov.ga.earthsci.discovery.IDiscoveryServiceProperty;
import au.gov.ga.earthsci.discovery.ui.Messages;

/**
 * Dialog used to edit/create discovery services.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EditDiscoveryServiceDialog extends StatusDialog
{
	private IDiscoveryService service = null;

	private Button okButton;
	private Combo typeCombo;
	private Text nameText;
	private Text urlText;
	private List<IDiscoveryProvider> providers;
	private Label[] propertyLabels;
	private Control[] propertyControls;

	public EditDiscoveryServiceDialog(Shell parent)
	{
		super(parent);
	}

	public IDiscoveryService getService()
	{
		return service;
	}

	public void setService(IDiscoveryService service)
	{
		this.service = service;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		validate();
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite comp = new Composite(parent, SWT.NONE);
		initializeDialogUnits(comp);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginTop = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);

		createTypeCombo(comp);
		createNameField(comp);
		createLocationField(comp);
		createProviderPropertyControls(comp, false);

		comp.setLayout(layout);
		GridData data = new GridData();
		comp.setLayoutData(data);

		Dialog.applyDialogFont(comp);
		return comp;
	}

	protected void createTypeCombo(final Composite parent)
	{
		providers = new ArrayList<IDiscoveryProvider>(DiscoveryProviderRegistry.getProviders());
		Collections.sort(providers, new DiscoveryProviderComparator());
		String[] items = new String[providers.size()];
		int selectedIndex = -1;
		for (int i = 0; i < providers.size(); i++)
		{
			IDiscoveryProvider provider = providers.get(i);
			items[i] = provider.getName() != null ? provider.getName() : ""; //$NON-NLS-1$
			if (service != null && service.getProvider() == provider)
			{
				selectedIndex = i;
			}
		}

		Label typeLabel = new Label(parent, SWT.NONE);
		typeLabel.setText(Messages.EditDiscoveryServiceDialog_TypeLabel);
		typeCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		typeCombo.setItems(items);
		typeCombo.select(selectedIndex);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		typeCombo.setLayoutData(data);

		typeCombo.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				createProviderPropertyControls(parent, true);
				validate();
			}
		});
	}

	protected void createNameField(Composite parent)
	{
		Label nameLabel = new Label(parent, SWT.NONE);
		nameLabel.setText(Messages.EditDiscoveryServiceDialog_NameLabel);
		nameText = new Text(parent, SWT.BORDER);
		String name = service == null ? null : service.getName();
		nameText.setText(name != null ? name : ""); //$NON-NLS-1$
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		nameText.setLayoutData(data);

		nameText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				validate();
			}
		});
	}

	protected void createLocationField(Composite parent)
	{
		Label urlLabel = new Label(parent, SWT.NONE);
		urlLabel.setText(Messages.EditDiscoveryServiceDialog_LocationLabel);
		urlText = new Text(parent, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		urlText.setLayoutData(data);
		DropTarget target = new DropTarget(urlText, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		target.setTransfer(new Transfer[] { URLTransfer.getInstance(), FileTransfer.getInstance() });
		target.addDropListener(new TextURLDropAdapter(urlText, true));
		URL url = service == null ? null : service.getServiceURL();
		urlText.setText(url != null ? url.toString() : ""); //$NON-NLS-1$
		urlText.setSelection(0, urlText.getText().length());

		urlText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				validate();
			}
		});
	}

	protected void createProviderPropertyControls(Composite parent, boolean packShell)
	{
		if (propertyControls != null)
		{
			for (Control control : propertyControls)
			{
				control.dispose();
			}
			for (Label label : propertyLabels)
			{
				label.dispose();
			}
		}
		IDiscoveryProvider provider = null;
		int selectedIndex = typeCombo.getSelectionIndex();
		if (selectedIndex >= 0)
		{
			provider = providers.get(selectedIndex);
		}

		@SuppressWarnings("unchecked")
		IDiscoveryServiceProperty<Object>[] properties =
				provider == null ? null : (IDiscoveryServiceProperty<Object>[]) provider.getProperties();
		if (properties == null)
		{
			propertyControls = new Control[0];
			propertyLabels = new Label[0];
		}
		else
		{
			propertyControls = new Control[properties.length];
			propertyLabels = new Label[properties.length];

			for (int i = 0; i < properties.length; i++)
			{
				IDiscoveryServiceProperty<Object> property = properties[i];

				String label = property.getLabel();
				label = label != null ? label + ":" : ""; //$NON-NLS-1$ //$NON-NLS-2$
				propertyLabels[i] = new Label(parent, SWT.NONE);
				propertyLabels[i].setText(label);

				Object value = service == null ? null : property.getValue(service);
				propertyControls[i] = property.createControl(parent, value, new ModifyListener()
				{
					@Override
					public void modifyText(ModifyEvent e)
					{
						validate();
					}
				});
				GridData data = new GridData(GridData.FILL_HORIZONTAL);
				data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
				propertyControls[i].setLayoutData(data);
			}
		}

		parent.pack();
		if (packShell)
		{
			getShell().pack();
		}
	}

	protected void validate()
	{
		service = null;

		IDiscoveryProvider provider = null;
		int selectedIndex = typeCombo.getSelectionIndex();
		if (selectedIndex >= 0)
		{
			provider = providers.get(selectedIndex);
		}

		String name = nameText.getText();

		URL url;
		try
		{
			url = new URL(urlText.getText());
		}
		catch (MalformedURLException e)
		{
			url = null;
		}

		if (provider != null)
		{
			IDiscoveryServiceProperty<?>[] properties = provider.getProperties();

			boolean valid = name != null && url != null;
			if (valid)
			{
				if (properties != null)
				{
					for (int i = 0; i < properties.length && valid; i++)
					{
						IDiscoveryServiceProperty<?> property = properties[i];
						valid &= property.validate(propertyControls[i]);
					}
				}
			}

			if (valid)
			{
				Map<IDiscoveryServiceProperty<?>, Object> propertyValues =
						new HashMap<IDiscoveryServiceProperty<?>, Object>();
				if (properties != null)
				{
					for (int i = 0; i < properties.length && valid; i++)
					{
						IDiscoveryServiceProperty<?> property = properties[i];
						Object value = property.getValue(propertyControls[i]);
						propertyValues.put(property, value);
					}
				}

				service = provider.createService(name, url, propertyValues);
			}
		}

		okButton.setEnabled(service != null);
	}
}
