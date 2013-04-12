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
 * {@link Vector} implementation that represents a 3-dimensional vector.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@SuppressWarnings("serial")
public class Vector3 implements Vector<Vector3>
{
	public final static Vector3 ZERO = new Vector3(0, 0, 0);
	public final static Vector3 UNIT_X = new Vector3(1, 0, 0);
	public final static Vector3 UNIT_Y = new Vector3(0, 1, 0);
	public final static Vector3 UNIT_Z = new Vector3(0, 0, 1);

	public double x;
	public double y;
	public double z;

	public Vector3()
	{
	}

	public Vector3(Vector3 v)
	{
		set(v);
	}

	public Vector3(double x, double y, double z)
	{
		set(x, y, z);
	}

	@Override
	public Vector3 createNew()
	{
		return new Vector3();
	}

	@Override
	public Vector3 clone()
	{
		return new Vector3(this);
	}

	@Override
	public Vector3 set(Vector3 v)
	{
		return set(v.x, v.y, v.z);
	}

	public Vector3 set(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	@Override
	public Vector3 mult(Vector3 v)
	{
		return mult(v, null);
	}

	@Override
	public Vector3 mult(Vector3 v, Vector3 store)
	{
		if (store == null)
		{
			store = new Vector3();
		}
		store.x = x * v.x;
		store.y = y * v.y;
		store.z = z * v.z;
		return store;
	}

	@Override
	public Vector3 multLocal(Vector3 v)
	{
		x *= v.x;
		y *= v.y;
		z *= v.z;
		return this;
	}

	@Override
	public Vector3 mult(double s)
	{
		return mult(s, null);
	}

	@Override
	public Vector3 mult(double s, Vector3 store)
	{
		if (store == null)
		{
			store = new Vector3();
		}
		store.x = x * s;
		store.y = y * s;
		store.z = z * s;
		return store;
	}

	@Override
	public Vector3 multLocal(double s)
	{
		x *= s;
		y *= s;
		z *= s;
		return this;
	}

	@Override
	public Vector3 divide(Vector3 v)
	{
		return divide(v, null);
	}

	@Override
	public Vector3 divide(Vector3 v, Vector3 store)
	{
		if (store == null)
		{
			store = new Vector3();
		}
		store.x = x / v.x;
		store.y = y / v.y;
		store.z = z / v.z;
		return store;
	}

	@Override
	public Vector3 divideLocal(Vector3 v)
	{
		x /= v.x;
		y /= v.y;
		z /= v.z;
		return this;
	}

	@Override
	public Vector3 divide(double s)
	{
		return divide(s, null);
	}

	@Override
	public Vector3 divide(double s, Vector3 store)
	{
		if (store == null)
		{
			store = new Vector3();
		}
		store.x = x / s;
		store.y = y / s;
		store.z = z / s;
		return store;
	}

	@Override
	public Vector3 divideLocal(double s)
	{
		x /= s;
		y /= s;
		z /= s;
		return this;
	}

	@Override
	public Vector3 add(Vector3 v)
	{
		return add(v, null);
	}

	@Override
	public Vector3 add(Vector3 v, Vector3 store)
	{
		if (store == null)
		{
			store = new Vector3();
		}
		store.x = x + v.x;
		store.y = y + v.y;
		store.z = z + v.z;
		return store;
	}

	@Override
	public Vector3 addLocal(Vector3 v)
	{
		x += v.x;
		y += v.y;
		z += v.z;
		return this;
	}

	@Override
	public Vector3 subtract(Vector3 v)
	{
		return subtract(v, null);
	}

	@Override
	public Vector3 subtract(Vector3 v, Vector3 store)
	{
		if (store == null)
		{
			store = new Vector3();
		}
		store.x = x - v.x;
		store.y = y - v.y;
		store.z = z - v.z;
		return store;
	}

	@Override
	public Vector3 subtractLocal(Vector3 v)
	{
		x -= v.x;
		y -= v.y;
		z -= v.z;
		return this;
	}

	@Override
	public Vector3 max(Vector3 v)
	{
		return max(v, null);
	}

	@Override
	public Vector3 max(Vector3 v, Vector3 store)
	{
		if (store == null)
		{
			store = new Vector3();
		}
		store.x = Math.max(x, v.x);
		store.y = Math.max(y, v.y);
		store.z = Math.max(z, v.z);
		return store;
	}

	@Override
	public Vector3 maxLocal(Vector3 v)
	{
		x = Math.max(x, v.x);
		y = Math.max(y, v.y);
		z = Math.max(z, v.z);
		return this;
	}

	@Override
	public Vector3 min(Vector3 v)
	{
		return min(v, null);
	}

	@Override
	public Vector3 min(Vector3 v, Vector3 store)
	{
		if (store == null)
		{
			store = new Vector3();
		}
		store.x = Math.min(x, v.x);
		store.y = Math.min(y, v.y);
		store.z = Math.min(z, v.z);
		return store;
	}

	@Override
	public Vector3 minLocal(Vector3 v)
	{
		x = Math.min(x, v.x);
		y = Math.min(y, v.y);
		z = Math.min(z, v.z);
		return this;
	}

	@Override
	public double distanceSquared()
	{
		return x * x + y * y + z * z;
	}

	@Override
	public double distance()
	{
		return Math.sqrt(distanceSquared());
	}

	@Override
	public Vector3 zeroLocal()
	{
		x = 0d;
		y = 0d;
		z = 0d;
		return this;
	}

	@Override
	public Vector3 negate()
	{
		return negate(null);
	}

	@Override
	public Vector3 negate(Vector3 store)
	{
		if (store == null)
		{
			store = new Vector3();
		}
		store.x = -x;
		store.y = -y;
		store.z = -z;
		return store;
	}

	@Override
	public Vector3 negateLocal()
	{
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	@Override
	public Vector3 interpolate(Vector3 v, double percent)
	{
		return interpolate(v, percent, null);
	}

	@Override
	public Vector3 interpolate(Vector3 v, double percent, Vector3 store)
	{
		if (store == null)
		{
			store = new Vector3();
		}
		store.x = interpolate(x, v.x, percent);
		store.y = interpolate(y, v.y, percent);
		store.z = interpolate(z, v.z, percent);
		return store;
	}

	@Override
	public Vector3 interpolateLocal(Vector3 v, double percent)
	{
		x = interpolate(x, v.x, percent);
		y = interpolate(y, v.y, percent);
		z = interpolate(z, v.z, percent);
		return this;
	}

	public static double interpolate(double d1, double d2, double percent)
	{
		return d1 * (1 - percent) + d2 * percent;
	}

	@Override
	public Vector3 normalize()
	{
		return normalize(null);
	}

	@Override
	public Vector3 normalize(Vector3 store)
	{
		if (store == null)
		{
			store = new Vector3();
		}
		store.divide(distance());
		return store;
	}

	@Override
	public Vector3 normalizeLocal()
	{
		return divideLocal(distance());
	}

	@Override
	public boolean equals(Object other)
	{
		return (other instanceof Vector3) && other != null && this.x == ((Vector3) other).x
				&& this.y == ((Vector3) other).y && this.z == ((Vector3) other).z;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		return this.getClass().getName() + " (" + x + ", " + y + ", " + z + ")";
	}
}
