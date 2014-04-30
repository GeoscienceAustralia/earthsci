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
package au.gov.ga.earthsci.application.parts.globe.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.newt.swt.WorldWindowNewtCanvasSWT;

/**
 * Handler for the fullscreen command.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FullscreenHandler
{
	private Shell fullscreenShell;

	@Execute
	public void execute(final Composite parent, final MToolItem item)
	{
		//already fullscreen, dispose of fullscreen shell
		if (fullscreenShell != null)
		{
			fullscreenShell.dispose();
			return;
		}

		WorldWindowNewtCanvasSWT wwd = null;
		for (Control child : parent.getChildren())
		{
			if (child instanceof WorldWindowNewtCanvasSWT)
			{
				wwd = (WorldWindowNewtCanvasSWT) child;
				break;
			}
		}

		if (wwd != null)
		{
			fullscreenShell = new Shell(parent.getDisplay(), SWT.NONE);
			fullscreenShell.setLayout(new FillLayout());
			final WorldWindowNewtCanvasSWT wwdFinal = wwd;
			wwd.setParent(fullscreenShell);

			parent.addDisposeListener(new DisposeListener()
			{
				@Override
				public void widgetDisposed(DisposeEvent e)
				{
					if (fullscreenShell != null && !fullscreenShell.isDisposed())
					{
						fullscreenShell.dispose();
					}
				}
			});
			fullscreenShell.addDisposeListener(new DisposeListener()
			{
				@Override
				public void widgetDisposed(DisposeEvent e)
				{
					if (!parent.isDisposed())
					{
						wwdFinal.setParent(parent);
						parent.layout();
					}
					fullscreenShell = null;
					item.setSelected(false);
				}
			});
			fullscreenShell.addTraverseListener(new TraverseListener()
			{
				@Override
				public void keyTraversed(TraverseEvent e)
				{
					if (e.detail == SWT.TRAVERSE_ESCAPE)
					{
						fullscreenShell.dispose();
					}
				}
			});

			fullscreenShell.setVisible(true);
			fullscreenShell.setFullScreen(true);
			item.setSelected(true);
		}
	}
}
