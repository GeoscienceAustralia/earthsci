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
package au.gov.ga.earthsci.core.math.bezier;

import java.io.Serializable;

import au.gov.ga.earthsci.common.util.Validate;
import au.gov.ga.earthsci.core.math.vector.Vector;

/**
 * An implementation of a Cubic Bezier curve.
 * <p/>
 * This class provides helper methods for retrieving the value of the curve at a
 * point along the curve.
 * <p/>
 * This implementation is immutable and discretises the curve by sampling at a
 * specified number of points along the curve.
 * 
 * @see http://en.wikipedia.org/wiki/B%C3%A9zier_curve
 * 
 * @param <V>
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Bezier<V extends Vector<V>> implements Serializable
{
	private static final long serialVersionUID = 20100823L;

	/** The default number of points to sample along the curve */
	private final static int DEFAULT_NUM_SUBDIVISIONS = 1000;

	/** The number of points to sample along the curve */
	private final int numSubdivisions;

	// The 4 control points needed to define the cubic bezier
	/**
	 * The beginning value of the bezier. The curve will pass through this
	 * point.
	 */
	private final V begin;

	/** The control point to use when exiting the beginning point. */
	private final V out;

	/** The control point to use when entering the end point. */
	private final V in;

	/** The end value of the bezier. The curve will pass through this point. */
	private final V end;

	/** The sampled points along the curve */
	private double[] percents;

	/** The length of the curve */
	private double length;

	/**
	 * Construct a new Bezier with the provided control points, using the
	 * default number of subdivisions to sample the curve.
	 * 
	 * @param begin
	 *            The beginning value of the bezier. The curve will pass through
	 *            this point.
	 * @param out
	 *            The control point to use when exiting the beginning point
	 * @param in
	 *            The control point to use when entering the end point
	 * @param end
	 *            The end value of the bezier. The curve will pass through this
	 *            point.
	 */
	public Bezier(V begin, V out, V in, V end)
	{
		this(begin, out, in, end, DEFAULT_NUM_SUBDIVISIONS);
	}

	/**
	 * Construct a new Bezier with the provided control points, using the
	 * provided number of subdivisions to sample the curve.
	 * 
	 * @param begin
	 *            The beginning value of the bezier. The curve will pass through
	 *            this point.
	 * @param out
	 *            The control point to use when exiting the beginning point
	 * @param in
	 *            The control point to use when entering the end point
	 * @param end
	 *            The end value of the bezier. The curve will pass through this
	 *            point.
	 * @param numSubdivisions
	 *            The number of subdivisions to use to sample the curve
	 */
	public Bezier(V begin, V out, V in, V end, int numSubdivisions)
	{
		Validate.notNull(begin, "A begin value is required"); //$NON-NLS-1$
		Validate.notNull(out, "An out value is required"); //$NON-NLS-1$
		Validate.notNull(in, "A in value is required"); //$NON-NLS-1$
		Validate.notNull(end, "A end value is required"); //$NON-NLS-1$

		this.begin = begin;
		this.end = end;
		this.out = out;
		this.in = in;
		this.numSubdivisions = numSubdivisions;
		subdivide();
	}

	/**
	 * Samples the bezier curve at {@link #numSubdivisions} points and populates
	 * the {@link #percents} array.
	 */
	private void subdivide()
	{
		percents = new double[numSubdivisions];
		length = 0d;

		V begin = this.begin.clone();
		V end;

		for (int i = 0; i < numSubdivisions; i++)
		{
			double t = (i + 1) / (double) numSubdivisions;
			end = bezierPointAt(t);
			length += begin.subtractLocal(end).distance();
			percents[i] = length;
			begin = end;
		}

		if (length > 0d)
		{
			for (int i = 0; i < numSubdivisions; i++)
			{
				percents[i] /= length;
			}
		}
		percents[numSubdivisions - 1] = 1d;
	}

	/**
	 * @return The length of the curve
	 */
	public double getLength()
	{
		return length;
	}

	/**
	 * Obtain the point on the curve that is <code>percent</code> of the way
	 * along the curve
	 * 
	 * @param percent
	 *            The percent along the curve to sample. In range
	 *            <code>[0,1]</code>
	 * 
	 * @return The value at the given percentage along the curve
	 */
	public V pointAt(double percent)
	{
		if (percent < 0 || percent > 1)
		{
			throw new IllegalArgumentException("Percent must be in range [0,1]. Value '" + percent + "' is illegal."); //$NON-NLS-1$ //$NON-NLS-2$
		}

		int i = 0;
		while (i < percents.length - 1 && percents[i] <= percent)
		{
			i++;
		}

		double percentStart = i > 0 ? percents[i - 1] : 0d;
		double percentWindow = percents[i] - percentStart;
		double p = (percent - percentStart) / percentWindow;
		percent = (p + i) / numSubdivisions;

		return bezierPointAt(percent);
	}

	/**
	 * Evaluates the bezier curve at the parametric point <code>t</code>
	 * 
	 * @return The value on the curve at the parametric point <code>t</code>
	 */
	private V bezierPointAt(double t)
	{
		double t2 = t * t;
		V c = out.subtract(begin).multLocal(3d);
		V b = in.subtract(out).multLocal(3d).subtractLocal(c);
		V a = end.subtract(begin).subtractLocal(c).subtractLocal(b);
		a.multLocal(t2 * t);
		b.multLocal(t2);
		c.multLocal(t);
		a.addLocal(b).addLocal(c).addLocal(begin);
		return a;
	}

	@SuppressWarnings("unused")
	private V deCasteljau(double t)
	{
		V v1 = this.out.subtract(this.begin).multLocal(t).addLocal(this.begin);
		V v2 = this.in.subtract(this.out).multLocal(t).addLocal(this.out);
		V v3 = this.end.subtract(this.in).multLocal(t).addLocal(this.in);

		v3 = v3.subtractLocal(v2).multLocal(t).addLocal(v2);
		v2 = v2.subtractLocal(v1).multLocal(t).addLocal(v1);

		return v3.subtractLocal(v2).multLocal(t).addLocal(v2);
	}

	/**
	 * @return The number or subdivisions used in this bezier
	 */
	public int getNumSubdivisions()
	{
		return numSubdivisions;
	}
}
