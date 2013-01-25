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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import au.gov.ga.earthsci.application.parts.globe.GlobePart;
import au.gov.ga.earthsci.core.worldwind.ITreeModel;

/**
 * Handler for the toggle hud command, which toggles hud layers on/off.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ToggleHudHandler
{
	public final static String HUD_ID_PARAMETER_ID = "au.gov.ga.earthsci.application.command.togglehud.hudid"; //$NON-NLS-1$
	public final static String TOOL_ID_PARAMETER_ID = "au.gov.ga.earthsci.application.command.togglehud.toolid"; //$NON-NLS-1$
	public final static String HUD_COMMAND_ID = "au.gov.ga.earthsci.application.command.togglehud"; //$NON-NLS-1$

	@Inject
	private EModelService service;

	@Inject
	private MApplication application;

	@Inject
	private ITreeModel model;

	@Execute
	public void execute(@Optional @Named(HUD_ID_PARAMETER_ID) String hudId,
			@Optional @Named(TOOL_ID_PARAMETER_ID) String toolId, MPart part, GlobePart globe)
	{
		if (hudId == null || toolId == null)
			return;

		MToolItem toolItem = (MToolItem) service.find(toolId, part.getToolbar());
		Layer layer = globe.getHudLayerForId(hudId);

		if (toolItem == null || layer == null)
			return;

		layer.setEnabled(!layer.isEnabled());
		toolItem.setSelected(layer.isEnabled());
		globe.getWorldWindow().redraw();
	}
}
