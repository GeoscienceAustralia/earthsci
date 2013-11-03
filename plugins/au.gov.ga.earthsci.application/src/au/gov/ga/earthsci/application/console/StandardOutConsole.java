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
		super(Messages.StandardOutConsole_Name,
				ImageRegistry.getInstance().getDescriptor(ImageRegistry.ICON_CONSOLE));

		IOConsoleOutputStream out = newOutputStream();
		IOConsoleOutputStream err = newOutputStream();
		err.setColor(new Color(Display.getCurrent(), 255, 0, 0));

		RegexOutputStream regexOutputStream = new RegexOutputStream(out);

		//FATAL > ERROR > WARN > INFO > DEBUG > TRACE
		addColor(regexOutputStream, ".*FATAL.*", new Color(Display.getCurrent(), 255, 0, 128)); //$NON-NLS-1$
		addColor(regexOutputStream, ".*ERROR.*", new Color(Display.getCurrent(), 255, 0, 0)); //$NON-NLS-1$
		addColor(regexOutputStream, ".*WARN.*", new Color(Display.getCurrent(), 255, 128, 0)); //$NON-NLS-1$
		addColor(regexOutputStream, ".*INFO.*", new Color(Display.getCurrent(), 0, 128, 0)); //$NON-NLS-1$
		addColor(regexOutputStream, ".*DEBUG.*", new Color(Display.getCurrent(), 0, 0, 128)); //$NON-NLS-1$
		addColor(regexOutputStream, ".*TRACE.*", new Color(Display.getCurrent(), 0, 0, 255)); //$NON-NLS-1$

		StandardOutputCollector.INSTANCE.addStreams(regexOutputStream, err, true);
	}

	public void addColor(RegexOutputStream os, String regex, Color color)
	{
		IOConsoleOutputStream cos = newOutputStream();
		cos.setColor(color);
		os.add(regex, cos);
	}
}
