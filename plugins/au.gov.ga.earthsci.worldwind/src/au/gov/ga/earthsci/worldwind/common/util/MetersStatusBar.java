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
package au.gov.ga.earthsci.worldwind.common.util;

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.StatusBar;

/**
 * {@link StatusBar} subclass that displays altitude in meters if below 10 kms.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MetersStatusBar extends StatusBar
{
	@Override
	protected String makeEyeAltitudeDescription(double metersAltitude)
	{
		String s;
		String altitude = Logging.getMessage("term.Altitude");
		if (UNIT_IMPERIAL.equals(getElevationUnit()))
			return super.makeEyeAltitudeDescription(metersAltitude);
		else
		{
			if (metersAltitude < 1e4)
				s = String.format(altitude + " %,7d m", (int) Math.round(metersAltitude));
			else
				s = String.format(altitude + " %,7d km", (int) Math.round(metersAltitude / 1e3));
		}
		return s;
	}
}
