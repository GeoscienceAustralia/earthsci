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
package au.gov.ga.earthsci.worldwind.common.layers.transform;

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.OGLStackHandler;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.view.transform.TransformView;

/**
 * An extension of the {@link SkyGradientLayer} that, if the view is a
 * {@link TransformView}, correctly calculates the projection matrix so that the
 * sky gradient is correctly offset for the transformed projection matrix.
 */
public class TransformSkyGradientLayer extends SkyGradientLayer
{
	@Override
	protected void applyDrawProjection(DrawContext dc, OGLStackHandler ogsh)
	{
		boolean loaded = false;
		if (dc.getView() instanceof TransformView)
		{
			TransformView transform = (TransformView) dc.getView();
			//near is the distance from the origin
			double near = 100;
			double far = transform.getHorizonDistance() + 10e3;
			Matrix projection = transform.computeProjection(near, far);

			if (projection != null)
			{
				double[] matrixArray = new double[16];
				GL2 gl = dc.getGL().getGL2();
				ogsh.pushProjection(gl);

				projection.toArray(matrixArray, 0, false);
				gl.glLoadMatrixd(matrixArray, 0);

				loaded = true;
			}
		}

		if (!loaded)
		{
			super.applyDrawProjection(dc, ogsh);
		}
	}
}
