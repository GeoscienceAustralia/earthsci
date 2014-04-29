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
package au.gov.ga.earthsci.worldwind.common.effects;

import gov.nasa.worldwind.render.DrawContext;

import java.awt.Dimension;

import au.gov.ga.earthsci.worldwind.common.render.FrameBuffer;
import au.gov.ga.earthsci.worldwind.common.render.Shader;

/**
 * Interface that defines a full-screen effect. {@link Effect}s are bound by the
 * scene controller, and are rendered in screen space. Each {@link Effect}
 * implementation should contain a {@link FrameBuffer} that has a depth texture,
 * and the effect shaders should write to the gl_FragDepth variable so that the
 * fragment depth is passed through the effect chain.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface Effect
{
	/**
	 * @return Is this effect enabled?
	 */
	boolean isEnabled();

	/**
	 * Called before the scene is rendered. The effect should setup it's
	 * framebuffer here.
	 * 
	 * @param dc
	 *            Draw context
	 * @param dimensions
	 *            Dimensions of the viewport (includes render scale during
	 *            rendering)
	 */
	void bindFrameBuffer(DrawContext dc, Dimension dimensions);

	/**
	 * Called after the scene is rendered. The effect should unbind it's
	 * framebuffer here.
	 * 
	 * @param dc
	 *            Draw context
	 * @param dimensions
	 *            Dimensions of the viewport (includes render scale during
	 *            rendering)
	 */
	void unbindFrameBuffer(DrawContext dc, Dimension dimensions);

	/**
	 * Called after the scene is rendered, and possibly after another
	 * {@link Effect}'s frame buffer has been bound. The effect should render
	 * it's framebuffer using it's effect shader here.
	 * 
	 * @param dc
	 *            Draw context
	 * @param dimensions
	 *            Dimensions of the viewport (includes render scale during
	 *            rendering)
	 */
	void drawFrameBufferWithEffect(DrawContext dc, Dimension dimensions);

	/**
	 * Release any resources ({@link FrameBuffer}s, {@link Shader}s) associated
	 * with this effect. This is called every frame if the effect is disabled.
	 * 
	 * @param dc
	 *            Draw context
	 */
	void releaseResources(DrawContext dc);
}
