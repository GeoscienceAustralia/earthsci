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
package au.gov.ga.earthsci.model.core.shader;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.media.opengl.GL2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;
import com.jogamp.opengl.util.glsl.ShaderUtil;

/**
 * A base class for high-level shader representations that take care of binding
 * GL uniforms etc. and present a clean interface to client code.
 * <p/>
 * Defines a standard lifecycle that shaders can use:
 * <ol>
 * <li>Load shader source
 * <li>Initialise shader program
 * <li>Set shader state
 * <li>Use (bind) shader program
 * <li><i>Perform drawing in client</i>
 * <li>Unbind shader program
 * </ol>
 * <p/>
 * A {@link Shader} can be marked as {@link #isDirty()}, which will prompt it to
 * be re-initialised prior to binding.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class Shader
{

	private static final Logger logger = LoggerFactory.getLogger(Shader.class);

	/**
	 * Flag indicates that the shader is in a dirty state and needs to be
	 * re-initialised prior to binding
	 */
	private AtomicBoolean dirty = new AtomicBoolean(true);

	private ShaderState shaderState;
	private ShaderProgram shaderProgram;
	private ShaderCode vertexShader;
	private ShaderCode fragmentShader;

	private Exception lastException;

	/**
	 * @return The source for the vertex shader
	 */
	protected abstract String getVertexShaderSource();

	/**
	 * @return The source for the fragment shader
	 */
	protected abstract String getFragmentShaderSource();

	/**
	 * Bind required uniforms etc. on the provided {@link ShaderState} object.
	 * 
	 * @param gl
	 *            The current GL context
	 * @param shaderState
	 *            The {@link ShaderState} to bind to
	 * 
	 * @return <code>true</code> if binding was successful; <code>false</code>
	 *         otherwise
	 */
	protected abstract boolean bindShaderState(GL2 gl, ShaderState shaderState);

	/**
	 * Initialise this shader using the provided GL context with current values
	 * as appropriate.
	 * 
	 * @param gl
	 * 
	 * @return <code>true</code> if initialisation was successful;
	 *         <code>false</code> otherwise.
	 */
	public boolean initialise(GL2 gl)
	{
		if (gl == null)
		{
			logger.debug("No GL provided. Unable to initialise shader."); //$NON-NLS-1$
			lastException = new IllegalArgumentException("A GL is required for binding"); //$NON-NLS-1$
			return false;
		}
		try
		{
			if (shaderProgram != null)
			{
				shaderProgram.destroy(gl);
				shaderProgram = null;
			}

			vertexShader = new ShaderCode(GL2.GL_VERTEX_SHADER, 1, new String[][] { { getVertexShaderSource() } });
			fragmentShader =
					new ShaderCode(GL2.GL_FRAGMENT_SHADER, 1, new String[][] { { getFragmentShaderSource() } });

			shaderProgram = new ShaderProgram();
			shaderProgram.add(gl, vertexShader, null);
			shaderProgram.add(gl, fragmentShader, null);

			if (shaderState == null)
			{
				shaderState = new ShaderState();
			}
			shaderState.attachShaderProgram(gl, shaderProgram, true);

			dirty.set(false);
			return true;
		}
		catch (Exception e)
		{
			logger.debug("Unable to initialise shader", e); //$NON-NLS-1$

			lastException = e;
			return false;
		}
	}

	/**
	 * @return Whether this shader has been initialised correctly
	 */
	public boolean isInitialised()
	{
		return shaderProgram != null;
	}

	/**
	 * Mark this shader as dirty. It will be re-initialised prior to the next
	 * draw, or manually using {@link #initialise(GL2)}
	 */
	public void markDirty()
	{
		this.dirty.set(true);
	}

	/**
	 * @return Whether this shader is in a 'dirty' state and needs to be
	 *         re-initialised.
	 * @see #markDirty()
	 */
	public boolean isDirty()
	{
		return dirty.get();
	}

	/**
	 * Bind this shader on the provided GL context, initialising if necessary.
	 * <p/>
	 * Should be called before drawing begins.
	 * 
	 * @param gl
	 *            The GL context to bind to
	 * @return <code>true</code> if the bind was successful; <code>false</code>
	 *         otherwise.
	 */
	public boolean bind(GL2 gl)
	{
		if (gl == null)
		{
			logger.debug("No GL provided. Unable to initialise shader."); //$NON-NLS-1$
			lastException = new IllegalArgumentException("A GL is required for binding"); //$NON-NLS-1$
			return false;
		}

		if (!ShaderUtil.isShaderCompilerAvailable(gl))
		{
			return false;
		}

		if (isDirty())
		{
			if (!initialise(gl))
			{
				return false;
			}
		}

		try
		{
			shaderState.useProgram(gl, true);
			boolean ok = bindShaderState(gl, shaderState);
			if (ok)
			{
				// Make sure we don't have left-over exceptions retained
				lastException = null;
			}
			else
			{
				if (lastException == null)
				{
					// Make sure we have an exception for debugging in case one wasnt raised as part of the binding
					lastException = new IllegalStateException("Shaderstate binding failed"); //$NON-NLS-1$
				}
			}
			return ok;
		}
		catch (Exception e)
		{
			lastException = e;
			return false;
		}
	}

	/**
	 * Unbind this shader from the provided GL context
	 * <p/>
	 * Should be called after drawing completes.
	 * 
	 * @param gl
	 * 
	 * @return <code>true</code> if the unbind was successful;
	 *         <code>false</code> otherwise.
	 */
	public boolean unbind(GL2 gl)
	{
		if (gl == null)
		{
			logger.debug("No GL provided. Unable to initialise shader."); //$NON-NLS-1$
			lastException = new IllegalArgumentException("A GL is required for binding"); //$NON-NLS-1$
			return false;
		}

		try
		{
			shaderState.useProgram(gl, false);
			return true;
		}
		catch (Exception e)
		{
			lastException = e;
			return false;
		}
	}

	/**
	 * @return The last (Java) error that occured (if any). Note: GL errors can
	 *         be retrieved from the current GL context as normal using
	 *         {@link GL2#glGetError()} or
	 */
	public Exception getLastError()
	{
		return lastException;
	}
}
