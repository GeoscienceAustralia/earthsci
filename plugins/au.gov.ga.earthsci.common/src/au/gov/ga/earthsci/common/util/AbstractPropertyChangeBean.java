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
package au.gov.ga.earthsci.common.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract {@link IPropertyChangeBean} implementation, using a
 * {@link PropertyChangeSupport} object for implementation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractPropertyChangeBean implements IPropertyChangeBean
{
	protected final transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	@Override
	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
	{
		changeSupport.firePropertyChange(propertyChangeEvent);
	}

	@Override
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		firePropertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		changeSupport.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		changeSupport.removePropertyChangeListener(listener);
	}

	@Override
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}
}
