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
package au.gov.ga.earthsci.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * {@link ByteArrayOutputStream} that only keeps the tail of bytes written,
 * defined by a customizable limit.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TailByteArrayOutputStream extends ByteArrayOutputStream
{
	protected int limit = 0; //0 means unlimited
	protected float loadFactor = 0.5f;
	protected int start = 0;

	public TailByteArrayOutputStream()
	{
		super();
	}

	public TailByteArrayOutputStream(int size)
	{
		super(size);
	}

	/**
	 * @return Tail limit of bytes to store
	 */
	public int getLimit()
	{
		return limit;
	}

	/**
	 * Set the limit of the number of tail bytes to store. Passing 0 or negative
	 * means unlimited.
	 * 
	 * @param limit
	 */
	public void setLimit(int limit)
	{
		this.limit = limit;
	}

	/**
	 * @return Percentage of the limit that the internal buffer is allowed to
	 *         increase its length to.
	 */
	public float getLoadFactor()
	{
		return loadFactor;
	}

	/**
	 * Set the percentage of the <code>limit</code> that the internal buffer is
	 * allowed to increase its length to. A value of 1.0 means that the internal
	 * buffer is allowed to be 100% larger (double) than the limit. A value of
	 * 0.0 means that the internal buffer will never be larger than the limit.
	 * <p/>
	 * Larger values mean the tail will be moved back to the start of the buffer
	 * less often, at the cost of increased memory usage.
	 * <p/>
	 * Default value is 0.5.
	 * 
	 * @param loadFactor
	 */
	public void setLoadFactor(float loadFactor)
	{
		this.loadFactor = Math.max(0, loadFactor);
	}

	@Override
	public synchronized void write(int b)
	{
		moveTail(1);
		super.write(b);
	}

	@Override
	public synchronized void write(byte[] b, int off, int len)
	{
		//if limited, and the length to be written is greater than the limit,
		//reduce the length to be written to the limit
		if (limit > 0 && len > limit)
		{
			off += len - limit;
			len = limit;
		}
		moveTail(len);
		super.write(b, off, len);
	}

	/**
	 * Moves the tail of bytes to the start of the buffer if <code>len</code>
	 * will increase the size of the buffer beyond the allowed capacity.
	 * 
	 * @param len
	 */
	protected void moveTail(int len)
	{
		if (limit > 0)
		{
			int newcount = count + len;
			if (newcount > limit * (1 + loadFactor))
			{
				newcount = Math.max(0, limit - len);
				System.arraycopy(buf, count - newcount, buf, 0, newcount);
				count = newcount;
				start = 0;
			}
			else
			{
				start = Math.max(0, newcount - limit);
			}
		}
	}

	@Override
	public synchronized void reset()
	{
		super.reset();
		start = 0;
	}

	@Override
	public synchronized byte[] toByteArray()
	{
		byte[] copy = new byte[count - start];
		System.arraycopy(buf, start, copy, 0, copy.length);
		return copy;
	}

	@Override
	public synchronized String toString()
	{
		return new String(buf, start, count);
	}

	@Deprecated
	@Override
	public synchronized String toString(int hibyte)
	{
		return new String(buf, hibyte, start, count);
	}

	@Override
	public synchronized String toString(String charsetName) throws UnsupportedEncodingException
	{
		return new String(buf, start, count, charsetName);
	}

	@Override
	public synchronized void writeTo(OutputStream out) throws IOException
	{
		out.write(buf, start, count);
	}
}
