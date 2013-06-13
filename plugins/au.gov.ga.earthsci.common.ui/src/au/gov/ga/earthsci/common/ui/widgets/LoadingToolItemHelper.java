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
package au.gov.ga.earthsci.common.ui.widgets;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ToolItem;

import au.gov.ga.earthsci.common.ui.util.ILoadingIconFrameListener;
import au.gov.ga.earthsci.common.ui.util.LoadingIconAnimator;

/**
 * ToolItem helper class that can show an animated loading icon as its image.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LoadingToolItemHelper implements ILoadingIconFrameListener
{
	private final ToolItem item;
	private final Object semaphore = new Object();
	private boolean loading = false;
	private Image backupImage;

	public LoadingToolItemHelper(ToolItem item)
	{
		this.item = item;
	}

	public ToolItem getItem()
	{
		return item;
	}

	public boolean isLoading()
	{
		return loading;
	}

	public void setLoading(boolean loading)
	{
		synchronized (semaphore)
		{
			if (loading != this.loading)
			{
				if (loading)
				{
					LoadingIconAnimator.get().addListener(this);
					backupImage = item.getImage();
				}
				else
				{
					LoadingIconAnimator.get().removeListener(this);
					item.setImage(backupImage);
				}
			}
			this.loading = loading;
		}
	}

	@Override
	public void nextFrame(final Image image)
	{
		synchronized (semaphore)
		{
			if (loading)
			{
				item.getDisplay().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						synchronized (semaphore)
						{
							if (loading)
							{
								item.setImage(image);
							}
						}
					}
				});
			}
		}
	}
}
