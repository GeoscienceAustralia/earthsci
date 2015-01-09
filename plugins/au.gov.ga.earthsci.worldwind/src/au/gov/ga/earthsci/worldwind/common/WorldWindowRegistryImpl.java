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
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingExceptionListener;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.swing.event.EventListenerList;

/**
 * Internal implementation of {@link WorldWindowRegistry}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
class WorldWindowRegistryImpl implements WorldWindowRegistry, RenderingListener, RenderingExceptionListener,
		PositionListener, SelectListener
{
	private final Set<WorldWindow> set = new HashSet<WorldWindow>();
	private final Stack<WorldWindow> stack = new Stack<WorldWindow>();
	private WorldWindow rendering;
	private WorldWindow first;
	private final EventListenerList listeners = new EventListenerList();

	@Override
	public void register(WorldWindow worldWindow)
	{
		synchronized (stack)
		{
			if (set.add(worldWindow))
			{
				worldWindow.addPositionListener(this);
				worldWindow.addRenderingExceptionListener(this);
				worldWindow.addRenderingListener(this);
				worldWindow.addSelectListener(this);
			}
			else
			{
				stack.remove(worldWindow);
			}
			stack.add(worldWindow);
			if (getFirstRegistered() == null)
			{
				first = worldWindow;
			}
		}
	}

	@Override
	public void unregister(WorldWindow worldWindow)
	{
		synchronized (stack)
		{
			if (set.remove(worldWindow))
			{
				worldWindow.removePositionListener(this);
				worldWindow.removeRenderingExceptionListener(this);
				worldWindow.removeRenderingListener(this);
				worldWindow.removeSelectListener(this);
				stack.remove(worldWindow);
			}
		}
	}

	@Override
	public Collection<WorldWindow> getAll()
	{
		return Collections.unmodifiableCollection(stack);
	}

	@Override
	public WorldWindow getActive()
	{
		synchronized (stack)
		{
			if (stack.isEmpty())
			{
				return null;
			}
			return stack.peek();
		}
	}

	@Override
	public void setActive(WorldWindow active)
	{
		register(active);
	}

	@Override
	public WorldWindow getFirstRegistered()
	{
		return first == null || first.getContext() == null ? null : first;
	}

	@Override
	public View getActiveView()
	{
		synchronized (stack)
		{
			if (stack.isEmpty())
			{
				return null;
			}
			return stack.peek().getView();
		}
	}

	@Override
	public WorldWindow getRendering()
	{
		return rendering;
	}

	@Override
	public View getRenderingView()
	{
		return rendering == null ? null : rendering.getView();
	}

	@Override
	public void addRenderingListener(RenderingListener listener)
	{
		listeners.add(RenderingListener.class, listener);
	}

	@Override
	public void removeRenderingListener(RenderingListener listener)
	{
		listeners.remove(RenderingListener.class, listener);
	}

	@Override
	public void addSelectListener(SelectListener listener)
	{
		listeners.add(SelectListener.class, listener);
	}

	@Override
	public void removeSelectListener(SelectListener listener)
	{
		listeners.remove(SelectListener.class, listener);
	}

	@Override
	public void addPositionListener(PositionListener listener)
	{
		listeners.add(PositionListener.class, listener);
	}

	@Override
	public void removePositionListener(PositionListener listener)
	{
		listeners.remove(PositionListener.class, listener);
	}

	@Override
	public void addRenderingExceptionListener(RenderingExceptionListener listener)
	{
		listeners.add(RenderingExceptionListener.class, listener);
	}

	@Override
	public void removeRenderingExceptionListener(RenderingExceptionListener listener)
	{
		listeners.remove(RenderingExceptionListener.class, listener);
	}

	@Override
	public void redraw()
	{
		synchronized (stack)
		{
			for (WorldWindow ww : stack)
			{
				ww.redraw();
			}
		}
	}

	@Override
	public void redrawNow()
	{
		synchronized (stack)
		{
			for (WorldWindow ww : stack)
			{
				ww.redrawNow();
			}
		}
	}

	@Override
	public void stageChanged(RenderingEvent event)
	{
		if (RenderingEvent.BEFORE_RENDERING.equals(event.getStage()) && event.getSource() instanceof WorldWindow)
		{
			rendering = (WorldWindow) event.getSource();
		}

		for (RenderingListener l : listeners.getListeners(RenderingListener.class))
		{
			l.stageChanged(event);
		}
	}

	@Override
	public void selected(SelectEvent event)
	{
		for (SelectListener l : listeners.getListeners(SelectListener.class))
		{
			l.selected(event);
		}
	}

	@Override
	public void moved(PositionEvent event)
	{
		for (PositionListener l : listeners.getListeners(PositionListener.class))
		{
			l.moved(event);
		}
	}

	@Override
	public void exceptionThrown(Throwable t)
	{
		for (RenderingExceptionListener l : listeners.getListeners(RenderingExceptionListener.class))
		{
			l.exceptionThrown(t);
		}
	}
}
