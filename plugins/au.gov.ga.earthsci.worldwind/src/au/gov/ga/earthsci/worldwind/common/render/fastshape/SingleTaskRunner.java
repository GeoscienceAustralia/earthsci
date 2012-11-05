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
package au.gov.ga.earthsci.worldwind.common.render.fastshape;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Helper class for running tasks. Contains a number of threads that accepts
 * runnables and their owner. Ensures that only one runnable from each owner
 * exists in the runnable queue.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SingleTaskRunner
{
	private BlockingQueue<OwnerRunnable> queue = new LinkedBlockingQueue<OwnerRunnable>();
	private Set<OwnerRunnable> set = Collections.synchronizedSet(new HashSet<OwnerRunnable>());
	private final int THREAD_COUNT = 2;

	public SingleTaskRunner(String threadName)
	{
		for (int i = 0; i < THREAD_COUNT; i++)
		{
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					while (true)
					{
						try
						{
							OwnerRunnable or = queue.take();
							or.runnable.run();
							set.remove(or);
						}
						catch (Throwable t)
						{
							t.printStackTrace();
						}
					}
				}
			});
			thread.setName(threadName + " " + i);
			thread.setDaemon(true);
			thread.start();
		}
	}

	public synchronized boolean run(Object owner, Runnable runnable)
	{
		OwnerRunnable or = new OwnerRunnable(owner, runnable);
		if (!set.contains(or))
		{
			set.add(or);
			queue.add(or);
			return true;
		}
		return false;
	}

	/**
	 * Helper class that associates a {@link Runnable} and it's owner.
	 */
	public static class OwnerRunnable
	{
		public final Object owner;
		public final Runnable runnable;

		public OwnerRunnable(Object owner, Runnable runnable)
		{
			this.owner = owner;
			this.runnable = runnable;
		}

		@Override
		public int hashCode()
		{
			return owner.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			if (owner.equals(obj))
			{
				return true;
			}
			if (obj instanceof OwnerRunnable && owner.equals(((OwnerRunnable) obj).owner))
			{
				return true;
			}
			return super.equals(obj);
		}
	}
}