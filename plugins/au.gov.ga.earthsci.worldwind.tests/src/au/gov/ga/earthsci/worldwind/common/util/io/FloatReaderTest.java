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
package au.gov.ga.earthsci.worldwind.common.util.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.ByteOrder;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.earthsci.worldwind.common.util.io.FloatReader.FloatFormat;

/**
 * Unit tests for the {@link FloatReader} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class FloatReaderTest
{

	private InputStream is;
	
	@Before
	public void setup()
	{
		is = getClass().getResourceAsStream("bytes.out");
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testReadValuesNullArray() throws Exception
	{
		FloatReader classUnderTest = new FloatReader(is);
		classUnderTest.readNextValues(null);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testReadValuesEmptyArray() throws Exception
	{
		FloatReader classUnderTest = new FloatReader(is);
		classUnderTest.readNextValues(new float[0]);
	}
	
	@Test
	public void testDefaultConstructorDefaultPatternValues() throws Exception
	{
		FloatReader classUnderTest = new FloatReader(is);
		assertPatternCorrect(classUnderTest, 0, 1, 0, 0, FloatFormat.IEEE, ByteOrder.LITTLE_ENDIAN);
	}
	
	@Test
	public void testReadNextValuesDefaultPattern() throws Exception
	{
		FloatReader classUnderTest = new FloatReader(is);
		
		float expected = 0.0f;
		float[] values = new float[]{-9999};
		for (int i = 0; i < 100; i++)
		{
			classUnderTest.readNextValues(values);
			assertEquals(expected, values[0], 0.001);
			expected += 0.3f;
			values[0] = -9999f;
		}
	}

	@Test
	public void testBuilderPatternValues() throws Exception
	{
		FloatReader classUnderTest = FloatReader.Builder.newFloatReaderForStream(is)
														.withByteOrder(ByteOrder.BIG_ENDIAN)
														.withFormat(FloatFormat.IBM)
														.withOffset(4)
														.withGroupSize(3)
														.withGroupSeparation(5)
														.withGroupValueGap(2)
														.build();
		assertPatternCorrect(classUnderTest, 4, 3, 5, 2, FloatFormat.IBM, ByteOrder.BIG_ENDIAN);
	}
	
	@Test
	public void testReadNextValuesWithCustomFormat() throws Exception
	{
		FloatReader classUnderTest = FloatReader.Builder.newFloatReaderForStream(is)
														.withOffset(4)
														.withGroupSize(3)
														.withGroupSeparation(8)
														.withGroupValueGap(4)
														.build();
		// Expected:
		// - Skip first value (offset 4 bytes)
		// - Read three values at a time (group size 3)
		// - Groups consist of every second value (group value gap of 4 bytes)
		// - Successive groups separated by 2 values (group separation 8 bytes)
		float[][] expected = {{0.3f, 0.9f, 1.5f},{2.4f, 3.0f, 3.6f}};
		float[] values = new float[3];
		
		classUnderTest.readNextValues(values);
		assertArrayEquals(expected[0], values, 0.001f);
		
		classUnderTest.readNextValues(values);
		assertArrayEquals(expected[1], values, 0.001f);
	}
	
	@Test 
	public void testReadNextValuesUsesNaNWhenNoMoreBytes() throws Exception
	{
		FloatReader classUnderTest = FloatReader.Builder.newFloatReaderForStream(is)
														.withGroupValueGap(1)
														.withGroupSize(101)
														.build();
		
		float[] values = new float[101];
		for (int i = 0; i < values.length; i++)
		{
			values[i] = -9999;
		}
		
		classUnderTest.readNextValues(values);
		
		assertTrue(Float.isNaN(values[100]));
	}
	
	@Test
	public void testSkipWithLessThanRemainingBytes() throws Exception
	{
		FloatReader classUnderTest = new FloatReader(is);
		
		classUnderTest.skip(40);
		
		float[] value = new float[1];
		classUnderTest.readNextValues(value);
		
		assertEquals(3.0f, value[0], 0.001);
	}
	
	@Test
	public void testSkipWithMoreThanRemainingBytes() throws Exception
	{
		FloatReader classUnderTest = new FloatReader(is);
		
		classUnderTest.skip(440);
		
		float[] value = new float[1];
		classUnderTest.readNextValues(value);
		
		assertTrue(Float.isNaN(value[0]));
	}
	
	private void assertPatternCorrect(FloatReader classUnderTest, int offset, int groupSize, int groupSeparation, int groupValueGap, FloatFormat format, ByteOrder order)
	{
		assertEquals(offset, classUnderTest.getOffset());
		assertEquals(groupSize, classUnderTest.getGroupSize());
		assertEquals(groupSeparation, classUnderTest.getGroupSeparation());
		assertEquals(groupValueGap, classUnderTest.getGroupValueGap());
		assertEquals(format, classUnderTest.getFormat());
		assertEquals(order, classUnderTest.getByteOrder());
	}
	
}
