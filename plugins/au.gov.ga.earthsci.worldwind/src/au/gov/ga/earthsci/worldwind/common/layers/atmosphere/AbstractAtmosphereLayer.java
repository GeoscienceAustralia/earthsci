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
package au.gov.ga.earthsci.worldwind.common.layers.atmosphere;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.OGLStackHandler;

import javax.media.opengl.GL2;

/**
 * Layer that renders atmospheric scattering effects.
 * <p/>
 * Based on Sean O'Neil's algorithm described in GPU Gems 2 <a
 * href="http://http.developer.nvidia.com/GPUGems2/gpugems2_chapter16.html"
 * >chapter 16</a>.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractAtmosphereLayer extends AbstractLayer
{
	private boolean inited = false;
	protected Vec4 lightDirection = new Vec4(0, 0, 1000).normalize3();

	@Override
	protected void doRender(DrawContext dc)
	{
		if (!inited)
		{
			init(dc);
			inited = true;
		}
		renderAtmosphere(dc);
	}

	protected abstract void init(DrawContext dc);

	protected void renderAtmosphere(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();

		//setup variables
		float innerRadius = (float) dc.getGlobe().getRadius();
		float outerRadius = innerRadius * Atmosphere.ATMOSPHERE_SCALE;
		View view = dc.getView();
		Vec4 eyePoint = view.getEyePoint();
		float eyeMagnitude = (float) eyePoint.getLength3();

		OGLStackHandler ogsh = new OGLStackHandler();
		try
		{
			ogsh.pushAttrib(gl, GL2.GL_TEXTURE_BIT | GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT
					| GL2.GL_POLYGON_BIT);
			renderAtmosphere(dc, lightDirection, eyePoint, eyeMagnitude, innerRadius, outerRadius);
		}
		finally
		{
			ogsh.pop(gl);
		}
	}

	protected abstract void renderAtmosphere(DrawContext dc, Vec4 lightDirection, Vec4 eyePoint, float eyeMagnitude,
			float innerRadius, float outerRadius);
}
