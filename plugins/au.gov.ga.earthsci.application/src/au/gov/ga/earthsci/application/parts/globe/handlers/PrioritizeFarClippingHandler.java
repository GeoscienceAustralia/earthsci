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

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;

import au.gov.ga.earthsci.application.parts.globe.GlobePart;
import au.gov.ga.earthsci.worldwind.common.view.target.ITargetView;

/**
 * Handler that toggles the {@link ITargetView}'s far clipping priority.
 *
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PrioritizeFarClippingHandler
{
	private GlobePart globe;
	private MToolItem toolItem;

	@Execute
	public void execute(MToolItem toolItem, GlobePart globe)
	{
		View view = globe.getWorldWindow().getView();
		if (view instanceof ITargetView)
		{
			PrioritizeFarClippingSwitcher.setPrioritizeFarClipping((ITargetView) view, toolItem.isSelected());
		}
	}

	@CanExecute
	public boolean canExecute(MToolItem toolItem, GlobePart globe)
	{
		this.toolItem = toolItem;
		this.globe = globe;
		return globe != null && globe.getWorldWindow().getView() instanceof ITargetView;
	}

	@Inject
	@Optional
	public void prioritizeFarClippingToggledHandler(@UIEventTopic(PrioritizeFarClippingSwitcher.EVENT_TOPIC) View view)
	{
		if (globe != null && toolItem != null &&
				view == globe.getWorldWindow().getView() && view instanceof ITargetView)
		{
			toolItem.setSelected(((ITargetView) view).isPrioritizeFarClipping());
		}
	}
}
