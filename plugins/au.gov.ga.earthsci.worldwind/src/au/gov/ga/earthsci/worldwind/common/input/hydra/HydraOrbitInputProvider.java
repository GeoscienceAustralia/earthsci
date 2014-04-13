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

import gov.nasa.worldwind.awt.ViewInputAttributes.ActionAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.DeviceAttributes;
import gov.nasa.worldwind.geom.Angle;
import au.gov.ga.earthsci.worldwind.common.input.IOrbitInputProvider;
import au.gov.ga.earthsci.worldwind.common.input.IProviderOrbitViewInputHandler;

/**
 * Input provider implementation for the Razer Hydra controller.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HydraOrbitInputProvider implements IOrbitInputProvider, IHydraListener
{
	private final static ActionAttributes horizontalAttributes = new ActionAttributes(2e-4, 1e2, false, 0.4);
	private final static ActionAttributes verticalAttributes = new ActionAttributes(1e0, 1e0, false, 0.85);
	private final static ActionAttributes headingAttributes = new ActionAttributes(1e2, 1e2, false, 0.85);
	private final static ActionAttributes pitchAttributes = new ActionAttributes(5e1, 1e2, false, 0.85);
	private final static DeviceAttributes deviceAttributes = new DeviceAttributes(1.0);

	private IProviderOrbitViewInputHandler inputHandler;
	private float x1, y1, x2, y2, z;
	private boolean flip1 = false, flip2 = false;
	private long lastNanos;

	public HydraOrbitInputProvider()
	{
		Hydra.getInstance().addListener(this);
	}

	@Override
	public void updated(HydraEvent event)
	{
	}

	@Override
	public void stickChanged(HydraStickEvent event)
	{
		lastNanos = System.nanoTime();
		x1 = event.stick1[0];
		y1 = event.stick1[1];
		x2 = event.stick2[0];
		y2 = event.stick2[1];

		if (inputHandler != null)
		{
			inputHandler.markViewChanged();
		}
	}

	@Override
	public void triggerChanged(HydraTriggerEvent event)
	{
		lastNanos = System.nanoTime();
		z = event.trigger1 - event.trigger2;

		if (inputHandler != null)
		{
			inputHandler.markViewChanged();
		}
	}

	@Override
	public void buttonChanged(HydraButtonEvent event)
	{
		if (event.button == HydraButtonEvent.STICK && !event.down)
		{
			if (event.controller == 1)
			{
				flip1 = !flip1;
			}
			else
			{
				flip2 = !flip2;
			}
		}
	}

	@Override
	public void apply(IProviderOrbitViewInputHandler inputHandler)
	{
		this.inputHandler = inputHandler;

		if (x1 != 0 || y1 != 0 || x2 != 0 || y2 != 0 || z != 0)
		{
			double mult1 = flip1 ? -1 : 1;
			double mult2 = flip2 ? -1 : 1;

			long currentNanos = System.nanoTime();
			double time = (currentNanos - lastNanos) / 1e9d;
			lastNanos = currentNanos;

			double translationAngle = Math.atan2(-x1 * mult1, y1 * mult1);
			double translationSpeed = Math.sqrt(x1 * x1 + y1 * y1);

			if (translationSpeed != 0)
			{
				inputHandler.onRotateFree(Angle.fromRadians(translationAngle),
						Angle.fromDegrees(time * translationSpeed), deviceAttributes, horizontalAttributes);
			}

			if (z != 0)
			{
				double zoomChange = time * z;
				inputHandler.onVerticalTranslate(zoomChange, zoomChange, deviceAttributes, verticalAttributes);
			}

			if (x2 != 0)
			{
				Angle headingMoveChange =
						Angle.fromDegrees(time * x2 * inputHandler.getScaleValueRotate(headingAttributes) * mult2);
				inputHandler.onRotateView(headingMoveChange, Angle.ZERO, headingAttributes);
			}

			if (y2 != 0)
			{
				Angle pitchChange = Angle.fromDegrees(time * -y2 * inputHandler.getScaleValueRotate(pitchAttributes));
				inputHandler.onRotateView(Angle.ZERO, pitchChange, pitchAttributes);
			}

			inputHandler.markViewChanged();
		}
	}
}
