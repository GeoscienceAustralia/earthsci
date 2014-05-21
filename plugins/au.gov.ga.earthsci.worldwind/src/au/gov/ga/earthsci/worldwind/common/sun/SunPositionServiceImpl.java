/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.sun;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Basic implementation of {@link SunPositionService}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SunPositionServiceImpl implements SunPositionService
{
	private SunPositionType type = SunPositionType.BehindCamera;
	private LatLon constant = LatLon.ZERO;
	private final Calendar currentTime = new GregorianCalendar();
	private LatLon currentTimeLatLon = SunCalculator.subsolarPoint(currentTime);
	private Calendar specificTime = new GregorianCalendar();
	private LatLon specificTimeLatLon = SunCalculator.subsolarPoint(specificTime);

	public SunPositionServiceImpl()
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					currentTime.setTimeInMillis(System.currentTimeMillis());
					currentTimeLatLon = SunCalculator.subsolarPoint(currentTime);
					try
					{
						//update every minute
						Thread.sleep(60000);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
		});
		thread.setName("Sun position updater"); //$NON-NLS-1$
		thread.setDaemon(true);
		thread.start();
	}

	@Override
	public LatLon getPosition(View view)
	{
		switch (type)
		{
		case Constant:
			return constant;
		case RealTime:
			return currentTimeLatLon;
		case SpecificTime:
			return specificTimeLatLon;
		default: //BehindCamera:
			return new LatLon(view.getEyePosition());
		}
	}

	@Override
	public Vec4 getDirection(View view)
	{
		return view.getGlobe().computePointFromLocation(getPosition(view)).normalize3();
	}

	@Override
	public SunPositionType getType()
	{
		return type;
	}

	@Override
	public void setType(SunPositionType type)
	{
		if (type == null)
		{
			throw new NullPointerException("type is null"); //$NON-NLS-1$
		}
		this.type = type;
	}

	@Override
	public LatLon getConstant()
	{
		return constant;
	}

	@Override
	public void setConstant(LatLon latlon)
	{
		this.constant = latlon;
	}

	@Override
	public Calendar getTime()
	{
		return specificTime;
	}

	@Override
	public void setTime(Calendar calendar)
	{
		this.specificTime = calendar;
		this.specificTimeLatLon = SunCalculator.subsolarPoint(calendar);
	}
}
