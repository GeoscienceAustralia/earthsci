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

/**
 * Represents a bean that supports listening to with
 * {@link PropertyChangeListener}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface ITreePropertyChangeBean extends IPropertyChangeBean
{
	/**
	 * @return This bean's tree parent, or null if this is the root.
	 */
	ITreePropertyChangeBean getParent();

	/**
	 * Fire an existing PropertyChangeEvent to any registered descendant
	 * listeners. No event is fired if the given event's old and new values are
	 * equal and non-null.
	 * 
	 * @param evt
	 *            The PropertyChangeEvent object.
	 */
	void fireAscendantPropertyChange(PropertyChangeEvent propertyChangeEvent);

	/**
	 * Add a PropertyChangeListener to the listener list for changes on this
	 * bean or any of its descendants. The listener is registered for all
	 * properties. The same listener object may be added more than once, and
	 * will be called as many times as it is added. If <code>listener</code> is
	 * null, no exception is thrown and no action is taken.
	 * 
	 * @param listener
	 *            The PropertyChangeListener to be added
	 */
	void addDescendantPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Add a PropertyChangeListener for a specific property change on this bean
	 * or any of its descendants. The listener will be invoked only when a call
	 * on firePropertyChange names that specific property. The same listener
	 * object may be added more than once. For each property, the listener will
	 * be invoked the number of times it was added for that property. If
	 * <code>propertyName</code> or <code>listener</code> is null, no exception
	 * is thrown and no action is taken.
	 * 
	 * @param propertyName
	 *            The name of the property to listen on.
	 * @param listener
	 *            The PropertyChangeListener to be added
	 */
	void addDescendantPropertyChangeListener(String propertyName, PropertyChangeListener listener);
}
