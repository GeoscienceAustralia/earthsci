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
package au.gov.ga.earthsci.application.parts.browser;

import java.net.URL;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.eclipse.extras.browser.BrowserViewer;

/**
 * Displays a web browser.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BrowserPart
{
	public static final String PART_ID = "au.gov.ga.earthsci.application.browser.part"; //$NON-NLS-1$
	public static final String INPUT_NAME = PART_ID + ".input"; //$NON-NLS-1$

	@Inject
	private IEclipseContext context;

	private BrowserViewer viewer;

	@PostConstruct
	public void init(final Composite parent)
	{
		context.set(BrowserPart.class, this);

		viewer = new BrowserViewer(parent, BrowserViewer.BUTTON_BAR | BrowserViewer.LOCATION_BAR);
	}

	@PreDestroy
	public void dispose()
	{
		context.remove(BrowserPart.class);
	}

	public String getURL()
	{
		return viewer.getURL();
	}

	@Inject
	@Optional
	private void setPartInput(@Named(INPUT_NAME) URL url)
	{
		viewer.setURL(url.toString());
	}

	public static MPart showPart(EPartService partService, EModelService modelService, MWindow window)
	{
		MPart part = null;

		List<MPart> reuse = modelService.findElements(window, PART_ID, MPart.class, null);
		if (!reuse.isEmpty())
		{
			part = reuse.get(reuse.size() - 1);
		}

		if (part == null)
		{
			//create the part from the PartDescriptor
			part = partService.createPart(PART_ID);

			//if there's a placeholder for the part, put it there
			List<MPlaceholder> placeholders = modelService.findElements(window, PART_ID, MPlaceholder.class, null);
			if (!placeholders.isEmpty())
			{
				MPlaceholder placeholder = placeholders.get(0);
				List<MUIElement> siblings = placeholder.getParent().getChildren();
				int index = siblings.indexOf(placeholder);
				siblings.add(index, part);
			}
		}

		//show and return the part
		return partService.showPart(part, PartState.ACTIVATE);
	}
}
