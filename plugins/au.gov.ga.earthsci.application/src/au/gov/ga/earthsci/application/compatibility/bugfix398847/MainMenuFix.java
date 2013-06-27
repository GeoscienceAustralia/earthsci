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
package au.gov.ga.earthsci.application.compatibility.bugfix398847;

import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;

/**
 * Workaround for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=398847.
 * <p/>
 * When an e4 application is using the 3.x workbench, Line 299 of
 * {@link org.eclipse.ui.internal.WorkbenchWindow} removes the main menu before
 * the model is saved. This class listens to all windows for the main menu to be
 * removed, and then adds it back in again so it can be saved.
 * <p/>
 * Should be added to the Application.e4xmi model as an Addon.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MainMenuFix
{
	@PostConstruct
	public void init(MApplication application)
	{
		List<MWindow> windows = application.getChildren();
		for (final MWindow childWindow : windows)
		{
			((EObject) childWindow).eAdapters().add(new AdapterImpl()
			{
				@Override
				public void notifyChanged(Notification notification)
				{
					if (notification.getFeatureID(MWindow.class) == BasicPackageImpl.WINDOW__MAIN_MENU)
					{
						if (notification.getOldValue() != null && notification.getNewValue() == null)
						{
							MMenu menu = (MMenu) notification.getOldValue();
							childWindow.setMainMenu(menu);
						}
					}
				}
			});
		}
	}
}
