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
package au.gov.ga.earthsci.application.parts.layer;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.application.LoadingIconAnimator;
import au.gov.ga.earthsci.application.LoadingIconFrameListener;
import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerNode;
import au.gov.ga.earthsci.core.retrieve.RetrievalJob;
import au.gov.ga.earthsci.core.retrieve.RetrievalServiceFactory;

/**
 * Label provider for the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerTreeLabelProvider extends ObservableMapLabelProvider
{
	private final IconLoader iconLoader = new IconLoader();
	private static final Logger logger = LoggerFactory.getLogger(LayerTreeLabelProvider.class);

	public LayerTreeLabelProvider(IObservableMap[] attributeMaps)
	{
		super(attributeMaps);
	}

	@Override
	public void dispose()
	{
		super.dispose();
		iconLoader.dispose();
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		if (element instanceof LayerNode)
		{
			LayerNode layer = (LayerNode) element;
			String label = layer.getLabelOrName();
			if (layer.getOpacity() < 1)
			{
				label += String.format(" (%d%%)", (int) (layer.getOpacity() * 100)); //$NON-NLS-1$
			}
			return label;
		}
		else if (element instanceof FolderNode)
		{
			FolderNode folder = (FolderNode) element;
			return folder.getName();
		}
		return super.getColumnText(element, columnIndex);
	}

	@Override
	public Image getImage(Object element)
	{
		if (element instanceof ILayerTreeNode)
		{
			ILayerTreeNode layer = (ILayerTreeNode) element;
			URL imageURL = layer.getIconURL();
			if (imageURL != null)
			{
				return iconLoader.getImage(element, imageURL);
			}
		}
		return super.getImage(element);
	}

	private class IconLoader implements LoadingIconFrameListener
	{
		private final ImageRegistry imageRegistry = new ImageRegistry();
		private final Set<Object> loadingElements = new HashSet<Object>();
		private final Set<Object> errorElements = new HashSet<Object>();
		private final Object semaphore = new Object();

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

		private void scheduleRetrievalJob(final Object element, final URL url)
		{
			final RetrievalJob job = RetrievalServiceFactory.getServiceInstance().retrieve(url);
			job.addJobChangeListener(new JobChangeAdapter()
			{
				@Override
				public void done(IJobChangeEvent event)
				{
					synchronized (semaphore)
					{
						setLoading(element, false);
						boolean success = false;
						if (job.getRetrievalResult().hasData())
						{
							try
							{
								Image image =
										new Image(Display.getDefault(), job.getRetrievalResult().getAsInputStream());
								setImageForURL(url, image);
								success = true;
							}
							catch (Exception e)
							{
								logger.error("Error loading image from " + url, e); //$NON-NLS-1$
							}
						}
						if (!success)
						{
							errorElements.add(element);
						}

						Display.getDefault().asyncExec(new Runnable()
						{
							@Override
							public void run()
							{
								fireLabelProviderChanged(new LabelProviderChangedEvent(LayerTreeLabelProvider.this,
										element));
							}
						});
					}
				}
			});
			job.schedule();
		}

		private Image getImageForURL(URL url)
		{
			synchronized (semaphore)
			{
				return imageRegistry.get(url.toString());
			}
		}

		private void setImageForURL(URL url, Image image)
		{
			imageRegistry.put(url.toString(), image);
		}

		private boolean isLoading(Object element)
		{
			synchronized (semaphore)
			{
				return loadingElements.contains(element);
			}
		}

		private void setLoading(Object element, boolean loading)
		{
			synchronized (semaphore)
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
							fireLabelProviderChanged(new LabelProviderChangedEvent(LayerTreeLabelProvider.this,
									loadingElementsArray));
						}
					});
				}
			}
		}
	}
}
