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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import au.gov.ga.earthsci.application.parts.globe.ViewLinker;
import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;

/**
 * Handler for the link views command; uses the {@link ViewLinker} to link the
 * views.
 *
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LinkViewsHandler
{
	private static final String TOOL_ITEM_ID = "au.gov.ga.earthsci.application.globe.toolitems.link"; //$NON-NLS-1$

	private MToolItem toolItem;

	@PostConstruct
	public void init(MPart part, EModelService service, ViewLinker linker)
	{
		MToolItem toolItem = (MToolItem) service.find(TOOL_ITEM_ID, part.getToolbar());
		if (toolItem != null)
		{
			toolItem.setSelected(linker.isEnabled());
		}
	}

	@Execute
	public void execute(MToolItem toolItem, ViewLinker linker)
	{
		linker.setEnabled(toolItem.isSelected());
	}

	@CanExecute
	public boolean canExecute(MToolItem toolItem)
	{
		this.toolItem = toolItem;
		return WorldWindowRegistry.INSTANCE.getAll().size() > 1;
	}

	@Inject
	@Optional
	public void linkViewsToggledHandler(@UIEventTopic(ViewLinker.EVENT_TOPIC) Boolean enabled)
	{
		if (toolItem != null && enabled != null)
		{
			toolItem.setSelected(enabled);
		}
	}
}
