/*******************************************************************************
 * Copyright 2015 Geoscience Australia
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
package au.gov.ga.earthsci.application.parts.globe;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.events.IEventBroker;

/**
 * Helper that links views from multiple globe parts together, so that the
 * camera's are sychronized.
 *
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
@Creatable
public class ViewLinker implements PropertyChangeListener
{
	private final List<View> views = new ArrayList<View>();
	private boolean propertyChanged = false;
	private boolean enabled = false;

	public static final String EVENT_TOPIC = "au/gov/ga/earthsci/application/globe/view/linked"; //$NON-NLS-1$

	@Inject
	private static IEventBroker eventBroker;

	public synchronized void link(View view)
	{
		view.addPropertyChangeListener(AVKey.VIEW, this);
		views.add(view);
	}

	public synchronized void unlink(View view)
	{
		view.removePropertyChangeListener(AVKey.VIEW, this);
		views.remove(view);
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		eventBroker.send(EVENT_TOPIC, enabled);
	}

	@Override
	public synchronized void propertyChange(PropertyChangeEvent evt)
	{
		if (!enabled || propertyChanged || !(evt.getNewValue() instanceof View))
		{
			return;
		}
		try
		{
			propertyChanged = true;
			View src = (View) evt.getNewValue();
			for (View view : views)
			{
				if (view == src)
				{
					continue;
				}
				view.copyViewState(src);
				view.firePropertyChange(AVKey.VIEW, null, view);
			}
		}
		finally
		{
			propertyChanged = false;
		}
	}
}
