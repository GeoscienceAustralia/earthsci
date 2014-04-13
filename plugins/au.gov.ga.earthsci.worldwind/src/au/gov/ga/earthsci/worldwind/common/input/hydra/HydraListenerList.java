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
package au.gov.ga.earthsci.worldwind.common.input.hydra;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains a list of {@link IHydraListener}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HydraListenerList implements IHydraListener
{
	private final List<IHydraListener> listeners = new ArrayList<IHydraListener>();

	@Override
	public void updated(HydraEvent event)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).updated(event);
		}
	}

	@Override
	public void buttonChanged(HydraButtonEvent event)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).buttonChanged(event);
		}
	}

	@Override
	public void stickChanged(HydraStickEvent event)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).stickChanged(event);
		}
	}

	@Override
	public void triggerChanged(HydraTriggerEvent event)
	{
		for (int i = listeners.size() - 1; i >= 0; i--)
		{
			listeners.get(i).triggerChanged(event);
		}
	}

	public void add(IHydraListener listener)
	{
		listeners.add(listener);
	}

	public void remove(IHydraListener listener)
	{
		listeners.remove(listener);
	}
}
