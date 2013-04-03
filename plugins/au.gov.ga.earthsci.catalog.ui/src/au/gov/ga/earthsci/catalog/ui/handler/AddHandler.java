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
package au.gov.ga.earthsci.catalog.ui.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;

import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.catalog.ui.ICatalogBrowserController;

/**
 * An handler that is used for add operations on the catalog browser tree
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AddHandler
{
	@Inject
	private ICatalogBrowserController controller;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) ICatalogTreeNode[] selectedNodes)
	{
		controller.addToLayerModel(selectedNodes);
	}

	@CanExecute
	public boolean canExecute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ICatalogTreeNode[] selectedNodes)
	{
		return selectedNodes != null && selectedNodes.length > 0 && controller.areAllLayerNodes(selectedNodes);
	}
}
