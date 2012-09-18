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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Represents a bean that supports listening to with
 * {@link PropertyChangeListener}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IPropertyChangeBean
{
	/**
	 * Fire the given {@link PropertyChangeEvent} to this bean's listeners.
	 * 
	 * @param propertyChangeEvent
	 */
	void firePropertyChange(PropertyChangeEvent propertyChangeEvent);

	/**
	 * Fire a property change to this bean's listeners. Creates a new
	 * {@link PropertyChangeEvent}. Should only be fired if the value changes.
	 * 
	 * @param propertyName
	 * @param oldValue
	 * @param newValue
	 */
	void firePropertyChange(String propertyName, Object oldValue, Object newValue);

	/**
	 * Add a {@link PropertyChangeListener} to this bean.
	 * 
	 * @param listener
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Remove the specified {@link PropertyChangeListener} from this bean.
	 * 
	 * @param listener
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Add a {@link PropertyChangeListener} to this bean, listening on the given
	 * property.
	 * 
	 * @param propertyName
	 * @param listener
	 */
	void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

	/**
	 * Remove the given {@link PropertyChangeListener} listening on the given
	 * property from this bean.
	 * 
	 * @param propertyName
	 * @param listener
	 */
	void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
}
