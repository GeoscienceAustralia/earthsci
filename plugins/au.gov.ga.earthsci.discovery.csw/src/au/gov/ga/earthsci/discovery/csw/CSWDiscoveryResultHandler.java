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
package au.gov.ga.earthsci.discovery.csw;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentManager;
import au.gov.ga.earthsci.intent.dispatch.Dispatcher;

/**
 * {@link IDiscoveryResultHandler} implementation for CSW.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CSWDiscoveryResultHandler implements IDiscoveryResultHandler
{
	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	private IEclipseContext context;

	@Override
	public void open(IDiscoveryResult r)
	{
		if (!(r instanceof CSWDiscoveryResult))
		{
			return;
		}

		CSWDiscoveryResult result = (CSWDiscoveryResult) r;
		List<URL> urls = result.getReferences();

		if (urls == null || urls.size() == 0)
		{
			IStatus status =
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CSWDiscoveryResultHandler_ErrorNoURLs);
			ErrorDialog.openError(shell, Messages.CSWDiscoveryResultHandler_Error, null, status);
			return;
		}

		CSWURLSelectionDialog dialog = new CSWURLSelectionDialog(shell, urls);
		dialog.setTitle(Messages.CSWDiscoveryResultHandler_URLSelectionDialogTitle);
		if (dialog.open() != Window.OK)
		{
			return;
		}

		URL url = dialog.getFinalUrl();
		if (url == null)
		{
			return;
		}

		URI uri = null;
		try
		{
			uri = url.toURI();
		}
		catch (URISyntaxException e)
		{
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage(), e);
			StackTraceDialog.openError(shell, Messages.CSWDiscoveryResultHandler_Error, null, status);
			return;
		}

		Intent intent = new Intent();
		intent.setURI(uri);
		IIntentCallback callback = new IIntentCallback()
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
						StackTraceDialog.openError(shell, Messages.CSWDiscoveryResultHandler_Error,
								Messages.CSWDiscoveryResultHandler_ErrorOpeningURL, status);
					}
				});
			}

			@Override
			public void completed(Object result, Intent intent)
			{
				Dispatcher.getInstance().dispatch(result, context);
			}

			@Override
			public void canceled(Intent intent)
			{
			}

			@Override
			public void aborted(Intent intent)
			{
			}
		};
		IntentManager.getInstance().start(intent, callback, context);
	}
}
