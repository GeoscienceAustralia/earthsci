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
package au.gov.ga.earthsci.application.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.swt.internal.copy.ShowViewDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

/**
 * Handler used to show a part (or "View").
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ShowViewHandler
{
	@Inject
	private EPartService partService;

	@Inject
	private MApplication application;

	@Inject
	private EModelService modelService;

	@Execute
	public void execute(IEclipseContext context, @Named(IServiceConstants.ACTIVE_SHELL) Shell shell)
			throws InvocationTargetException, InterruptedException
	{
		// XXX: Warning - this dialog is from the internal package. Expect behaviour/API to change!
		final ShowViewDialog dialog = new ShowViewDialog(shell, application, context);
		dialog.open();
		if (dialog.getReturnCode() != Window.OK)
		{
			return;
		}

		for (MPartDescriptor part : dialog.getSelection())
		{
			showPart(part);
		}
	}

	private void showPart(MPartDescriptor descriptor)
	{
		if (descriptor == null)
		{
			return;
		}

		List<MPart> siblings = modelService.findElements(application, descriptor.getElementId(), MPart.class, null);

		MPart part = null;
		if (!siblings.isEmpty())
		{
			if (descriptor.isAllowMultiple())
			{
				//if the part is allowed multiple, find a part that isn't rendered, and make it rendered
				for (MPart sibling : siblings)
				{
					if (!sibling.isToBeRendered())
					{
						part = sibling;
						part.setToBeRendered(true);
					}
				}
			}
			else
			{
				//otherwise just get the first part that matches the descriptor's id
				part = siblings.get(0);
				part.setToBeRendered(true);
			}
		}

		if (part == null)
		{
			//if no matching part was found, create a new one
			part = partService.createPart(descriptor.getElementId());

			//if creating a new part, select a stack next to one of the siblings
			MPartStack stack = null;
			int index = -1;
			for (MPart sibling : siblings)
			{
				MElementContainer<?> parent = sibling.getParent();
				if (parent instanceof MPartStack)
				{
					stack = (MPartStack) parent;
					index = ((MPartStack) parent).getChildren().indexOf(sibling) + 1;
					if (sibling.isToBeRendered())
					{
						//prefer a visible sibling's stack
						break;
					}
				}
			}

			if (stack != null)
			{
				try
				{
					stack.getChildren().add(index, part);
				}
				catch (IndexOutOfBoundsException e)
				{
					stack.getChildren().add(part);
				}
			}
		}

		//activate the part
		partService.showPart(part, PartState.ACTIVATE);
	}
}
