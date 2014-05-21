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

/**
 * Service that provides the current sun position.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface SunPositionService
{
	public static final SunPositionService INSTANCE = new SunPositionServiceImpl();

	enum SunPositionType
	{
		Constant,
		BehindCamera,
		RealTime,
		SpecificTime,
	}

	LatLon getPosition(View view);

	Vec4 getDirection(View view);

	SunPositionType getType();

	void setType(SunPositionType type);

	LatLon getConstant();

	void setConstant(LatLon latlon);

	Calendar getTime();

	void setTime(Calendar calendar);
}
