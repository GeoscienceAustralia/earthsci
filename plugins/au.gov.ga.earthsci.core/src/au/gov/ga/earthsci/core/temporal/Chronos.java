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
package au.gov.ga.earthsci.core.temporal;

import java.util.LinkedHashSet;
import java.util.concurrent.locks.ReadWriteLock;

import javax.inject.Singleton;

import au.gov.ga.earthsci.core.util.AbstractPropertyChangeBean;
import au.gov.ga.earthsci.core.util.Validate;
import au.gov.ga.earthsci.worldwind.common.util.LenientReadWriteLock;

/**
 * The central time keeper/server in the EarthSci platform.
 * <p/>
 * The current application time can be set and accessed through this class. Listeners
 * can also be attached using the standard property change mechanism to listen
 * for changes to the {@value #CURRENT_TIME_PROPERTY_NAME} property. 
 * <p/>
 * This class also maintains a collection of {@link ITemporal} objects and notifies them of changes to
 * the current application time. {@link ITemporal} objects will be notified of changes to current time 
 * <em>before</em> other listeners that have been registered via the property change mechanism.
 * <p/>
 * This class is intended to be used as singleton via the DI mechanism - there should only 
 * exist one instance per application. All public methods are threadsafe and can be executed concurrently
 * with well defined behaviour.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Singleton
public class Chronos extends AbstractPropertyChangeBean 
{

	/** The name of the property event issued when the current time is changed */
	public static final String CURRENT_TIME_PROPERTY_NAME = "currentTime"; //$NON-NLS-1$
	
	private LinkedHashSet<ITemporal> temporalObjects = new LinkedHashSet<ITemporal>();
	private ReadWriteLock temporalObjectsLock = new LenientReadWriteLock();
	
	private BigTime currentTime = BigTime.now();
	
	/**
	 * Add the provided temporal object to the central time server. The object will receive future
	 * notifications about changes to the current time.
	 * 
	 * @param t The temporal object to add
	 */
	public void addTemporal(ITemporal t)
	{
		if (t == null)
		{
			return;
		}
		try
		{
			temporalObjectsLock.writeLock().lock();
			temporalObjects.add(t);
		}
		finally
		{
			temporalObjectsLock.writeLock().unlock();
		}
	}
	
	/**
	 * Remove the provided temporal object from the central time server, if it exists. The object
	 * will receive no further notifications about changes to current time.
	 * 
	 * @param t The temporal object to remove
	 */
	public void removeTemporal(ITemporal t)
	{
		try
		{
			temporalObjectsLock.writeLock().lock();
			temporalObjects.remove(t);
		}
		finally
		{
			temporalObjectsLock.writeLock().unlock();
		}
	}
	
	/**
	 * Set the current application time. All registered temporal objects and listeners will be
	 * notified of the change in time.
	 *  
	 * @param currentTime the currentTime to set
	 */
	public synchronized void setCurrentTime(BigTime currentTime)
	{
		Validate.notNull(currentTime, "A time instant is required"); //$NON-NLS-1$
		
		BigTime oldTime = currentTime;
		this.currentTime = currentTime;
		
		if (!oldTime.equals(currentTime))
		{
			notifyTemporalObjectsOfChangedTime();
			firePropertyChange(CURRENT_TIME_PROPERTY_NAME, oldTime, currentTime);
		}
	}
	
	/**
	 * @return the current application time
	 */
	public synchronized BigTime getCurrentTime()
	{
		return currentTime;
	}
	
	private void notifyTemporalObjectsOfChangedTime()
	{
		try
		{
			temporalObjectsLock.readLock().lock();
			for (ITemporal t : temporalObjects)
			{
				t.apply(currentTime);
			}
		}
		finally
		{
			temporalObjectsLock.readLock().unlock();
		}
	}
}
