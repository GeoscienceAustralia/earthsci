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
package au.gov.ga.earthsci.core.retrieve.retriever;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import au.gov.ga.earthsci.core.retrieve.IRetrieverMonitor;

/**
 * InputStream that progresses an {@link IRetrieverMonitor}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MonitorInputStream extends FilterInputStream
{
	private final IRetrieverMonitor monitor;

	public MonitorInputStream(InputStream stream, IRetrieverMonitor monitor)
	{
		super(stream);
		this.monitor = monitor;
	}

	@Override
	public int read() throws IOException
	{
		int i = super.read();
		if (i >= 0)
		{
			//1 byte read
			progress(1);
		}
		return i;
	}

	@Override
	public int read(byte[] b) throws IOException
	{
		return progress(super.read(b));
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		return progress(super.read(b, off, len));
	}

	private int progress(int i)
	{
		if (monitor.isCanceled() || monitor.isPaused())
		{
			throw new MonitorCancelledOrPausedException();
		}
		if (i > 0)
		{
			monitor.progress(i);
		}
		return i;
	}
}
