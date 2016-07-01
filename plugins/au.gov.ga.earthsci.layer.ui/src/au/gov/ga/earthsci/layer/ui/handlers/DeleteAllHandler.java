/*******************************************************************************
 * Copyright 2016 Geoscience Australia
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
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.layer.ui.Messages;
import au.gov.ga.earthsci.layer.worldwind.WorldWindModel;

/**
 * Delete all layers.
 *
 * @author Michael de Hoog
 */
public class DeleteAllHandler
{
	@Inject
	private WorldWindModel model;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell)
	{
		if (MessageDialog.openQuestion(shell, Messages.DeleteAllHandler_QuestionTitle,
				Messages.DeleteAllHandler_QuestionMessage))
		{
			model.removeAllLayers();
		}
	}
}
