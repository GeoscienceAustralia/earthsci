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
package au.gov.ga.earthsci.application.parts.legend;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;

/**
 * Part that displays layer legends.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LegendPart
{
	public static final String PART_ID = "au.gov.ga.earthsci.application.legend.part"; //$NON-NLS-1$
	public static final String WINDOW_ID = PART_ID + ".window"; //$NON-NLS-1$
	public static final String STACK_ID = PART_ID + ".stack"; //$NON-NLS-1$
	public static final String INPUT_NAME = PART_ID + ".input"; //$NON-NLS-1$

	private Browser browser;

	@Inject
	public void init(Composite parent)
	{
		browser = new Browser(parent, SWT.NONE);
	}

	@PostConstruct
	private void postConstruct()
	{
	}

	@PreDestroy
	private void preDestroy()
	{
	}

	@Inject
	@Optional
	private void setPartInput(@Named(INPUT_NAME) ILayerTreeNode partInput)
	{
		ILayerTreeNode layer = partInput;
		if (layer.getLegendURL() != null)
		{
			browser.setUrl(layer.getLegendURL().toString());
		}
		else
		{
			browser.setText(generateMissingHtml());
		}
	}

	private String generateMissingHtml()
	{
		return "<html><body>No legend defined.</body></html>";
	}

	public static MPart showPart(EPartService partService, EModelService modelService, MWindow window, String reuseTag,
			String label)
	{
		MPart part = null;

		//find a part to reuse if possible
		if (reuseTag != null)
		{
			List<MPart> reuse =
					modelService.findElements(window, PART_ID, MPart.class, Arrays.asList(new String[] { reuseTag }));
			if (!reuse.isEmpty())
			{
				part = reuse.get(0);
			}
		}

		if (part == null)
		{
			//create the part from the PartDescriptor
			part = partService.createPart(PART_ID);
			if (reuseTag != null)
			{
				part.getTags().add(reuseTag);
			}

			//first find the stack to add the part to
			MPartStack stack = null;

			//find other legend parts to put this part next to
			List<MPart> siblings = modelService.findElements(window, PART_ID, MPart.class, null);

			//use this opportunity to clean up old legend parts
			for (MPart sibling : siblings)
			{
				//legend parts are not reused, so remove them if they are not ToBeRendered
				if (!sibling.isToBeRendered() && sibling.getParent() != null)
				{
					sibling.getParent().getChildren().remove(sibling);
				}
			}

			//select a stack next to one of the siblings
			for (MPart sibling : siblings)
			{
				if (sibling.isToBeRendered())
				{
					Object parent = sibling.getParent();
					if (parent instanceof MPartStack)
					{
						stack = (MPartStack) parent;
						if (getFirstWindowParent(sibling) != window)
						{
							//prefer an external window if possible
							break;
						}
					}
				}
			}

			//if no stack was found with a sibling, create a new one in a new window
			if (stack == null)
			{
				MWindow newWindow = MBasicFactory.INSTANCE.createWindow();
				newWindow.setElementId(WINDOW_ID);
				newWindow.setWidth(300); //TODO fix arbitrary constants
				newWindow.setHeight(250);
				window.getWindows().add(newWindow);
				stack = MBasicFactory.INSTANCE.createPartStack();
				stack.setElementId(STACK_ID);
				newWindow.getChildren().add(stack);
			}

			//add it to the stack
			stack.getChildren().add(part);
		}

		//show and return the part
		part.setLabel(label);
		partService.showPart(part, PartState.ACTIVATE);
		return part;
	}

	private static MWindow getFirstWindowParent(MUIElement element)
	{
		while (element != null && !(element instanceof MWindow))
		{
			element = element.getParent();
		}
		return (MWindow) element;
	}
}
