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
import java.util.HashSet;
import java.util.Set;

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
	private final Set<WorldWindow> worldWindows = new HashSet<WorldWindow>();
	private final Set<View> views = new HashSet<View>();
	private WorldWindow lastWorldWindow;
	private View lastView;

	public void register(WorldWindow worldWindow)
	{
		worldWindows.add(worldWindow);
		views.add(worldWindow.getView());
	}

	public void unregister(WorldWindow worldWindow)
	{
		worldWindows.remove(worldWindow);
		views.remove(worldWindow.getView());
	}

	public Collection<WorldWindow> getWorldWindows()
	{
		return Collections.unmodifiableCollection(worldWindows);
	}

	public Collection<View> getViews()
	{
		return Collections.unmodifiableCollection(views);
	}

	public WorldWindow getLastWorldWindow()
	{
		return lastWorldWindow;
	}

	public void setLastWorldWindow(WorldWindow lastWorldWindow)
	{
		this.lastWorldWindow = lastWorldWindow;
		this.lastView = lastWorldWindow.getView();
	}

	public View getLastView()
	{
		return lastView;
	}
}
