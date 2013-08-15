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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.nasa.worldwind.globes.Earth;

import org.junit.Test;

import au.gov.ga.earthsci.model.core.tests.util.GLTest;

/**
 * Unit tests for the {@link BasicRendererShader}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@SuppressWarnings("nls")
public class BasicRendererShaderTest extends GLTest
{

	private BasicRendererShader classUnderTest;

	@Override
	public void doSetup()
	{
		classUnderTest = new BasicRendererShader();
	}

	@Test
	public void testShaderLifecycle()
	{
		classUnderTest.initialise(getGL());

		assertTrue("Expected shader to be initialised", classUnderTest.isInitialised());

		classUnderTest.setGlobe(new Earth());
		classUnderTest.setNodata(null);
		classUnderTest.setOpacity(1.0f);
		classUnderTest.setVerticalExaggeration(1.0f);

		assertTrue("Expected shader to bind correctly", classUnderTest.bind(getGL()));
		assertTrue("Expected shader to unbind correctly", classUnderTest.bind(getGL()));
	}

	@Test
	public void testShaderBindNoGlobe()
	{
		classUnderTest.setNodata(null);
		classUnderTest.setOpacity(1.0f);
		classUnderTest.setVerticalExaggeration(1.0f);

		assertFalse(classUnderTest.bind(getGL()));
		assertNotNull(classUnderTest.getLastError());
	}

}
