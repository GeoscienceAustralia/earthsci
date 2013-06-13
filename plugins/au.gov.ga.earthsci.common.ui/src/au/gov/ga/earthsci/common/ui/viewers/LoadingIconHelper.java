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
package au.gov.ga.earthsci.common.ui.viewers;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.common.ui.util.ILoadingIconFrameListener;
import au.gov.ga.earthsci.common.ui.util.LoadingIconAnimator;

/**
 * Helper class for getting the animated loading icon for loading tree nodes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LoadingIconHelper
{
	private final IFireableLabelProvider labelProvider;
	private final Set<Object> loadingElements = new HashSet<Object>();
	private boolean listenerAdded = false;
	private final ILoadingIconFrameListener listener = new ILoadingIconFrameListener()
	{
		@Override
		public void nextFrame(Image image)
		{
			synchronized (loadingElements)
			{
				final Object[] array = loadingElements.toArray();
				Display.getDefault().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						labelProvider.fireLabelProviderChanged(new LabelProviderChangedEvent(labelProvider, array));
					}
				});
				LoadingIconAnimator.get().removeListener(this);
				listenerAdded = false;
				loadingElements.clear();
			}
		}
	};

	public LoadingIconHelper(IFireableLabelProvider labelProvider)
	{
		this.labelProvider = labelProvider;
	}

	/**
	 * Get the current loading icon frame to show for the given element.
	 * <p/>
	 * This element will be passed to the
	 * {@link IFireableLabelProvider#fireLabelProviderChanged(LabelProviderChangedEvent)}
	 * method when the next loading frame is ready to tell the label provider to
	 * reload the element. This only happens once; when the label provider
	 * reloads the element it can call this method again for register for the
	 * next frame.
	 * 
	 * @param element
	 *            Element that is loading (eg a tree node)
	 * @return Current loading icon frame
	 */
	public Image getLoadingIcon(final Object element)
	{
		synchronized (loadingElements)
		{
			if (!listenerAdded)
			{
				LoadingIconAnimator.get().addListener(listener);
				listenerAdded = true;
			}
			loadingElements.add(element);
		}
		return LoadingIconAnimator.get().getCurrentFrame();
	}
}
