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
package au.gov.ga.earthsci.editable.factories;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.editable.IFactory;

/**
 * {@link IFactory} implementation for creating new string objects. Prompts the
 * user for a string using an {@link InputDialog}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StringFactory implements IFactory<String>
{
	@Override
	public String create(ModelElementType type, ModelProperty property, Object parent, Shell shell)
	{
		String label = property.getLabel(true, CapitalizationType.NO_CAPS, false);
		String title = "New";
		String message = property instanceof ListProperty ?
				"Enter a value to add to the list of " + label + ":" : //$NON-NLS-2$
				"Enter a new " + label + ":"; //$NON-NLS-2$
		InputDialog dialog = new InputDialog(shell, title, message, null, null);
		if (dialog.open() == Dialog.OK)
		{
			return dialog.getValue();
		}
		return null;
	}
}
