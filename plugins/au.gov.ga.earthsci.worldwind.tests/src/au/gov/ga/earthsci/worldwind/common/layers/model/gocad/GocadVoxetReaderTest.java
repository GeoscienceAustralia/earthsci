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
package au.gov.ga.earthsci.worldwind.common.layers.model.gocad;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import javax.media.opengl.GL;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.earthsci.worldwind.common.layers.Bounds;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;

/**
 * Unit tests for the {@link GocadVoxetReader} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GocadVoxetReaderTest extends AbstractGocadReaderTest<FastShape>
{

	private GocadVoxetReader classUnderTest;
	private GocadReaderParameters params;
	private URL testFile = getClass().getResource("voxet/test_voxet_5_5_5.vo");

	@Before
	public void setup()
	{
		classUnderTest = new GocadVoxetReader();
		params = new GocadReaderParameters();
	}

	@Test
	public void testBasicRead() throws Exception
	{
		FastShape result = readFile(classUnderTest, params, testFile);

		assertBasicProperties(result);
	}

	@Test
	public void testReadWithBilinearMinification() throws Exception
	{
		params.setBilinearMinification(true);

		FastShape result = readFile(classUnderTest, params, testFile);

		assertBasicProperties(result);
	}

	private void assertBasicProperties(FastShape result)
	{
		assertNotNull(result);
		assertTrue(result.getMode() == GL.GL_POINTS);
		assertEquals(125, result.getPositions().size());
		assertEquals("test2", result.getName());

		Bounds bounds = result.getBounds();
		assertEquals(-4535070.00, bounds.minimum.latitude.degrees, 0.001);
		assertEquals(-1731127.25, bounds.minimum.longitude.degrees, 0.001);
		assertEquals(-1355693.00, bounds.maximum.latitude.degrees, 0.001);
		assertEquals(1542068.00, bounds.maximum.longitude.degrees, 0.001);
	}
}
