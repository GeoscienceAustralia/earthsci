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
import java.beans.PropertyChangeSupport;

/**
 * Abstract {@link ITreePropertyChangeBean} implementation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractTreePropertyChangeBean extends AbstractPropertyChangeBean implements
		ITreePropertyChangeBean
{
	protected final transient PropertyChangeSupport descendantChangeSupport = new PropertyChangeSupport(this);

	@Override
	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
	{
		super.firePropertyChange(propertyChangeEvent);
		fireAscendantPropertyChange(propertyChangeEvent);
	}

	@Override
	public void fireAscendantPropertyChange(PropertyChangeEvent propertyChangeEvent)
	{
		descendantChangeSupport.firePropertyChange(propertyChangeEvent);

		//recurse up to root
		ITreePropertyChangeBean parent = getParent();
		if (parent != null)
		{
			parent.fireAscendantPropertyChange(propertyChangeEvent);
		}
	}

	@Override
	public void addDescendantPropertyChangeListener(PropertyChangeListener listener)
	{
		descendantChangeSupport.addPropertyChangeListener(listener);
	}

	@Override
	public void addDescendantPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		descendantChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		super.removePropertyChangeListener(listener);
		descendantChangeSupport.removePropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		super.removePropertyChangeListener(propertyName, listener);
		descendantChangeSupport.removePropertyChangeListener(propertyName, listener);
	}
}
