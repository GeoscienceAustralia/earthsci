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
package au.gov.ga.earthsci.core.math.vector;

/**
 * A subclass of {@link Vector2} that holds time (x) and value (y).
 * <p/>
 * Distance functions are replaced to calculate the distance on the time (x)
 * axis only.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class TimeVector extends Vector2 implements Vector<Vector2>
{
	private static final long serialVersionUID = 20100824L;

	public TimeVector()
	{
		super();
	}

	public TimeVector(double x, double y)
	{
		super(x, y);
	}

	public TimeVector(Vector2 v)
	{
		super(v);
	}

	@Override
	public double distanceSquared()
	{
		return x * x;
	}

	@Override
	public double distance()
	{
		return Math.abs(x);
	}

	@Override
	public Vector2 createNew()
	{
		return new TimeVector();
	}

	@Override
	public Vector2 clone()
	{
		return new TimeVector(this);
	}

	@Override
	public boolean equals(Object other)
	{
		return super.equals(other);
	}
}
