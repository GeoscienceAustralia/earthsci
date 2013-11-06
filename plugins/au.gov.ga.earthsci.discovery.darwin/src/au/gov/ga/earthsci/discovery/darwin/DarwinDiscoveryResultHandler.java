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

import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.common.ui.dialogs.StackTraceDialog;
import au.gov.ga.earthsci.discovery.IDiscoveryResult;
import au.gov.ga.earthsci.discovery.IDiscoveryResultHandler;
import au.gov.ga.earthsci.intent.AbstractIntentCallback;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentManager;
import au.gov.ga.earthsci.intent.dispatch.Dispatcher;

/**
 * {@link IDiscoveryResultHandler} implementation for DARWIN.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DarwinDiscoveryResultHandler implements IDiscoveryResultHandler
{
	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	private IEclipseContext context;

	@Override
	public void open(IDiscoveryResult r)
	{
		if (!(r instanceof DarwinDiscoveryResult))
		{
			return;
		}

		DarwinDiscoveryResult result = (DarwinDiscoveryResult) r;
		List<DarwinDiscoveryResultURL> urls = result.getUrls();

		if (urls == null || urls.isEmpty())
		{
			IStatus status =
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Search result contains no referenced URLs.");
			ErrorDialog.openError(shell, "Error", null, status);
			return;
		}

		DarwinDiscoveryResultURL url = null;
		if (urls.size() == 1)
		{
			url = urls.get(0);
		}
		else
		{
			DarwinURLSelectionDialog dialog = new DarwinURLSelectionDialog(shell, urls);
			dialog.setTitle("Select URL to open");
			if (dialog.open() != Window.OK)
			{
				return;
			}
			url = dialog.getSelected();
		}

		if (url == null)
		{
			return;
		}

		URI uri = null;
		try
		{
			uri = url.getUrlWithRequiredParameters().toURI();
		}
		catch (Exception e)
		{
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
			StackTraceDialog.openError(shell, "Error", null, status);
			return;
		}

		Intent intent = new Intent();
		intent.setURI(uri);
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
						IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
						StackTraceDialog.openError(shell, "Error", "Error opening selected URL.", status);
					}
				});
			}

			@Override
			public void completed(Object result, Intent intent)
			{
				if (result != null)
				{
					Dispatcher.getInstance().dispatch(result, intent, context);
				}
			}
		};
		IntentManager.getInstance().start(intent, callback, context);
	}
}
