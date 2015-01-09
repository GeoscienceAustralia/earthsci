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
package au.gov.ga.earthsci.worldwind.common.retrieve;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Polyline} extension used to render a line around a sector.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SectorPolyline extends Polyline
{
	private final Sector sector;

	public SectorPolyline(Sector sector)
	{
		sector = sanitizeSector(sector);
		this.sector = sector;

		List<LatLon> latlons = new ArrayList<LatLon>();
		latlons.add(new LatLon(sector.getMinLatitude(), sector.getMinLongitude()));
		latlons.add(new LatLon(sector.getMinLatitude(), sector.getMaxLongitude()));
		latlons.add(new LatLon(sector.getMaxLatitude(), sector.getMaxLongitude()));
		latlons.add(new LatLon(sector.getMaxLatitude(), sector.getMinLongitude()));
		setPositions(latlons, 0);
		setFollowTerrain(true);
		setClosed(true);
		setPathType(LINEAR);
	}

	private static Sector sanitizeSector(Sector sector)
	{
		return new Sector(Angle.fromDegreesLatitude(sector.getMinLatitude().degrees),
				Angle.fromDegreesLatitude(sector.getMaxLatitude().degrees),
				Angle.fromDegreesLongitude(sector.getMinLongitude().degrees),
				Angle.fromDegreesLongitude(sector.getMaxLongitude().degrees));
	}

	@Override
	public void render(DrawContext dc)
	{
		try
		{
			super.render(dc);
		}
		catch (NullPointerException e)
		{
			//catch bug in Position.interpolate
			boolean followTerrain = isFollowTerrain();
			try
			{
				setFollowTerrain(false);
				super.render(dc);
			}
			finally
			{
				setFollowTerrain(followTerrain);
			}
		}
	}

	public Sector getSector()
	{
		return sector;
	}
}
