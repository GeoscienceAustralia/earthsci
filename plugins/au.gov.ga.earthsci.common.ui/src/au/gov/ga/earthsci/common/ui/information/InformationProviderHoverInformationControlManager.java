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
package au.gov.ga.earthsci.common.ui.information;

import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.jface.extras.information.AbstractHoverInformationControlManager;
import au.gov.ga.earthsci.jface.extras.information.ControlStickyHoverManager;

/**
 * {@link AbstractHoverInformationControlManager} implementation that uses an
 * {@link IInformationProvider} as the provider of information for the control.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class InformationProviderHoverInformationControlManager extends AbstractHoverInformationControlManager
{
	public static InformationProviderHoverInformationControlManager install(Control control,
			IInformationProvider provider, IInformationControlCreator creator)
	{
		final InformationProviderHoverInformationControlManager manager =
				new InformationProviderHoverInformationControlManager(provider, creator);
		manager.install(control);

		// MouseListener to show the information when the user hovers a table item
		control.addMouseTrackListener(new MouseTrackAdapter()
		{
			@Override
			public void mouseHover(MouseEvent event)
			{
				manager.showInformation();
			}
		});

		// DisposeListener to uninstall the information control manager
		control.addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				manager.dispose();
			}
		});

		return manager;
	}

	private final IInformationProvider provider;

	public InformationProviderHoverInformationControlManager(IInformationProvider provider,
			final IInformationControlCreator creator)
	{
		super(creator);
		this.provider = provider;
		setAnchor(ANCHOR_RIGHT);
	}

	@Override
	protected void computeInformation()
	{
		Display display = getSubjectControl().getDisplay();
		Point mouseLocation = display.getCursorLocation();
		mouseLocation = getSubjectControl().toControl(mouseLocation);

		Object info = provider.getInformation(mouseLocation);
		Rectangle area = provider.getArea(mouseLocation);

		setInformation(info, area);
	}

	@Override
	public void install(Control subjectControl)
	{
		super.install(subjectControl);
		ControlStickyHoverManager sticky = new ControlStickyHoverManager(subjectControl);
		getInternalAccessor().setInformationControlReplacer(sticky);
	}
}
