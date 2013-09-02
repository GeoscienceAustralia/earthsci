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
package au.gov.ga.earthsci.application;

import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class which instantiates parts for any placeholders with the same
 * element id as the part.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PartInstantiator
{
	private final static Logger logger = LoggerFactory.getLogger(PartInstantiator.class);

	public static void createParts(MApplication application, EModelService service, EPartService partService)
	{
		//Sometimes, when switching windows at startup, the active context
		//is null or doesn't have a window, and the part instantiation fails.
		//Ensure that a child context with a window is activated:
		IEclipseContext activeChild = application.getContext().getActiveChild();
		if (activeChild == null || activeChild.get(MTrimmedWindow.class) == null)
		{
			boolean activated = false;
			if (application.getContext() instanceof EclipseContext)
			{
				for (IEclipseContext child : ((EclipseContext) application.getContext()).getChildren())
				{
					MTrimmedWindow window = child.get(MTrimmedWindow.class);
					if (window != null)
					{
						child.activate();
						activated = true;
						break;
					}
				}
			}
			if (!activated)
			{
				logger.error("Could not activate window for part instantiation"); //$NON-NLS-1$
				return;
			}
		}

		// find all placeholders
		List<MPlaceholder> placeholders = service.findElements(application, null, MPlaceholder.class, null);

		for (int i = placeholders.size() - 1; i >= 0; i--)
		{
			MPlaceholder placeholder = placeholders.get(i);
			if (placeholder.isVisible())
			{
				MPart part = partService.createPart(placeholder.getElementId());
				if (part != null)
				{
					List<MUIElement> siblings = placeholder.getParent().getChildren();
					int index = siblings.indexOf(placeholder);
					siblings.add(index, part);
					siblings.remove(placeholder);
					partService.activate(part);
				}
			}
		}
	}
}