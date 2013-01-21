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
package au.gov.ga.earthsci.core.worldwind;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

/**
 * Helper class that keeps track of open {@link WorldWindow}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Creatable
@Singleton
public class WorldWindowRegistry
{
	private final Map<WorldWindow, View> map = new HashMap<WorldWindow, View>();
	private final Stack<WorldWindow> stack = new Stack<WorldWindow>();

	public void register(WorldWindow worldWindow)
	{
		if (map.containsKey(worldWindow))
		{
			stack.remove(worldWindow);
		}
		map.put(worldWindow, worldWindow.getView());
		stack.add(worldWindow);
	}

	public void unregister(WorldWindow worldWindow)
	{
		if (map.containsKey(worldWindow))
		{
			stack.remove(worldWindow);
			map.remove(worldWindow);
		}
	}

	public Collection<WorldWindow> getWorldWindows()
	{
		return Collections.unmodifiableCollection(map.keySet());
	}

	public Collection<View> getViews()
	{
		return Collections.unmodifiableCollection(map.values());
	}

	public void setActiveWindow(WorldWindow lastWorldWindow)
	{
		register(lastWorldWindow);
	}

	public WorldWindow getLastWorldWindow()
	{
		if (stack.isEmpty())
			return null;
		return stack.peek();
	}

	public View getLastView()
	{
		if (stack.isEmpty())
			return null;
		return map.get(stack.peek());
	}
}
