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
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;

/**
 * Handles properties action.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PropertiesHandler
{
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode layer, Clipboard clipboard)
	{
		execute(new ILayerTreeNode[] { layer }, clipboard);
	}

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode[] layers, Clipboard clipboard)
	{
		if (layers == null || layers.length == 0 || layers[0] == null)
		{
			return;
		}

		ILayerTreeNode layer = layers[0];
		System.out.println(layer);


		/*SapphireLayerImpl element = new SapphireLayerImpl(layer);
		Reference<DialogDef> definition =
				DefinitionLoader.context(SapphireLayer.class).sdef("layer").dialog("dialog");
		SapphireDialog dialog = new SapphireDialog(shell, element, definition);
		if (dialog.open() == Dialog.OK)
		{
			System.out.println("OK PRESSED!");
		}*/
	}

	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode layer)
	{
		return layer != null;
	}

	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) ILayerTreeNode[] layers)
	{
		return layers != null && layers.length == 1;
	}
}
