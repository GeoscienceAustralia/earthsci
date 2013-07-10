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
package au.gov.ga.earthsci.application.console;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link OutputStream} subclass that delegates all methods to a {@link Set} of
 * output streams. Callers can add output streams to the set.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OutputStreamSet extends OutputStream
{
	private final Set<OutputStream> set = new HashSet<OutputStream>();

	/**
	 * Add the given {@link OutputStream} to the delegate set.
	 * 
	 * @param os
	 */
	public void add(OutputStream os)
	{
		synchronized (set)
		{
			set.add(os);
		}
	}

	/**
	 * Remove the given {@link OutputStream} from the delegate set.
	 * 
	 * @param os
	 */
	public void remove(OutputStream os)
	{
		synchronized (set)
		{
			set.remove(os);
		}
	}

	@Override
	public void write(int b) throws IOException
	{
		synchronized (set)
		{
			for (OutputStream os : set)
			{
				os.write(b);
			}
		}
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		synchronized (set)
		{
			for (OutputStream os : set)
			{
				os.write(b);
			}
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		synchronized (set)
		{
			for (OutputStream os : set)
			{
				os.write(b, off, len);
			}
		}
	}

	@Override
	public void close() throws IOException
	{
		synchronized (set)
		{
			for (OutputStream os : set)
			{
				os.close();
			}
		}
	}

	@Override
	public void flush() throws IOException
	{
		synchronized (set)
		{
			for (OutputStream os : set)
			{
				os.flush();
			}
		}
	}
}
