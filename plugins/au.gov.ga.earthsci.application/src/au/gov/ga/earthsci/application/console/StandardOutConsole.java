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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

import au.gov.ga.earthsci.application.ImageRegistry;

/**
 * Console that shows data written to standard out/err. Uses the
 * {@link StandardOutputCollector} to get changes to standard out.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StandardOutConsole extends IOConsole
{
	public StandardOutConsole()
	{
		super("Console", ImageRegistry.getInstance().getDescriptor(ImageRegistry.ICON_CONSOLE));
		IOConsoleOutputStream out = newOutputStream();
		IOConsoleOutputStream err = newOutputStream();
		err.setColor(new Color(Display.getCurrent(), 255, 0, 0));
		StandardOutputCollector.getInstance().addStreams(out, err, true);
	}
}
