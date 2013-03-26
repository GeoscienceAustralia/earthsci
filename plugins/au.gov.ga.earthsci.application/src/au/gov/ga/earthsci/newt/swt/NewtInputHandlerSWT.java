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
package au.gov.ga.earthsci.newt.swt;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.event.InputHandler;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;

import java.awt.AWTEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseWheelEvent;

import au.gov.ga.earthsci.newt.awt.NewtEventConverterAWT;

import com.jogamp.newt.opengl.GLWindow;

/**
 * {@link InputHandler} implementation that captures NEWT events from a
 * {@link GLWindow} and dispatches them as AWT events to the event source.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NewtInputHandlerSWT extends AWTInputHandler
{
	protected GLWindow window;
	protected NewtEventProcessorSWT eventProcessor;

	@Override
	public void setEventSource(WorldWindow newWorldWindow)
	{
		this.wwd = newWorldWindow;
		if (this.wwd != null)
		{
			this.wwd.getView().getViewInputHandler().setWorldWindow(this.wwd);
		}

		this.selectListener = new SelectListener()
		{
			@Override
			public void selected(SelectEvent event)
			{
				if (event.getEventAction().equals(SelectEvent.ROLLOVER))
				{
					doHover(true);
				}
			}
		};
		this.wwd.addSelectListener(this.selectListener);

		if (this.wwd.getSceneController() != null)
		{
			this.wwd.getSceneController().addPropertyChangeListener(AVKey.VIEW, this);
		}


		if (newWorldWindow != null && !(newWorldWindow instanceof WorldWindowNewtCanvasSWT))
		{
			throw new IllegalArgumentException(
					"newWorldWindow must be an instanceof " + WorldWindowNewtCanvasSWT.class.getSimpleName()); //$NON-NLS-1$
		}
		WorldWindowNewtCanvasSWT swtWorldWindow = (WorldWindowNewtCanvasSWT) newWorldWindow;

		GLWindow window = newWorldWindow == null ? null : swtWorldWindow.getWindow();
		if (this.window == window)
		{
			return;
		}

		if (this.window != null)
		{
			this.window.removeMouseListener(eventProcessor);
			this.window.removeKeyListener(eventProcessor);
			this.window.removeWindowListener(eventProcessor);
		}

		this.window = window;

		if (window == null)
		{
			return;
		}

		eventProcessor = new NewtEventProcessorSWT(this);
		window.addMouseListener(eventProcessor);
		window.addKeyListener(eventProcessor);
		window.addWindowListener(eventProcessor);
	}

	//focus and mouse wheel events still get passed to AWT, so only process them if they are from the NEWT component:

	@Override
	public void focusGained(FocusEvent focusEvent)
	{
		if (isEventFromNewt(focusEvent))
		{
			super.focusGained(focusEvent);
		}
	}

	@Override
	public void focusLost(FocusEvent focusEvent)
	{
		if (isEventFromNewt(focusEvent))
		{
			super.focusLost(focusEvent);
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent)
	{
		if (isEventFromNewt(mouseWheelEvent))
		{
			super.mouseWheelMoved(mouseWheelEvent);
		}
	}

	protected boolean isEventFromNewt(AWTEvent event)
	{
		return event instanceof NewtEventConverterAWT.AWTEventFromNewt;
	}
}
