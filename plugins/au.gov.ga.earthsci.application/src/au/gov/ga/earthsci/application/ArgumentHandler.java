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
package au.gov.ga.earthsci.application;

import jargs.gnu.LenientCmdLineParser;
import jargs.gnu.LenientCmdLineParser.IllegalOptionValueException;

import java.net.URL;
import java.util.Vector;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.equinox.internal.app.CommandLineArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.intent.AbstractIntentCallback;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentManager;
import au.gov.ga.earthsci.intent.dispatch.Dispatcher;

/**
 * Handles the command line arguments passed to the application.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
public class ArgumentHandler
{
	private static final Logger logger = LoggerFactory.getLogger(ArgumentHandler.class);

	@Inject
	private IEclipseContext context;

	@PostConstruct
	public void handle()
	{
		//parse command line arguments for -o/--open arguments, and pass them to the intent system
		LenientCmdLineParser parser = new LenientCmdLineParser();
		LenientCmdLineParser.Option openOption = parser.addStringOption('o', "open"); //$NON-NLS-1$
		try
		{
			parser.parse(CommandLineArgs.getApplicationArgs());
		}
		catch (IllegalOptionValueException e)
		{
			logger.error("Error parsing command line arguments", e); //$NON-NLS-1$
		}

		@SuppressWarnings("unchecked")
		Vector<String> openValues = parser.getOptionValues(openOption);

		for (final String arg : openValues)
		{
			logger.info("Starting Intent for resource provided via command line: " + arg); //$NON-NLS-1$
			try
			{
				URL url = new URL(arg);
				Intent intent = new Intent();
				intent.setURI(url.toURI());
				IIntentCallback callback = new AbstractIntentCallback()
				{
					@Override
					public void error(Exception e, Intent intent)
					{
						handleError(arg, e);
					}

					@Override
					public void completed(Object result, Intent intent)
					{
						if (result != null)
						{
							Dispatcher.getInstance().dispatch(result, context);
						}
					}
				};
				IntentManager.getInstance().start(intent, callback, context);
			}
			catch (Exception e)
			{
				handleError(arg, e);
			}
		}
	}

	private void handleError(String argument, Exception e)
	{
		logger.error("Error handling command line argument as Intent: " + argument, e); //$NON-NLS-1$
	}
}
