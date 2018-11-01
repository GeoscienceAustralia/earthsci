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
package au.gov.ga.earthsci.application.catalog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.core.seed.SeedXmlIntentHandler;
import au.gov.ga.earthsci.intent.AbstractIntentCallback;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentManager;
import au.gov.ga.earthsci.intent.dispatch.Dispatcher;
import au.gov.ga.earthsci.layer.worldwind.WorldWindModel;

/**
 * Dialog which allows a user to choose from a catalog list.
 * 
 * @author Elton Carneiro (elton.carneiro@ga.gov.au)
 */
@SuppressWarnings("nls")
public class CatalogSelectionDialog extends Dialog
{
	private static final Logger logger = LoggerFactory.getLogger(CatalogSelectionDialog.class);
	private static final String CATALOG_LIST_URL = "http://data.earthsci.ga.gov.au/catalogs.properties";

	private Properties catalogProperties;
	private String catalogName;
	private Button resetToDefaultLayers;
	private IEclipseContext eclipseContext;
	private WorldWindModel worldWindModel;

	public CatalogSelectionDialog(Shell parentShell, IEclipseContext eclipseContext, WorldWindModel worldWindModel)
	{
		super(parentShell);
		try
		{
			this.eclipseContext = eclipseContext;
			this.worldWindModel = worldWindModel;
			this.loadCatalogProperties();
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	private void loadCatalogProperties() throws IOException
	{
		catalogProperties = new Properties();
		InputStream is = null;
		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(CATALOG_LIST_URL);
			connection = (HttpURLConnection) url.openConnection();
			is = connection.getInputStream();
			catalogProperties.load(is);
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e)
				{
				}
			}
			if (connection != null)
			{
				connection.disconnect();
			}
		}
	}

	private String getCatalogValue(String key, int index)
	{
		String value = catalogProperties.getProperty(key);
		String[] valueArray = value.split(";");
		return valueArray[index];
	}

	private void loadCatalog(String catalogUrl)
	{
		try
		{
			URI uri = null;

			// Try a local file first, then a remote URL
			File file = new File(catalogUrl);
			if (file.isFile())
			{
				uri = file.toURI();
			}
			else
			{
				uri = new URI(catalogUrl);
			}

			Intent intent = new Intent();
			intent.setURI(uri);
			intent.setHandler(SeedXmlIntentHandler.class);

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
					Dispatcher.getInstance().dispatch(result, intent, eclipseContext);
				}
			};
			IntentManager.getInstance().start(intent, callback, eclipseContext);
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
		shell.setText(Messages.CatalogSelectionDialog_CatalogSelection);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginTop = 5;
		gridLayout.marginLeft = 5;
		gridLayout.marginBottom = 0;
		gridLayout.marginRight = 5;
		composite.setLayout(gridLayout);

		GridData data = new GridData();
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);

		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.CatalogSelectionDialog_ChooseCatalog + ':');
		label.setLayoutData(data);

		createCatalogCombo(composite);
		createResetCheckBox(composite);

		return composite;
	}

	protected void createCatalogCombo(Composite parent)
	{

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);

		final Combo catalogCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);

		// Add all catalog names into the drop-down list
		String value;
		catalogCombo.add("None", 0);
		for (Object key : catalogProperties.keySet())
		{
			value = getCatalogValue(key.toString(), 0);
			catalogCombo.add(value);
		}

		catalogCombo.select(0);
		catalogCombo.setLayoutData(data);

		catalogCombo.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent event)
			{
				catalogName = catalogCombo.getText();
			}
		});
	}

	protected void createResetCheckBox(Composite parent)
	{
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 30;
		data.verticalAlignment = GridData.VERTICAL_ALIGN_END;
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);

		resetToDefaultLayers = new Button(parent, SWT.CHECK);
		resetToDefaultLayers.setSelection(false);
		resetToDefaultLayers.setText(Messages.CatalogSelectionDialog_ResetLayers);
		resetToDefaultLayers.setLayoutData(data);
	}

	@Override
	protected void okPressed()
	{

		logger.info("worldWindModel? " + worldWindModel);
		logger.info("Will reset layers to default? " + resetToDefaultLayers.getSelection());

		// Reset to default is checked
		if (resetToDefaultLayers.getSelection())
		{
			worldWindModel.resetToDefaultLayers(eclipseContext);
		}

		// Obtain and load a catalog
		String catalogUrl = null;
		if (!"None".equals(catalogName))
		{
			String value;
			for (Object key : catalogProperties.keySet())
			{
				value = getCatalogValue(key.toString(), 0);
				if (value.equals(catalogName))
				{
					catalogUrl = getCatalogValue(key.toString(), 1);
					break;
				}
			}
			if (catalogUrl != null)
			{
				loadCatalog(catalogUrl);
			}
		}

		super.okPressed();
	}

	private void showError(final Exception e)
	{
		logger.warn("showError - could not load selected catalog, reason: ", e);
	}

	public static void openDialog(IEclipseContext context, WorldWindModel worldWindModel)
	{
		Shell activeShell = Display.getDefault().getActiveShell();
		CatalogSelectionDialog catalogSelectionDialog =
				new CatalogSelectionDialog(activeShell, context, worldWindModel);
		catalogSelectionDialog.open();
	}

}
