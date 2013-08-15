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
import javax.media.opengl.GLContext;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

/**
 * A convenience base class for tests that work with GL and shaders.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@SuppressWarnings("nls")
public abstract class GLTest
{

	private GLContext context;

	@Before
	public void setup()
	{
		Assume.assumeTrue("No GL available - skipping tests", GLTestUtil.isGLAvailable());

		context = GLTestUtil.createGLContext();
		context.makeCurrent();

		Assume.assumeTrue("No shader compiler available - skipping tests", GLTestUtil.isShadersAvailable(getGL()));

		doSetup();
	}


	@After
	public void tearDown()
	{
		doTeardown();

		if (context != null)
		{
			context.destroy();
		}
	}

	/**
	 * Override to implement specific setup code
	 */
	protected void doSetup()
	{

	}

	/**
	 * Override to implement specific teardown code
	 */
	protected void doTeardown()
	{

	}

	protected final GLContext getGLContext()
	{
		return context;
	}

	protected final GL2 getGL()
	{
		if (context == null)
		{
			return null;
		}
		return (GL2) context.getGL();
	}
}
