package au.gov.ga.earthsci.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PatternPipeTest
{
	private InputStream is;

	@Before
	public void setup()
	{
		is = new InputStream()
		{
			private int position = 0;
			private String[] strings = new String[] { "one fish ", "two fi", "sh red fish blue fi", "sh" };

			@Override
			public int read(byte[] b, int off, int len) throws IOException
			{
				if (position < strings.length)
				{
					byte[] s = strings[position++].getBytes();
					len = Math.min(len, s.length);
					System.arraycopy(s, 0, b, off, len);
					return len;
				}
				return -1;
			}

			@Override
			public int read() throws IOException
			{
				return -1;
			}
		};
	}

	@Test
	public void testSinglePattern() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Map<String, byte[]> patterns = new HashMap<String, byte[]>();
		patterns.put("fish", "fish".getBytes());
		final List<String> patternsFound = new ArrayList<String>();
		PatternPipe.copy(is, baos, patterns, new PatternPipe.Callback()
		{
			@Override
			public void patternFound(String patternId, byte[] pattern, OutputStream out)
			{
				patternsFound.add(patternId);
			}
		});
		Assert.assertEquals("one  two  red  blue ", baos.toString());
		Assert.assertArrayEquals(patternsFound.toArray(), new String[] { "fish", "fish", "fish", "fish" });
	}
	
	@Test
	public void testMultiplePatterns() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Map<String, byte[]> patterns = new HashMap<String, byte[]>();
		patterns.put("fish", "fish".getBytes());
		patterns.put("two", "two".getBytes());
		patterns.put("red", "red ".getBytes());
		final List<String> patternsFound = new ArrayList<String>();
		PatternPipe.copy(is, baos, patterns, new PatternPipe.Callback()
		{
			@Override
			public void patternFound(String patternId, byte[] pattern, OutputStream out)
			{
				patternsFound.add(patternId);
			}
		});
		Assert.assertEquals("one     blue ", baos.toString());
		Assert.assertArrayEquals(patternsFound.toArray(), new String[] { "fish", "two", "fish", "red", "fish", "fish" });
	}
}
