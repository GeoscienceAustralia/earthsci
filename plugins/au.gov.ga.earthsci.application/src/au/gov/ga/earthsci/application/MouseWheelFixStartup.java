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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * {@link IStartup} that changes the default behaviour of the mouse-wheel such
 * that, if scrolled, the control under the cursor will be focused and scrolled.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MouseWheelFixStartup implements IStartup
{
	@Override
	public void earlyStartup()
	{
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				workbench.getDisplay().addFilter(SWT.MouseVerticalWheel, new Listener()
				{
					@Override
					public void handleEvent(Event event)
					{
						Control control = workbench.getDisplay().getCursorControl();
						if ((control == null) || (control.isDisposed()) || (control.isFocusControl()))
						{
							return;
						}
						control.setFocus();

						event.doit = false;
						event.widget = control;
						workbench.getDisplay().post(event);
					}
				});
			}
		});
	}
}
