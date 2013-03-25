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
package au.gov.ga.earthsci.core.math.interpolation;

import gov.nasa.worldwind.cache.BasicMemoryCache;
import au.gov.ga.earthsci.core.math.bezier.Bezier;
import au.gov.ga.earthsci.core.math.vector.Vector;
import au.gov.ga.earthsci.core.util.Validate;


/**
 * An implementation of the {@link Interpolator} interface that performs interpolation
 * using cubic bezier curves.
 * <p/>
 * This interpolator needs to be primed with the four control points needed to define the bezier curve to use
 * for interpolation
 *
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BezierInterpolator<V extends Vector<V>> implements Interpolator<V>
{
	/** A cache for computed beziers */
	private static final int DEFAULT_CACHE_SIZE = 30000000;
	private static final BasicMemoryCache BEZIER_CACHE = new BasicMemoryCache((int)0.85*DEFAULT_CACHE_SIZE, DEFAULT_CACHE_SIZE);
	
	// The four points needed to describe the cubic bezier control curve
	/** The beginning value of the bezier. The curve will pass through this point. */
	private V begin;
	
	/** The control point to use when exiting the beginning point. */
	private V out;
	
	/** The control point to use when entering the end point. */
	private V in;
	
	/** The end value of the bezier. The curve will pass through this point. */
	private V end;
	
	/** The bezier curve to use to calculate the interpolation */
	private Bezier<V> bezier;
	
	/**
	 * Constructor. Initialises the control points.
	 */
	public BezierInterpolator(V begin, V out, V in, V end)
	{
		setControlPoints(begin, out, in, end);
	}
	
	@Override
	public V computeValue(double percent)
	{
		if (bezier == null) 
		{
			bezier = getBezier();
		}
		return bezier.pointAt(percent);
	}

	/**
	 * @return A bezier to use from the current control points
	 */
	@SuppressWarnings("unchecked")
	private Bezier<V> getBezier()
	{
		String cacheKey = createCacheKey();
		Bezier<V> result = (Bezier<V>)BEZIER_CACHE.getObject(cacheKey);
		if (result != null)
		{
			return result;
		}
		 
		result = new Bezier<V>(begin, out, in, end);
		BEZIER_CACHE.add(cacheKey, result, result.getNumSubdivisions() * (Double.SIZE / 8));
		return result;
	}

	/**
	 * @return A cache key to use for the current bezier values
	 */
	private String createCacheKey()
	{
		return "[" + begin + out + in + end + "]";  //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Set the four control points needed to define the bezier curve
	 * 
	 * @param begin The beginning value of the bezier. The curve will pass through this point.
	 * @param out The control point to use when exiting the beginning point
	 * @param in The control point to use when entering the end point
	 * @param end The end value of the bezier. The curve will pass through this point.
	 */
	public void setControlPoints(V begin, V out, V in, V end)
	{
		setBegin(begin);
		setEnd(end);
		setOut(out);
		setIn(in);
	}

	/**
	 * @return The beginning value of the bezier. The curve will pass through this point.
	 */
	public V getBegin()
	{
		return begin;
	}

	/**
	 * @param begin the begin value to set
	 */
	public void setBegin(V begin)
	{
		Validate.notNull(begin, "A begin value is required"); //$NON-NLS-1$
		if (this.begin == null || !this.begin.equals(begin)) {
			this.begin = begin;
			this.bezier = null;
		}
	}

	/**
	 * @return The control point to use when exiting the beginning point
	 */
	public V getOut()
	{
		return out;
	}

	/**
	 * @param out the out value to set
	 */
	public void setOut(V out)
	{
		Validate.notNull(out, "An out value is required"); //$NON-NLS-1$
		if (this.out == null || !this.out.equals(out)) {
			this.out = out;
			this.bezier = null;
		}
	}

	/**
	 * @return The control point to use when entering the end point
	 */
	public V getIn()
	{
		return in;
	}

	/**
	 * @param in the in value to set
	 */
	public void setIn(V in)
	{
		Validate.notNull(in, "An in value is required"); //$NON-NLS-1$
		if (this.in == null || !this.in.equals(in)) {
			this.in = in;
			this.bezier = null;
		}
	}

	/**
	 * @return The end value of the bezier. The curve will pass through this point.
	 */
	public V getEnd()
	{
		return end;
	}

	/**
	 * @param end the end value to set
	 */
	public void setEnd(V end)
	{
		Validate.notNull(end, "An end value is required"); //$NON-NLS-1$
		if (this.end == null || !this.end.equals(end)) {
			this.end = end;
			this.bezier = null;
		}
	}
	
}
