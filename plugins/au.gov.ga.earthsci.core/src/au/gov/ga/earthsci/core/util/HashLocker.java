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
package au.gov.ga.earthsci.core.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class that provides the ability to synchronize on an Object. The
 * difference between using this and locking on the Object itself is that
 * Objects with the same hashCode will share the same lock.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HashLocker
{
	private final Map<Object, Object> locked = new HashMap<Object, Object>();

	public void lock(Object key)
	{
		Object lock;
		synchronized (locked)
		{
			lock = locked.get(key);
			if (lock == null)
			{
				locked.put(key, new Object());
				return;
			}
		}

		//wait for the lock to be unlocked
		synchronized (lock)
		{
			try
			{
				lock.wait();
			}
			catch (InterruptedException e)
			{
			}
		}

		//try again
		lock(key);
	}

	public void unlock(Object key)
	{
		synchronized (locked)
		{
			Object lock = locked.remove(key);
			if (lock != null)
			{
				synchronized (lock)
				{
					//only need to wake one waiting thread
					lock.notify();
				}
			}
		}
	}
}
