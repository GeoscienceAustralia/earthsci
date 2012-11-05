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
package au.gov.ga.earthsci.worldwind.common.view.stereo;

import gov.nasa.worldwind.geom.Matrix;
import au.gov.ga.earthsci.worldwind.common.view.state.ViewStateBasicOrbitView;

/**
 * Orbit view with {@link StereoView} support.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StereoOrbitView extends ViewStateBasicOrbitView implements StereoView
{
	private StereoViewHelper helper = new StereoViewHelper();

	@Override
	public void setup(boolean stereo, Eye eye)
	{
		helper.setup(stereo, eye);
	}

	@Override
	public boolean isStereo()
	{
		return helper.isStereo();
	}

	@Override
	public Eye getEye()
	{
		return helper.getEye();
	}

	@Override
	public StereoViewParameters getParameters()
	{
		return helper.getParameters();
	}

	@Override
	public void setParameters(StereoViewParameters parameters)
	{
		helper.setParameters(parameters);
	}

	@Override
	public double getCurrentFocalLength()
	{
		return helper.getCurrentFocalLength();
	}

	@Override
	public double getCurrentEyeSeparation()
	{
		return helper.getCurrentEyeSeparation();
	}

	@Override
	public Matrix calculateProjectionMatrix(double nearDistance, double farDistance)
	{
		return helper.calculateProjectionMatrix(this, nearDistance, farDistance);
	}

	@Override
	public void beforeComputeMatrices()
	{
		super.beforeComputeMatrices();
		helper.beforeComputeMatrices(this);
	}

	@Override
	public Matrix computeModelView()
	{
		Matrix matrix = super.computeModelView();
		return helper.transformModelView(matrix);
	}

	@Override
	public Matrix computeProjection()
	{
		return helper.computeProjection(this);
	}
}
