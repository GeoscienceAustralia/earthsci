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
package au.gov.ga.earthsci.worldwind.common.view.delegate;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.render.DrawContext;
import au.gov.ga.earthsci.worldwind.common.render.DrawableSceneController;

/**
 * View delegate. An instance of {@link IDelegateView} can delegate its
 * MODELVIEW and PROJECTION matrices computation to implementations of this
 * interface. These can also be used for custom draw logic.
 * <p/>
 * An example view delegate could be a stereo view, which calculates custom
 * transform matrices for the left/right eye views, and performs custom drawing
 * such as drawing to the left/right OpenGL buffers using quad-buffering.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IViewDelegate
{
	/**
	 * Called when this delegate is set as the delegate for the given view,
	 * using {@link IDelegateView#setDelegate(IViewDelegate)}.
	 * 
	 * @param view
	 *            View for which this is a delegate
	 */
	void installed(IDelegateView view);

	/**
	 * Called when this delegate is no longer the delegate for the given view.
	 * 
	 * @param view
	 *            View for which this is no longer a delegate
	 */
	void uninstalled(IDelegateView view);

	/**
	 * Called by doApply function, before the transform matrices are computed.
	 * <p/>
	 * Can call {@link IDelegateView#beforeComputeMatrices()} by default.
	 * 
	 * @param view
	 *            View calling this delegate
	 */
	void beforeComputeMatrices(IDelegateView view);

	/**
	 * Calculate a MODELVIEW transform for this view.
	 * <p/>
	 * Can call {@link IDelegateView#computeModelView()} for a default
	 * implementation.
	 * 
	 * @param view
	 *            View calling this delegate
	 */
	Matrix computeModelView(IDelegateView view);

	/**
	 * Return the MODELVIEW transform/matrix that was calculated before being
	 * transformed in some way. Usually this is the same as the value returned
	 * by {@link #computeModelView(IDelegateView)}, but some views can apply
	 * additional transformations (such as head tracking rotation in a HMD).
	 * This method returns the matrix before these additional transformations
	 * are applied.
	 * <p/>
	 * Can call {@link IDelegateView#getPretransformedModelView()} for a default
	 * implementation.
	 * 
	 * @param view
	 *            View calling this delegate
	 * @return Model view matrix that was calculated before being tranformed
	 */
	Matrix getPretransformedModelView(IDelegateView view);

	/**
	 * Calculates a PROJECTION transform for this view.
	 * <p/>
	 * Cann call {@link IDelegateView#computeProjection(double, double)} for a
	 * default implementation.
	 * 
	 * @param view
	 *            View calling this delegate
	 * @param horizontalFieldOfView
	 *            Horizontal field-of-view
	 * @param nearDistance
	 *            Near frustum value
	 * @param farDistance
	 *            Far frustum value
	 * @return Projection matrix
	 */
	Matrix computeProjection(IDelegateView view, Angle horizontalFieldOfView, double nearDistance, double farDistance);

	/**
	 * Pick this view.
	 * <p/>
	 * Can call {@link IDelegateView#pick(DrawContext, DrawableSceneController)}
	 * for a default implementation.
	 * 
	 * @param view
	 *            View calling this delegate
	 * @param dc
	 *            Current draw context
	 * @param sc
	 *            Scene controller performing the pick
	 */
	void pick(IDelegateView view, DrawContext dc, DrawableSceneController sc);

	/**
	 * Draw this view.
	 * <p/>
	 * Can call {@link IDelegateView#draw(DrawContext, DrawableSceneController)}
	 * for a default implementation.
	 * 
	 * @param view
	 *            View calling this delegate
	 * @param dc
	 *            Current draw context
	 * @param sc
	 *            Scene controller performing the draw
	 */
	void draw(IDelegateView view, DrawContext dc, DrawableSceneController sc);
}
