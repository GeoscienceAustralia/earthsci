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
package au.gov.ga.earthsci.common.util;

import java.beans.PropertyChangeListener;

/**
 * An interface for classes that can change to a 'dirty' state and notify
 * others of that fact.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IDirtyable
{

	/**
	 * Return whether this object is in a 'dirty' state.
	 * 
	 * @return <code>true</code> if this object is in a dirty state; <code>false</code> otherwise.
	 */
	boolean isDirty();
	
	/**
	 * Add a change listener that will be notified when this object enters a 'dirty' state.
	 * 
	 * @param l The change listener to add.
	 */
	void addDirtyChangeListener(PropertyChangeListener l);
	
	/**
	 * Remove the change listener from this object. No further 'dirty' events will be sent to 
	 * the listener.
	 * 
	 * @param l The change listener to remove.
	 */
	void removeDirtyChangeListener(PropertyChangeListener l);
}
