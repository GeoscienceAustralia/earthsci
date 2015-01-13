/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.model.core.tests.util;

import javax.media.opengl.GL2;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;

import com.jogamp.opengl.util.glsl.ShaderUtil;

/**
 * A util class for working with GL in unit tests
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class GLTestUtil
{

	/**
	 * Determine if GL is available on the machine running the unit tests
	 * 
	 * @return <code>true</code> if GL is available; <code>false</code>
	 *         otherwise.
	 */
	public static boolean isGLAvailable()
	{
		try
		{
			GLProfile profile = GLProfile.getDefault();
			return profile.isGL2();
		}
		catch (Throwable e)
		{
			System.err.println("No OpenGL available"); //$NON-NLS-1$
			return false;
		}
	}

	/**
	 * Return whether shaders are available on the machine running the unit
	 * tests.
	 * 
	 * @param gl
	 *            the GL to test
	 * @return <code>true</code> if shaders are available; <code>false</code>
	 *         otherwise.
	 */
	public static boolean isShadersAvailable(GL2 gl)
	{
		return ShaderUtil.isShaderCompilerAvailable(gl);
	}

	/**
	 * Create a GLContext for use in unit tests, if possible.
	 * 
	 * @return The created GLContext, or <code>null</code> if none available
	 */
	public static GLContext createGLContext()
	{
		GLDrawableFactory fact = GLDrawableFactory.getFactory(GLProfile.getGL2ES2());
		GLCapabilities caps = new GLCapabilities(GLProfile.getGL2ES2());
		caps.setFBO(true);
		caps.setOnscreen(false);
		caps.setPBuffer(true);
		caps.setDoubleBuffered(false);
		GLOffscreenAutoDrawable drawable = fact.createOffscreenAutoDrawable(null, caps, null, 100, 100);
		return drawable.getContext();
	}
}
