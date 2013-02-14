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

import javax.inject.Named;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.application.about.AboutDialog;

/**
 * Handler which shows the About dialog box.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AboutHandler
{
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell)
	{
		IProduct product = Platform.getProduct();
		
		AboutDialog dialog = new AboutDialog(shell, product);
		dialog.open();
	}
}
