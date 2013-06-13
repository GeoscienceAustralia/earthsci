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
package au.gov.ga.earthsci.application.parts.browser.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.program.Program;

import au.gov.ga.earthsci.application.parts.browser.BrowserPart;

/**
 * Handles the external browser command, which opens the current URL in an
 * external browser.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ExternalBrowserHandler
{
	@Execute
	public void execute(BrowserPart browser)
	{
		Program.launch(browser.getURL());
	}

	@CanExecute
	public boolean canExecute(BrowserPart browser)
	{
		String url = browser.getURL();
		return url != null && url.length() > 0 && !url.equals("about:blank"); //$NON-NLS-1$
	}
}
