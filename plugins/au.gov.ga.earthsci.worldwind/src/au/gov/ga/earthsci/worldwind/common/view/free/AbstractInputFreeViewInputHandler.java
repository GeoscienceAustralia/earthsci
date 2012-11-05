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
package au.gov.ga.earthsci.worldwind.common.view.free;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

/**
 * Extension of the {@link AbstractFreeViewInputHandler} that adds the
 * mouse/keyboard handling methods, passing them to the abstract move() and
 * look() methods.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractInputFreeViewInputHandler extends AbstractFreeViewInputHandler
{
	private final Robot robot;
	private final Cursor blankCursor;
	private Cursor lastCursor;
	private int screenX, screenY, startX, startY;
	private int deltaX, deltaY, lastDeltaX, lastDeltaY;
	private boolean button1Down = false;
	private boolean button2Down = false;
	private boolean button3Down = false;

	public AbstractInputFreeViewInputHandler()
	{
		Toolkit tk = Toolkit.getDefaultToolkit();
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		blankCursor = tk.createCustomCursor(image, new Point(0, 0), "BlackCursor");

		Robot robot = null;
		try
		{
			robot = new Robot();
		}
		catch (AWTException e)
		{
		}
		this.robot = robot;
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		if (!(button1Down || button2Down || button3Down))
			lastCursor = e.getComponent().getCursor();
		e.getComponent().setCursor(blankCursor);

		screenX = e.getXOnScreen();
		screenY = e.getYOnScreen();
		startX = e.getX();
		startY = e.getY();
		deltaX = deltaY = lastDeltaX = lastDeltaY = 0;

		switch (e.getButton())
		{
		case MouseEvent.BUTTON1:
			button1Down = true;
			break;
		case MouseEvent.BUTTON2:
			button2Down = true;
			break;
		case MouseEvent.BUTTON3:
			button3Down = true;
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		switch (e.getButton())
		{
		case MouseEvent.BUTTON1:
			button1Down = false;
			break;
		case MouseEvent.BUTTON2:
			button2Down = false;
			break;
		case MouseEvent.BUTTON3:
			button3Down = false;
			break;
		}

		if (!(button1Down || button2Down || button3Down))
			e.getComponent().setCursor(lastCursor);
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (robot != null)
		{
			deltaX += e.getX() - startX;
			deltaY += e.getY() - startY;
			robot.mouseMove(screenX, screenY);
			e =
					new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), startX + deltaX, startY
							+ deltaY, screenX + deltaX, screenY + deltaY, e.getClickCount(), false, e.getButton());
		}
		else
		{
			deltaX = e.getX() - startX;
			deltaY = e.getX() - startY;
		}

		int xChange = deltaX - lastDeltaX;
		int yChange = deltaY - lastDeltaY;

		if (button1Down)
		{
			move(xChange, yChange, 0);
		}
		if (button2Down)
		{
			move(0, 0, -yChange);
			look(0, 0, xChange);
		}
		if (button3Down)
		{
			look(xChange, -yChange, 0);
		}

		deltaX = lastDeltaX;
		deltaY = lastDeltaY;
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		move(0, 0, -e.getWheelRotation() * 20);
	}
}
