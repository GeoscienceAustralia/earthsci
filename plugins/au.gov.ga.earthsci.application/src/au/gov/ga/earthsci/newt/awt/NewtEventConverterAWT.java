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

import gov.nasa.worldwind.awt.AWTInputHandler;

import java.awt.Component;
import java.awt.Window;

import javax.swing.SwingUtilities;

import jogamp.newt.awt.event.AWTNewtEventFactory;

import com.jogamp.common.util.IntIntHashMap;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.NEWTEvent;
import com.jogamp.newt.event.WindowEvent;

/**
 * Helper class used to convert NEWT events to corresponding AWT events. Similar
 * to {@link AWTNewtEventFactory}, but converts in the opposite direction.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NewtEventConverterAWT
{
	protected static final int KEY_NOT_FOUND_VALUE = 0xFFFFFFFF;
	protected static final IntIntHashMap eventTypeAWT2NEWT;

	static
	{
		// @formatter:off
		
		IntIntHashMap map = new IntIntHashMap();
		map.setKeyNotFoundValue(KEY_NOT_FOUND_VALUE);
		// n/a map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_OPENED, java.awt.event.WindowEvent.WINDOW_OPENED);
		map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_DESTROY_NOTIFY, java.awt.event.WindowEvent.WINDOW_CLOSING);
		map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_DESTROYED, java.awt.event.WindowEvent.WINDOW_CLOSED);
		// n/a map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_ICONIFIED, java.awt.event.WindowEvent.WINDOW_ICONIFIED);
		// n/a map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_DEICONIFIED, java.awt.event.WindowEvent.WINDOW_DEICONIFIED);
		//map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_GAINED_FOCUS, java.awt.event.WindowEvent.WINDOW_ACTIVATED);
		//map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_GAINED_FOCUS, java.awt.event.WindowEvent.WINDOW_GAINED_FOCUS);
		map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_GAINED_FOCUS, java.awt.event.FocusEvent.FOCUS_GAINED);
		//map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_LOST_FOCUS, java.awt.event.WindowEvent.WINDOW_DEACTIVATED);
		//map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_LOST_FOCUS, java.awt.event.WindowEvent.WINDOW_LOST_FOCUS);
		map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_LOST_FOCUS, java.awt.event.FocusEvent.FOCUS_LOST);
		// n/a map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_STATE_CHANGED, java.awt.event.WindowEvent.WINDOW_STATE_CHANGED);

		map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_MOVED, java.awt.event.ComponentEvent.COMPONENT_MOVED);
		map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_RESIZED, java.awt.event.ComponentEvent.COMPONENT_RESIZED);
		// n/a map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_SHOWN, java.awt.event.ComponentEvent.COMPONENT_SHOWN);
		// n/a map.put(com.jogamp.newt.event.WindowEvent.EVENT_WINDOW_HIDDEN, java.awt.event.ComponentEvent.COMPONENT_HIDDEN);

		map.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_CLICKED, java.awt.event.MouseEvent.MOUSE_CLICKED);
		map.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_PRESSED, java.awt.event.MouseEvent.MOUSE_PRESSED);
		map.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_RELEASED, java.awt.event.MouseEvent.MOUSE_RELEASED);
		map.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_MOVED, java.awt.event.MouseEvent.MOUSE_MOVED);
		map.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_ENTERED, java.awt.event.MouseEvent.MOUSE_ENTERED);
		map.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_EXITED, java.awt.event.MouseEvent.MOUSE_EXITED);
		map.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_DRAGGED, java.awt.event.MouseEvent.MOUSE_DRAGGED);
		map.put(com.jogamp.newt.event.MouseEvent.EVENT_MOUSE_WHEEL_MOVED, java.awt.event.MouseEvent.MOUSE_WHEEL);

		map.put(com.jogamp.newt.event.KeyEvent.EVENT_KEY_PRESSED, java.awt.event.KeyEvent.KEY_PRESSED);
		map.put(com.jogamp.newt.event.KeyEvent.EVENT_KEY_RELEASED, java.awt.event.KeyEvent.KEY_RELEASED);
		map.put(com.jogamp.newt.event.KeyEvent.EVENT_KEY_TYPED, java.awt.event.KeyEvent.KEY_TYPED);

		eventTypeAWT2NEWT = map;
		
		// @formatter:on
	}

	/**
	 * Convert a NEWT modifiers mask int to an AWT mask.
	 * 
	 * @param newtMods
	 *            NEWT modifiers mask
	 * @return Corresponding AWT modifiers mask
	 */
	protected static final int newtModifiers2Awt(int newtMods)
	{
		int awtMods = 0;
		if ((newtMods & com.jogamp.newt.event.InputEvent.SHIFT_MASK) != 0)
			awtMods |= java.awt.event.InputEvent.SHIFT_DOWN_MASK;
		if ((newtMods & com.jogamp.newt.event.InputEvent.CTRL_MASK) != 0)
			awtMods |= java.awt.event.InputEvent.CTRL_DOWN_MASK;
		if ((newtMods & com.jogamp.newt.event.InputEvent.META_MASK) != 0)
			awtMods |= java.awt.event.InputEvent.META_DOWN_MASK;
		if ((newtMods & com.jogamp.newt.event.InputEvent.ALT_MASK) != 0)
			awtMods |= java.awt.event.InputEvent.ALT_DOWN_MASK;
		if ((newtMods & com.jogamp.newt.event.InputEvent.ALT_GRAPH_MASK) != 0)
			awtMods |= java.awt.event.InputEvent.ALT_GRAPH_MASK;
		if ((newtMods & com.jogamp.newt.event.InputEvent.BUTTON1_MASK) != 0)
			awtMods |= java.awt.event.InputEvent.BUTTON1_DOWN_MASK;
		if ((newtMods & com.jogamp.newt.event.InputEvent.BUTTON2_MASK) != 0)
			awtMods |= java.awt.event.InputEvent.BUTTON2_DOWN_MASK;
		if ((newtMods & com.jogamp.newt.event.InputEvent.BUTTON3_MASK) != 0)
			awtMods |= java.awt.event.InputEvent.BUTTON3_DOWN_MASK;
		return awtMods;
	}

	/**
	 * Convert a NEWT button int to an AWT button int.
	 * 
	 * @param newtButton
	 *            NEWT button
	 * @return Corresponding AWT button
	 */
	protected static final int newtButton2Awt(int newtButton)
	{
		switch (newtButton)
		{
		case com.jogamp.newt.event.MouseEvent.BUTTON1:
			return java.awt.event.MouseEvent.BUTTON1;
		case com.jogamp.newt.event.MouseEvent.BUTTON2:
			return java.awt.event.MouseEvent.BUTTON2;
		case com.jogamp.newt.event.MouseEvent.BUTTON3:
			return java.awt.event.MouseEvent.BUTTON3;
		}
		return 0;
	}

	/**
	 * Create an {@link java.awt.AWTEvent} from the given
	 * com.jogamp.newt.event.NEWTEvent.
	 * 
	 * @param event
	 *            NEWT event to convert
	 * @param awtSourceComponent
	 *            Component to set as the AWT event source
	 * @return AWT event corresponding to the given NEWT event
	 */
	public static final java.awt.AWTEvent createEvent(com.jogamp.newt.event.NEWTEvent event,
			java.awt.Component awtSourceComponent)
	{
		if (event instanceof KeyEvent)
		{
			return createKeyEvent((KeyEvent) event, awtSourceComponent);
		}
		else if (event instanceof MouseEvent)
		{
			return createMouseEvent((MouseEvent) event, awtSourceComponent);
		}
		else if (event instanceof WindowEvent)
		{
			return createComponentEvent((WindowEvent) event, awtSourceComponent);
		}
		return null;
	}

	/**
	 * Create an AWT {@link java.awt.event.ComponentEvent} from the given NEWT
	 * {@link com.jogamp.newt.event.WindowEvent}. Return object type may be a
	 * {@link java.awt.event.ComponentEvent}, or one of its subclasses
	 * {@link java.awt.event.WindowEvent} or {@link java.awt.event.FocusEvent},
	 * according to the NEWT event type. Return value implements
	 * {@link AWTEventFromNewt}.
	 * 
	 * @param event
	 *            NEWT event to convert
	 * @param awtSource
	 *            Component to set as the AWT event source
	 * @return AWT event corresponding to the given NEWT event
	 */
	public static final java.awt.event.ComponentEvent createComponentEvent(com.jogamp.newt.event.WindowEvent event,
			java.awt.Component awtSource)
	{
		int id = eventTypeAWT2NEWT.get(event.getEventType());
		if (id != KEY_NOT_FOUND_VALUE)
		{
			switch (id)
			{
			case java.awt.event.FocusEvent.FOCUS_GAINED:
			case java.awt.event.FocusEvent.FOCUS_LOST:
				return new FocusEventFromNewt(event, awtSource, id);
			case java.awt.event.ComponentEvent.COMPONENT_MOVED:
			case java.awt.event.ComponentEvent.COMPONENT_RESIZED:
				return new ComponentEventFromNewt(event, awtSource, id);
			default:
				return new WindowEventFromNewt(event, SwingUtilities.getWindowAncestor(awtSource), id);
			}
		}
		return null;
	}

	/**
	 * Create an AWT {@link java.awt.event.MouseEvent} from the given NEWT
	 * {@link com.jogamp.newt.event.MouseEvent}. Return object type may be a
	 * {@link java.awt.event.MouseEvent}, or a
	 * {@link java.awt.event.MouseWheelEvent} if the NEWT event is a mouse wheel
	 * event. Return value implements {@link AWTEventFromNewt}.
	 * 
	 * @param event
	 *            NEWT event to convert
	 * @param awtSource
	 *            Component to set as the AWT event source
	 * @return AWT event corresponding to the given NEWT event
	 */
	public static final java.awt.event.MouseEvent createMouseEvent(com.jogamp.newt.event.MouseEvent event,
			java.awt.Component awtSource)
	{
		int id = eventTypeAWT2NEWT.get(event.getEventType());
		if (id != KEY_NOT_FOUND_VALUE)
		{
			// AWT/NEWT rotation is reversed - AWT +1 is down, NEWT +1 is up.
			int rotation = -event.getWheelRotation();
			int mods = newtModifiers2Awt(event.getModifiers());
			int button = newtButton2Awt(event.getButton());

			switch (id)
			{
			case java.awt.event.MouseEvent.MOUSE_WHEEL:
				return new MouseWheelEventFromNewt(event, awtSource, id, event.getWhen(), mods, event.getX(),
						event.getY(), event.getClickCount(), false, java.awt.event.MouseWheelEvent.WHEEL_UNIT_SCROLL,
						rotation, rotation);
			default:
				return new MouseEventFromNewt(event, awtSource, id, event.getWhen(), mods, event.getX(), event.getY(),
						event.getClickCount(), false, button);
			}
		}
		return null; // no mapping ..
	}

	/**
	 * Create an AWT {@link java.awt.event.KeyEvent} from the given NEWT
	 * {@link com.jogamp.newt.event.KeyEvent}. Return value implements
	 * {@link AWTEventFromNewt}.
	 * 
	 * @param event
	 *            NEWT event to convert
	 * @param awtSource
	 *            Component to set as the AWT event source
	 * @return AWT event corresponding to the given NEWT event
	 */
	public static final java.awt.event.KeyEvent createKeyEvent(com.jogamp.newt.event.KeyEvent event,
			java.awt.Component awtSource)
	{
		int id = eventTypeAWT2NEWT.get(event.getEventType());
		if (id != KEY_NOT_FOUND_VALUE)
		{
			int code =
					id == java.awt.event.KeyEvent.KEY_TYPED ? java.awt.event.KeyEvent.VK_UNDEFINED : event.getKeyCode();
			int location =
					id == java.awt.event.KeyEvent.KEY_TYPED ? java.awt.event.KeyEvent.KEY_LOCATION_UNKNOWN
							: java.awt.event.KeyEvent.KEY_LOCATION_STANDARD;
			return new KeyEventFromNewt(event, awtSource, id, event.getWhen(), newtModifiers2Awt(event.getModifiers()),
					code, event.getKeyChar(), location);
		}
		return null; // no mapping ..
	}

	/////////////////////////////////////
	// AWTEvent subclasses used above: //
	/////////////////////////////////////

	/**
	 * Interface which all AWT events created by the
	 * {@link NewtEventConverterAWT} implement. Provides access to the original
	 * NEWT event from which the AWT event was created.
	 */
	public interface AWTEventFromNewt
	{
		/**
		 * @return The original NEWT event from which this AWT event was
		 *         created.
		 */
		NEWTEvent getOriginalEvent();

		/**
		 * Forward this event to the given {@link AWTInputHandler}
		 * 
		 * @param inputHandler
		 */
		void forward(AWTInputHandler inputHandler);
	}

	/**
	 * {@link java.awt.event.ComponentEvent} subclass that implements
	 * {@link AWTEventFromNewt}.
	 */
	public static class ComponentEventFromNewt extends java.awt.event.ComponentEvent implements AWTEventFromNewt
	{
		private final NEWTEvent originalEvent;

		public ComponentEventFromNewt(NEWTEvent originalEvent, Component source, int id)
		{
			super(source, id);
			this.originalEvent = originalEvent;
		}

		@Override
		public NEWTEvent getOriginalEvent()
		{
			return originalEvent;
		}

		@Override
		public void forward(AWTInputHandler inputHandler)
		{
		}
	}

	/**
	 * {@link java.awt.event.FocusEvent} subclass that implements
	 * {@link AWTEventFromNewt}.
	 */
	public static class FocusEventFromNewt extends java.awt.event.FocusEvent implements AWTEventFromNewt
	{
		private final NEWTEvent originalEvent;

		public FocusEventFromNewt(NEWTEvent originalEvent, Component source, int id, boolean temporary,
				Component opposite)
		{
			super(source, id, temporary, opposite);
			this.originalEvent = originalEvent;
		}

		public FocusEventFromNewt(NEWTEvent originalEvent, Component source, int id, boolean temporary)
		{
			super(source, id, temporary);
			this.originalEvent = originalEvent;
		}

		public FocusEventFromNewt(NEWTEvent originalEvent, Component source, int id)
		{
			super(source, id);
			this.originalEvent = originalEvent;
		}

		@Override
		public NEWTEvent getOriginalEvent()
		{
			return originalEvent;
		}

		@Override
		public void forward(AWTInputHandler inputHandler)
		{
			switch (getID())
			{
			case java.awt.event.FocusEvent.FOCUS_GAINED:
				inputHandler.focusGained(this);
				break;
			case java.awt.event.FocusEvent.FOCUS_LOST:
				inputHandler.focusLost(this);
				break;
			}
		}
	}

	/**
	 * {@link java.awt.event.KeyEvent} subclass that implements
	 * {@link AWTEventFromNewt}.
	 */
	public static class KeyEventFromNewt extends java.awt.event.KeyEvent implements AWTEventFromNewt
	{
		private final NEWTEvent originalEvent;

		public KeyEventFromNewt(NEWTEvent originalEvent, Component source, int id, long when, int modifiers,
				int keyCode, char keyChar, int keyLocation)
		{
			super(source, id, when, modifiers, keyCode, keyChar, keyLocation);
			this.originalEvent = originalEvent;
		}

		public KeyEventFromNewt(NEWTEvent originalEvent, Component source, int id, long when, int modifiers,
				int keyCode, char keyChar)
		{
			super(source, id, when, modifiers, keyCode, keyChar);
			this.originalEvent = originalEvent;
		}

		@Override
		public NEWTEvent getOriginalEvent()
		{
			return originalEvent;
		}

		@Override
		public void forward(AWTInputHandler inputHandler)
		{
			switch (getID())
			{
			case java.awt.event.KeyEvent.KEY_PRESSED:
				inputHandler.keyPressed(this);
				break;
			case java.awt.event.KeyEvent.KEY_RELEASED:
				inputHandler.keyReleased(this);
				break;
			case java.awt.event.KeyEvent.KEY_TYPED:
				inputHandler.keyTyped(this);
				break;
			}
		}
	}

	/**
	 * {@link java.awt.event.MouseEvent} subclass that implements
	 * {@link AWTEventFromNewt}.
	 */
	public static class MouseEventFromNewt extends java.awt.event.MouseEvent implements AWTEventFromNewt
	{
		private final NEWTEvent originalEvent;

		public MouseEventFromNewt(NEWTEvent originalEvent, Component source, int id, long when, int modifiers, int x,
				int y, int clickCount, boolean popupTrigger, int button)
		{
			super(source, id, when, modifiers, x, y, clickCount, popupTrigger, button);
			this.originalEvent = originalEvent;
		}

		public MouseEventFromNewt(NEWTEvent originalEvent, Component source, int id, long when, int modifiers, int x,
				int y, int clickCount, boolean popupTrigger)
		{
			super(source, id, when, modifiers, x, y, clickCount, popupTrigger);
			this.originalEvent = originalEvent;
		}

		public MouseEventFromNewt(NEWTEvent originalEvent, Component source, int id, long when, int modifiers, int x,
				int y, int xAbs, int yAbs, int clickCount, boolean popupTrigger, int button)
		{
			super(source, id, when, modifiers, x, y, xAbs, yAbs, clickCount, popupTrigger, button);
			this.originalEvent = originalEvent;
		}

		@Override
		public NEWTEvent getOriginalEvent()
		{
			return originalEvent;
		}

		@Override
		public void forward(AWTInputHandler inputHandler)
		{
			switch (getID())
			{
			case java.awt.event.MouseEvent.MOUSE_CLICKED:
				inputHandler.mouseClicked(this);
				break;
			case java.awt.event.MouseEvent.MOUSE_PRESSED:
				inputHandler.mousePressed(this);
				break;
			case java.awt.event.MouseEvent.MOUSE_RELEASED:
				inputHandler.mouseReleased(this);
				break;
			case java.awt.event.MouseEvent.MOUSE_MOVED:
				inputHandler.mouseMoved(this);
				break;
			case java.awt.event.MouseEvent.MOUSE_ENTERED:
				inputHandler.mouseEntered(this);
				break;
			case java.awt.event.MouseEvent.MOUSE_EXITED:
				inputHandler.mouseExited(this);
				break;
			case java.awt.event.MouseEvent.MOUSE_DRAGGED:
				inputHandler.mouseDragged(this);
				break;
			}
		}
	}

	/**
	 * {@link java.awt.event.MouseWheelEvent} subclass that implements
	 * {@link AWTEventFromNewt}.
	 */
	public static class MouseWheelEventFromNewt extends java.awt.event.MouseWheelEvent implements AWTEventFromNewt
	{
		private final NEWTEvent originalEvent;

		public MouseWheelEventFromNewt(NEWTEvent originalEvent, Component source, int id, long when, int modifiers,
				int x, int y, int clickCount, boolean popupTrigger, int scrollType, int scrollAmount, int wheelRotation)
		{
			super(source, id, when, modifiers, x, y, clickCount, popupTrigger, scrollType, scrollAmount, wheelRotation);
			this.originalEvent = originalEvent;
		}

		public MouseWheelEventFromNewt(NEWTEvent originalEvent, Component source, int id, long when, int modifiers,
				int x, int y, int xAbs, int yAbs, int clickCount, boolean popupTrigger, int scrollType,
				int scrollAmount, int wheelRotation)
		{
			super(source, id, when, modifiers, x, y, xAbs, yAbs, clickCount, popupTrigger, scrollType, scrollAmount,
					wheelRotation);
			this.originalEvent = originalEvent;
		}

		@Override
		public NEWTEvent getOriginalEvent()
		{
			return originalEvent;
		}

		@Override
		public void forward(AWTInputHandler inputHandler)
		{
			switch (getID())
			{
			case java.awt.event.MouseEvent.MOUSE_WHEEL:
				inputHandler.mouseWheelMoved(this);
				break;
			}
		}
	}

	/**
	 * {@link java.awt.event.WindowEvent} subclass that implements
	 * {@link AWTEventFromNewt}.
	 */
	public static class WindowEventFromNewt extends java.awt.event.WindowEvent implements AWTEventFromNewt
	{
		private final NEWTEvent originalEvent;

		public WindowEventFromNewt(NEWTEvent originalEvent, Window source, int id, int oldState, int newState)
		{
			super(source, id, oldState, newState);
			this.originalEvent = originalEvent;
		}

		public WindowEventFromNewt(NEWTEvent originalEvent, Window source, int id, Window opposite, int oldState,
				int newState)
		{
			super(source, id, opposite, oldState, newState);
			this.originalEvent = originalEvent;
		}

		public WindowEventFromNewt(NEWTEvent originalEvent, Window source, int id, Window opposite)
		{
			super(source, id, opposite);
			this.originalEvent = originalEvent;
		}

		public WindowEventFromNewt(NEWTEvent originalEvent, Window source, int id)
		{
			super(source, id);
			this.originalEvent = originalEvent;
		}

		@Override
		public NEWTEvent getOriginalEvent()
		{
			return originalEvent;
		}

		@Override
		public void forward(AWTInputHandler inputHandler)
		{
		}
	}
}
