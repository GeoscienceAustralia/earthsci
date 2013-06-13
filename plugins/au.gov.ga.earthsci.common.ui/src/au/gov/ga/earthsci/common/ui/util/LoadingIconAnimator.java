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
package au.gov.ga.earthsci.common.ui.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a set of {@link Image}s that, when animated, appear as a loading
 * icon.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public final class LoadingIconAnimator
{
	private static final LoadingIconAnimator INSTANCE = new LoadingIconAnimator();

	public static LoadingIconAnimator get()
	{
		return INSTANCE;
	}

	private static final Logger logger = LoggerFactory.getLogger(LoadingIconAnimator.class);
	private final Image[] loadingFrames;
	private int frame = 0;
	private final Set<ILoadingIconFrameListener> listeners = new HashSet<ILoadingIconFrameListener>();
	private boolean dirty = false;
	private ILoadingIconFrameListener[] listenerArray;

	private LoadingIconAnimator()
	{
		Image[] loadingFramesLocal = null;
		InputStream stream = getClass().getResourceAsStream("/icons/loading.gif"); //$NON-NLS-1$
		try
		{
			loadingFramesLocal = SWTUtil.loadAnimatedImage(Display.getDefault(), stream);
		}
		catch (IOException e)
		{
			logger.error("Error reading loading icon", e); //$NON-NLS-1$
		}
		finally
		{
			try
			{
				stream.close();
			}
			catch (Exception e)
			{
			}
		}
		this.loadingFrames = loadingFramesLocal;

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					synchronized (listeners)
					{
						if (listeners.isEmpty())
						{
							try
							{
								listeners.wait();
							}
							catch (InterruptedException e)
							{
							}
						}

						if (dirty)
						{
							listenerArray = listeners.toArray(new ILoadingIconFrameListener[listeners.size()]);
							dirty = false;
						}
					}
					for (ILoadingIconFrameListener listener : listenerArray)
					{
						try
						{
							listener.nextFrame(getCurrentFrame());
						}
						catch (Exception e)
						{
							logger.warn("Error calling loading icon frame listener", e); //$NON-NLS-1$
						}
					}
					frame = (frame + 1) % loadingFrames.length;
					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
		});
		thread.setName("Loading icon animator"); //$NON-NLS-1$
		thread.setDaemon(true);
		thread.start();
	}

	public void addListener(ILoadingIconFrameListener listener)
	{
		synchronized (listeners)
		{
			listeners.add(listener);
			dirty = true;
			listeners.notifyAll();
		}
	}

	public void removeListener(ILoadingIconFrameListener listener)
	{
		synchronized (listeners)
		{
			listeners.remove(listener);
			dirty = true;
		}
	}

	public Image getCurrentFrame()
	{
		return loadingFrames[frame];
	}
}
