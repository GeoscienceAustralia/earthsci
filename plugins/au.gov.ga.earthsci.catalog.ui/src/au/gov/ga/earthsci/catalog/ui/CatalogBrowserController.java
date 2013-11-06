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
package au.gov.ga.earthsci.catalog.ui;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;

import au.gov.ga.earthsci.application.util.UserActionPreference;
import au.gov.ga.earthsci.catalog.CatalogLayerHelper;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.catalog.ui.preferences.ICatalogBrowserPreferences;
import au.gov.ga.earthsci.layer.tree.ILayerTreeNode;
import au.gov.ga.earthsci.layer.worldwind.ITreeModel;

/**
 * The default implementation of the {@link ICatalogBrowserController}
 * interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
@Singleton
public class CatalogBrowserController implements ICatalogBrowserController
{
	@Inject
	private ITreeModel currentLayerModel;

	@Inject
	private ICatalogBrowserPreferences preferences;

	@Inject
	private IEclipseContext context;

	@Override
	public boolean areAllLayerNodes(ICatalogTreeNode... nodes)
	{
		if (nodes == null)
		{
			return true;
		}
		for (ICatalogTreeNode node : nodes)
		{
			if (node != null && !node.isLayerNode())
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public void addToLayerModel(ICatalogTreeNode... nodes)
	{
		boolean fullNodePathRequiredOnAdd = isFullNodePathRequiredOnAdd();
		for (ICatalogTreeNode node : nodes)
		{
			ILayerTreeNode parent = fullNodePathRequiredOnAdd ? createNodePath(node) : currentLayerModel.getRootNode();
			CatalogLayerHelper.insertIntoLayerModel(parent, node, context);
		}
	}

	/**
	 * Initialises (if required) the node path in the current layer model into
	 * which the given node and its children should be inserted. The parent node
	 * into which the given catalog node should be inserted is returned.
	 * <p/>
	 * If the node path does not yet exist, it will be created.
	 * <p/>
	 * If the node path does exist, it will be reused.
	 * 
	 * @param node
	 *            The catalog being inserted into the layer model
	 * 
	 * @return The node into which the given catalog node (and its children)
	 *         should be inserted. This may be an existing node, or a new node
	 *         as required.
	 */
	private ILayerTreeNode createNodePath(ICatalogTreeNode node)
	{
		if (node.isRoot())
		{
			return currentLayerModel.getRootNode();
		}

		ILayerTreeNode[] folders = currentLayerModel.getRootNode().getNodesForURI(node.getURI());
		if (folders.length != 0)
		{
			return folders[0];
		}

		ILayerTreeNode parent = createNodePath(node.getParent());

		if (!node.isLayerNode())
		{
			ILayerTreeNode folder = CatalogLayerHelper.createFolderNode(node);
			parent.addChild(folder);
			return folder;
		}

		return parent;
	}

	private boolean isFullNodePathRequiredOnAdd()
	{
		if (preferences.getAddNodeStructureMode() != UserActionPreference.ASK)
		{
			return preferences.getAddNodeStructureMode() == UserActionPreference.ALWAYS;
		}

		MessageDialogWithToggle message =
				MessageDialogWithToggle.openYesNoQuestion(null,
						Messages.CatalogBrowserController_AddNodePathDialogTitle,
						Messages.CatalogBrowserController_AddNodePathDialogMessage,
						Messages.CatalogBrowserController_DialogDontAsk, false, null, null);

		UserActionPreference preference =
				(message.getReturnCode() == IDialogConstants.YES_ID) ? UserActionPreference.ALWAYS
						: UserActionPreference.NEVER;
		preferences.setAddNodeStructureMode(message.getToggleState() ? preference : UserActionPreference.ASK);

		return preference == UserActionPreference.ALWAYS;
	}

	public void setCurrentLayerModel(ITreeModel currentLayerModel)
	{
		this.currentLayerModel = currentLayerModel;
	}
}
