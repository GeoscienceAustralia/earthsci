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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import au.gov.ga.earthsci.common.util.TailByteArrayOutputStream;

/**
 * Helper class that installs delegator output streams to intercept System.out
 * and System.err calls. The streams delegate their methods to an internal list
 * of output streams. The data is collected, up to a configurable limit, after
 * which only the tail is kept.
 * <p/>
 * Callers can add output streams that will be written to whenever
 * System.out/System.err are written to. The history will also optionally be
 * written to the added streams.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public enum StandardOutputCollector
{
	INSTANCE;

	public static final int DEFAULT_LIMIT = 1024 * 1024; //1MB

	private final OutputStreamSet outSet = new OutputStreamSet();
	private final OutputStreamSet errSet = new OutputStreamSet();
	private final PrintStream outStream = new PrintStream(outSet);
	private final PrintStream errStream = new PrintStream(errSet);
	private final OutputCollector collector = new OutputCollector();

	private StandardOutputCollector()
	{
		collector.setLimit(DEFAULT_LIMIT);
	}

	/**
	 * Replace the System.out and System.err with those in this class, to
	 * capture the bytes written to them. Calls
	 * {@link System#setOut(PrintStream)} and {@link System#setErr(PrintStream)}
	 * with the sets from this class.
	 */
	public void install()
	{
		if (System.out != outStream)
		{
			outSet.add(System.out);
			outSet.add(collector.out);
			System.setOut(outStream);
		}
		if (System.err != errStream)
		{
			errSet.add(System.err);
			errSet.add(collector.err);
			System.setErr(errStream);
		}
	}

	/**
	 * @return The limit of tail bytes to capture
	 */
	public int getLimit()
	{
		return collector.getLimit();
	}

	/**
	 * Set the limit of bytes to capture. The tail is always kept. Default value
	 * is {@value #DEFAULT_LIMIT}.
	 * 
	 * @param limit
	 */
	public void setLimit(int limit)
	{
		collector.setLimit(limit);
	}

	/**
	 * Write the data collected by this collector to the given output streams.
	 * 
	 * @param out
	 *            Output stream to write the standard out data collected
	 * @param err
	 *            Output stream to write the standard err data collected
	 * @throws IOException
	 */
	public void writeHistory(OutputStream out, OutputStream err) throws IOException
	{
		collector.writeToStreams(out, err);
	}

	/**
	 * Add the given streams as streams that are written to when standard out
	 * and standard error are written to. Can also optionally write all the data
	 * collected up until now.
	 * 
	 * @param out
	 *            Stream to write standard out to
	 * @param err
	 *            Stream to write standard err to
	 * @param writeHistory
	 *            Should the data collected until now be written immediately?
	 */
	public void addStreams(OutputStream out, OutputStream err, boolean writeHistory)
	{
		IOException exception = null;
		synchronized (collector)
		{
			if (writeHistory)
			{
				try
				{
					writeHistory(out, err);
				}
				catch (IOException e)
				{
					//don't write anything to the console here, must write outside
					//the synch block
					exception = e;
				}
			}

			//there's no chance for System.out to be written to between the above
			//and below, due to the sychronization on the collector and the fact
			//that the collector's write methods are also synchronized on itself

			outSet.add(out);
			errSet.add(err);
		}

		if (exception != null)
		{
			exception.printStackTrace();
		}
	}

	/**
	 * Remove the given streams from the set of streams written to.
	 * 
	 * @param out
	 *            Output stream to remove
	 * @param err
	 *            Error stream to remove
	 * @see #addStreams(OutputStream, OutputStream, boolean)
	 */
	public void removeStreams(OutputStream out, OutputStream err)
	{
		synchronized (collector)
		{
			outSet.remove(out);
			errSet.remove(err);
		}
	}

	/**
	 * Collects output from output and error streams into a byte array with a
	 * configurable limit. Keeps track of which sections of the byte array come
	 * from the output stream and which come from the error stream. Can rewrite
	 * the historic output/error data captured to another pair of streams, and
	 * will write in the same order that it was written to.
	 */
	private class OutputCollector extends TailByteArrayOutputStream
	{
		private int[] switchIndices = new int[8];
		private int switchIndicesCount = 0;

		public OutputCollector()
		{
			setLimit(DEFAULT_LIMIT);
		}

		/**
		 * Write the historic stream data to the given output streams.
		 * 
		 * @param out
		 * @param err
		 * @throws IOException
		 */
		protected synchronized void writeToStreams(OutputStream out, OutputStream err)
				throws IOException
		{
			OutputStream current = out;

			//find first index that is not before the start index
			int startIndex = 0;
			for (int i = 0; i < switchIndicesCount; i++)
			{
				if (switchIndices[i] >= start)
				{
					break;
				}
				startIndex++;
				current = swap(out, err, current);
			}

			int previousIndex = start;
			for (int i = startIndex; i < switchIndicesCount; i++)
			{
				int index = switchIndices[i];
				int length = index - previousIndex;
				if (length > 0)
				{
					current.write(buf, previousIndex, length);
					current.flush();
				}
				previousIndex = index;
				current = swap(out, err, current);
			}

			int length = count - previousIndex;
			if (length > 0)
			{
				current.write(buf, previousIndex, length);
				current.flush();
			}
		}

		private OutputStream swap(OutputStream stream1, OutputStream stream2,
				OutputStream current)
		{
			return current != stream1 ? stream1 : stream2;
		}

		@Override
		protected synchronized void tailUpdated(int movement, int start, int count)
		{
			if (movement != 0)
			{
				//remove any pairs of indices that will both be negative
				//after the movement:
				int removeCount = 0;
				for (int i = 1; i < switchIndicesCount; i += 2)
				{
					if (switchIndices[i] - movement >= 0)
					{
						//found a second index in the pair that will be
						//positive, so don't remove
						break;
					}
					removeCount += 2;
				}
				removeCount = Math.min(removeCount, switchIndicesCount);
				System.arraycopy(switchIndices, removeCount, switchIndices, 0,
						switchIndicesCount - removeCount);
				switchIndicesCount -= removeCount;

				//decrease the remainder of the indices by the movement amount
				for (int i = 0; i < switchIndicesCount; i++)
				{
					switchIndices[i] -= movement;
				}
			}
		}

		private synchronized void switchToError(boolean error)
		{
			//an even number of indices means current mode is out,
			//odd means current mode is error 
			if ((switchIndicesCount % 2 == 0) == error)
			{
				if (switchIndicesCount + 1 > switchIndices.length)
				{
					int[] newArray = new int[switchIndices.length << 1];
					System.arraycopy(switchIndices, 0, newArray, 0, switchIndicesCount);
					switchIndices = newArray;
				}
				switchIndices[switchIndicesCount++] = count;
			}
		}

		/**
		 * {@link FilterOutputStream} that writes to the {@link OutputCollector}
		 * after switching it to non-error mode.
		 */
		public OutputStream out = new FilterOutputStream(this)
		{
			@Override
			public void write(int b) throws IOException
			{
				switchToError(false);
				out.write(b);
			}

			@Override
			public void write(byte[] b) throws IOException
			{
				switchToError(false);
				out.write(b);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException
			{
				switchToError(false);
				out.write(b, off, len);
			}
		};

		/**
		 * {@link FilterOutputStream} that writes to the {@link OutputCollector}
		 * after switching it to error mode.
		 */
		public OutputStream err = new FilterOutputStream(this)
		{
			@Override
			public void write(int b) throws IOException
			{
				switchToError(true);
				out.write(b);
			}

			@Override
			public void write(byte[] b) throws IOException
			{
				switchToError(true);
				out.write(b);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException
			{
				switchToError(true);
				out.write(b, off, len);
			}
		};
	}
}
