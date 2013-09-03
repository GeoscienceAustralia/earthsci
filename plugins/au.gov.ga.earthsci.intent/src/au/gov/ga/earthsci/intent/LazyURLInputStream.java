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
package au.gov.ga.earthsci.intent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * InputStream that reads from a URL, using {@link URL#openStream()} to open the
 * stream. The stream is opened upon the first use of InputStream's methods.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LazyURLInputStream extends InputStream
{
	private final URL url;
	private InputStream is;

	public LazyURLInputStream(URL url)
	{
		this.url = url;
	}

	protected void ensureOpen() throws IOException
	{
		if (is == null)
		{
			is = url.openStream();
		}
	}

	protected void tryEnsureOpen()
	{
		try
		{
			ensureOpen();
		}
		catch (IOException e)
		{
		}
	}

	@Override
	public void close() throws IOException
	{
		if (is != null)
		{
			is.close();
			is = null;
		}
	}

	@Override
	public int read() throws IOException
	{
		ensureOpen();
		return is.read();
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		ensureOpen();
		return is.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		ensureOpen();
		return is.read(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException
	{
		ensureOpen();
		return is.skip(n);
	}

	@Override
	public int available() throws IOException
	{
		ensureOpen();
		return is.available();
	}

	@Override
	public void reset() throws IOException
	{
		ensureOpen();
		is.reset();
	}

	@Override
	public boolean markSupported()
	{
		tryEnsureOpen();
		if (is != null)
		{
			return is.markSupported();
		}
		return false;
	}

	@Override
	public void mark(int readlimit)
	{
		tryEnsureOpen();
		if (is != null)
		{
			is.mark(readlimit);
		}
	}
}
