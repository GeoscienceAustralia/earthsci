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
package au.gov.ga.earthsci.worldwind.common.util;

import gov.nasa.worldwind.util.Logging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Helper class that extracts the native libraries required by the WorldWind
 * applications into a temporary directory, and then adds the directory to the
 * java.library.path.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class NativeLibraries
{
	protected final static String BASE_DIR = "/native/";
	protected final static String TEMP_DIR = "ga-worldwind-natives";
	protected static final String[] libraries = new String[] { "jogl_desktop", "nativewindow_awt",
			"nativewindow_win32", "nativewindow_macosx", "nativewindow_x11", "newt", "gluegen-rt", "gdalalljni",
			"gdalalljni32", "gdalalljni64", "webview", "WebView32", "WebView64" };
	protected static final String JAVA_LIBRARY_PATH = "java.library.path";

	/**
	 * Extract any native libraries found using the class-resource mechanism to
	 * a temporary directory, and then add the temporary directory to the
	 * java.library.path. Requires the Oracle JVM.
	 */
	public static void init()
	{
		//-Djogamp.gluegen.UseTempJarCache=false
		System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
		
		String osName = System.getProperty("os.name").toLowerCase();
		String osArch = System.getProperty("os.arch").toLowerCase();
		String directory, prefix, suffix;
		File tmpdir = new File(System.getProperty("java.io.tmpdir") + "/" + TEMP_DIR);

		if (osName.startsWith("wind"))
		{
			prefix = "";
			suffix = ".dll";
			if (osArch.startsWith("x86_64") || osArch.startsWith("amd64"))
			{
				directory = "windows-amd64";
			}
			else
			{
				directory = "windows-i586";
			}
		}
		else if (osName.startsWith("mac"))
		{
			prefix = "lib";
			suffix = ".jnilib";
			if (osArch.startsWith("ppc"))
			{
				directory = "macosx-ppc";
			}
			else
			{
				directory = "macosx-universal";
			}
		}
		else if (osName.startsWith("linux"))
		{
			prefix = "lib";
			suffix = ".so";
			if (osArch.startsWith("x86_64") || osArch.startsWith("amd64"))
			{
				directory = "linux-amd64";
			}
			else
			{
				directory = "linux-i586";
			}
		}
		else if (osName.startsWith("sun") || osName.startsWith("solaris"))
		{
			prefix = "lib";
			suffix = ".so";
			if (osArch.startsWith("sparcv9"))
			{
				directory = "solaris-sparcv9";
			}
			else if (osArch.startsWith("sparc"))
			{
				directory = "solaris-sparc";
			}
			else if (osArch.startsWith("x86_64") || osArch.startsWith("amd64"))
			{
				directory = "solaris-amd64";
			}
			else
			{
				directory = "solaris-i586";
			}
		}
		else
		{
			return;
		}

		boolean anyLibrariesWritten = false;
		for (String lib : libraries)
		{
			String filename = prefix + lib + suffix;
			String library = BASE_DIR + directory + "/" + filename;
			InputStream is = NativeLibraries.class.getResourceAsStream(library);
			if (is != null)
			{
				if (!tmpdir.exists())
				{
					tmpdir.mkdirs();
				}
				tmpdir.deleteOnExit();

				File file = new File(tmpdir, filename);
				writeStreamToFile(is, file);
				if (file.exists())
				{
					file.deleteOnExit();
					anyLibrariesWritten = true;
				}
			}
		}

		if (anyLibrariesWritten)
		{
			String pathString = buildPathString(tmpdir.getAbsolutePath());
			try
			{
				alterJavaLibraryPath(pathString);
			}
			catch (Exception e)
			{
				String message = "Error altering java.library.path: " + e.getLocalizedMessage();
				Logging.logger().severe(message);
			}
		}
	}

	protected static void writeStreamToFile(InputStream is, File file)
	{
		byte[] buffer = new byte[512];
		BufferedOutputStream out = null;
		try
		{
			try
			{
				out = new BufferedOutputStream(new FileOutputStream(file));
				while (true)
				{
					int read = is.read(buffer);
					if (read > -1)
					{
						out.write(buffer, 0, read);
					}
					else
					{
						break;
					}
				}
			}
			finally
			{
				if (out != null)
				{
					out.close();
				}
			}
		}
		catch (IOException e)
		{
		}
	}

	protected static String buildPathString(String extraFolder)
	{
		String del = System.getProperty("path.separator");
		StringBuffer path = new StringBuffer();

		path.append(extraFolder).append(del);
		path.append(".").append(del); // append current directory
		path.append(System.getProperty("user.dir")).append(del);
		path.append(System.getProperty(JAVA_LIBRARY_PATH));

		return path.toString();
	}

	protected static void alterJavaLibraryPath(String newJavaLibraryPath) throws IllegalAccessException,
			NoSuchFieldException
	{
		System.setProperty(JAVA_LIBRARY_PATH, newJavaLibraryPath);

		Class<?> classLoader = ClassLoader.class;
		Field fieldSysPaths = classLoader.getDeclaredField("sys_paths");
		if (null != fieldSysPaths)
		{
			fieldSysPaths.setAccessible(true);
			// Reset it to null so that whenever "System.loadLibrary" is called,
			// it will be reconstructed with the changed value.
			fieldSysPaths.set(classLoader, null);
		}
	}
}
