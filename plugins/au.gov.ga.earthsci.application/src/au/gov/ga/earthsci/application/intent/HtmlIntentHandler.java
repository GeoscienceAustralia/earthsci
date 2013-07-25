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
package au.gov.ga.earthsci.application.intent;

import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.application.parts.browser.BrowserPart;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.IIntentHandler;
import au.gov.ga.earthsci.intent.Intent;

/**
 * Intent handler for any intent that has a HTML content type.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HtmlIntentHandler implements IIntentHandler
{
	private static final Logger logger = LoggerFactory.getLogger(HtmlIntentHandler.class);

	@Inject
	private EPartService partService;

	@Inject
	private EModelService modelService;

	@Inject
	private MWindow window;

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Override
	public void handle(final Intent intent, IIntentCallback callback)
	{
		try
		{
			final URL url = intent.getURL();
			if (url != null)
			{
				shell.getDisplay().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						MPart part = BrowserPart.showPart(partService, modelService, window);
						part.getContext().modify(BrowserPart.INPUT_NAME, url);
						part.getContext().declareModifiable(BrowserPart.INPUT_NAME);
					}
				});
			}
		}
		catch (Exception e)
		{
			logger.error("Error loading intent Browser content", e); //$NON-NLS-1$
		}
		callback.completed(null, intent);
	}
}
