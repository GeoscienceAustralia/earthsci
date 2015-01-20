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
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.ViewUtil;
import gov.nasa.worldwind.view.orbit.OrbitView;
import au.gov.ga.earthsci.worldwind.common.input.IOrbitInputProvider;
import au.gov.ga.earthsci.worldwind.common.input.IProviderOrbitViewInputHandler;
import au.gov.ga.earthsci.worldwind.common.view.orbit.AbstractView;
import au.gov.ga.earthsci.worldwind.common.view.target.ITargetView;

/**
 * Input provider implementation for the Razer Hydra controller.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HydraOrbitInputProvider implements IOrbitInputProvider, IHydraListener
{
	private final static ActionAttributes horizontalAttributes = new ActionAttributes(3e-4, 1e2, false, 0.4);
	private final static ActionAttributes verticalAttributes = new ActionAttributes(1e0, 1e0, false, 0.85);
	private final static ActionAttributes headingAttributes = new ActionAttributes(1e2, 1e2, false, 0.85);
	private final static ActionAttributes pitchAttributes = new ActionAttributes(5e1, 1e2, false, 0.85);
	private final static DeviceAttributes deviceAttributes = new DeviceAttributes(1.0);
	private final static ActionAttributes flyHorizontalAttributes = new ActionAttributes(5e3, 5e7, false, 0.4);

	private IProviderOrbitViewInputHandler inputHandler;
	private float x1, y1, x2, y2, z;
	private boolean flip1 = true, flip2 = false;
	private long lastNanos;

	private boolean fly = false;

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
		if (!event.down)
		{
			if (event.button == HydraButtonEvent.STICK)
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
			else if (event.button == HydraButtonEvent.START)
			{
				fly = !fly;

				OrbitView view = inputHandler.getView();
				if (view instanceof ITargetView)
				{
					((ITargetView) view).setTargetMode(fly);
				}
			}
		}
	}

	@Override
	public void apply(IProviderOrbitViewInputHandler inputHandler)
	{
		this.inputHandler = inputHandler;

		if (x1 != 0 || y1 != 0 || x2 != 0 || y2 != 0 || z != 0)
		{
			long currentNanos = System.nanoTime();
			double time = (currentNanos - lastNanos) / 1e9d;
			lastNanos = currentNanos;

			if (!fly)
			{
				double mult1 = flip1 ? -1 : 1;
				double mult2 = flip2 ? -1 : 1;
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
					Angle pitchChange =
							Angle.fromDegrees(time * -y2 * inputHandler.getScaleValueRotate(pitchAttributes));
					inputHandler.onRotateView(Angle.ZERO, pitchChange, pitchAttributes);
				}
			}
			else
			{
				OrbitView view = inputHandler.getView();
				if (!(view instanceof AbstractView))
				{
					//AbstractView needed to get DrawContext below for altitude calculation
					return;
				}

				Globe globe = view.getGlobe();
				DrawContext dc = ((AbstractView) view).getDC();

				Position eyePosition = view.getEyePosition();
				Vec4 eyePoint = view.getEyePoint();
				Vec4 forward = view.getForwardVector();
				Vec4 up = view.getUpVector();
				Vec4 side = forward.cross3(up);

				double altitude = ViewUtil.computeElevationAboveSurface(dc, eyePosition);
				double radius = globe.getRadius();
				double[] range = flyHorizontalAttributes.getValues();
				double speed = getScaleValue(range[0], range[1], altitude, 3.0 * radius, true) * time;

				double eyeDistance = 10000;
				Vec4 centerPoint = eyePoint.add3(forward.multiply3(eyeDistance));
				centerPoint = centerPoint.add3(forward.multiply3(y1 * speed));
				centerPoint = centerPoint.add3(side.multiply3(x1 * speed));

				double altitudeChange = speed * z;
				Position centerPosition = globe.computePositionFromPoint(centerPoint);
				centerPosition = new Position(centerPosition, centerPosition.elevation + altitudeChange);

				double minimumAltitude = 100;
				double maximumAltitude = radius * 0.5;
				double centerAltitude = ViewUtil.computeElevationAboveSurface(dc, centerPosition);
				if (centerAltitude < minimumAltitude)
				{
					/*centerPosition = new Position(centerPosition,
							centerPosition.elevation - centerAltitude + minimumAltitude);*/
				}
				else if (centerAltitude > maximumAltitude)
				{
					centerPosition = new Position(centerPosition,
							centerPosition.elevation - centerAltitude + maximumAltitude);
				}

				view.setZoom(eyeDistance);
				view.setCenterPosition(centerPosition);
				view.setPitch(Angle.POS90);

				view.setHeading(view.getHeading().addDegrees(x2 * time * 100d));
			}

			inputHandler.markViewChanged();
		}
	}

	protected double getScaleValue(double minValue, double maxValue, double value, double range, boolean isExp)
	{
		double t = value / range;
		t = t < 0 ? 0 : (t > 1 ? 1 : t);
		if (isExp)
		{
			t = Math.pow(2.0, t) - 1.0;
		}
		return (minValue * (1.0 - t) + maxValue * t);
	}
}
