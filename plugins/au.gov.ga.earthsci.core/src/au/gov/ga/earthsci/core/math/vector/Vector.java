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

import java.io.Serializable;

/**
 * Vector interface, represents a single-dimensional or multi-dimensional
 * vector. Vectors are mutable.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <V>
 *            Vector type (dimension)
 */
public interface Vector<V> extends Cloneable, Serializable
{
	/**
	 * @return A new instance of this vector
	 */
	public V createNew();

	/**
	 * @return A copy of this vector
	 */
	public V clone();

	/**
	 * Set this vector's components to the components in the provided vector.
	 * 
	 * @param v
	 *            Vector to set this vector's components to
	 * @return This vector
	 */
	public V set(V v);

	/**
	 * Multiply this vector by the provided vector, and return the result as a
	 * new vector.
	 * 
	 * @param v
	 *            Vector to multiply by
	 * @return New vector equal to the product of the two vectors
	 */
	public V mult(V v);

	/**
	 * Multiply this vector by the provided vector, storing the result in the
	 * provided store.
	 * 
	 * @param v
	 *            Vector to multiply by
	 * @param store
	 *            Vector to store the product in (if null, a new vector is
	 *            returned)
	 * @return store (or a new vector if store is null) containing the product
	 *         of the two vectors
	 */
	public V mult(V v, V store);

	/**
	 * Multiply this vector by the provided vector, storing the result in this
	 * vector.
	 * 
	 * @param v
	 *            Vector to multiply by
	 * @return This vector
	 */
	public V multLocal(V v);

	/**
	 * Multiply this vector by the provided scalar, and return the result as a
	 * new vector.
	 * 
	 * @param s
	 *            Scalar to multiply by
	 * @return New vector equal to the product of this vector and the scalar
	 */
	public V mult(double s);

	/**
	 * Multiply this vector by the provided scalar, storing the result in the
	 * provided store.
	 * 
	 * @param s
	 *            Scalar to multiply by
	 * @param store
	 *            Vector to store the product in (if null, a new vector is
	 *            returned)
	 * @return store (or a new vector if store is null) containing the product
	 *         of this vector and the scalar
	 */
	public V mult(double s, V store);

	/**
	 * Multiply this vector by the provided scalar, storing the result in this
	 * vector.
	 * 
	 * @param s
	 *            Scalar to multiply by
	 * @return This vector
	 */
	public V multLocal(double s);

	/**
	 * Divide this vector by the provided vector, and return the result as a new
	 * vector.
	 * 
	 * @param v
	 *            Vector to divide by
	 * @return New vector equal to the division of the two vectors
	 */
	public V divide(V v);

	/**
	 * Divide this vector by the provided vector, storing the result in the
	 * provided store.
	 * 
	 * @param v
	 *            Vector to divide by
	 * @param store
	 *            Vector to store the division in (if null, a new vector is
	 *            returned)
	 * @return store (or a new vector if store is null) containing the division
	 *         of the two vectors
	 */
	public V divide(V v, V store);

	/**
	 * Divide this vector by the provided vector, storing the result in this
	 * vector.
	 * 
	 * @param v
	 *            Vector to divide by
	 * @return This vector
	 */
	public V divideLocal(V v);

	/**
	 * Divide this vector by the provided scalar, and return the result as a new
	 * vector.
	 * 
	 * @param s
	 *            Scalar to divide by
	 * @return New vector equal to the division of this vector and the scalar
	 */
	public V divide(double s);

	/**
	 * Divide this vector by the provided scalar, storing the result in the
	 * provided store.
	 * 
	 * @param s
	 *            Scalar to divide by
	 * @param store
	 *            Vector to store the division in (if null, a new vector is
	 *            returned)
	 * @return store (or a new vector if store is null) containing the division
	 *         of this vector and the scalar
	 */
	public V divide(double s, V store);

	/**
	 * Divide this vector by the provided scalar, storing the result in this
	 * vector.
	 * 
	 * @param s
	 *            Scalar to divide by
	 * @return This vector
	 */
	public V divideLocal(double s);

	/**
	 * Add the provided vector to this vector, and return the result as a new
	 * vector.
	 * 
	 * @param v
	 *            Vector to add
	 * @return New vector equal to the sum of the two vectors
	 */
	public V add(V v);

	/**
	 * Add the provided vector to this vector, storing the result in the
	 * provided store.
	 * 
	 * @param v
	 *            Vector to add
	 * @param store
	 *            Vector to store the sum in (if null, a new vector is returned)
	 * @return store (or a new vector if store is null) containing the sum of
	 *         the two vectors
	 */
	public V add(V v, V store);

	/**
	 * Add the provided vector to this vector, storing the result in this
	 * vector.
	 * 
	 * @param v
	 *            Vector to add
	 * @return This vector
	 */
	public V addLocal(V v);

	/**
	 * Subtract the provided vector from this vector, and return the result as a
	 * new vector.
	 * 
	 * @param v
	 *            Vector to subtract
	 * @return New vector equal to the difference of the two vectors
	 */
	public V subtract(V v);

