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
package au.gov.ga.earthsci.worldwind.common.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A {@link ReadWriteLock} that behaves the same as a {@link ReentrantReadWriteLock},
 * with the exception that calls to <code>readLock().lock()</code> will not block if 
 * the thread holds the <code>writeLock</code>.
 * <p/>
 * Essentially treats the <code>writeLock</code> as the 'master lock' and permits reads and 
 * writes to the thread that holds the lock.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LenientReadWriteLock implements ReadWriteLock
{
	private ReentrantReadWriteLock lockDelegate;
	
	private Lock readLock = new Lock()
	{

		@Override
		public void lock()
		{
			if (lockDelegate.isWriteLockedByCurrentThread())
			{
				return;
			}
			lockDelegate.readLock().lock();
		}

		@Override
		public void lockInterruptibly() throws InterruptedException
		{
			lockDelegate.readLock().lockInterruptibly();
		}

		@Override
		public boolean tryLock()
		{
			return lockDelegate.readLock().tryLock();
		}

		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException
		{
			return lockDelegate.readLock().tryLock(time, unit);
		}

		@Override
		public void unlock()
		{
			if (lockDelegate.getReadHoldCount() > 0)
			{
				lockDelegate.readLock().unlock();
			}
		}

		@Override
		public Condition newCondition()
		{
			return lockDelegate.readLock().newCondition();
		}
	};
	
	public LenientReadWriteLock()
	{
		lockDelegate = new ReentrantReadWriteLock(true);
	}
	
	@Override
	public Lock readLock()
	{
		return readLock;
	}

	@Override
	public Lock writeLock()
	{
		return lockDelegate.writeLock();
	}
	
}
