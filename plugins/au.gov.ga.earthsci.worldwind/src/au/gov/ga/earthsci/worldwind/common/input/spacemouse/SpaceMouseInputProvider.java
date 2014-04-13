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

import gov.nasa.worldwind.awt.ViewInputAttributes.ActionAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.DeviceAttributes;
import gov.nasa.worldwind.geom.Angle;
import au.gov.ga.earthsci.worldwind.common.input.IOrbitInputProvider;
import au.gov.ga.earthsci.worldwind.common.input.IProviderOrbitViewInputHandler;

/**
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SpaceMouseInputProvider implements ISpaceMouseListener, IOrbitInputProvider
{
	private float xt, yt, zt, xr, zr;
	private long lastNanos;

	private final static ActionAttributes horizontalAttributes = new ActionAttributes(1e-6, 1e0, false, 0.4);
	private final static ActionAttributes verticalAttributes = new ActionAttributes(5e-3, 5e-3, false, 0.85);
	private final static ActionAttributes headingAttributes = new ActionAttributes(2e-1, 2e-1, false, 0.85);
	private final static ActionAttributes pitchAttributes = new ActionAttributes(1e-1, 2e-1, false, 0.85);
	private final static DeviceAttributes deviceAttributes = new DeviceAttributes(1.0);

	private IProviderOrbitViewInputHandler inputHandler;

	public SpaceMouseInputProvider()
	{
		SpaceMouse.getInstance().addListener(this);
	}

	@Override
	public void axisChanged(SpaceMouseAxisEvent event)
	{
		lastNanos = System.nanoTime();
		xt = event.values[0];
		yt = event.values[1];
		zt = event.values[2];
		xr = event.values[3];
		//yr = event.values[4];
		zr = event.values[5];

		if (inputHandler != null)
		{
			inputHandler.markViewChanged();
		}
	}

	@Override
	public void buttonChanged(SpaceMouseButtonEvent event)
	{
	}

	@Override
	public void apply(IProviderOrbitViewInputHandler inputHandler)
	{
		this.inputHandler = inputHandler;

		if (xt != 0 || yt != 0 || zt != 0 || xr != 0 || zr != 0)
		{
			long currentNanos = System.nanoTime();
			double time = (currentNanos - lastNanos) / 1e9d;
			lastNanos = currentNanos;

			double translationAngle = Math.atan2(-xt, -yt);
			//double translationAngle = Math.atan2(xt, yt);
			double translationSpeed = Math.sqrt(xt * xt + yt * yt);

			if (translationSpeed != 0)
			{
				inputHandler.onRotateFree(Angle.fromRadians(translationAngle),
						Angle.fromDegrees(time * translationSpeed), deviceAttributes, horizontalAttributes);
			}

			if (zt != 0)
			{
				double zoomChange = time * -zt;
				inputHandler.onVerticalTranslate(zoomChange, zoomChange, deviceAttributes, verticalAttributes);
			}

			if (zr != 0)
			{
				Angle headingMoveChange =
						Angle.fromDegrees(time * -zr * inputHandler.getScaleValueRotate(headingAttributes));
				inputHandler.onRotateView(headingMoveChange, Angle.ZERO, headingAttributes);
			}

			if (xr != 0)
			{
				Angle pitchChange = Angle.fromDegrees(time * xr * inputHandler.getScaleValueRotate(pitchAttributes));
				inputHandler.onRotateView(Angle.ZERO, pitchChange, pitchAttributes);
			}

			inputHandler.markViewChanged();
		}
	}
}
