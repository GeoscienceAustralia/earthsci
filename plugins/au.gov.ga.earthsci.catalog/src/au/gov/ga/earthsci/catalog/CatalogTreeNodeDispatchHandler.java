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
package au.gov.ga.earthsci.catalog;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.dispatch.IDispatchHandler;

/**
 * {@link IDispatchHandler} implementation for catalog tree nodes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CatalogTreeNodeDispatchHandler implements IDispatchHandler
{
	private static final String CATALOG_PART_ID = "au.gov.ga.earthsci.application.catalog.part"; //$NON-NLS-1$

	@Inject
	private ICatalogModel model;

	@Inject
	private EPartService partService;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Override
	public void handle(Object object, Intent intent)
	{
		if (object instanceof ICatalogTreeNode)
		{
			ICatalogTreeNode node = (ICatalogTreeNode) object;
			ICatalogTreeNode existing = model.getTopLevelCatalogForURI(node.getURI());
			if (existing == null)
			{
				model.addTopLevelCatalog(node);
			}
			else
			{
				node = existing;
			}
			final ICatalogTreeNode selection = node;

			shell.getDisplay().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					MPart part = partService.showPart(CATALOG_PART_ID, PartState.ACTIVATE);
					ESelectionService selectionService = part.getContext().get(ESelectionService.class);
					selectionService.setSelection(new ICatalogTreeNode[] { selection });
				}
			});
		}
	}
}
