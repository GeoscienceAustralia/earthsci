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
package au.gov.ga.earthsci.application.handlers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import javax.inject.Named;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.application.Activator;
import au.gov.ga.earthsci.common.ui.dialogs.StackTraceDialog;
import au.gov.ga.earthsci.intent.AbstractIntentCallback;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentManager;
import au.gov.ga.earthsci.intent.dispatch.Dispatcher;

/**
 * Handler used to open files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OpenHandler
{
	@Execute
	public void execute(final IEclipseContext context, @Named(IServiceConstants.ACTIVE_SHELL) final Shell shell)
			throws InvocationTargetException, InterruptedException
	{
		FileDialog dialog = new FileDialog(shell);
		String file = dialog.open();

		if (file == null)
		{
			return;
		}

		URI uri = new File(file).toURI();
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
						IStatus status =
								new Status(IStatus.ERROR, Activator.getBundleName(), e.getLocalizedMessage(), e);
						StackTraceDialog.openError(shell, "Error", "Error opening file.",
								status);
					}
				});
			}

			@Override
			public void completed(Object result, Intent intent)
			{
				Dispatcher.getInstance().dispatch(result, context);
			}
		};
		IntentManager.getInstance().start(intent, callback, context);
	}
}
