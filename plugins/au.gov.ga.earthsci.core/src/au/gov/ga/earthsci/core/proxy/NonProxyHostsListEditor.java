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
package au.gov.ga.earthsci.core.proxy;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link ListEditor} subclass which allows editing of the non-proxy hosts field
 * (which is delimited by the pipe '|' character).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NonProxyHostsListEditor extends ListEditor
{
	public NonProxyHostsListEditor(String name, String labelText, Composite parent)
	{
		super(name, labelText, parent);
	}

	@Override
	protected String createList(String[] items)
	{
		if (items == null || items.length == 0)
		{
			return ""; //$NON-NLS-1$
		}

		StringBuilder sb = new StringBuilder();
		for (String i : items)
		{
			sb.append("|" + i); //$NON-NLS-1$
		}
		return sb.substring(1);
	}

	@Override
	protected String getNewInputObject()
	{
		InputDialog inputDialog =
				new InputDialog(getShell(), "New non-proxy host", "Enter a new non-proxy host.", "",
						new IInputValidator()
						{
							@Override
							public String isValid(String s)
							{
								if (s != null && s.length() > 0)
								{
									return null;
								}
								return "Please enter a value";
							}
						});
		inputDialog.open();

		if (inputDialog.getReturnCode() != Window.OK)
		{
			return null;
		}
		return inputDialog.getValue();
	}

	@Override
	protected String[] parseString(String stringList)
	{
		if (stringList == null)
		{
			return new String[] {};
		}
		return stringList.split("\\|"); //$NON-NLS-1$
	}
}
