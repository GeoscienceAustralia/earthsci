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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.application.intent.HtmlIntentHandler;
import au.gov.ga.earthsci.application.intent.HttpIntentHandler;
import au.gov.ga.earthsci.eclipse.extras.browser.BrowserViewer;
import au.gov.ga.earthsci.intent.AbstractIntentCallback;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.IIntentFilterSelectionPolicy;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.intent.IntentFilter;
import au.gov.ga.earthsci.intent.IntentManager;
import au.gov.ga.earthsci.intent.dispatch.Dispatcher;

/**
 * Displays a web browser.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BrowserPart
{
	public static final String PART_ID = "au.gov.ga.earthsci.application.browser.part"; //$NON-NLS-1$
	public static final String INPUT_NAME = PART_ID + ".input"; //$NON-NLS-1$
	public static final String PERSISTED_URL_KEY = PART_ID + ".persistedUrl"; //$NON-NLS-1$

	@Inject
	private IEclipseContext context;

	private BrowserViewer viewer;

	@Inject
	private EPartService partService;

	@Inject
	private EModelService modelService;

	@Inject
	private MWindow window;

	@Inject
	private MPart part;

	private boolean dontStartIntent = false;

	@PostConstruct
	public void init(final Composite parent)
	{
		context.set(BrowserPart.class, this);

		viewer =
				new BrowserViewer(parent, BrowserViewer.BUTTON_BAR | BrowserViewer.LOCATION_BAR
						| BrowserViewer.DISABLE_NEW_WINDOW);
		viewer.getBrowser().addLocationListener(new LocationAdapter()
		{
			@Override
			public void changing(LocationEvent event)
			{
				if (!dontStartIntent)
				{
					startIntent(parent, event.location);
				}
				dontStartIntent = false;
				part.getPersistedState().put(PERSISTED_URL_KEY, event.location);
			}
		});
		viewer.getBrowser().addOpenWindowListener(new OpenWindowListener()
		{
			@Override
			public void open(WindowEvent event)
			{
				MPart part = BrowserPart.showPart(partService, modelService, window, true);
				Object object = part.getObject();
				if (object instanceof BrowserPart)
				{
					BrowserPart browserPart = (BrowserPart) object;
					event.browser = browserPart.viewer.getBrowser();
				}
				else
				{
					partService.hidePart(part, true);
				}
			}
		});

		String persistedUrl = part.getPersistedState().get(PERSISTED_URL_KEY);
		if (persistedUrl != null)
		{
			try
			{
				dontStartIntent = true;
				URL url = new URL(persistedUrl);
				setPartInput(url);
			}
			catch (MalformedURLException e)
			{
			}
		}
	}

	@PreDestroy
	public void dispose()
	{
		context.remove(BrowserPart.class);
	}

	protected void startIntent(final Composite parent, String location)
	{
		//Raise an "optional" intent for event.location. For example, if it is a WMS
		//capabilities document, the user can optionally open it as a catalog or layer.
		URI uri;
		try
		{
			uri = new URI(location);
		}
		catch (URISyntaxException e)
		{
			return;
		}
		Intent intent = new Intent();
		intent.setURI(uri);
		IIntentFilterSelectionPolicy filterSelectionPolicy = new IIntentFilterSelectionPolicy()
		{
			@Override
			public boolean allowed(Intent intent, IntentFilter filter)
			{
				//don't allow the filter to be the one that opens in the browser, causing a looping intent
				return !(HtmlIntentHandler.class.equals(filter.getHandler()) || HttpIntentHandler.class
						.equals(filter.getHandler()));
			}
		};
		IIntentCallback callback = new AbstractIntentCallback()
		{
			@Override
			public boolean filters(List<IntentFilter> filters, Intent intent)
			{
				if (filters.isEmpty())
				{
					return false;
				}
				final boolean[] result = new boolean[1];
				if (!parent.isDisposed())
				{
					parent.getDisplay().syncExec(new Runnable()
					{
						@Override
						public void run()
						{
							result[0] = MessageDialog.openQuestion(parent.getShell(), "Open URL",
									"This URL can be handled by EarthSci. Would you like to continue?");
						}
					});
				}
				return result[0];
			}

			@Override
			public void error(Exception e, Intent intent)
			{
			}

			@Override
			public void completed(Object result, Intent intent)
			{
				if (result != null)
				{
					Dispatcher.getInstance().dispatch(result, context);
				}
			}
		};
		IntentManager.getInstance().start(intent, filterSelectionPolicy, false, callback, context);
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

	public static MPart showPart(EPartService partService, EModelService modelService, MWindow window, boolean newPart)
	{
		MPart part = null;

		if (!newPart)
		{
			List<MPart> reuse = modelService.findElements(window, PART_ID, MPart.class, null);
			if (!reuse.isEmpty())
			{
				part = reuse.get(reuse.size() - 1);
			}
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
