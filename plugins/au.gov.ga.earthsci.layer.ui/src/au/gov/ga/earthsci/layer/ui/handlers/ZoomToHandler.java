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
package au.gov.ga.earthsci.layer.ui.handlers;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;

import au.gov.ga.earthsci.layer.tree.ILayerTreeNode;
import au.gov.ga.earthsci.layer.ui.LayerTreePart;
import au.gov.ga.earthsci.worldwind.common.layers.Bounded;

/**
 * Handler for the zoomToLayer command.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ZoomToHandler
{
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode layer)
	{
		LayerTreePart.flyToLayer(layer);
	}

	@CanExecute
	public boolean canExecute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode layer)
	{
		return layer != null && Bounded.Reader.getBounds(layer) != null;
	}
}
