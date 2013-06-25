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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.TreeViewer;

import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.worldwind.ITreeModel;

/**
 * Handles new folder commands for the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NewFolderHandler
{
	@Inject
	private ITreeModel model;

	@Execute
	public void execute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode parent, TreeViewer viewer)
	{
		FolderNode folder = new FolderNode();
		folder.setName(Messages.NewFolderHandler_DefaultNewFolderName);
		if (parent == null)
		{
			model.getRootNode().addChild(folder);
		}
		else
		{
			parent.addChild(folder);
		}
		viewer.editElement(folder, 0);
	}
}
