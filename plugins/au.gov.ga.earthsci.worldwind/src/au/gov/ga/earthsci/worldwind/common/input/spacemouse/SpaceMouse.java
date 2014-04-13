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
package au.gov.ga.earthsci.worldwind.common.input.spacemouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;

/**
 * Singleton that handles 3Dconnexion SpaceMouse input. Uses JInput.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SpaceMouse
{
	private final static SpaceMouse INSTANCE = new SpaceMouse();

	public static SpaceMouse getInstance()
	{
		return INSTANCE;
	}

	private final Controller spacemouse;
	private final float[] axesValue = new float[6];
	private final Map<Component, Integer> axesIndex = new HashMap<Component, Integer>();
	private final Map<Component, Float> buttons = new HashMap<Component, Float>();
	private final Map<Component, Integer> buttonsIndex = new HashMap<Component, Integer>();
	private final SpaceMouseListenerList listeners = new SpaceMouseListenerList();

	private final int POLLING_DELAY = 100; //ms
	private final float DEAD_ZONE = 50;

	private SpaceMouse()
	{
		spacemouse = getSpaceMouse();
		if (spacemouse == null)
		{
			return;
		}

		int button = 0;
		spacemouse.poll();
		for (Component c : spacemouse.getComponents())
		{
			if (c.isAnalog())
			{
				String lowerName = c.getName().toLowerCase();
				int axisIndex = lowerName.contains("z") ? 2 : lowerName.contains("y") ? 1 : 0;
				if (lowerName.contains("r"))
				{
					axisIndex += 3;
				}
				axesValue[axisIndex] = deadenValue(c.getPollData());
				axesIndex.put(c, axisIndex);
			}
			else
			{
				buttons.put(c, c.getPollData());
				buttonsIndex.put(c, button++);
			}
		}

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Event event = new Event();
				List<SpaceMouseButtonEvent> buttonEvents = new ArrayList<SpaceMouseButtonEvent>();

				while (true)
				{
					try
					{
						Thread.sleep(POLLING_DELAY);
					}
					catch (InterruptedException e)
					{
					}

					if (spacemouse.poll())
					{
						boolean axisChanged = false;

						SpaceMouseAxisEvent axisEvent = new SpaceMouseAxisEvent();
						for (int i = 0; i < 6; i++)
						{
							axisEvent.values[i] = axesValue[i];
						}

						while (spacemouse.getEventQueue().getNextEvent(event))
						{
							Component c = event.getComponent();
							float value = event.getValue();

							Integer axisIndex = axesIndex.get(c);
							if (axisIndex != null)
							{
								value = deadenValue(value);
								float oldValue = axesValue[axisIndex];
								axisEvent.values[axisIndex] = value;
								if (oldValue != value)
								{
									axisChanged = true;
									axisEvent.deltas[axisIndex] = value - oldValue;
								}
							}
							else
							{
								Float oldValue = buttons.get(c);
								if (oldValue != null && oldValue != value)
								{
									SpaceMouseButtonEvent buttonEvent = new SpaceMouseButtonEvent();
									buttonEvent.button = buttonsIndex.get(c);
									buttonEvent.down = Math.abs(value) > 0.00001f;
									buttonEvents.add(buttonEvent);
									buttons.put(c, value);
								}
							}
						}

						if (axisChanged)
						{
							for (int i = 0; i < 6; i++)
							{
								axesValue[i] = axisEvent.values[i];
							}
							listeners.axisChanged(axisEvent);
						}

						for (SpaceMouseButtonEvent buttonEvent : buttonEvents)
						{
							listeners.buttonChanged(buttonEvent);
						}
						buttonEvents.clear();
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.setName("SpaceMouse poller");
		thread.start();
	}

	/**
	 * Add a SpaceMouse input listener.
	 * 
	 * @param listener
	 */
	public void addListener(ISpaceMouseListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Remove a SpaceMouse input listener.
	 * 
	 * @param listener
	 */
	public void removeListener(ISpaceMouseListener listener)
	{
		listeners.remove(listener);
	}

	private Controller getSpaceMouse()
	{
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
		for (Controller c : ce.getControllers())
		{
			if (c.getName().toLowerCase().contains("spacemouse"))
			{
				return c;
			}
		}
		return null;
	}

	private float deadenValue(float value)
	{
		if (System.getProperty("os.name").toLowerCase().contains("mac"))
		{
			//mac reports axis values much lower than windows, so pre-multiply
			value *= 500;
		}
		return deadenValue(value, DEAD_ZONE);
	}

	private static float deadenValue(float value, float deadZone)
	{
		if (Math.abs(value) <= deadZone)
		{
			return 0;
		}
		return value - Math.signum(value) * deadZone;
	}
}
