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
package au.gov.ga.earthsci.worldwind.common.layers.curtain;

import gov.nasa.worldwind.cache.Cacheable;

/**
 * Defines a (sub) segment of a {@link Path}. Segments are defined in
 * percentages; a vertical 'top' and 'bottom' percentage of the path, and a
 * horizontal 'start' and 'end' of the path.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Segment implements Cacheable
{
	//percentages, between 0.0 and 1.0
	private final double start, end;
	private final double top, bottom;

	public final static Segment FULL = new Segment(0d, 1d, 0d, 1d);

	public Segment(double start, double end, double top, double bottom)
	{
		this.start = start;
		this.end = end;
		this.top = top;
		this.bottom = bottom;
	}

	public double getStart()
	{
		return start;
	}

	public double getEnd()
	{
		return end;
	}

	public double getTop()
	{
		return top;
	}

	public double getBottom()
	{
		return bottom;
	}

	public double getHorizontalCenter()
	{
		return 0.5 * (start + end);
	}

	public double getHorizontalDelta()
	{
		return end - start;
	}

	public double getVerticalCenter()
	{
		return 0.5 * (top + bottom);
	}

	public double getVerticalDelta()
	{
		return top - bottom;
	}

	@Override
	public long getSizeInBytes()
	{
		return 4 * Double.SIZE;
	}

	@Override
	public String toString()
	{
		return "(" + start + "," + top + " to " + end + "," + bottom + ")";
	}
}
