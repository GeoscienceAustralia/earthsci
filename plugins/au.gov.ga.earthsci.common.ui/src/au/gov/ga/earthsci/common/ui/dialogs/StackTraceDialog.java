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
package au.gov.ga.earthsci.common.ui.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * {@link ErrorDialog} subclass that shows the error's stacktrace in the details
 * view.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StackTraceDialog extends ErrorDialog
{
	private IStatus status;

	public StackTraceDialog(Shell parentShell, String dialogTitle, String message, IStatus status, int displayMask)
	{
		super(parentShell, dialogTitle, message, status, displayMask);
		this.status = status;
	}

	public static int openError(Shell parentShell, String title, String message, IStatus status)
	{
		return openError(parentShell, title, message, status, IStatus.OK | IStatus.INFO | IStatus.WARNING
				| IStatus.ERROR);
	}

	public static int openError(Shell parentShell, String title, String message, IStatus status, int displayMask)
	{
		StackTraceDialog dialog = new StackTraceDialog(parentShell, title, message, status, displayMask);
		return dialog.open();
	}

	@Override
	protected List createDropDownList(Composite parent)
	{
		List list = super.createDropDownList(parent);
		if (status != null && status.getException() != null)
		{
			StackTraceElement[] elements = status.getException().getStackTrace();
			for (StackTraceElement element : elements)
			{
				list.add("    " + element.toString()); //$NON-NLS-1$
			}
		}
		return list;
	}
}
