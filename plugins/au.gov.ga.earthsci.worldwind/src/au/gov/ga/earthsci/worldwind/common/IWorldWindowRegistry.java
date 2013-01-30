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
package au.gov.ga.earthsci.worldwind.common;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.event.SelectListener;

import java.util.Collection;

/**
 * Helper that keeps track of open {@link WorldWindow}s, and allows for
 * listening to events on them.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IWorldWindowRegistry
{
	public final IWorldWindowRegistry INSTANCE = new WorldWindowRegistry();
	
	/**
	 * Register a new world window. Should be called when a new world window is
	 * created.
	 * 
	 * @param worldWindow
	 */
	void register(WorldWindow worldWindow);

	/**
	 * Unregister a registered world window. Should be called when a world
	 * window is hidden and will not be used again.
	 * 
	 * @param worldWindow
	 */
	void unregister(WorldWindow worldWindow);

	/**
	 * @return A readonly collection of the registered world windows.
	 */
	Collection<WorldWindow> getAll();

	/**
	 * Get the last world window that was activated (ie received focus) by the
	 * user.
	 * <p/>
	 * Returns null if no world windows are registered, otherwise always returns
	 * the last active window (or the last registered).
	 * 
	 * @return The last active world window.
	 */
	WorldWindow getActive();

	/**
	 * Mark the given world window as activated. Should be called when the user
	 * gives a world window input focus.
	 * <p/>
	 * Calling this on an unregistered world window will register the window.
	 * 
	 * @param active
	 *            World window that was activated
	 */
	void setActive(WorldWindow active);

	/**
	 * @return The view of the last active world window.
	 */
	View getActiveView();

	/**
	 * Get the world window that is currently being rendered, or the last to be
	 * rendered. When a world window is registered, a {@link RenderingListener}
	 * is added to it, which listens for render events and sets the last
	 * rendering world window.
	 * 
	 * @return The world window being rendered, or the last to be rendered.
	 */
	WorldWindow getRendering();

	/**
	 * @return The view of the currently rendering or last rendered world
	 *         window.
	 */
	View getRenderingView();

	/**
	 * Adds a rendering listener to all registered world windows.
	 * 
	 * @see WorldWindow#addRenderingListener(RenderingListener)
	 */
	void addRenderingListener(RenderingListener listener);

	/**
	 * Removes a rendering listener.
	 * 
	 * @see WorldWindow#removeRenderingListener(RenderingListener)
	 */
	void removeRenderingListener(RenderingListener listener);

	/**
	 * Adds a select listener to all registered world windows.
	 * 
	 * @see WorldWindow#addSelectListener(SelectListener)
	 */
	void addSelectListener(SelectListener listener);

	/**
	 * Removes a select listener.
	 * 
	 * @see WorldWindow#removeSelectListener(SelectListener)
	 */
	void removeSelectListener(SelectListener listener);

	/**
	 * Adds a position listener to all registered world windows.
	 * 
	 * @see WorldWindow#addPositionListener(PositionListener)
	 */
	void addPositionListener(PositionListener listener);

	/**
	 * Removes a position listener.
	 * 
	 * @see WorldWindow#removePositionListener(PositionListener)
	 */
	void removePositionListener(PositionListener listener);

	/**
	 * Adds an exception listener to all registered world windows.
	 * 
	 * @see WorldWindow#addRenderingExceptionListener(RenderingExceptionListener)
	 */
	void addRenderingExceptionListener(RenderingExceptionListener listener);

	/**
	 * Removes a rendering exception listener.
	 * 
	 * @see WorldWindow#removeRenderingExceptionListener(RenderingExceptionListener)
	 */
	void removeRenderingExceptionListener(RenderingExceptionListener listener);

	/**
	 * Call {@link WorldWindow#redraw()} on all registered world windows.
	 */
	void redraw();

	/**
	 * Call {@link WorldWindow#redrawNow()} on all registered world windows.
	 */
	void redrawNow();
}
