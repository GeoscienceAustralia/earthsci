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
package au.gov.ga.earthsci.ant;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Helper utility class.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Util
{
	private static Map<String, String> osMap = new HashMap<String, String>();
	private static Map<String, String> archMap = new HashMap<String, String>();

	static
	{
		osMap.put("win32", "Windows");
		osMap.put("macosx", "Mac OS X");
		osMap.put("linux", "Linux");
	}

	public static String getJnlpOs(String os)
	{
		if (osMap.containsKey(os))
		{
			return osMap.get(os);
		}
		return os;
	}

	public static String getJnlpArch(String arch)
	{
		if (archMap.containsKey(arch))
		{
			return archMap.get(arch);
		}
		return arch;
	}

	/**
	 * From <a
	 * href="http://stackoverflow.com/a/1269907">http://stackoverflow.com
	 * /a/1269907</a>.
	 * 
	 * @param target
	 *            File to relativize
	 * @param baseDirectory
	 *            Base directory to relativize to (treated as a directory:
	 *            /dir/file.txt treated as /dir/file.txt/)
	 * @return Relativized file
	 * @throws IOException
	 *             If an IOException occurs when calling
	 *             {@link File#getCanonicalPath()}
	 */
	public static File getRelativeFile(File target, File baseDirectory) throws IOException
	{
		String[] baseComponents = baseDirectory.getCanonicalPath().split(Pattern.quote(File.separator));
		String[] targetComponents = target.getCanonicalPath().split(Pattern.quote(File.separator));

		// skip common components
		int index = 0;
		for (; index < targetComponents.length && index < baseComponents.length; ++index)
		{
			if (!targetComponents[index].equals(baseComponents[index]))
				break;
		}

		StringBuilder result = new StringBuilder();
		if (index != baseComponents.length)
		{
			// backtrack to base directory
			for (int i = index; i < baseComponents.length; ++i)
				result.append(".." + File.separator);
		}
		for (; index < targetComponents.length; ++index)
			result.append(targetComponents[index] + File.separator);
		if (!target.getPath().endsWith("/") && !target.getPath().endsWith("\\"))
		{
			// remove final path separator
			result.delete(result.length() - File.separator.length(), result.length());
		}
		return new File(result.toString());
	}

	/**
	 * Compare two version strings. Compares parts between '.' characters in
	 * turn. If all parts are the same, but one string has more parts, it is
	 * treated as greater.
	 * <p/>
	 * 1.0.0 == 1.0.0<br/>
	 * 1.0.0 &lt; 1.0.1<br/>
	 * 1.0 &lt; 1.0.1<br/>
	 * 1.0.0 &lt; 1.1<br/>
	 * 1.0.0.v20130101 &lt; 1.0.0.v20130102<br/>
	 * 
	 * @param version1
	 * @param version2
	 * @return 0 if versions are equal, negative integer if version 1 is less
	 *         than version2, positive integer otherwise
	 */
	public static int compareVersionStrings(String version1, String version2)
	{
		String[] split1 = version1.split("\\.");
		String[] split2 = version2.split("\\.");
		int min = Math.min(split1.length, split2.length);
		for (int i = 0; i < min; i++)
		{
			int c = compareIntString(split1[i], split2[i]);
			if (c != 0)
			{
				return c;
			}
		}
		return compareInts(split1.length, split2.length);
	}

	private static int compareIntString(String s1, String s2)
	{
		if (s1.equals(s2))
		{
			return 0;
		}
		try
		{
			int i1 = Integer.parseInt(s1);
			int i2 = Integer.parseInt(s2);
			return compareInts(i1, i2);
		}
		catch (Exception e)
		{
			//not ints, compare as strings
			return s1.compareToIgnoreCase(s2);
		}
	}

	private static int compareInts(int i1, int i2)
	{
		return i1 - i2;
	}
	
	public static boolean isEmpty(String s)
	{
		return s == null || s.length() == 0;
	}
}
