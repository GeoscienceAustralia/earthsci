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

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.core.retrieve.IRetrieval;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.RetrievalAdapter;
import au.gov.ga.earthsci.core.retrieve.RetrievalServiceFactory;

/**
 * Helper class which provides the ability to load icons for structured viewers,
 * and show the loading icon in the meantime.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class IconLoader implements ILoadingIconFrameListener
{
	private static final Logger logger = LoggerFactory.getLogger(IconLoader.class);

	private final IFireableLabelProvider labelProvider;
	private final ImageRegistry imageRegistry = new ImageRegistry();
	private final Set<Object> loadingElements = new HashSet<Object>();
	private final Set<Object> errorElements = new HashSet<Object>();
	private final Map<URL, Set<Object>> urlElements = new HashMap<URL, Set<Object>>();
	private final Object semaphore = new Object();

	public IconLoader(IFireableLabelProvider labelProvider)
	{
		this.labelProvider = labelProvider;
	}

	/**
	 * Retrieve an icon for a viewer element from the given url. If the url
	 * hasn't yet been retrieved, a frame of the loading icon will be returned.
	 * If the url could not be retrieved, an error icon is returned.
	 * 
	 * @param element
	 *            Viewer element
	 * @param url
	 *            Icon url
	 * @return Icon to be displayed
	 */
	public Image getImage(final Object element, URL url)
	{
		synchronized (semaphore)
		{
			//first check the registry to see if the image has been loaded yet
			Image image = getImageForURL(url);
			if (image != null)
			{
				setLoading(element, false);
				return image;
			}

			//if an error occured when loading the icon, return the error icon
			if (errorElements.contains(element))
			{
				return au.gov.ga.earthsci.application.ImageRegistry.getInstance().get(
						au.gov.ga.earthsci.application.ImageRegistry.ICON_ERROR);
			}

			//if its not already loading, begin loading
			if (!isLoading(element))
			{
				scheduleRetrievalJob(element, url);
				setLoading(element, true);
			}

			return LoadingIconAnimator.get().getCurrentFrame();
		}
	}

	public void dispose()
	{
		imageRegistry.dispose();
	}

	private void scheduleRetrievalJob(Object element, final URL url)
	{
		if (!urlElements.containsKey(url))
		{
			urlElements.put(url, new HashSet<Object>());
			final IRetrieval retrieval = RetrievalServiceFactory.getServiceInstance().retrieve(this, url);
			retrieval.addListener(new RetrievalAdapter()
			{
				@Override
				public void cached(IRetrieval retrieval)
				{
					retrievalDone(retrieval.getData(), url);
				}

				@Override
				public void complete(IRetrieval retrieval)
				{
					if (retrieval.hasResult() && !retrieval.getResult().isFromCache())
					{
						retrievalDone(retrieval.getData(), url);
					}
				}
			});
			retrieval.start();
		}
		urlElements.get(url).add(element);
	}

	private void retrievalDone(IRetrievalData data, URL url)
	{
		synchronized (semaphore)
		{
			final Set<Object> elements = urlElements.remove(url);
			for (Object element : elements)
			{
				setLoading(element, false);
			}
			boolean success = false;
			if (data != null)
			{
				try
				{
					InputStream is = data.getInputStream();
					try
					{
						Image image = new Image(Display.getDefault(), is);
						setImageForURL(url, image);
						success = true;
					}
					finally
					{
						is.close();
					}
				}
				catch (Exception e)
				{
					logger.error("Error loading image from " + url, e); //$NON-NLS-1$
				}
			}
			if (!success)
			{
				errorElements.addAll(elements);
			}

			Display.getDefault().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					labelProvider.fireLabelProviderChanged(new LabelProviderChangedEvent(labelProvider, elements
							.toArray()));
				}
			});
		}
	}

	private Image getImageForURL(URL url)
	{
		return imageRegistry.get(url.toString());
	}

	private void setImageForURL(URL url, Image image)
	{
		imageRegistry.put(url.toString(), image);
	}

	private boolean isLoading(Object element)
	{
		return loadingElements.contains(element);
	}

	private void setLoading(Object element, boolean loading)
	{
		if (loading)
		{
			boolean wasEmpty = loadingElements.isEmpty();
			loadingElements.add(element);
			if (wasEmpty)
			{
				LoadingIconAnimator.get().addListener(this);
			}
		}
		else
		{
			loadingElements.remove(element);
			boolean isEmpty = loadingElements.isEmpty();
			if (isEmpty)
			{
				LoadingIconAnimator.get().removeListener(this);
			}
		}
	}

	@Override
	public void nextFrame(Image image)
	{
		synchronized (semaphore)
		{
			if (!loadingElements.isEmpty())
			{
				final Object[] loadingElementsArray = loadingElements.toArray(new Object[loadingElements.size()]);
				Display.getDefault().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						labelProvider.fireLabelProviderChanged(new LabelProviderChangedEvent(labelProvider,
								loadingElementsArray));
					}
				});
			}
		}
	}
}