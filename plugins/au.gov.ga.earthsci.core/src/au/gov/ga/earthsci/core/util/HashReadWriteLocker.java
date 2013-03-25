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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Provides a ReadWriteLock for objects via their hash. Allows you to lock an
 * object for reading/writing, and any other objects with the same hash will
 * share the lock.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HashReadWriteLocker
{
	private final Map<Object, ReadWriteLock> locks = new HashMap<Object, ReadWriteLock>();
	private final Map<Object, Integer> lockCount = new HashMap<Object, Integer>();

	public void lockRead(Object key)
	{
		getLock(key, true).readLock().lock();
	}

	public void lockWrite(Object key)
	{
		getLock(key, true).writeLock().lock();
	}

	public void unlockRead(Object key)
	{
		getLock(key, false).readLock().unlock();
	}

	public void unlockWrite(Object key)
	{
		getLock(key, false).writeLock().unlock();
	}

	private ReadWriteLock getLock(Object key, boolean forAquire)
	{
		synchronized (locks)
		{
			ReadWriteLock lock = locks.get(key);
			Integer count = lockCount.get(key);
			if (lock == null)
			{
				if (!forAquire)
				{
					throw new IllegalStateException("Attempting to unlock a non-existant lock"); //$NON-NLS-1$
				}
				lock = new ReentrantReadWriteLock();
				count = 0;
				locks.put(key, lock);
			}
			count = count + (forAquire ? 1 : -1);
			if (count == 0)
			{
				locks.remove(key);
				lockCount.remove(key);
			}
			else
			{
				lockCount.put(key, count);
			}
			return lock;
		}
	}
}
