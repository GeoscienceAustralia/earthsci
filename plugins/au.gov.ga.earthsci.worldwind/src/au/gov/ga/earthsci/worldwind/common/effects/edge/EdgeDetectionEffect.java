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
package au.gov.ga.earthsci.worldwind.common.effects.edge;

import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.effects.Effect;
import au.gov.ga.earthsci.worldwind.common.effects.EffectBase;
import au.gov.ga.earthsci.worldwind.common.render.FrameBuffer;

/**
 * Example {@link Effect} that convolves the input with a kernel matrix,
 * producing different filter effects like blurring, edge detection, sharpening,
 * embossing, etc.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EdgeDetectionEffect extends EffectBase
{
	private final EdgeShader edgeShader = new EdgeShader();

	@Override
	protected void drawFrameBufferWithEffect(DrawContext dc, Dimension dimensions, FrameBuffer frameBuffer)
	{
		GL2 gl = dc.getGL().getGL2();

		edgeShader.createIfRequired(gl);
		try
		{
			edgeShader.use(gl, dimensions.width, dimensions.height);
			FrameBuffer.renderTexturedQuad(gl, frameBuffer.getTexture().getId(), frameBuffer.getDepth().getId());
		}
		finally
		{
			edgeShader.unuse(gl);
		}
	}

	@Override
	protected void releaseEffect(DrawContext dc)
	{
		edgeShader.deleteIfCreated(dc.getGL().getGL2());
	}
}
