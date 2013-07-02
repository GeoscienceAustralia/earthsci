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

import java.util.regex.Pattern;

/**
 * Some utility methods for working with Files and file paths.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class FileUtil
{

	private static final String FILE_PATH_PATTERN = "(file://)?[a-zA-Z]:[/\\\\]([^/\\\\]+[/\\\\]?)+"; //$NON-NLS-1$

	/**
	 * Return whether or not the provided string is possibly a valid file path.
	 * <p/>
	 * Note that this method is purely a pattern-based heuristic and doesn't
	 * attempt to query the file system for a file's existence.
	 * 
	 * @param s
	 *            The string to test
	 * 
	 * @return <code>true</code> if the string is likely a valid file path;
	 *         <code>false</code> otherwise.
	 */
	public static boolean isLikelyFilePath(String s)
	{
		if (s == null || s.trim().length() == 0)
		{
			return false;
		}

		return Pattern.matches(FILE_PATH_PATTERN, s);
	}

	/**
	 * Return the filename component of the provided string path, if one exists.
	 * 
	 * @param path
	 *            the string path to extract the filename from
	 * 
	 * @return The filename component of the provided path, or <code>null</code>
	 *         if none exists.
	 */
	public static String getFileName(String path)
	{
		if (!isLikelyFilePath(path))
		{
			return null;
		}

		String[] components = path.split("\\\\|/"); //$NON-NLS-1$
		return components[components.length - 1];
	}
}
