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
package au.gov.ga.earthsci.newt.awt;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.event.InputHandler;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.MouseWheelEvent;

import com.jogamp.newt.opengl.GLWindow;

/**
 * {@link InputHandler} implementation that captures NEWT events from a
 * {@link GLWindow} and dispatches them as AWT events to the event source.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NewtInputHandlerAWT extends AWTInputHandler
{
	protected GLWindow window;
	protected NewtEventProcessorAWT eventProcessor;

	@Override
	public void setEventSource(WorldWindow newWorldWindow)
	{
		super.setEventSource(newWorldWindow);

		if (newWorldWindow != null && !(newWorldWindow instanceof WorldWindowNewtCanvasAWT))
		{
			throw new IllegalArgumentException(
					"newWorldWindow must be an instanceof " + WorldWindowNewtCanvasAWT.class.getSimpleName()); //$NON-NLS-1$
		}

		GLWindow window = newWorldWindow == null ? null : ((WorldWindowNewtCanvasAWT) newWorldWindow).getWindow();
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

		eventProcessor = new NewtEventProcessorAWT((Component) newWorldWindow);
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
