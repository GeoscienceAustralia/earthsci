package au.gov.ga.earthsci.worldwind.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.BufferWrapper;

import java.nio.ByteBuffer;

import org.junit.Test;

/**
 * Unit tests for the {@link IOUtil} class
 */
public class IOUtilTest
{
	/*
	 * Note: Test file bytes.out is a simple binay file containing bytes for int 1,2,3,4,5
	 * in order.
	 */
	
	@Test
	public void testReadByteBufferNonZip() throws Exception
	{
		ByteBuffer byteBuffer = IOUtil.readByteBuffer(getClass().getResource("bytes.out"));
		
		assertNotNull(byteBuffer);
		
		assertEquals(1, byteBuffer.get());
		assertEquals(2, byteBuffer.get());
		assertEquals(3, byteBuffer.get());
		assertEquals(4, byteBuffer.get());
		assertEquals(5, byteBuffer.get());
	}
	
	@Test
	public void testReadByteBufferZip() throws Exception
	{
		ByteBuffer byteBuffer = IOUtil.readByteBuffer(getClass().getResource("bytes.zip"));
		
		assertNotNull(byteBuffer);
		
		assertEquals(1, byteBuffer.get());
		assertEquals(2, byteBuffer.get());
		assertEquals(3, byteBuffer.get());
		assertEquals(4, byteBuffer.get());
		assertEquals(5, byteBuffer.get());
	}
	
	@Test
	public void testReadByteBufferWrapperWithNativeFormat() throws Exception
	{
		BufferWrapper bufferWrapper = IOUtil.readByteBuffer(getClass().getResource("bytes.out"), AVKey.INT8, AVKey.LITTLE_ENDIAN);
		
		assertNotNull(bufferWrapper);
		assertEquals(5, bufferWrapper.getSizeInBytes());
		
		assertEquals(1, bufferWrapper.getInt(0));
		assertEquals(2, bufferWrapper.getInt(1));
		assertEquals(3, bufferWrapper.getInt(2));
		assertEquals(4, bufferWrapper.getInt(3));
		assertEquals(5, bufferWrapper.getInt(4));
	}
	
	@Test
	public void testReadByteBufferWrapperWithNonNativeFormat() throws Exception
	{
		BufferWrapper bufferWrapper = IOUtil.readByteBuffer(getClass().getResource("bytes.out"), AVKey.INT16, AVKey.LITTLE_ENDIAN);
		
		assertNotNull(bufferWrapper);
		assertEquals(4, bufferWrapper.getSizeInBytes());
		
		assertEquals(513, bufferWrapper.getInt(0));
		assertEquals(1027, bufferWrapper.getInt(1));
	}
}
