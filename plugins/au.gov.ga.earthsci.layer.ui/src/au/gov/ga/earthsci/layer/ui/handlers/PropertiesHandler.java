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

import au.gov.ga.earthsci.layer.ILayerTreeNode;

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

		/*final ILayerTreeNode layer = layers[0];
		if (layer instanceof LayerNode)
		{
			Layer l = ((LayerNode) layer).getLayer();
			ModelAndDefinition editor = EditableManager.getInstance().edit(l);

			editor.getModel().attach(new Listener()
			{
				@Override
				public void handle(Event event)
				{
					if (event instanceof PropertyContentEvent)
					{
						//a layer property changed, redraw the world windows
						WorldWindowRegistry.INSTANCE.redraw();
					}
				}
			});

			Reference<DialogDef> definition = editor.getLoader().dialog();
			SapphireDialog dialog = new SapphireDialog(shell, editor.getModel(), definition);
			if (dialog.open() != Dialog.OK)
			{
				editor.getModel().revert();
				WorldWindowRegistry.INSTANCE.redraw();
			}
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
