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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.internal.Position;
import org.eclipse.e4.ui.model.internal.PositionInfo;
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

	public static final String PARENT_ID = "parent"; //$NON-NLS-1$
	public static final String POSITION_ID = "position"; //$NON-NLS-1$
	public static final String VISIBLE_ID = "visible"; //$NON-NLS-1$
	public static final String ONTOP_ID = "ontop"; //$NON-NLS-1$

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

		List<MPart> ontops = new ArrayList<MPart>();
		for (MPartDescriptor descriptor : application.getDescriptors())
		{
			if (!(descriptor.getPersistedState().containsKey(VISIBLE_ID) && Boolean.toString(true).equalsIgnoreCase(
					descriptor.getPersistedState().get(VISIBLE_ID))))
			{
				continue;
			}
			List<MPart> existingParts = service.findElements(application, descriptor.getElementId(), MPart.class, null);
			if (!existingParts.isEmpty())
			{
				//part is already instantiated
				continue;
			}
			MPart part = partService.createPart(descriptor.getElementId());
			if (part == null)
			{
				continue;
			}
			addPartToAppropriateContainer(part, descriptor, application, service);
			partService.activate(part);
			if (descriptor.getPersistedState().containsKey(ONTOP_ID)
					&& Boolean.toString(true).equalsIgnoreCase(descriptor.getPersistedState().get(ONTOP_ID)))
			{
				ontops.add(part);
			}
		}

		//reactivate ontop parts to ensure they are on-top
		for (MPart ontop : ontops)
		{
			partService.activate(ontop);
		}
	}

	public static void addPartToAppropriateContainer(MPart part, MPartDescriptor descriptor, MApplication application,
			EModelService service)
	{
		//first try and find the container specified by the id in the "parent" persisted state
		MElementContainer<MUIElement> container = null;
		String parentId = descriptor.getPersistedState().get(PARENT_ID);
		if (parentId != null)
		{
			@SuppressWarnings("rawtypes")
			List<MElementContainer> containers =
					service.findElements(application, parentId, MElementContainer.class, null);
			if (!containers.isEmpty())
			{
				@SuppressWarnings("unchecked")
				MElementContainer<MUIElement> uncheckedContainer = containers.get(0);
				container = uncheckedContainer;
			}
		}
		//next try and find a sibling, and use the sibling's container
		if (container == null)
		{
			List<MPart> siblings = service.findElements(application, descriptor.getElementId(), MPart.class, null);
			for (MPart sibling : siblings)
			{
				if (sibling == part)
				{
					continue;
				}
				container = sibling.getParent();
				if (sibling.isToBeRendered())
				{
					//prefer visible siblings
					break;
				}
			}
		}
		if (container != null)
		{
			String position = descriptor.getPersistedState().get(POSITION_ID);
			int index = getPositionIndex(position, container);
			if (index < 0 || index > container.getChildren().size())
			{
				container.getChildren().add(part);
			}
			else
			{
				container.getChildren().add(index, part);
			}
		}
	}

	public static int getPositionIndex(String positionInList, MElementContainer<MUIElement> container)
	{
		if (positionInList == null || positionInList.trim().length() == 0)
		{
			return -1;
		}
		PositionInfo posInfo = PositionInfo.parse(positionInList);
		if (posInfo == null)
		{
			return -1;
		}
		switch (posInfo.getPosition())
		{
		case FIRST:
			return 0;

		case INDEX:
			return posInfo.getPositionReferenceAsInteger();

		case BEFORE:
		case AFTER:
			String elementId = posInfo.getPositionReference();
			List<MUIElement> siblings = container.getChildren();
			for (int i = 0; i < siblings.size(); i++)
			{
				MUIElement sibling = siblings.get(i);
				if (elementId.equals(sibling.getElementId()))
				{
					return posInfo.getPosition() == Position.BEFORE ? i : i + 1;
				}
			}

		case LAST:
		default:
			return -1;
		}
	}
}