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
package au.gov.ga.earthsci.worldwind.common.layers.fogmask;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import javax.media.opengl.GL2;

/**
 * Layer that masks the back of the globe when rendering with a large far
 * clipping plane, using fog to fade to black from the center of the globe to
 * the back.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FogMaskLayer extends AbstractLayer
{
	@Override
	protected void doRender(DrawContext dc)
	{
		Position eyePos = dc.getView().getEyePosition();
		if (eyePos != null)
		{
			double altitude = eyePos.getElevation();
			double radius = dc.getGlobe().getRadius();
			float start = (float) (altitude + radius);
			float end = (float) (altitude + radius + radius);
			GL2 gl = dc.getGL().getGL2();
			float fogColor[] = new float[] { 0.0f, 0.0f, 0.0f, 0.0f };
			gl.glFogfv(GL2.GL_FOG_COLOR, fogColor, 0);
			gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_LINEAR);
			gl.glFogf(GL2.GL_FOG_START, start);
			gl.glFogf(GL2.GL_FOG_END, end);
			gl.glHint(GL2.GL_FOG_HINT, GL2.GL_DONT_CARE);
			gl.glEnable(GL2.GL_FOG);
		}
	}
}
