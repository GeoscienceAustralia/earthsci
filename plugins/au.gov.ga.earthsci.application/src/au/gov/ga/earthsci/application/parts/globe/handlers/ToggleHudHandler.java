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

import gov.nasa.worldwind.layers.Layer;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;

import au.gov.ga.earthsci.application.parts.globe.GlobePart;

/**
 * Handler for the toggle hud command, which toggles hud layers on/off.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ToggleHudHandler
{
	public final static String HUD_ID_PARAMETER_ID = "au.gov.ga.earthsci.application.command.toggleHud.hudId"; //$NON-NLS-1$
	public final static String HUD_COMMAND_ID = "au.gov.ga.earthsci.application.command.toggleHud"; //$NON-NLS-1$

	@Execute
	public void execute(@Optional @Named(HUD_ID_PARAMETER_ID) String hudId, MPart part, MToolItem toolItem,
			GlobePart globe)
	{
		if (hudId == null)
		{
			return;
		}

		Layer layer = globe.getHudLayerForId(hudId);
		if (layer == null)
		{
			return;
		}

		layer.setEnabled(!layer.isEnabled());
		toolItem.setSelected(layer.isEnabled());
		globe.getWorldWindow().redraw();
	}
}
