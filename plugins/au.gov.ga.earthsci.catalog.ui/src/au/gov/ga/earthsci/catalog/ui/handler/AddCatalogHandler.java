/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.catalog.ui.handler;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.inject.Named;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.application.Activator;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.common.ui.dialogs.StackTraceDialog;
import au.gov.ga.earthsci.intent.AbstractIntentCallback;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentManager;
import au.gov.ga.earthsci.intent.dispatch.Dispatcher;

/**
 * Handles adding catalogs to the catalog tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AddCatalogHandler
{
	@Execute
	public void execute(final IEclipseContext context, @Named(IServiceConstants.ACTIVE_SHELL) final Shell shell)
	{
		BrowseInputDialog dialog =
				new BrowseInputDialog(shell, "Add catalog", "Enter the catalog URL:", "", new IInputValidator()
				{
					@Override
					public String isValid(String newText)
					{
						try
						{
							new URL(newText);
						}
						catch (MalformedURLException e)
						{
							return "URL error: " + e.getLocalizedMessage();
						}
						return null;
					}
				});
		int result = dialog.open();
		if (result != Window.OK)
		{
			return;
		}

		URI uri = null;
		try
		{
			String value = dialog.getValue();
			value = value.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
			URL url = new URL(value);
			uri = url.toURI();
		}
		catch (Exception e)
		{
			IStatus status =
					new Status(IStatus.ERROR, Activator.getBundleName(), e.getLocalizedMessage(), e);
			StackTraceDialog.openError(shell, "Error", "Error opening catalog.",
					status);
			return;
		}

		Intent intent = new Intent();
		intent.setURI(uri);
		intent.setRequiredReturnType(ICatalogTreeNode.class);
		IIntentCallback callback = new AbstractIntentCallback()
		{
			@Override
			public void error(final Exception e, Intent intent)
			{
				shell.getDisplay().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						IStatus status =
								new Status(IStatus.ERROR, Activator.getBundleName(), e.getLocalizedMessage(), e);
						StackTraceDialog.openError(shell, "Error", "Error opening catalog.",
								status);
					}
				});
			}

			@Override
			public void completed(Object result, Intent intent)
			{
				Dispatcher.getInstance().dispatch(result, intent, context);
			}
		};
		IntentManager.getInstance().start(intent, callback, context);
	}

	@CanExecute
	public boolean canExecute()
	{
		return true;
	}
}
