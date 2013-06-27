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
package au.gov.ga.earthsci.application.compatibility;

import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.renderers.swt.MenuManagerRenderer;

/**
 * Subclass of {@link MenuManagerRenderer} that cleans up any contributed menu
 * items that have been contributed into children of menus. The superclass only
 * looks at the root menu element during cleanup, and not the children.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MenuManagerRendererFixed extends MenuManagerRenderer
{
	@Override
	public void cleanUp(MMenu menuModel)
	{
		super.cleanUp(menuModel);
		for (MMenuElement child : menuModel.getChildren())
		{
			if (child instanceof MMenu)
			{
				cleanUp((MMenu) child);
			}
		}
	}
}
