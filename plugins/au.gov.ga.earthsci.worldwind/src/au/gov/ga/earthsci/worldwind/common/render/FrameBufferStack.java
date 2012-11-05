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
package au.gov.ga.earthsci.worldwind.common.render;

import java.util.Stack;

import javax.media.opengl.GL2;

/**
 * Stack that the {@link FrameBuffer}s call when binding/unbinding. This ensures
 * that, when binding a new {@link FrameBuffer}, the previously bound
 * {@link FrameBuffer} is rebound when the new one is unbound.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FrameBufferStack
{
	private static Stack<Integer> stack = new Stack<Integer>();
	private static int currentFrameBufferId = 0;

	public static synchronized void push(GL2 gl, int frameBufferId)
	{
		stack.push(currentFrameBufferId);
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBufferId);
		currentFrameBufferId = frameBufferId;
	}

	public static synchronized void pop(GL2 gl)
	{
		int frameBufferId = 0;
		if (!stack.isEmpty())
		{
			frameBufferId = stack.pop();
		}
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBufferId);
		currentFrameBufferId = frameBufferId;
	}
}
