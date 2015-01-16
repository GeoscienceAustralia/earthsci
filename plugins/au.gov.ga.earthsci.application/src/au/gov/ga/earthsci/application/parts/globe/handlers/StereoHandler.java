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
package au.gov.ga.earthsci.application.parts.globe.handlers;

import gov.nasa.worldwind.View;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PreferencesUtil;

import au.gov.ga.earthsci.application.parts.globe.GlobePart;
import au.gov.ga.earthsci.application.parts.globe.GlobePreferencePage;
import au.gov.ga.earthsci.application.parts.globe.Messages;
import au.gov.ga.earthsci.worldwind.common.view.delegate.IDelegateView;
import au.gov.ga.earthsci.worldwind.common.view.delegate.IViewDelegate;
import au.gov.ga.earthsci.worldwind.common.view.oculus.RiftViewDistortionDelegate;
import au.gov.ga.earthsci.worldwind.common.view.stereo.StereoMode;
import au.gov.ga.earthsci.worldwind.common.view.stereo.StereoViewDelegate;
import au.gov.ga.earthsci.worldwind.common.view.stereo.StereoViewParameters;

/**
 * Handler that sets the {@link IDelegateView}'s {@link IViewDelegate} for
 * changing the stereo rendering mode.
 *
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StereoHandler
{
	public static final String MENU_NONE = "au.gov.ga.earthsci.application.globe.stereo.menuitems.none"; //$NON-NLS-1$
	public static final String MENU_REDCYAN = "au.gov.ga.earthsci.application.globe.stereo.menuitems.redcyan"; //$NON-NLS-1$
	public static final String MENU_GREENMAGENTA = "au.gov.ga.earthsci.application.globe.stereo.menuitems.greenmagenta"; //$NON-NLS-1$
	public static final String MENU_BLUEYELLOW = "au.gov.ga.earthsci.application.globe.stereo.menuitems.blueyellow"; //$NON-NLS-1$
	public static final String MENU_QUADBUFFERED = "au.gov.ga.earthsci.application.globe.stereo.menuitems.quadbuffered"; //$NON-NLS-1$
	public static final String MENU_OCULUSRIFT = "au.gov.ga.earthsci.application.globe.stereo.menuitems.oculusrift"; //$NON-NLS-1$

	private String lastMenuItemId = MENU_REDCYAN;

	@Inject
	@Preference(nodePath = GlobePreferencePage.QUALIFIER_ID, value = GlobePreferencePage.STEREO_PREFERENCE_NAME)
	private boolean stereo;

	private boolean lastWasStereo = false;

	@Execute
	public void execute(MToolItem toolItem, GlobePart globe, EModelService service, Composite parent)
	{
		View view = globe.getWorldWindow().getView();
		if (view instanceof IDelegateView)
		{
			IDelegateView delegateView = (IDelegateView) view;

			String menuItemId = lastMenuItemId;
			if (delegateView.getDelegate() != null)
			{
				menuItemId = MENU_NONE;
			}

			for (MMenuElement menuElement : toolItem.getMenu().getChildren())
			{
				if (menuElement instanceof MMenuItem)
				{
					((MMenuItem) menuElement).setSelected(false);
				}
			}

			setupViewDelegate(delegateView, menuItemId, parent);
			MMenuItem menuItem = (MMenuItem) service.find(menuItemId, toolItem.getMenu());
			if (menuItem != null)
			{
				menuItem.setSelected(true);
			}
		}
	}

	@Execute
	public void execute(MMenuItem menuItem, GlobePart globe, Composite parent)
	{
		View view = globe.getWorldWindow().getView();
		if (view instanceof IDelegateView)
		{
			setupViewDelegate((IDelegateView) view, menuItem.getElementId(), parent);
		}
	}

	protected void setupViewDelegate(IDelegateView view, String menuItemId, Composite parent)
	{
		try
		{
			if (MENU_NONE.equals(menuItemId))
			{
				view.setDelegate(null);
				return;
			}
			lastMenuItemId = menuItemId;
			if (MENU_OCULUSRIFT.equals(menuItemId))
			{
				view.setDelegate(new RiftViewDistortionDelegate());
			}
			else
			{
				StereoViewDelegate delegate = new StereoViewDelegate();
				view.setDelegate(delegate);

				StereoViewParameters parameters = delegate.getParameters();
				parameters.setStereoEnabled(true);
				if (MENU_REDCYAN.equals(menuItemId))
				{
					parameters.setStereoMode(StereoMode.RC_ANAGLYPH);
				}
				else if (MENU_GREENMAGENTA.equals(menuItemId))
				{
					parameters.setStereoMode(StereoMode.GM_ANAGLYPH);
				}
				else if (MENU_BLUEYELLOW.equals(menuItemId))
				{
					parameters.setStereoMode(StereoMode.BY_ANAGLYPH);
				}
				else
				{
					parameters.setStereoMode(StereoMode.STEREO_BUFFER);

					if (!lastWasStereo && !stereo)
					{
						if (MessageDialog.openQuestion(parent.getShell(),
								Messages.StereoHandler_StereoDialogTitle, Messages.StereoHandler_StereoDialogMessage))
						{
							PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
									parent.getShell(), GlobePreferencePage.PREFERENCE_PAGE_ID, null, null);
							dialog.open();
						}
					}
				}
			}
		}
		finally
		{
			lastWasStereo = MENU_QUADBUFFERED.equals(menuItemId);
		}
	}
}
