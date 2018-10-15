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
package au.gov.ga.earthsci.application.dataset;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.application.Activator;
import au.gov.ga.earthsci.catalog.dataset.DatasetIntentHandler;
import au.gov.ga.earthsci.common.ui.dialogs.StackTraceDialog;
import au.gov.ga.earthsci.intent.AbstractIntentCallback;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentManager;
import au.gov.ga.earthsci.intent.dispatch.Dispatcher;

/**
 * Dialog which allows a user to choose form a dataset list.
 * 
 * @author Elton Carneiro (elton.carneiro@ga.gov.au)
 */
@SuppressWarnings("nls")
public class DatasetSelectionDialog extends Dialog
{
	private static final String BUNDLE_NAME = "datasets.properties";
	private Properties datasetProperties;
	private String datasetName = null;
	private IEclipseContext context;

	public DatasetSelectionDialog(Shell parentShell, IEclipseContext context)
	{
		super(parentShell);
		try
		{
			this.context = context;
			loadDatasetProperties();
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	private void loadDatasetProperties() throws IOException
	{
		datasetProperties = new Properties();
		InputStream inputStream = getClass().getResourceAsStream(BUNDLE_NAME);
		datasetProperties.load(inputStream);
		inputStream.close();
	}

	private String getDatasetValue(String key, int index)
	{
		String value = datasetProperties.getProperty(key);
		String[] valueArray = value.split(";");
		return valueArray[index];
	}

	private void loadDataset(String datasetUrl)
	{
		try
		{
			//URI uri = new File(datasetUrl).toURI();
			URI uri = new URL(datasetUrl).toURI();
			Intent intent = new Intent();
			intent.setURI(uri);
			//intent.setContentType(contentType)
			intent.setHandler(DatasetIntentHandler.class);
			IIntentCallback callback = new AbstractIntentCallback()
			{
				@Override
				public void error(final Exception e, Intent intent)
				{
					showError(e);
				}

				@Override
				public void completed(Object result, Intent intent)
				{
					Dispatcher.getInstance().dispatch(result, intent, context);
				}
			};
			IntentManager.getInstance().start(intent, callback, context);
		}
		catch (Exception e)
		{
			showError(e);
		}
	}

	@Override
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText(Messages.DatasetSelectionDialog_DatasetSelection);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout());

		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.DatasetSelectionDialog_ChooseDataset + ':');

		createDatasetCombo(composite);

		return composite;
	}

	protected void createDatasetCombo(Composite parent)
	{
		final Combo datasetCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);

		// Add all dataset names into the drop-down list
		String value;
		datasetCombo.add("None", 0);
		for (Object key : datasetProperties.keySet())
		{
			value = getDatasetValue(key.toString(), 0);
			datasetCombo.add(value);
		}

		datasetCombo.select(0);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
		datasetCombo.setLayoutData(data);

		datasetCombo.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent event)
			{
				datasetName = datasetCombo.getText();
			}
		});
	}

	@Override
	protected void okPressed()
	{
		// Obtain and load a dataset
		String datasetUrl = null;
		if (!"None".equals(datasetName))
		{
			String value;
			for (Object key : datasetProperties.keySet())
			{
				value = getDatasetValue(key.toString(), 0);
				if (value.equals(datasetName))
				{
					datasetUrl = getDatasetValue(key.toString(), 1);
					break;
				}
			}
			if (datasetUrl != null)
			{
				loadDataset(datasetUrl);
			}
		}

		super.okPressed();
	}

	private void showError(final Exception e)
	{
		final Shell shell = Display.getDefault().getActiveShell();
		shell.getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				IStatus status = new Status(IStatus.ERROR, Activator.getBundleName(), e.getLocalizedMessage(), e);
				StackTraceDialog.openError(shell, "Error", "Error opening file.", status);
			}
		});
	}

	public static void openDialog(IEclipseContext context)
	{
		Shell activeShell = Display.getDefault().getActiveShell();
		DatasetSelectionDialog datasetSelectionDialog = new DatasetSelectionDialog(activeShell, context);
		datasetSelectionDialog.open();
	}
}
