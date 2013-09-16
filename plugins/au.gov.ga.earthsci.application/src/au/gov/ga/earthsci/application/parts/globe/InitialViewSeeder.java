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
package au.gov.ga.earthsci.application.parts.globe;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Earth;

import java.net.URL;
import java.util.Collection;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.seeder.ISeeder;
import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * Seeder that sets the initial view of the globe parts.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class InitialViewSeeder implements ISeeder
{
	@Override
	public void seed(Element element, URL context)
	{
		XPath xpath = XMLUtil.makeXPath();

		Double latitude = XMLUtil.getDouble(element, "Latitude", xpath); //$NON-NLS-1$
		Double longitude = XMLUtil.getDouble(element, "Longitude", xpath); //$NON-NLS-1$
		Double altitude = XMLUtil.getDouble(element, "Altitude", xpath); //$NON-NLS-1$
		Double heading = XMLUtil.getDouble(element, "Heading", xpath); //$NON-NLS-1$
		Double pitch = XMLUtil.getDouble(element, "Pitch", xpath); //$NON-NLS-1$

		if (latitude == null && longitude == null && altitude == null && heading == null && pitch == null)
		{
			return;
		}

		if (latitude != null)
		{
			Configuration.setValue(AVKey.INITIAL_LATITUDE, latitude);
		}
		if (longitude != null)
		{
			Configuration.setValue(AVKey.INITIAL_LONGITUDE, longitude);
		}
		if (altitude != null)
		{
			Configuration.setValue(AVKey.INITIAL_ALTITUDE, altitude);
		}
		if (heading != null)
		{
			Configuration.setValue(AVKey.INITIAL_HEADING, heading);
		}
		if (pitch != null)
		{
			Configuration.setValue(AVKey.INITIAL_PITCH, pitch);
		}

		latitude = Configuration.getDoubleValue(AVKey.INITIAL_LATITUDE);
		longitude = Configuration.getDoubleValue(AVKey.INITIAL_LONGITUDE);
		altitude = Configuration.getDoubleValue(AVKey.INITIAL_ALTITUDE);
		heading = Configuration.getDoubleValue(AVKey.INITIAL_HEADING);
		pitch = Configuration.getDoubleValue(AVKey.INITIAL_PITCH);

		if (latitude == null)
		{
			latitude = 0d;
		}
		if (longitude == null)
		{
			longitude = 0d;
		}
		if (altitude == null)
		{
			altitude = 3d * Earth.WGS84_EQUATORIAL_RADIUS;
		}
		if (heading == null)
		{
			heading = 0d;
		}
		if (pitch == null)
		{
			pitch = 0d;
		}

		Collection<WorldWindow> worldWindows = WorldWindowRegistry.INSTANCE.getAll();
		for (WorldWindow ww : worldWindows)
		{
			ww.getView().setEyePosition(new Position(LatLon.fromDegrees(latitude, longitude), altitude));
			ww.getView().setPitch(Angle.fromDegrees(pitch));
			ww.getView().setHeading(Angle.fromDegrees(heading));
		}
	}
}
