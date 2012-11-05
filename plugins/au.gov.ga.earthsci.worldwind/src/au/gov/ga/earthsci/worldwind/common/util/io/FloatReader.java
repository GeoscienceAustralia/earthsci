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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * A class that can read a specified number of 32bit float values
 * from a binary source.
 * <p/>
 * Values are read in groups; and an offset, separation and gap can be specified to control 
 * the pattern of bytes that are read. This allows complex striding patterns to be specified 
 * as required.
 * <p/>
 * This implementation is threadsafe <em>if all access to the underlying stream is performed through this class's methods</em>.
 * If the underlying stream is accessed outside of this class behaviour is indeterminate.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class FloatReader
{

	/** The input stream to read bytes from */
	private final InputStream is;
	
	/** The offset to start reading from in the provided input stream */
	private final int offset;
	
	/** The number of floats to read in each call to {@link #readNextValues} */
	private final int groupSize;
	
	/** The number of bytes to skip between groups */
	private final int groupSeparation;
	
	/** The number of bytes to skip between elements of a single group */
	private final int groupValueGap;
	
	/** The format of floats to read */
	private final FloatFormat format;;
	
	/** The byte order of the stream being read */
	private final ByteOrder byteOrder;
	
	/**
	 * Create a new float reader that reads floats one at a time from the provided input stream.
	 * <p/>
	 * Convenience constructor. For more configuration options, use the provided Builder class.
	 */
	public FloatReader(InputStream is) throws IOException
	{
		this(is, 0, 1, 0, 0, FloatFormat.IEEE, ByteOrder.LITTLE_ENDIAN);
	}
	
	/**
	 * Private constructor used to set all configuration parameters.
	 * <p/>
	 * Use the builder class to instantiate fully configured readers.
	 */
	private FloatReader(InputStream is, int offset, int groupSize, int groupSeparation, int groupValueGap, FloatFormat format, ByteOrder byteOrder) throws IOException
	{
		Validate.notNull(is, "An input stream is required");
		this.is = is;
		this.offset = offset;
		this.groupSize = groupSize;
		this.groupSeparation = groupSeparation;
		this.groupValueGap = groupValueGap;
		this.format = format;
		this.byteOrder = byteOrder;
		skipToStart();
	}

	/**
	 * Read the next group of values from the input and place them in the provided
	 * values array.
	 * <p/>
	 * Read values will be written into the values array from index 0.
	 * <p/>
	 * If the provided array does not have enough capacity to store the read values, 
	 * an {@link IllegalArgumentException} will be thrown.
	 * <p/>
	 * If there are not enough bytes left in the input stream to read the float group the remainder of the group 
	 * will be set to NaN.
	 * <p/>
	 * After reading the input stream will be positioned at the start of the <em>next</em> value group.
	 * 
	 * @param values An array to store the read float values in. 
	 * 
	 * @throws IllegalArgumentException if the provided array does not have enough capacity to store the read values
	 * @throws IOException if there is a problem reading from the underlying stream
	 */
	synchronized public void readNextValues(float[] values) throws IOException
	{
		Validate.notNull(values, "A values array is required");
		Validate.isTrue(values.length >= groupSize, "Provided values array has length " + values.length + ". Must have at least " + groupSize + " elements to read float group");
		
		for (int i = 0; i < groupSize; i++)
		{
			values[i] = readFloat();
			if (i != groupSize - 1)
			{
				skipToNextValueInGroup();
			}
		}
		skipToStartOfNextGroup();
	}

	/**
	 * Read the next group of values from the input and returns them in the result array.
	 * <p/>
	 * Provided as a convenience method. This method creates a new result array on every call.
	 * The alternative {@link #readNextValues(float[])} is more efficient as the
	 * same array can be reused by the client.
	 *  
	 * @see #readNextValues(float[])
	 */
	synchronized public float[] readNextValues() throws IOException
	{
		float[] values = new float[groupSize];
		readNextValues(values);
		return values;
	}
	
	/**
	 * Skip ahead to the start of the next value group.
	 * <p/>
	 * Has the same effect as {@link #readNextValues(float[])} except no 
	 * values are read from the input stream.
	 * 
	 * @throws IOException if there is a problem reading from the underlying stream
	 */
	synchronized public void skipToNextGroup() throws IOException
	{
		int skipCount = groupSize * 4 + (groupSize - 1) * groupValueGap + groupSeparation;
		skip(skipCount);
	}
	
	/**
	 * Skip ahead by the provided number of bytes, or to the end of the stream if there are fewer bytes in the stream
	 * than are to be skipped.
	 * 
	 * @param numBytes The number of bytes to skip
	 * 
	 * @throws IOException If there is a problem accessing the underling stream
	 */
	synchronized public void skip(long numBytes) throws IOException
	{
		is.skip(numBytes);
	}
	
	/**
	 * @return The next float value in the stream
	 */
	private float readFloat() throws IOException
	{
		int b0, b1, b2, b3;
		if (byteOrder == ByteOrder.LITTLE_ENDIAN)
		{
			b3 = is.read();
			b2 = is.read();
			b1 = is.read();
			b0 = is.read();
		}
		else
		{
			b0 = is.read();
			b1 = is.read();
			b2 = is.read();
			b3 = is.read();
		}
		return format.bytesToFloat(b0, b1, b2, b3);
	}
	
	/**
	 * Skip forward to the start of the next value in the current value group
	 */
	private void skipToNextValueInGroup() throws IOException
	{
		if (groupValueGap > 0)
		{
			skip(groupValueGap);
		}
	}
	
	/**
	 * Skip forward to the start of the next value group
	 */
	private void skipToStartOfNextGroup() throws IOException
	{
		if (groupSeparation > 0)
		{
			skip(groupSeparation);
		}
	}
	
	/**
	 * Skip forward to the start of the first value group
	 */
	private void skipToStart() throws IOException
	{
		skip(offset);
	}
	
	/** An enumeration of supported floating point formats */ 
	public static enum FloatFormat
	{
		// Enum strategy pattern
		
		IEEE {
			@Override
			public float bytesToFloat(int b0, int b1, int b2, int b3)
			{
				return Float.intBitsToFloat((b0) | (b1 << 8) | (b2 << 16) | b3 << 24);
			}
		},
		IBM {
			@Override
			public float bytesToFloat(int b0, int b1, int b2, int b3)
			{
				byte S = (byte) ((b3 & 0x80) >> 7);
				int E = (b3 & 0x7f);
				long F = (b2 << 16) + (b1 << 8) + b0;
		
				if (S == 0 && E == 0 && F == 0)
				{
					return 0;
				}
		
				double A = 16.0;
				double B = 64.0;
				double e24 = 16777216.0; // 2^24
				double M = F / e24;
		
				double F1 = S == 0 ? 1.0 : -1.0;
				return (float) (F1 * M * Math.pow(A, E - B));
			}
		};
		
		public abstract float bytesToFloat(int b0, int b1, int b2, int b3);
	}
	
	/**
	 * A Builder used to construct fully configured {@link FloatReader} instances
	 * 
	 * @author James Navin (james.navin@ga.gov.au)
	 */
	public static class Builder
	{
		private Builder(){};
		
		private InputStream is;
		private int offset = 0;
		private int groupSize = 1;
		private int groupSeparation = 0;
		private int groupValueGap = 0;
		private FloatFormat format = FloatFormat.IEEE;
		private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
		
		/** Create a new builder for a {@link FloatReader} that wraps the provided {@link InputStream} */
		public static Builder newFloatReaderForStream(InputStream s)
		{
			Builder result = new Builder();
			result.is = s;
			return result;
		}
		
		/** Configure the offset to start reading from in the input stream */
		public Builder withOffset(int offset)
		{
			this.offset = offset;
			return this;
		}
		
		/** Configure the number of floats to read in each call to {@link FloatReader#readNextValues} */
		public Builder withGroupSize(int groupSize)
		{
			this.groupSize = groupSize;
			return this;
		}
		
		/** Configure the number of bytes to skip between groups */
		public Builder withGroupSeparation(int groupSeparation)
		{
			this.groupSeparation = groupSeparation;
			return this;
		}
		
		/** Configure the number of bytes to skip between elements of a single group */
		public Builder withGroupValueGap(int groupValueGap)
		{
			this.groupValueGap = groupValueGap;
			return this;
		}
		
		/** Configure the format of floats to read */
		public Builder withFormat(FloatFormat format)
		{
			this.format = format;
			return this;
		}
		
		/** Configure the byte order of the stream being read */
		public Builder withByteOrder(ByteOrder byteOrder)
		{
			this.byteOrder = byteOrder;
			return this;
		}
		
		/** Construct a {@link FloatReader} using the configured parameters */
		public FloatReader build() throws IOException
		{
			return new FloatReader(is, offset, groupSize, groupSeparation, groupValueGap, format, byteOrder);
		}
	}

	public int getOffset()
	{
		return offset;
	}

	public int getGroupSize()
	{
		return groupSize;
	}

	public int getGroupSeparation()
	{
		return groupSeparation;
	}

	public int getGroupValueGap()
	{
		return groupValueGap;
	}

	public FloatFormat getFormat()
	{
		return format;
	}

	public ByteOrder getByteOrder()
	{
		return byteOrder;
	}
}
