/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.renderers.swt.WBWRenderer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * Bugfix for <a
 * href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=428519">428519</a>.
 * <p/>
 * {@link WorkbenchWindow} calls {@link Shell#setBounds(int, int, int, int)},
 * which overrides the {@link Shell#setMaximized(boolean)} state set by the
 * {@link WBWRenderer}. This class just calls
 * {@link Shell#setMaximized(boolean)} after the shell is opened.
 *
 * @author Michael de Hoog <michael.dehoog@ga.gov.au>
 */
public class WindowRenderer extends WBWRenderer
{
	private static String ShellMinimizedTag = "shellMinimized"; //$NON-NLS-1$
	private static String ShellMaximizedTag = "shellMaximized"; //$NON-NLS-1$

	@Override
	public void postProcess(MUIElement shellME)
	{
		super.postProcess(shellME);

		Shell shell = (Shell) shellME.getWidget();
		if (shellME.getTags().contains(ShellMaximizedTag))
		{
			shell.setMaximized(true);
		}
		else if (shellME.getTags().contains(ShellMinimizedTag))
		{
			shell.setMinimized(true);
		}
	}
}
