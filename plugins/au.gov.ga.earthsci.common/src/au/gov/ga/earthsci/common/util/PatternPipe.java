package au.gov.ga.earthsci.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Helper class that reads bytes from an {@link InputStream}, piping them to an
 * {@link OutputStream} until a pattern is found. If a pattern is found, a
 * callback is notified, and the pattern is not written to the output.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PatternPipe
{
	/**
	 * Callback interface for the {@link PatternPipe#copy} method
	 */
	public static interface Callback
	{
		void patternFound(String patternId, byte[] pattern, OutputStream out);
	}

	/**
	 * Copy the data from the input to the output, searching for patterns along
	 * the way. If a pattern is found, the callback is notified, and the pattern
	 * bytes are not written to the output.
	 * 
	 * @param in
	 *            Input to read from
	 * @param out
	 *            Output to write to
	 * @param patterns
	 *            Patterns to search for ({@link Map} key is the pattern id
	 *            passed to the callback, the actual byte[] pattern is stored in
	 *            the Map value)
	 * @param callback
	 *            Callback to notify of pattern matches
	 * @throws IOException
	 */
	public static void copy(InputStream in, OutputStream out, Map<String, byte[]> patterns, Callback callback)
			throws IOException
	{
		byte[][] patternsArray = new byte[patterns.size()][];
		String[] patternIds = new String[patterns.size()];
		int p = 0;
		int longestPatternLength = 0;
		for (Entry<String, byte[]> pattern : patterns.entrySet())
		{
			patternIds[p] = pattern.getKey();
			patternsArray[p] = pattern.getValue();
			longestPatternLength = Math.max(longestPatternLength, patternsArray[p].length);
			p++;
		}
		//buffer the streams, because we read a byte at a time below
		@SuppressWarnings("resource")
		InputStream input = in instanceof BufferedInputStream ? in : new BufferedInputStream(in);
		@SuppressWarnings("resource")
		OutputStream output = out instanceof BufferedOutputStream ? out : new BufferedOutputStream(out);
		//we read into a buffer that is the length of the longest pattern,
		//so we can find the pattern within
		int bufferLength = Math.max(1, longestPatternLength);
		byte[] buffer = new byte[bufferLength];
		int position = 0, count = 0;
		int skip = bufferLength - 1;
		boolean filled = false, closed = false;
		while (true)
		{
			int b = closed ? -1 : input.read();
			if (b >= 0)
			{
				buffer[position] = (byte) b;
				count = Math.min(count + 1, bufferLength);
				filled = filled || count == bufferLength;
			}
			else
			{
				closed = true;
				if (!filled)
				{
					// special case where there weren't enough input characters
					skip = 0;
					position = -1;
					filled = true;
				}
				else
				{
					count--;
				}
				if (count <= 0)
				{
					break;
				}
			}
			position = (position + 1) % bufferLength;

			for (int i = 0; i < patternsArray.length; i++)
			{
				if (count >= patternsArray[i].length
						&& arrayContainsPattern(patternsArray[i], buffer, position))
				{
					skip = Math.max(skip, patternsArray[i].length);
					if (callback != null)
					{
						output.flush();
						callback.patternFound(patternIds[i], patternsArray[i], out);
					}
				}
			}
			if (skip > 0)
			{
				skip--;
			}
			else
			{
				output.write(buffer[position]);
			}
		}
		output.flush();
	}

	/**
	 * Does <code>array</code> contain <code>pattern</code> at position
	 * <code>start</code>? Searching begins at <code>start</code> and wraps
	 * around if the position goes passed the end of the array.
	 * 
	 * @param pattern
	 *            Pattern to search for
	 * @param array
	 *            Array to search in
	 * @param start
	 *            Position in <code>array</code> to start searching from
	 * @return True if <code>array</code> contains <code>pattern</code>
	 */
	protected static boolean arrayContainsPattern(byte[] pattern, byte[] array, int start)
	{
		for (int i = 0; i < pattern.length; i++)
		{
			if (array[(start + i) % array.length] != pattern[i])
			{
				return false;
			}
		}
		return true;
	}
}
