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
package au.gov.ga.earthsci.application.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;

/**
 * {@link IConsoleFactory} implementation that creates
 * {@link StandardOutConsole}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StandardOutConsoleFactory implements IConsoleFactory
{
	private IConsoleManager consoleManager = null;
	private StandardOutConsole console = null;

	public StandardOutConsoleFactory()
	{
		consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		consoleManager.addConsoleListener(new IConsoleListener()
		{
			@Override
			public void consolesAdded(IConsole[] consoles)
			{
			}

			@Override
			public void consolesRemoved(IConsole[] consoles)
			{
			}
		});
	}

	@Override
	public void openConsole()
	{
		if (console == null)
		{
			console = new StandardOutConsole();
			consoleManager.addConsoles(new IConsole[] { console });
		}
		consoleManager.showConsoleView(console);
	}
}
