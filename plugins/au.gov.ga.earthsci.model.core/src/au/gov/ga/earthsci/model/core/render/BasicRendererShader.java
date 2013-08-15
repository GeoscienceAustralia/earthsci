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
package au.gov.ga.earthsci.model.core.render;

import gov.nasa.worldwind.globes.Globe;

import java.io.IOException;

import javax.media.opengl.GL2;
import javax.media.opengl.GLUniformData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.model.core.shader.Shader;
import au.gov.ga.earthsci.model.core.shader.include.ShaderIncludeProcessor;

import com.jogamp.opengl.util.glsl.ShaderState;

/**
 * The shader used by the {@link BasicRenderer}.
 * <p/>
 * Supports:
 * <ul>
 * <li>Reprojection of coordinates from geographic into cartesian coordinate
 * systems based on {@link Globe} parameters;
 * <li>Z-encoded NODATA values;
 * <li>Dynamic vertical exaggeration
 * <li>Color buffers
 * </ul>
 * <p/>
 * Parameters can be set using provided setters. Values set will then be used
 * when {@link #bind(GL2)} is called.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicRendererShader extends Shader
{
	private static final Logger logger = LoggerFactory.getLogger(BasicRendererShader.class);

	private static final String VERTEX_SHADER = "BasicRenderer.vert"; //$NON-NLS-1$
	private static final String FRAGMENT_SHADER = "BasicRenderer.frag"; //$NON-NLS-1$

	private static final String OPACITY = "opacity"; //$NON-NLS-1$
	private static final String VE = "ve"; //$NON-NLS-1$
	private static final String ES = "es"; //$NON-NLS-1$
	private static final String RADIUS = "radius"; //$NON-NLS-1$
	private static final String ZNODATA = "zNodata"; //$NON-NLS-1$

	private ShaderIncludeProcessor processor = new ShaderIncludeProcessor();

	private transient Globe globe;
	private boolean globeDirty = true;

	private transient float ve = 1.0f;
	private boolean veDirty = true;

	private transient float opacity = 1.0f;
	private boolean opacityDirty = true;

	private transient Float nodata;
	private boolean nodataDirty = true;


	@Override
	protected String getVertexShaderSource()
	{
		try
		{
			return processor.processResource(getClass(), VERTEX_SHADER);
		}
		catch (IOException e)
		{
			logger.debug("Unable to load vertex shader", e); //$NON-NLS-1$
			return null;
		}
	}

	@Override
	protected String getFragmentShaderSource()
	{
		try
		{
			return processor.processResource(getClass(), FRAGMENT_SHADER);
		}
		catch (IOException e)
		{
			logger.debug("Unable to load fragment shader", e); //$NON-NLS-1$
			return null;
		}
	}

	@Override
	protected boolean bindShaderState(GL2 gl, ShaderState shaderState)
	{
		boolean uniformsSet = true;
		if (globeDirty)
		{
			if (globe == null)
			{
				throw new IllegalStateException("A Globe must set before call to bind()"); //$NON-NLS-1$
			}
			uniformsSet &= shaderState.uniform(gl, new GLUniformData(RADIUS, (float) globe.getRadius()));
			uniformsSet &= shaderState.uniform(gl, new GLUniformData(ES, (float) globe.getEccentricitySquared()));
		}
		if (opacityDirty)
		{
			uniformsSet &= shaderState.uniform(gl, new GLUniformData(OPACITY, opacity));
		}
		if (veDirty)
		{
			uniformsSet &= shaderState.uniform(gl, new GLUniformData(VE, ve));
		}
		if (nodata != null && nodataDirty)
		{
			uniformsSet &= shaderState.uniform(gl, new GLUniformData(ZNODATA, nodata));
		}
		return uniformsSet;
	}

	/**
	 * Set the globe on this shader
	 */
	public void setGlobe(Globe g)
	{
		if (this.globe != g)
		{
			this.globe = g;
			globeDirty = true;
		}
	}

	/**
	 * Set the vertical exaggeration on this shader
	 */
	public void setVerticalExaggeration(float ve)
	{
		if (ve != this.ve)
		{
			this.ve = ve;
			veDirty = true;
		}
	}

	/**
	 * Set the opacity on this shader
	 */
	public void setOpacity(float opacity)
	{
		if (opacity != this.opacity)
		{
			this.opacity = opacity;
			opacityDirty = true;
		}
	}

	/**
	 * Set the nodata value on this shader
	 */
	public void setNodata(Float nodata)
	{
		if (this.nodata != nodata)
		{
			this.nodata = nodata;
			nodataDirty = true;
		}
	}

}