	/**
	 * Subtract the provided vector from this vector, storing the result in the
	 * provided store.
	 * 
	 * @param v
	 *            Vector to subtract
	 * @param store
	 *            Vector to store the difference in (if null, a new vector is
	 *            returned)
	 * @return store (or a new vector if store is null) containing the
	 *         difference of the two vectors
	 */
	public V subtract(V v, V store);

	/**
	 * Subtract the provided vector from this vector, storing the result in this
	 * vector.
	 * 
	 * @param v
	 *            Vector to subtract
	 * @return This vector
	 */
	public V subtractLocal(V v);

	/**
	 * Determine the maximum of each component between this vector and the
	 * provided vector, and return the maximums in a new vector.
	 * 
	 * @param v
	 *            Vector to compare
	 * @return New vector containing the maximum components between the two
	 *         vectors
	 */
	public V max(V v);

	/**
	 * Determine the maximum of each component between this vector and the
	 * provided vector, and store the maximums in the provided store.
	 * 
	 * @param v
	 *            Vector to compare
	 * @param store
	 *            Vector to store the result in
	 * @return store (or a new vector if store is null) containing the maximum
	 *         components between the two vectors
	 */
	public V max(V v, V store);

	/**
	 * Determine the maximum of each component between this vector and the
	 * provided vector, and store the maximums in this vector.
	 * 
	 * @param v
	 *            Vector to compare
	 * @return This vector
	 */
	public V maxLocal(V v);

	/**
	 * Determine the minimum of each component between this vector and the
	 * provided vector, and return the minimums in a new vector.
	 * 
	 * @param v
	 *            Vector to compare
	 * @return New vector containing the minimum components between the two
	 *         vectors
	 */
	public V min(V v);

	/**
	 * Determine the minimum of each component between this vector and the
	 * provided vector, and store the minimums in the provided store.
	 * 
	 * @param v
	 *            Vector to compare
	 * @param store
	 *            Vector to store the result in
	 * @return store (or a new vector if store is null) containing the minimum
	 *         components between the two vectors
	 */
	public V min(V v, V store);

	/**
	 * Determine the minimum of each component between this vector and the
	 * provided vector, and store the minimums in this vector.
	 * 
	 * @param v
	 *            Vector to compare
	 * @return This vector
	 */
	public V minLocal(V v);

	/**
	 * @return The squared distance from this vector to the origin (squared
	 *         vector length)
	 */
	public double distanceSquared();

	/**
	 * @return The distance from this vector to the origin (vector length)
	 */
	public double distance();

	/**
	 * Set this vector's components to zero.
	 * 
	 * @return This vector
	 */
	public V zeroLocal();

	/**
	 * Negate this vector's components, and return the result in a new vector.
	 * 
	 * @return A new vector containing the negation of this vector's components
	 */
	public V negate();

	/**
	 * Negate this vector's components, and store the result in the provided
	 * store.
	 * 
	 * @param store
	 *            Vector to store the negation result in
	 * @return store (or a new vector if store is null) containing the negation
	 *         of this vector's components
	 */
	public V negate(V store);

	/**
	 * Negate this vector's components, and store the result in this vector.
	 * 
	 * @return This vector
	 */
	public V negateLocal();

	/**
	 * Interpolate between this vector and the provided vector, and return the
	 * result in a new vector.
	 * 
	 * @param v
	 *            Vector to interpolate to
	 * @param percent
	 *            Amount to interpolate, between 0 and 1
	 * @return New vector containing the interpolation between this vector and
	 *         the provided vector at the provided percentage
	 */
	public V interpolate(V v, double percent);

	/**
	 * Interpolate between this vector and the provided vector, and store the
	 * result in the provided store.
	 * 
	 * @param v
	 *            Vector to interpolate to
	 * @param percent
	 *            Amount to interpolate, between 0 and 1
	 * @param store
	 *            Vector to store the interpolation result in
	 * @return store (or a new vector if store is null) containing the
	 *         interpolation between this vector and the provided vector at the
	 *         provided percentage
	 */
	public V interpolate(V v, double percent, V store);

	/**
	 * Interpolate between this vector and the provided vector, and store this
	 * result in this vector.
	 * 
	 * @param v
	 *            Vector to interpolate to
	 * @param percent
	 *            Amount to interpolate, between 0 and 1
	 * @return This vector
	 */
	public V interpolateLocal(V v, double percent);

	/**
	 * Normalize this vector (such that length == 1), and return the result in a
	 * new vector.
	 * 
	 * @return A new vector containing the normalization of this vector
	 */
	public V normalize();

	/**
	 * Normalize this vector (such that length == 1), and store the result in
	 * the provided store.
	 * 
	 * @param store
	 *            Vector in which to store the normalization result
	 * @return store (or a new vector if store is null) containing the
	 *         normalization of this vector
	 */
	public V normalize(V store);

	/**
	 * Normalize this vector (such that length == 1), and store the result in
	 * this vector.
	 * 
	 * @return This vector
	 */
	public V normalizeLocal();
}
