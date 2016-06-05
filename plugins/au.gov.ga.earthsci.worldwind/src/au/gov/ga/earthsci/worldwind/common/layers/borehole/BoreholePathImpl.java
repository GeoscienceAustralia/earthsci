/*******************************************************************************
 * Copyright 2016 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.layers.borehole;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Basic implementation of {@link BoreholePath}.
 *
 * @author Michael de Hoog
 */
public class BoreholePathImpl implements BoreholePath
{
	private final NavigableMap<Double, Position> positions = new TreeMap<Double, Position>();

	@Override
	public SortedMap<Double, Position> getPositions()
	{
		return positions;
	}

	@Override
	public void addPosition(double measuredDepth, Position position)
	{
		positions.put(measuredDepth, position);
	}

	@Override
	public Position getPosition(double measuredDepth)
	{
		if (positions.isEmpty())
		{
			return null;
		}
		if (positions.size() == 1)
		{
			//only one position, so extrapolate out
			Entry<Double, Position> firstEntry = positions.firstEntry();
			Position position = firstEntry.getValue();
			double elevationDelta = firstEntry.getKey() - measuredDepth;
			return new Position(position, position.elevation + elevationDelta);
		}
		Entry<Double, Position> floor = positions.floorEntry(measuredDepth);
		Entry<Double, Position> ceiling = positions.ceilingEntry(measuredDepth);
		if (floor == null)
		{
			floor = ceiling;
			ceiling = positions.higherEntry(floor.getKey());
		}
		if (ceiling == null)
		{
			ceiling = floor;
			floor = positions.lowerEntry(ceiling.getKey());
		}

		double amount = (measuredDepth - floor.getKey()) / (ceiling.getKey() - floor.getKey());
		Position floorPosition = floor.getValue();
		Position ceilingPosition = ceiling.getValue();

		//can't use Position.interpolate, because amount is clamped when interpolating elevation
		LatLon latlon = LatLon.interpolate(amount, floorPosition, ceilingPosition);
		double elevation = floorPosition.elevation + amount * (ceilingPosition.elevation - floorPosition.elevation);
		return new Position(latlon, elevation);
	}

}
