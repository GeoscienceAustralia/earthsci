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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.media.opengl.GL2;

import org.junit.Test;

import au.gov.ga.earthsci.model.core.tests.util.GLTest;

import com.jogamp.opengl.util.glsl.ShaderState;

/**
 * Unit tests for the {@link Shader} base class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@SuppressWarnings("nls")
public class ShaderTest extends GLTest
{
	private Shader classUnderTest;

	@Override
	public void doSetup()
	{
		classUnderTest = new ShaderTestImpl();
	}

	@Test
	public void testLifecycle()
	{
		assertTrue(classUnderTest.initialise(getGL()));
		assertNull(classUnderTest.getLastError());

		assertTrue(classUnderTest.bind(getGL()));
		assertNull(classUnderTest.getLastError());

		assertTrue(classUnderTest.unbind(getGL()));
		assertNull(classUnderTest.getLastError());
	}

	@Test
	public void testInitialiseWithNullGL()
	{
		assertFalse(classUnderTest.initialise(null));
		assertNotNull(classUnderTest.getLastError());
	}

	@Test
	public void testInitialiseWithAllOK()
	{
		assertTrue(classUnderTest.initialise(getGL()));
		assertNull(classUnderTest.getLastError());
	}

	@Test
	public void testInitialiseWithNullVertexSource()
	{
		classUnderTest = new ShaderTestImpl()
		{
			@Override
			protected String getVertexShaderSource()
			{
				return null;
			}
		};

		assertFalse(classUnderTest.initialise(getGL()));
		assertNotNull(classUnderTest.getLastError());
	}

	@Test
	public void testInitialiseWithNullFragmentSource()
	{
		classUnderTest = new ShaderTestImpl()
		{
			@Override
			protected String getFragmentShaderSource()
			{
				return null;
			}
		};

		assertFalse(classUnderTest.initialise(getGL()));
		assertNotNull(classUnderTest.getLastError());
	}

	@Test
	public void testInitialiseWithInvalidVertexSource()
	{
		classUnderTest = new ShaderTestImpl()
		{
			@Override
			protected String getVertexShaderSource()
			{
				return super.getVertexShaderSource() + "bad";
			}
		};

		assertFalse(classUnderTest.initialise(getGL()));
		assertNotNull(classUnderTest.getLastError());
	}

	@Test
	public void testInitialiseWithInvalidFragmentSource()
	{
		classUnderTest = new ShaderTestImpl()
		{
			@Override
			protected String getFragmentShaderSource()
			{
				return super.getFragmentShaderSource() + "bad";
			}
		};

		assertFalse(classUnderTest.initialise(getGL()));
		assertNotNull(classUnderTest.getLastError());
	}

	@Test
	public void testInitialiseWithExceptionLoadingVertexSource()
	{
		classUnderTest = new ShaderTestImpl()
		{
			@Override
			protected String getVertexShaderSource()
			{
				throw new RuntimeException();
			}
		};

		assertFalse(classUnderTest.initialise(getGL()));
		assertNotNull(classUnderTest.getLastError());
	}

	@Test
	public void testInitialiseWithExceptionLoadingFragmentSource()
	{
		classUnderTest = new ShaderTestImpl()
		{
			@Override
			protected String getFragmentShaderSource()
			{
				throw new RuntimeException();
			}
		};

		assertFalse(classUnderTest.initialise(getGL()));
		assertNotNull(classUnderTest.getLastError());
	}

	@Test
	public void testBindWithAllOK()
	{
		assertTrue(classUnderTest.bind(getGL()));
		assertNull(classUnderTest.getLastError());
		assertTrue(classUnderTest.unbind(getGL()));
	}

	@Test
	public void testBindWithNullGL()
	{
		assertFalse(classUnderTest.bind(null));
		assertNotNull(classUnderTest.getLastError());
	}

	@Test
	public void testBindWithFailedShaderStateBind()
	{
		classUnderTest = new ShaderTestImpl()
		{
			@Override
			protected boolean bindShaderState(GL2 gl, ShaderState shaderState)
			{
				return false;
			}
		};

		assertFalse(classUnderTest.bind(getGL()));
		assertNotNull(classUnderTest.getLastError());
	}

	@Test
	public void testBindWithExceptionDuringShaderStateBind()
	{
		classUnderTest = new ShaderTestImpl()
		{
			@Override
			protected boolean bindShaderState(GL2 gl, ShaderState shaderState)
			{
				throw new RuntimeException();
			}
		};

		assertFalse(classUnderTest.bind(getGL()));
		assertNotNull(classUnderTest.getLastError());
	}

	@Test
	public void testUnbindWithNullGL()
	{
		assertFalse(classUnderTest.unbind(null));
		assertNotNull(classUnderTest.getLastError());
	}

	@Test
	public void testUnbindWithNoBind()
	{
		assertFalse(classUnderTest.unbind(getGL()));
		assertNotNull(classUnderTest.getLastError());
	}

	private class ShaderTestImpl extends Shader
	{

		@Override
		protected String getVertexShaderSource()
		{
			return "#version 110\n"
					+ "void main(void){\n"
					+ "gl_FrontColor = vec4(1.0);\n"
					+ "}";
		}

		@Override
		protected String getFragmentShaderSource()
		{
			return "#version 110\n"
					+ "void main(void){\n"
					+ "gl_FragColor = vec4(1.0)\n;"
					+ "}";
		}

		@Override
		protected boolean bindShaderState(GL2 gl, ShaderState shaderState)
		{
			return true;
		}

	}

}
