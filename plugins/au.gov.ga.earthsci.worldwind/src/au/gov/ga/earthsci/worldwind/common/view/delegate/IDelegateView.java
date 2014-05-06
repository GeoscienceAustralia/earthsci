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

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import au.gov.ga.earthsci.worldwind.common.render.DrawableSceneController;
import au.gov.ga.earthsci.worldwind.common.view.orbit.IViewState;

/**
 * View that can delegate methods to an {@link IViewDelegate} implementation.
 * MODELVIEW and PROJECTION matrix calculation can be delegated, as well as
 * custom drawing.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IDelegateView extends View
{
	/**
	 * @return The {@link IViewDelegate} this view is delegating to,
	 *         <code>null</code> if none
	 */
	IViewDelegate getDelegate();

	/**
	 * Set the {@link IViewDelegate} this view should delegate to. Passing
	 * <code>null</code> will cause the view to use a default implementation,
	 * generally defined by the superclass.
	 * <p/>
	 * Implementations should call
	 * {@link IViewDelegate#uninstalled(IDelegateView)} on the previously set
	 * delegate (if any), and {@link IViewDelegate#installed(IDelegateView)} on
	 * the new delegate.
	 * 
	 * @param delegate
	 */
	void setDelegate(IViewDelegate delegate);

	/**
	 * Called by doApply function, before the transform matrices are computed.
	 */
	void beforeComputeMatrices();

	/**
	 * Calculate a MODELVIEW transform for this view. Default implementation
	 * uses {@link IViewState#getTransform(Globe)}.
	 */
	Matrix computeModelView();

	/**
	 * Return the MODELVIEW transform/matrix that was calculated before being
	 * transformed in some way. Usually this is the value calculated by
	 * {@link #computeModelView()}, but some views can apply additional
	 * transformations (such as head tracking rotation in a HMD). This method
	 * returns the matrix before these additional transformations are applied.
	 * 
	 * @return Model view matrix that was calculated before being tranformed
	 */
	Matrix getPretransformedModelView();

	/**
	 * Calculates a PROJECTION transform for this view. Default implementation
	 * uses {@link Matrix#fromPerspective}. Uses this {@link View}'s current
	 * field-of-view value.
	 * 
	 * @param nearDistance
	 *            Near frustum value
	 * @param farDistance
	 *            Far frustum value
	 * @return Projection matrix
	 */
	Matrix computeProjection(double nearDistance, double farDistance);

	/**
	 * Calculates a PROJECTION transform for this view. Default implementation
	 * uses {@link Matrix#fromPerspective}.
	 * 
	 * @param horizontalFieldOfView
	 *            Horizontal field-of-view
	 * @param nearDistance
	 *            Near frustum value
	 * @param farDistance
	 *            Far frustum value
	 * @return Projection matrix
	 */
	Matrix computeProjection(Angle horizontalFieldOfView, double nearDistance, double farDistance);

	/**
	 * Draw this view.
	 * <p/>
	 * Implementation should call
	 * {@link DrawableSceneController#draw(DrawContext)} at least once.
	 * 
	 * @param dc
	 *            Current draw context
	 * @param sc
	 *            Scene controller performing the draw
	 */
	void draw(DrawContext dc, DrawableSceneController sc);
}
