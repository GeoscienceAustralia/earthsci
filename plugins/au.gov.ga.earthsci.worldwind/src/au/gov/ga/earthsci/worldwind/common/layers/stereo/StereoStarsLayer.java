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
package au.gov.ga.earthsci.worldwind.common.layers.stereo;

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.layers.ProjectionStarsLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.OGLStackHandler;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.view.stereo.StereoView;

/**
 * An extension of the {@link ProjectionStarsLayer} that supports stereo rendering of stars
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class StereoStarsLayer extends ProjectionStarsLayer
{
	@Override
	protected void applyDrawProjection(DrawContext dc, OGLStackHandler ogsh)
	{
		boolean loaded = false;
		if (dc.getView() instanceof StereoView && ((StereoView) dc.getView()).isStereo())
		{
			StereoView stereo = (StereoView) dc.getView();
			//near is the distance from the origin
			double near = stereo.getEyePoint().getLength3();
			double far = this.radius + near;
			Matrix projection = stereo.calculateProjectionMatrix(near, far);

			if (projection != null)
			{
				double[] matrixArray = new double[16];
				GL2 gl = dc.getGL();
				gl.glMatrixMode(GL2.GL_PROJECTION);
				gl.glPushMatrix();

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

	@Override
	public void doRender(DrawContext dc)
	{
		float pointSize = 1f;
		if (dc.getView() instanceof StereoView && ((StereoView) dc.getView()).isStereo())
		{
			pointSize *= 2f;
		}
		dc.getGL().glPointSize(pointSize);

		super.doRender(dc);
	}
}
