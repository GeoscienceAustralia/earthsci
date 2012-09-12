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
package au.gov.ga.earthsci.newt.swt;

import javax.media.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.Animator;

/**
 * Animator subclass which animates for a certain period of time, and then
 * pauses itself until the next time {@link #resume()} is called.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TimedAnimator extends Animator
{
	protected int period = 1000; //ms
	protected PeriodTimer timer;

	public TimedAnimator()
	{
		super();
	}

	public TimedAnimator(GLAutoDrawable drawable)
	{
		super(drawable);
	}

	public TimedAnimator(ThreadGroup tg, GLAutoDrawable drawable)
	{
		super(tg, drawable);
	}

	public TimedAnimator(ThreadGroup tg)
	{
		super(tg);
	}

	public int getPeriod()
	{
		return period;
	}

	public void setPeriod(int period)
	{
		this.period = period;
	}

	@Override
	public synchronized boolean start()
	{
		pauseAfterPeriod();
		return super.start();
	}

	@Override
	public synchronized boolean resume()
	{
		pauseAfterPeriod();
		return super.resume();
	}

	protected void pauseAfterPeriod()
	{
		if (timer == null)
		{
			timer = new PeriodTimer(period);
		}
		else
		{
			timer.pauseIn(period);
		}
	}

	protected class PeriodTimer implements Runnable
	{
		private final static long msToNs = 1000000l;
		protected Long pauseTime;

		public PeriodTimer(long delay)
		{
			pauseIn(delay);
			Thread thread = new Thread(threadGroup, this, "Animator pauser"); //$NON-NLS-1$
			thread.setDaemon(true);
			thread.start();
		}

		public void pauseIn(long delay)
		{
			synchronized (this)
			{
				long currentNanos = System.nanoTime();
				pauseTime = currentNanos + delay * msToNs;
				notifyAll();
			}
		}

		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					long currentNanos = System.nanoTime();
					long sleepTime;
					synchronized (this)
					{
						sleepTime = pauseTime == null ? -1 : pauseTime - currentNanos;
						sleepTime /= msToNs;
						if (sleepTime <= 0)
						{
							pause();
							wait();
						}
					}
					if (sleepTime > 0)
					{
						Thread.sleep(sleepTime);
					}
				}
				catch (InterruptedException e)
				{
				}
			}
		}
	}
}
