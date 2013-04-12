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
package au.gov.ga.earthsci.common.math.vector;

/**
 * {@link Vector} implementation that represents a 1-dimensional vector.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@SuppressWarnings("serial")
public class Vector1 implements Vector<Vector1>
{
	public final static Vector1 ZERO = new Vector1(0);
	public final static Vector1 UNIT_X = new Vector1(1);

	public double x;

	public Vector1()
	{
	}

	public Vector1(Vector1 v)
	{
		set(v);
	}

	public Vector1(double x)
	{
		set(x);
	}

	@Override
	public Vector1 createNew()
	{
		return new Vector1();
	}

	@Override
	public Vector1 clone()
	{
		return new Vector1(this);
	}

	@Override
	public Vector1 set(Vector1 v)
	{
		return set(v.x);
	}

	public Vector1 set(double x)
	{
		this.x = x;
		return this;
	}

	@Override
	public Vector1 mult(Vector1 v)
	{
		return mult(v, null);
	}

	@Override
	public Vector1 mult(Vector1 v, Vector1 store)
	{
		if (store == null)
		{
			store = new Vector1();
		}
		store.x = x * v.x;
		return store;
	}

	@Override
	public Vector1 multLocal(Vector1 v)
	{
		x *= v.x;
		return this;
	}

	@Override
	public Vector1 mult(double s)
	{
		return mult(s, null);
	}

	@Override
	public Vector1 mult(double s, Vector1 store)
	{
		if (store == null)
		{
			store = new Vector1();
		}
		store.x = x * s;
		return store;
	}

	@Override
	public Vector1 multLocal(double s)
	{
		x *= s;
		return this;
	}

	@Override
	public Vector1 divide(Vector1 v)
	{
		return divide(v, null);
	}

	@Override
	public Vector1 divide(Vector1 v, Vector1 store)
	{
		if (store == null)
		{
			store = new Vector1();
		}
		store.x = x / v.x;
		return store;
	}

	@Override
	public Vector1 divideLocal(Vector1 v)
	{
		x /= v.x;
		return this;
	}

	@Override
	public Vector1 divide(double s)
	{
		return divide(s, null);
	}

	@Override
	public Vector1 divide(double s, Vector1 store)
	{
		if (store == null)
		{
			store = new Vector1();
		}
		store.x = x / s;
		return store;
	}

	@Override
	public Vector1 divideLocal(double s)
	{
		x /= s;
		return this;
	}

	@Override
	public Vector1 add(Vector1 v)
	{
		return add(v, null);
	}

	@Override
	public Vector1 add(Vector1 v, Vector1 store)
	{
		if (store == null)
		{
			store = new Vector1();
		}
		store.x = x + v.x;
		return store;
	}

	@Override
	public Vector1 addLocal(Vector1 v)
	{
		x += v.x;
		return this;
	}

	@Override
	public Vector1 subtract(Vector1 v)
	{
		return subtract(v, null);
	}

	@Override
	public Vector1 subtract(Vector1 v, Vector1 store)
	{
		if (store == null)
		{
			store = new Vector1();
		}
		store.x = x - v.x;
		return store;
	}

	@Override
	public Vector1 subtractLocal(Vector1 v)
	{
		x -= v.x;
		return this;
	}

	@Override
	public Vector1 max(Vector1 v)
	{
		return max(v, null);
	}

	@Override
	public Vector1 max(Vector1 v, Vector1 store)
	{
		if (store == null)
		{
			store = new Vector1();
		}
		store.x = Math.max(x, v.x);
		return store;
	}

	@Override
	public Vector1 maxLocal(Vector1 v)
	{
		x = Math.max(x, v.x);
		return this;
	}

	@Override
	public Vector1 min(Vector1 v)
	{
		return min(v, null);
	}

	@Override
	public Vector1 min(Vector1 v, Vector1 store)
	{
		if (store == null)
		{
			store = new Vector1();
		}
		store.x = Math.min(x, v.x);
		return store;
	}

	@Override
	public Vector1 minLocal(Vector1 v)
	{
		x = Math.min(x, v.x);
		return this;
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
	public Vector1 zeroLocal()
	{
		x = 0d;
		return this;
	}

	@Override
	public Vector1 negate()
	{
		return negate(null);
	}

	@Override
	public Vector1 negate(Vector1 store)
	{
		if (store == null)
		{
			store = new Vector1();
		}
		store.x = -x;
		return store;
	}

	@Override
	public Vector1 negateLocal()
	{
		x = -x;
		return this;
	}

	@Override
	public Vector1 interpolate(Vector1 v, double percent)
	{
		return interpolate(v, percent, null);
	}

	@Override
	public Vector1 interpolate(Vector1 v, double percent, Vector1 store)
	{
		if (store == null)
		{
			store = new Vector1();
		}
		store.x = interpolate(x, v.x, percent);
		return store;
	}

	@Override
	public Vector1 interpolateLocal(Vector1 v, double percent)
	{
		x = interpolate(x, v.x, percent);
		return this;
	}

	public static double interpolate(double d1, double d2, double percent)
	{
		return d1 * (1 - percent) + d2 * percent;
	}

	@Override
	public Vector1 normalize()
	{
		return normalize(null);
	}

	@Override
	public Vector1 normalize(Vector1 store)
	{
		if (store == null)
		{
			store = new Vector1();
		}
		store.divide(distance());
		return store;
	}

	@Override
	public Vector1 normalizeLocal()
	{
		return divideLocal(distance());
	}

	@Override
	public boolean equals(Object other)
	{
		return (other instanceof Vector1) && other != null && this.x == ((Vector1) other).x;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		return this.getClass().getName() + " (" + x + ")";
	}

}
