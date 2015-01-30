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
package au.gov.ga.earthsci.worldwind.common.layers.delegate.render;

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.Shader;
import au.gov.ga.earthsci.worldwind.common.sun.SunPositionService;

/**
 * Shader used by the {@link ShaderRenderDelegate}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ShaderRenderDelegateShader extends Shader
{
	protected final String vertexSource;
	protected final String fragmentSource;

	protected final Map<Integer, IUniformSetter> uniforms = new HashMap<Integer, IUniformSetter>();

	public ShaderRenderDelegateShader(String vertexSource, String fragmentSource)
	{
		this.vertexSource = vertexSource;
		this.fragmentSource = fragmentSource;
	}

	public void use(DrawContext dc)
	{
		GL2 gl = dc.getGL().getGL2();
		if (super.use(gl))
		{
			for (Entry<Integer, IUniformSetter> entry : uniforms.entrySet())
			{
				entry.getValue().set(dc, gl, entry.getKey());
			}
		}
	}

	@Override
	protected void getUniformLocations(GL2 gl)
	{
		readUniformsFromSource(gl, vertexSource);
		readUniformsFromSource(gl, fragmentSource);
	}

	protected void readUniformsFromSource(GL2 gl, String source)
	{
		//read the uniforms defined by the shader source
		int samplerNumber = 0;
		Pattern uniformPattern = Pattern.compile("uniform\\s+(\\w+)\\s+([^;]+);");
		Matcher matcher = uniformPattern.matcher(source);
		while (matcher.find())
		{
			String[] names = matcher.group(2).split("\\s*,\\s*");
			if (matcher.group(1).equals("sampler2D"))
			{
				//a sampler2D was found; set them to incremental integers (assume that they are defined by texture order)
				for (String name : names)
				{
					gl.glUniform1i(gl.glGetUniformLocation(shaderProgram, name), samplerNumber++);
				}
			}
			else
			{
				for (String name : names)
				{
					//check if we have a setter for this uniform name
					IUniformSetter setter = setters.get(name);
					if (setter != null)
					{
						int uniformLocation = gl.glGetUniformLocation(shaderProgram, name);
						uniforms.put(uniformLocation, setter);
					}
				}
			}
		}
	}

	@Override
	protected InputStream getVertexSource()
	{
		try
		{
			return new ByteArrayInputStream(vertexSource.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected InputStream getFragmentSource()
	{
		try
		{
			return new ByteArrayInputStream(fragmentSource.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Object that knows how to set a shader uniform's value.
	 */
	protected static interface IUniformSetter
	{
		void set(DrawContext dc, GL2 gl, int uniformLocation);
	}

	/**
	 * Abstract {@link IUniformSetter} implementation used for setting
	 * {@link Matrix} uniforms.
	 */
	protected static abstract class MatrixUniformSetter implements IUniformSetter
	{
		protected abstract Matrix getMatrix(DrawContext dc);

		protected float[] array = new float[16];

		@Override
		public void set(DrawContext dc, GL2 gl, int uniformLocation)
		{
			Matrix matrix = getMatrix(dc);
			array[0] = (float) matrix.m11;
			array[1] = (float) matrix.m21;
			array[2] = (float) matrix.m31;
			array[3] = (float) matrix.m41;
			array[4] = (float) matrix.m12;
			array[5] = (float) matrix.m22;
			array[6] = (float) matrix.m32;
			array[7] = (float) matrix.m42;
			array[8] = (float) matrix.m13;
			array[9] = (float) matrix.m23;
			array[10] = (float) matrix.m33;
			array[11] = (float) matrix.m43;
			array[12] = (float) matrix.m14;
			array[13] = (float) matrix.m24;
			array[14] = (float) matrix.m34;
			array[15] = (float) matrix.m44;
			gl.glUniformMatrix4fv(uniformLocation, 1, false, array, 0);
		}
	}

	/**
	 * Map of setters.
	 */
	protected static Map<String, IUniformSetter> setters = new HashMap<String, IUniformSetter>()
	{
		{
			put("modelViewMatrix", new MatrixUniformSetter()
			{
				@Override
				protected Matrix getMatrix(DrawContext dc)
				{
					return dc.getView().getModelviewMatrix();
				}
			});

			put("modelViewMatrixInverse", new MatrixUniformSetter()
			{
				@Override
				protected Matrix getMatrix(DrawContext dc)
				{
					return dc.getView().getModelviewMatrix().getInverse();
				}
			});

			put("projectionMatrix", new MatrixUniformSetter()
			{
				@Override
				protected Matrix getMatrix(DrawContext dc)
				{
					return dc.getView().getProjectionMatrix();
				}
			});

			put("projectionMatrixInverse", new MatrixUniformSetter()
			{
				@Override
				protected Matrix getMatrix(DrawContext dc)
				{
					return dc.getView().getProjectionMatrix().getInverse();
				}
			});

			put("lightDirection", new IUniformSetter()
			{
				@Override
				public void set(DrawContext dc, GL2 gl, int uniformLocation)
				{
					Vec4 lightDirection = SunPositionService.INSTANCE.getDirection(dc.getView());
					gl.glUniform3f(uniformLocation, (float) lightDirection.x, (float) lightDirection.y,
							(float) lightDirection.z);
				}
			});

			put("eyePosition", new IUniformSetter()
			{
				@Override
				public void set(DrawContext dc, GL2 gl, int uniformLocation)
				{
					Vec4 eyePosition = dc.getView().getEyePoint();
					gl.glUniform3f(uniformLocation, (float) eyePosition.x, (float) eyePosition.y, (float) eyePosition.z);
				}
			});
		}
	};
}
