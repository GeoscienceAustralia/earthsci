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

import gov.nasa.worldwind.util.Logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import javax.media.opengl.GL;
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
	protected int geometryShader = 0;
	protected boolean creationFailed = false;

	/**
	 * @return An {@link InputStream} containing the GLSL vertex shader string
	 */
	protected abstract InputStream getVertexSource();

	/**
	 * @return An {@link InputStream} containing the GLSL fragment shader string
	 */
	protected abstract InputStream getFragmentSource();

	/**
	 * @return An {@link InputStream} containing the GLSL geometry shader string
	 */
	protected InputStream getGeometrySource()
	{
		return null;
	}

	/**
	 * Locate the uniforms for this shader
	 * 
	 * @param gl
	 */
	protected abstract void getUniformLocations(GL2 gl);

	/**
	 * @return Value to pass to glProgramParameteriARB for
	 *         GL_GEOMETRY_INPUT_TYPE_ARB
	 */
	protected int getGeometryInputType()
	{
		return GL2.GL_TRIANGLES;
	}

	/**
	 * @return Value to pass to glProgramParameteriARB for
	 *         GL_GEOMETRY_OUTPUT_TYPE_ARB
	 */
	protected int getGeometryOutputType()
	{
		return GL2.GL_TRIANGLE_STRIP;
	}

	/**
	 * @return Value to pass to glProgramParameteriARB for
	 *         GL_GEOMETRY_VERTICES_OUT_ARB
	 */
	protected int getGeometryVerticesOut()
	{
		return 3;
	}

	/**
	 * Setup this GLSL shader in OpenGL
	 * 
	 * @param gl
	 * @return Was this shader created?
	 */
	public final boolean create(GL2 gl)
	{
		if (isCreated() || creationFailed)
		{
			return false;
		}

		try
		{
			InputStream vertex = getVertexSource();
			InputStream fragment = getFragmentSource();
			InputStream geometry = getGeometrySource();

			String vsrc = null, fsrc = null, gsrc = null;
			try
			{
				vsrc = IOUtil.readStreamToStringKeepingNewlines(vertex, null);
				fsrc = IOUtil.readStreamToStringKeepingNewlines(fragment, null);
				gsrc = geometry == null ? null : IOUtil.readStreamToStringKeepingNewlines(geometry, null);
			}
			catch (IOException e)
			{
				throw new ShaderException("Error reading shader source", e);
			}

			String[] defines = getDefines();
			vsrc = insertDefines(vsrc, defines, "_VERTEX_");
			fsrc = insertDefines(fsrc, defines, "_FRAGMENT_");
			gsrc = gsrc == null ? null : insertDefines(gsrc, defines, "_GEOMETRY_");

			vertexShader = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
			if (vertexShader <= 0)
			{
				delete(gl);
				throw new ShaderException("Error creating vertex shader");
			}
			fragmentShader = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
			if (fragmentShader <= 0)
			{
				delete(gl);
				throw new ShaderException("Error creating fragment shader");
			}
			if (gsrc != null)
			{
				geometryShader = gl.glCreateShader(GL2.GL_GEOMETRY_SHADER_ARB);
				if (geometryShader <= 0)
				{
					delete(gl);
					throw new ShaderException("Error creating geometry shader");
				}
			}

			gl.glShaderSource(vertexShader, 1, new String[] { vsrc }, new int[] { vsrc.length() }, 0);
			gl.glCompileShader(vertexShader);
			gl.glShaderSource(fragmentShader, 1, new String[] { fsrc }, new int[] { fsrc.length() }, 0);
			gl.glCompileShader(fragmentShader);
			if (gsrc != null)
			{
				gl.glShaderSource(geometryShader, 1, new String[] { gsrc }, new int[] { gsrc.length() }, 0);
				gl.glCompileShader(geometryShader);
			}

			shaderProgram = gl.glCreateProgram();
			if (shaderProgram <= 0)
			{
				delete(gl);
				throw new ShaderException("Error creating shader program");
			}

			gl.glAttachShader(shaderProgram, vertexShader);
			gl.glAttachShader(shaderProgram, fragmentShader);
			if (gsrc != null)
			{
				gl.glAttachShader(shaderProgram, geometryShader);
				gl.glProgramParameteriARB(shaderProgram, GL2.GL_GEOMETRY_INPUT_TYPE_ARB, getGeometryInputType());
				gl.glProgramParameteriARB(shaderProgram, GL2.GL_GEOMETRY_OUTPUT_TYPE_ARB, getGeometryOutputType());
				gl.glProgramParameteriARB(shaderProgram, GL2.GL_GEOMETRY_VERTICES_OUT_ARB, getGeometryVerticesOut());
			}
			gl.glLinkProgram(shaderProgram);
			gl.glValidateProgram(shaderProgram);

			int[] status = new int[1];
			gl.glGetProgramiv(shaderProgram, GL2.GL_VALIDATE_STATUS, status, 0);
			if (status[0] != GL2.GL_TRUE)
			{
				StringBuilder message = new StringBuilder();

				int maxLength = 10240;
				int[] length = new int[1];
				byte[] bytes = new byte[maxLength];
				gl.glGetProgramInfoLog(shaderProgram, maxLength, length, 0, bytes, 0);
				if (length[0] > 0)
				{
					String programInfo = new String(bytes, 0, length[0]);
					message.append("Program info: " + programInfo);
				}

				gl.glGetShaderInfoLog(vertexShader, maxLength, length, 0, bytes, 0);
				if (length[0] > 0)
				{
					String vertexInfo = new String(bytes, 0, length[0]);
					message.append("Vertex shader info: " + vertexInfo);
				}

				gl.glGetShaderInfoLog(fragmentShader, maxLength, length, 0, bytes, 0);
				if (length[0] > 0)
				{
					String fragmentInfo = new String(bytes, 0, length[0]);
					message.append("Fragment shader info: " + fragmentInfo);
				}

				if (gsrc != null)
				{
					gl.glGetShaderInfoLog(geometryShader, maxLength, length, 0, bytes, 0);
					if (length[0] > 0)
					{
						String geometryInfo = new String(bytes, 0, length[0]);
						message.append("Geometry shader info: " + geometryInfo);
					}
				}

				delete(gl);
				throw new ShaderException(message.toString());
			}

			gl.glUseProgram(shaderProgram);
			getUniformLocations(gl);
			gl.glUseProgram(0);

			return true;
		}
		catch (ShaderException e)
		{
			creationFailed = true;
			Logging.logger().log(Level.SEVERE, "Shader creation failed", e);
			return false;
		}
	}

	/**
	 * @return List of strings to #define at the top of the shader source
	 */
	protected String[] getDefines()
	{
		return null;
	}

	protected String insertDefines(String src, String[] defines, String extraDefine)
	{
		StringBuffer sb = new StringBuffer(src);
		int defineIndex = 0;
		int versionIndex = src.indexOf("#version");
		if (versionIndex >= 0)
		{
			defineIndex = src.indexOf('\n', versionIndex) + 1;
		}
		if (defines != null)
		{
			for (int i = defines.length - 1; i >= 0; i--)
			{
				sb.insert(defineIndex, "#define " + defines[i] + "\n");
			}
		}
		sb.insert(defineIndex, "#define " + extraDefine + "\n");
		return sb.toString();
	}

	/**
	 * @return Has this shader been created yet (and not
	 *         {@link #isCreationFailed()})?
	 */
	public final boolean isCreated()
	{
		return shaderProgram > 0 && !isCreationFailed();
	}

	/**
	 * @return Did this shader fail to be created (due to compile error, etc)?
	 */
	public final boolean isCreationFailed()
	{
		return creationFailed;
	}

	/**
	 * Tell OpenGL to use this shader program. This method must be called by
	 * {@link Shader} subclasses.
	 * 
	 * @param gl
	 * @return <code>True</code> if this shader was created and used
	 */
	protected boolean use(GL2 gl)
	{
		if (isCreated())
		{
			gl.glUseProgram(shaderProgram);
			return true;
		}
		return false;
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
		if (geometryShader > 0)
		{
			gl.glDeleteShader(geometryShader);
		}
		shaderProgram = 0;
		vertexShader = 0;
		fragmentShader = 0;
		geometryShader = 0;
	}

	/**
	 * Create this shader if not created already.
	 * 
	 * @param gl
	 * @return Is this shader created, ie {@link #isCreated()}
	 * @see Shader#create(GL)
	 */
	public boolean createIfRequired(GL2 gl)
	{
		if (!isCreated())
		{
			create(gl);
			return true;
		}
		return false;
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
