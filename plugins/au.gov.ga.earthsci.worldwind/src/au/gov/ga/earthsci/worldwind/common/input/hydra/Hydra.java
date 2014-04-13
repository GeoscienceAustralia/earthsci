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
package au.gov.ga.earthsci.worldwind.common.input.hydra;

import com.sixense.ControllerData;
import com.sixense.Sixense;
import com.sixense.SixenseActivator;
import com.sixense.utils.ControllerManager;
import com.sixense.utils.enums.EnumGameType;
import com.sixense.utils.enums.EnumSetupStep;

/**
 * Singleton that handles Razer Hydra input. Uses SixenseJava library.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Hydra
{
	private static final Hydra INSTANCE = new Hydra();

	public static Hydra getInstance()
	{
		return INSTANCE;
	}

	private final int POLLING_DELAY = 50; //ms
	private final int FINDING_DELAY = 1000; //ms
	private final ControllerData[] data = { new ControllerData(), new ControllerData(), new ControllerData(),
			new ControllerData() };
	private final HydraListenerList listeners = new HydraListenerList();
	private int lastButton1;
	private int lastButton2;
	private HydraStickEvent lastStickEvent = new HydraStickEvent();
	private HydraTriggerEvent lastTriggerEvent = new HydraTriggerEvent();

	private Hydra()
	{
		if (!loadLibrary())
		{
			return;
		}

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				if (Sixense.init())
				{
					Sixense.setActiveBase(0);

					final ControllerManager cm = ControllerManager.getInstance();
					cm.setGameType(EnumGameType.ONE_PLAYER_ONE_CONTROLLER);

					while (true)
					{
						cm.update(data);

						EnumSetupStep step = cm.getCurrentStep();
						//enum equality doesn't work here?! what?
						if (step.ordinal() == EnumSetupStep.P1C1_POWER_UP_0.ordinal())
						{
							startPolling();
							break;
						}

						try
						{
							Thread.sleep(FINDING_DELAY);
						}
						catch (InterruptedException e)
						{
						}
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.setName("Razer Hydra finder");
		thread.start();
	}

	private static boolean loadLibrary()
	{
		return SixenseActivator.isLibraryLoaded();
	}

	private void startPolling()
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					Sixense.getAllNewestData(data);
					processData();

					try
					{
						Thread.sleep(POLLING_DELAY);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
		});
		thread.setName("Razer Hydra poller");
		thread.setDaemon(true);
		thread.start();
	}

	private void processData()
	{
		listeners.updated(new HydraEvent(data[0], data[1]));

		lastButton1 = processButtons(data[0].buttons, lastButton1, 1);
		lastButton2 = processButtons(data[1].buttons, lastButton2, 2);

		processSticks();
		processTriggers();
	}

	private int processButtons(int value, int lastValue, int controller)
	{
		if (value != lastValue)
		{
			int mask = 1;
			for (int i = 1; i < 16; i++)
			{
				if ((value & mask) != (lastValue & mask))
				{
					boolean down = (value & mask) != 0;
					listeners.buttonChanged(new HydraButtonEvent(i, controller, down));
				}
				mask <<= 1;
			}
		}
		return value;
	}

	private void processSticks()
	{
		if (lastStickEvent.stick1[0] != data[0].joystick_x || lastStickEvent.stick1[1] != data[0].joystick_y
				|| lastStickEvent.stick2[0] != data[1].joystick_x || lastStickEvent.stick2[1] != data[1].joystick_y)
		{
			HydraStickEvent stickEvent = new HydraStickEvent();
			stickEvent.stick1[0] = data[0].joystick_x;
			stickEvent.stick1[1] = data[0].joystick_y;
			stickEvent.stick2[0] = data[1].joystick_x;
			stickEvent.stick2[1] = data[1].joystick_y;
			stickEvent.delta1[0] = stickEvent.stick1[0] - lastStickEvent.stick1[0];
			stickEvent.delta1[1] = stickEvent.stick1[1] - lastStickEvent.stick1[1];
			stickEvent.delta2[0] = stickEvent.stick2[0] - lastStickEvent.stick2[0];
			stickEvent.delta2[1] = stickEvent.stick2[1] - lastStickEvent.stick2[1];
			listeners.stickChanged(stickEvent);
			lastStickEvent = stickEvent;
		}
	}

	private void processTriggers()
	{
		if (lastTriggerEvent.trigger1 != data[0].trigger || lastTriggerEvent.trigger2 != data[1].trigger)
		{
			HydraTriggerEvent triggerEvent = new HydraTriggerEvent();
			triggerEvent.trigger1 = data[0].trigger;
			triggerEvent.trigger2 = data[1].trigger;
			triggerEvent.delta1 = triggerEvent.trigger1 - lastTriggerEvent.trigger1;
			triggerEvent.delta2 = triggerEvent.trigger2 - lastTriggerEvent.trigger2;
			listeners.triggerChanged(triggerEvent);
			lastTriggerEvent = triggerEvent;
		}
	}

	public void addListener(IHydraListener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(IHydraListener listener)
	{
		listeners.remove(listener);
	}
}
