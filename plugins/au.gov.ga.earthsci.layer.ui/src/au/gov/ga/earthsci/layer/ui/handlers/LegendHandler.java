/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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
package au.gov.ga.earthsci.layer.ui.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import au.gov.ga.earthsci.application.parts.legend.LegendPart;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;

/**
 * Handles layer node legend button selection.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LegendHandler
{
	@Inject
	private EPartService partService;

	@Inject
	private EModelService modelService;

	@Inject
	private MWindow window;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode layerNode)
	{
		if (layerNode.getLegendURL() != null)
		{
			String tag = layerNode.getLegendURL().toString();
			tag += "|" + layerNode.getURI(); //$NON-NLS-1$
			String label = Messages.bind(Messages.LegendHandler_PartLabel, layerNode.getLabelOrName());

			MPart part = LegendPart.showPart(partService, modelService, window, tag, label);
			part.getContext().modify(LegendPart.INPUT_NAME, layerNode);
			part.getContext().declareModifiable(LegendPart.INPUT_NAME);
		}
	}

	@CanExecute
	public boolean canExecute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode layer)
	{
		return layer != null && layer.getLegendURL() != null;
	}
}
