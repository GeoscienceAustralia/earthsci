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

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.application.parts.globe.GlobePart;
import au.gov.ga.earthsci.application.parts.globe.Messages;
import au.gov.ga.earthsci.newt.swt.WorldWindowNewtCanvasSWT;

/**
 * Handler for the fullscreen command.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FullscreenHandler
{
	public final static String COMMAND_ID = "au.gov.ga.earthsci.application.command.fullscreen"; //$NON-NLS-1$

	private Shell fullscreenShell;
	private Composite fullscreenComposite;

	@Execute
	public void execute(final Composite parent, final MToolItem item)
	{
		toggleFullscreen(parent, item, null);
	}

	@Execute
	public void execute(final Composite parent, final MMenuItem item, final MPart part, final EModelService service)
	{
		MToolBar toolbar = part.getToolbar();
		MToolItem toolItem = (MToolItem) service.find(GlobePart.FULLSCREEN_ID, toolbar);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = ge.getScreenDevices();
		GraphicsDevice device = null;
		for (GraphicsDevice d : devices)
		{
			if (item.getTags().contains(d.getIDstring()))
			{
				device = d;
				break;
			}
		}
		toggleFullscreen(parent, toolItem, device);

	}

	public void toggleFullscreen(final Composite parent, final MToolItem item, final GraphicsDevice device)
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
			if (device != null)
			{
				Rectangle bounds = device.getDefaultConfiguration().getBounds();
				fullscreenShell.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
			}
			final WorldWindowNewtCanvasSWT wwdFinal = wwd;
			wwd.setParent(fullscreenShell);

			fullscreenComposite = new Composite(parent, SWT.NONE);
			fullscreenComposite.setLayout(new GridLayout());
			fullscreenComposite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			Label fullscreenLabel = new Label(fullscreenComposite, SWT.NONE);
			fullscreenLabel.setText(Messages.FullscreenHandler_FullscreenLabel);
			GridData gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			gridData.horizontalAlignment = SWT.CENTER;
			gridData.grabExcessVerticalSpace = true;
			gridData.grabExcessHorizontalSpace = true;
			fullscreenLabel.setLayoutData(gridData);

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
					fullscreenComposite.dispose();
					if (!parent.isDisposed())
					{
						wwdFinal.setParent(parent);
						parent.layout();
					}
					fullscreenShell = null;
					fullscreenComposite = null;
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
			parent.layout();
		}
	}
}
