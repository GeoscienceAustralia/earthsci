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

import gov.nasa.worldwind.awt.AWTInputHandler;

import java.awt.Component;
import java.awt.Container;

import au.gov.ga.earthsci.newt.awt.NewtEventConverterAWT;
import au.gov.ga.earthsci.newt.awt.NewtEventConverterAWT.AWTEventFromNewt;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowUpdateEvent;

/**
 * Helper class that processes NEWT events from a NEWT {@link Window}, converts
 * the events to AWT events, and forwards the AWT events to AWT input handler
 * provided in the constructor.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NewtEventProcessorSWT implements com.jogamp.newt.event.MouseListener, com.jogamp.newt.event.KeyListener,
		com.jogamp.newt.event.WindowListener
{
	protected final AWTInputHandler inputHandler;
	protected final Component dummyComponent;

	protected boolean mouseDragged = false;

	public NewtEventProcessorSWT(AWTInputHandler inputHandler)
	{
		this.inputHandler = inputHandler;
		this.dummyComponent = new Container();
	}

	/**
	 * Forward the NEWT events captured by this listener to the AWT handlers.
	 */
	protected void forward(NEWTEvent event)
	{
		java.awt.AWTEvent awtEvent = NewtEventConverterAWT.createEvent(event, dummyComponent);
		if (awtEvent != null)
		{
			((AWTEventFromNewt) awtEvent).forward(inputHandler);
		}
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		forward(e);
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		forward(e);
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		forward(e);
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		//AWT doesn't raise click events after a drag, but NEWT does, so follow AWT behaviour.
		if (!mouseDragged)
		{
			forward(e);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		forward(e);
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		forward(e);
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		forward(e);
		mouseDragged = false;
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		forward(e);
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		forward(e);
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		forward(e);
		mouseDragged = true;
	}

	@Override
	public void mouseWheelMoved(MouseEvent e)
	{
		forward(e);
	}

	@Override
	public void windowGainedFocus(WindowEvent e)
	{
		forward(e);
	}

	@Override
	public void windowLostFocus(WindowEvent e)
	{
		forward(e);
	}

	@Override
	public void windowResized(WindowEvent e)
	{
		forward(e);
	}

	@Override
	public void windowMoved(WindowEvent e)
	{
		forward(e);
	}

	@Override
	public void windowDestroyNotify(WindowEvent e)
	{
		forward(e);
	}

	@Override
	public void windowDestroyed(WindowEvent e)
	{
		forward(e);
	}

	@Override
	public void windowRepaint(WindowUpdateEvent e)
	{
		forward(e);
	}
}
