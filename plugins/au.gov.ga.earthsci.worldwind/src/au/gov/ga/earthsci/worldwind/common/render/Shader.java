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

import java.io.IOException;
import java.io.InputStream;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.util.IOUtil;

/**
 * Abstract base class for shaders. Handles the OpenGL setup for GLSL shaders.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class Shader
{
	protected int shaderProgram = 0;
	protected int vertexShader = 0;
	protected int fragmentShader = 0;

	/**
	 * @return An {@link InputStream} containing the GLSL vertex shader string
	 */
	protected abstract InputStream getVertexSource();

	/**
	 * @return An {@link InputStream} containing the GLSL fragment shader string
	 */
	protected abstract InputStream getFragmentSource();

	/**
	 * Locate the uniforms for this shader
	 * 
	 * @param gl
	 */
	protected abstract void getUniformLocations(GL2 gl);

	/**
	 * Setup this GLSL shader in OpenGL
	 * 
	 * @param gl
	 */
	public final void create(GL2 gl)
	{
		if (isCreated())
			return;

		InputStream vertex = getVertexSource();
		InputStream fragment = getFragmentSource();

		String vsrc = null, fsrc = null;
		try
		{
			vsrc = IOUtil.readStreamToStringKeepingNewlines(vertex, null);
			fsrc = IOUtil.readStreamToStringKeepingNewlines(fragment, null);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		vertexShader = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
		if (vertexShader <= 0)
		{
			delete(gl);
			throw new IllegalStateException("Error creating vertex shader");
		}
		fragmentShader = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
		if (fragmentShader <= 0)
		{
			delete(gl);
			throw new IllegalStateException("Error creating fragment shader");
		}

		gl.glShaderSource(vertexShader, 1, new String[] { vsrc }, new int[] { vsrc.length() }, 0);
		gl.glCompileShader(vertexShader);
		gl.glShaderSource(fragmentShader, 1, new String[] { fsrc }, new int[] { fsrc.length() }, 0);
		gl.glCompileShader(fragmentShader);

		shaderProgram = gl.glCreateProgram();
		if (shaderProgram <= 0)
		{
			delete(gl);
			throw new IllegalStateException("Error creating shader program");
		}

		gl.glAttachShader(shaderProgram, vertexShader);
		gl.glAttachShader(shaderProgram, fragmentShader);
		gl.glLinkProgram(shaderProgram);
		gl.glValidateProgram(shaderProgram);

		int[] status = new int[1];
		gl.glGetProgramiv(shaderProgram, GL2.GL_VALIDATE_STATUS, status, 0);
		if (status[0] != GL2.GL_TRUE)
		{
			int maxLength = 10240;
			int[] length = new int[1];
			byte[] bytes = new byte[maxLength];
			gl.glGetProgramInfoLog(shaderProgram, maxLength, length, 0, bytes, 0);
			String info = new String(bytes, 0, length[0]);
			System.out.println(info);

			delete(gl);
			throw new IllegalStateException("Validation of shader program failed");
		}

		gl.glUseProgram(shaderProgram);
		getUniformLocations(gl);
		gl.glUseProgram(0);
	}

	/**
	 * @return Has this shader been created yet?
	 */
	public final boolean isCreated()
	{
		return shaderProgram > 0;
	}

	/**
	 * Tell OpenGL to use this shader program. This method must be called by
	 * {@link Shader} subclasses.
	 * 
	 * @param gl
	 */
	protected void use(GL2 gl)
	{
		gl.glUseProgram(shaderProgram); //if !isCreated(), then shaderProgram == 0
	}

	/**
	 * Tell OpenGL to stop using this shader.
	 * 
	 * @param gl
	 */
	public void unuse(GL2 gl)
	{
		gl.glUseProgram(0);
	}

	/**
	 * Delete any OpenGL resources associated with this shader.
	 * 
	 * @param gl
	 */
	public void delete(GL2 gl)
	{
		if (shaderProgram > 0)
		{
			gl.glDeleteProgram(shaderProgram);
		}
		if (vertexShader > 0)
		{
			gl.glDeleteShader(vertexShader);
		}
		if (fragmentShader > 0)
		{
			gl.glDeleteShader(fragmentShader);
		}
		shaderProgram = 0;
		vertexShader = 0;
		fragmentShader = 0;
	}

	/**
	 * Create this shader if not created already.
	 * 
	 * @param gl
	 * @see Shader#create(GL)
	 */
	public void createIfRequired(GL2 gl)
	{
		if (!isCreated())
		{
			create(gl);
		}
	}

	/**
	 * Delete if created.
	 * 
	 * @param gl
	 * @see Shader#delete(GL)
	 */
	public void deleteIfCreated(GL2 gl)
	{
		if (isCreated())
		{
			delete(gl);
		}
	}
}
