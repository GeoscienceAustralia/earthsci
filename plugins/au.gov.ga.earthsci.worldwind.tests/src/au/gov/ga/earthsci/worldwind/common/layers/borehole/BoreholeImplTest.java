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
package au.gov.ga.earthsci.worldwind.common.layers.borehole;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import gov.nasa.worldwind.util.Logging;

import java.util.logging.Level;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for the {@link BoreholeImpl}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BoreholeImplTest
{

	@BeforeClass
	public static void init()
	{
		Logging.logger().setLevel(Level.OFF);
	}

	@AfterClass
	public static void destroy()
	{
		Logging.logger().setLevel(Level.SEVERE);
	}

	BoreholeImpl classUnderTest;

	@Before
	public void setup()
	{
		Position position = Position.fromDegrees(100, 100);
		MarkerAttributes attrs = new BasicMarkerAttributes();

		classUnderTest = new BoreholeImpl(position, attrs);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithNullLayer()
	{
		Position position = Position.fromDegrees(100, 100);
		MarkerAttributes attrs = new BasicMarkerAttributes();

		new BoreholeImpl(position, attrs);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithNullPosition()
	{
		Position position = null;
		MarkerAttributes attrs = new BasicMarkerAttributes();

		new BoreholeImpl(position, attrs);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithNullAttributes()
	{
		Position position = Position.fromDegrees(100, 100);
		MarkerAttributes attrs = null;

		new BoreholeImpl(position, attrs);
	}
}
