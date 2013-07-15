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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import ch.qos.logback.core.CoreConstants;

/**
 * {@link OutputStream} that redirects lines to other output streams if the line
 * matches a given regex.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RegexOutputStream extends OutputStream
{
	private final Charset charset = Charset.forName("UTF-8"); //$NON-NLS-1$
	private final StringBuilder buffer = new StringBuilder();
	private final OutputStream defaultOutputStream;

	private final Map<String, Pattern> patterns = new HashMap<String, Pattern>();
	private final Map<String, OutputStream> streams = new HashMap<String, OutputStream>();

	/**
	 * Constructor.
	 * 
	 * @param defaultOutputStream
	 *            {@link OutputStream} that is written to if a line doesn't
	 *            match any regexes
	 */
	public RegexOutputStream(OutputStream defaultOutputStream)
	{
		this.defaultOutputStream = defaultOutputStream;
	}

	/**
	 * Added an {@link OutputStream} that will be written to if a line written
	 * to this matches the given regex pattern.
	 * 
	 * @param regex
	 *            Regex to match
	 * @param os
	 *            Output stream to write to
	 */
	public void add(String regex, OutputStream os)
	{
		streams.put(regex, os);
		patterns.put(regex, Pattern.compile(regex));
	}

	/**
	 * Remove the added regex, and it's associated {@link OutputStream}.
	 * 
	 * @param regex
	 *            Regex to remove
	 */
	public void remove(String regex)
	{
		streams.remove(regex);
		patterns.remove(regex);
	}

	@Override
	public void write(int b) throws IOException
	{
		write(new byte[] { (byte) b });
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		String s = new String(b, off, len, charset);
		buffer.append(s);

		int indexOfNewLine;
		while ((indexOfNewLine = buffer.indexOf(CoreConstants.LINE_SEPARATOR)) >= 0)
		{
			int end = indexOfNewLine + CoreConstants.LINE_SEPARATOR_LEN;
			String line = buffer.substring(0, end);
			String lineWithoutNewLine = buffer.substring(0, indexOfNewLine);
			boolean matched = false;
			for (Entry<String, Pattern> pattern : patterns.entrySet())
			{
				if (pattern.getValue().matcher(lineWithoutNewLine).matches())
				{
					streams.get(pattern.getKey()).write(line.getBytes());
					matched = true;
					break;
				}
			}
			if (!matched)
			{
				defaultOutputStream.write(line.getBytes());
			}
			buffer.delete(0, end);
		}
	}

	@Override
	public void flush() throws IOException
	{
		defaultOutputStream.flush();
		for (OutputStream os : streams.values())
		{
			os.flush();
		}
	}
}
