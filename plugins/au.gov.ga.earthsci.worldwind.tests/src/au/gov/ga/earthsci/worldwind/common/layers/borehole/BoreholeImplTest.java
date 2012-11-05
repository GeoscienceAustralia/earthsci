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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import gov.nasa.worldwind.util.Logging;

import java.awt.Color;
import java.util.logging.Level;

import org.jmock.Mockery;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;

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
	
	private Mockery mockContext;
	BoreholeImpl classUnderTest;
	
	@Before
	public void setup()
	{
		mockContext = new Mockery();
		
		BoreholeLayer layer = mockContext.mock(BoreholeLayer.class);
		Position position = Position.fromDegrees(100, 100);
		MarkerAttributes attrs = new BasicMarkerAttributes();
		
		classUnderTest = new BoreholeImpl(layer, position, attrs);
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void testCreateWithNullLayer()
	{
		BoreholeLayer layer = null;
		Position position = Position.fromDegrees(100, 100);
		MarkerAttributes attrs = new BasicMarkerAttributes();
		
		new BoreholeImpl(layer, position, attrs);
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void testCreateWithNullPosition()
	{
		BoreholeLayer layer = mockContext.mock(BoreholeLayer.class);
		Position position = null;
		MarkerAttributes attrs = new BasicMarkerAttributes();
		
		new BoreholeImpl(layer, position, attrs);
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void testCreateWithNullAttributes()
	{
		BoreholeLayer layer = mockContext.mock(BoreholeLayer.class);
		Position position = Position.fromDegrees(100, 100);
		MarkerAttributes attrs = null;
		
		new BoreholeImpl(layer, position, attrs);
	}
	
	@Test
	public void testShapesNotInitialisedOnCreate()
	{
		assertNull(classUnderTest.getSamplesShape());
		assertNull(classUnderTest.getCentrelineShape());
		
		assertEquals(0, classUnderTest.getSamples().size());
	}
	
	@Test
	public void testAddNonNullSample()
	{
		classUnderTest.addSample(createSampleForBoreHole(classUnderTest, 0, 15, Color.RED));
		
		assertNull(classUnderTest.getSamplesShape());
		assertNull(classUnderTest.getCentrelineShape());
		
		assertEquals(1, classUnderTest.getSamples().size());
	}
	
	@Test
	public void testAddNullSample()
	{
		classUnderTest.addSample(createSampleForBoreHole(classUnderTest, 0, 15, Color.RED));
		classUnderTest.addSample(null);
		
		assertNull(classUnderTest.getSamplesShape());
		assertNull(classUnderTest.getCentrelineShape());
		
		assertEquals(1, classUnderTest.getSamples().size());
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void testAddSampleFromWrongBorehole()
	{
		classUnderTest.addSample(createSampleForBoreHole(mockContext.mock(Borehole.class), 0, 15, Color.RED));
	}
	
	@Test
	public void testLoadCompleteWithNoSamples()
	{
		classUnderTest.loadComplete();
		
		FastShape samplesShape = classUnderTest.getSamplesShape();
		assertNotNull(samplesShape);
		assertEquals(0, samplesShape.getPositions().size());
		
		FastShape centrelineShape = classUnderTest.getCentrelineShape();
		assertNotNull(centrelineShape);
		assertEquals(0, centrelineShape.getPositions().size());
	}
	
	@Test
	public void testLoadCompleteWithSamples()
	{
		classUnderTest.addSample(createSampleForBoreHole(classUnderTest, 0, 15, Color.RED));
		classUnderTest.addSample(createSampleForBoreHole(classUnderTest, 20, 25, Color.GREEN));
		classUnderTest.addSample(createSampleForBoreHole(classUnderTest, 50, 150, Color.BLUE));
		
		classUnderTest.loadComplete();
		
		FastShape samplesShape = classUnderTest.getSamplesShape();
		assertNotNull(samplesShape);
		assertEquals(6, samplesShape.getPositions().size());
		float[] colorBuffer = new float[]{1,0,0,1,0,0,
										  0,1,0,0,1,0,
										  0,0,1,0,0,1};
		assertArrayEquals(colorBuffer, samplesShape.getColorBuffer(), 0.001f);
		
		FastShape centrelineShape = classUnderTest.getCentrelineShape();
		assertNotNull(centrelineShape);
		assertEquals(2, centrelineShape.getPositions().size());
		assertNull(centrelineShape.getColorBuffer());
		assertEquals(Color.LIGHT_GRAY, centrelineShape.getColor());
	}
	
	private BoreholeSample createSampleForBoreHole(Borehole b, double depthFrom, double depthTo, Color color)
	{
		BoreholeSampleImpl sample = new BoreholeSampleImpl(b);
		sample.setColor(color);
		sample.setDepthFrom(depthFrom);
		sample.setDepthTo(depthTo);
		return sample;
	}
	
}
