/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.view.orbit;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

/**
 * State of a {@link View}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IViewState
{
	/**
	 * @return View center
	 */
	Position getCenter();

	/**
	 * Set the view's center. This is both the look-at point at the viewport
	 * center, and the point around which the eye rotates.
	 * 
	 * @param center
	 */
	void setCenter(Position center);

	/**
	 * @return View zoom
	 */
	double getZoom();

	/**
	 * Set the view's zoom. This is defined as the distance between the eye and
	 * the center point.
	 * 
	 * @param zoom
	 */
	void setZoom(double zoom);

	/**
	 * @return View heading
	 */
	Angle getHeading();

	/**
	 * Set the view's heading. This is the rotation around the surface normal at
	 * the center point.
	 * 
	 * @param heading
	 */
	void setHeading(Angle heading);

	/**
	 * @return View pitch
	 */
	Angle getPitch();

	/**
	 * Set the view's pitch. This is the rotation around the side vector after
	 * the heading has been applied.
	 * 
	 * @param pitch
	 */
	void setPitch(Angle pitch);

	/**
	 * @return View roll
	 */
	Angle getRoll();

	/**
	 * Set the view's roll. This is the rotation around the Z-axis.
	 * 
	 * @param roll
	 */
	void setRoll(Angle roll);

	/**
	 * Get the rotation matrix that is applied to the modelview state, without
	 * including any translation. This is the matrix that rotates the center to
	 * the eye. This matrix will rotate:
	 * <ul>
	 * <li>the unit-X vector to the side vector</li>
	 * <li>the unit-Y vector to the up vector</li>
	 * <li>the unit-Z vector to the forward vector</li>
	 * </ul>
	 * 
	 * @param globe
	 * @return The view's rotation matrix
	 */
	Matrix getRotation(Globe globe);

	/**
	 * Get the inverse of {@link #getRotation(Globe)}.
	 * 
	 * @param globe
	 * @return The view's inverse rotation matrix
	 */
	Matrix getRotationInverse(Globe globe);

	/**
	 * The view's forward vector.
	 * 
	 * @param globe
	 * @return Forward vector
	 */
	Vec4 getForward(Globe globe);

	/**
	 * The view's up vector.
	 * 
	 * @param globe
	 * @return Up vector
	 */
	Vec4 getUp(Globe globe);

	/**
	 * The view's side vector.
	 * 
	 * @param globe
	 * @return Side vector
	 */
	Vec4 getSide(Globe globe);

	/**
	 * Get the transform matrix for the current state of the view, including
	 * rotation and translation. This can be used as the GL_MODELVIEW matrix.
	 * 
	 * @param globe
	 * @return Transform matrix
	 */
	Matrix getTransform(Globe globe);

	/**
	 * Get the view's center. This is the same as {@link #getCenter()}, but in
	 * cartesian coordinates.
	 * 
	 * @param globe
	 * @return View center
	 */
	Vec4 getCenterPoint(Globe globe);

	/**
	 * Get the view's eye position, in geographic coordinates.
	 * 
	 * @param globe
	 * @return View eye position
	 */
	Position getEye(Globe globe);

	/**
	 * Get the view's eye point. This is the same as {@link #getEye(Globe)}, but
	 * in cartesian coordinates.
	 * 
	 * @param globe
	 * @return View eye point
	 */
	Vec4 getEyePoint(Globe globe);

	/**
	 * Set the position of the eye.
	 * <p/>
	 * Implementations should change the heading/pitch/zoom to keep the center
	 * constant.
	 * 
	 * @param eye
	 *            New eye position
	 * @param globe
	 */
	void setEye(Position eye, Globe globe);
}