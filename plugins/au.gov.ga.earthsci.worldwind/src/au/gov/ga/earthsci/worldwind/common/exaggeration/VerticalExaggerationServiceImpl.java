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
package au.gov.ga.earthsci.worldwind.common.exaggeration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Internal implementation of the {@link VerticalExaggerationService}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
class VerticalExaggerationServiceImpl implements VerticalExaggerationService
{
	private double value = 1;
	private Map<Object, Double> marks = new WeakHashMap<Object, Double>();
	private final List<VerticalExaggerationListener> listeners = new ArrayList<VerticalExaggerationListener>();

	@Override
	public double get()
	{
		return value;
	}

	@Override
	public void set(double exaggeration)
	{
		if (value != exaggeration)
		{
			double oldValue = value;
			value = exaggeration;

			for (int i = listeners.size() - 1; i >= 0; i--)
			{
				listeners.get(i).verticalExaggerationChanged(oldValue, value);
			}
		}
	}

	@Override
	public void addListener(VerticalExaggerationListener listener)
	{
		listeners.add(listener);
	}

	@Override
	public void removeListener(VerticalExaggerationListener listener)
	{
		listeners.remove(listener);
	}

	@Override
	public void markVerticalExaggeration(Object key)
	{
		marks.put(key, value);
	}

	@Override
	public boolean isVerticalExaggerationChanged(Object key)
	{
		if (!marks.containsKey(key))
		{
			return true;
		}
		return marks.get(key) != value;
	}

	@Override
	public void clearMark(Object key)
	{
		marks.remove(key);
	}

	@Override
	public boolean checkAndMarkVerticalExaggeration(Object key)
	{
		if (isVerticalExaggerationChanged(key))
		{
			markVerticalExaggeration(key);
			return true;
		}
		return false;
	}
}
