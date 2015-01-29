/*******************************************************************************
 * Copyright 2015 Geoscience Australia
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
package au.gov.ga.earthsci.application.compatibility;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;

/**
 * Compatibility Mode issue results in MainMenu (org.eclipse.ui.main.menu) being
 * null after the workbench is reloaded. This results in fragments' 'menu'
 * contributions being ignored after the initial launch. This is an issue
 * regardless if org.eclipse.ui.main.menu is defined in application's
 * legacy.e4xmi file or not.
 * 
 * @author Steven Spungin
 * @see <a
 *      href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=388808">https://bugs.eclipse.org/bugs/show_bug.cgi?id=388808</a>
 */
public class ForceMainMenuProcessor
{
	@Execute
	public void run(MApplication app)
	{
		try
		{
			MMenu menu = app.getChildren().get(0).getMainMenu();
			if (menu == null)
			{
				menu = MMenuFactory.INSTANCE.createMenu();
				menu.setElementId("org.eclipse.ui.main.menu"); //$NON-NLS-1$
				app.getChildren().get(0).setMainMenu(menu);
			}
		}
		catch (Exception ex)
		{
		}
	}
}
