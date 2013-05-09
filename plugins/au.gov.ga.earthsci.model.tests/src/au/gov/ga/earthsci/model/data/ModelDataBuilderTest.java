package au.gov.ga.earthsci.model.data;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.eclipse.uomo.units.SI;
import org.junit.Test;

import au.gov.ga.earthsci.common.buffer.BufferType;

/**
 * Unit tests for the {@link ModelDataBuilder} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ModelDataBuilderTest
{

	@Test(expected = IllegalArgumentException.class)
	public void testBuildWithNullBuffer()
	{
		ByteBuffer buffer = null;

		ModelDataBuilder.createFromBuffer(buffer).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildWithNoBufferType()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[12]);

		ModelDataBuilder.createFromBuffer(buffer).build();
	}

	@Test
	public void testBuildWithMinimalValid()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[12]);

		IModelData data = ModelDataBuilder.createFromBuffer(buffer).ofType(BufferType.BYTE).build();

		assertNotNull(data.getId());
		assertNull(data.getName());
		assertNull(data.getDescription());
		assertEquals(buffer, data.getSource());
		assertNull(data.getNoDataValue());
		assertFalse(data.hasUnits());
		assertNull(data.getUnits());
		assertEquals(1, data.getGroupSize());
		assertEquals(12, data.getNumberOfValues());
		assertEquals(12, data.getNumberOfGroups());
	}

	@Test
	public void testBuildWithCompleteValid()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[12]);

		IModelData data =
				ModelDataBuilder.createFromBuffer(buffer).ofType(BufferType.BYTE).withId("dataId").named("myData")
						.describedAs("some data").withNodata((byte) 1).withUnits(SI.SECOND).withGroupSize(3).build();

		assertEquals("dataId", data.getId());
		assertEquals("myData", data.getName());
		assertEquals("some data", data.getDescription());
		assertEquals(buffer, data.getSource());
		assertEquals((byte) 1, data.getNoDataValue());
		assertTrue(data.hasUnits());
		assertEquals(SI.SECOND, data.getUnits());
		assertEquals(3, data.getGroupSize());
		assertEquals(12, data.getNumberOfValues());
		assertEquals(4, data.getNumberOfGroups());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuildWithInvalidGroupSize()
	{
		ByteBuffer buffer = ByteBuffer.wrap(new byte[12]);

		ModelDataBuilder.createFromBuffer(buffer).ofType(BufferType.BYTE).withGroupSize(0).build();
	}
}
