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

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;

import javax.swing.JOptionPane;

/**
 * Utility class for opening a file or url with the operating system's default
 * program or browser.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DefaultLauncher
{
	/**
	 * Open the given file with the default program.
	 * 
	 * @param file
	 *            File to open
	 */
	public static void openFile(final File file)
	{
		boolean desktopSupported = false;
		/*try
		{
			Class<?> desktopClass = getDesktopClassIfSupported();
			Object desktopInstance = getDesktopInstance(desktopClass);
			if (desktopClass != null && desktopInstance != null)
			{
				Method openMethod = desktopClass.getMethod("open", new Class<?>[] { File.class });
				openMethod.invoke(desktopInstance, new Object[] { file });
				desktopSupported = true;
			}
		}
		catch (Exception cnfe)
		{
		}*/

		if (!desktopSupported)
		{
			Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					String osName = System.getProperty("os.name").toLowerCase();
					try
					{
						if (osName.startsWith("windows"))
						{
							Runtime.getRuntime().exec("cmd.exe /C " + file.getAbsolutePath());
						}
						else if (osName.startsWith("mac"))
						{
							macFileManagerOpen(file.toURI().toURL());
						}
						else
						{
							linuxBrowserOpen(file.toURI().toURL());
						}
					}
					catch (Exception e)
					{
						JOptionPane.showMessageDialog(null,
								"Error attempting to open file" + ":\n" + e.getLocalizedMessage());
					}
				}
			};
			thread.setDaemon(true);
			thread.start();
		}
	}

	/**
	 * Open the given url in the user's default browser.
	 * 
	 * @param url
	 */
	public static void openURL(final URL url)
	{
		/*if ("file".equalsIgnoreCase(url.getProtocol()))
		{
			try
			{
				File file = new File(url.toURI());
				System.out.println(file.getAbsolutePath());
				openFile(file);
				return;
			}
			catch (URISyntaxException e)
			{
			}
		}*/

		boolean desktopSupported = false;
		/*try
		{
			Class<?> desktopClass = getDesktopClassIfSupported();
			Object desktopInstance = getDesktopInstance(desktopClass);
			if (desktopClass != null && desktopInstance != null)
			{
				String methodName = "browse";
				if (url.toExternalForm().toLowerCase().startsWith("mailto:"))
					methodName = "mail";
				Method method = desktopClass.getMethod(methodName, new Class<?>[] { URI.class });
				URI uri = url.toURI();
				method.invoke(desktopInstance, new Object[] { uri });
				desktopSupported = true;
			}
		}
		catch (Exception cnfe)
		{
		}*/

		if (!desktopSupported)
		{
			Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					String osName = System.getProperty("os.name").toLowerCase();
					try
					{
						if (osName.startsWith("windows"))
						{
							Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url.toExternalForm());
						}
						else if (osName.startsWith("mac"))
						{
							macFileManagerOpen(url);
						}
						else
						{
							linuxBrowserOpen(url);
						}
					}
					catch (Exception e)
					{
						JOptionPane.showMessageDialog(null,
								"Error attempting to launch web browser" + ":\n" + e.getLocalizedMessage());
					}
				}
			};
			thread.setDaemon(true);
			thread.start();
		}
	}

	protected static Class<?> getDesktopClassIfSupported()
	{
		try
		{
			Class<?> desktopClass = Class.forName("java.awt.Desktop");
			Method isDesktopSupportedMethod = desktopClass.getMethod("isDesktopSupported", new Class<?>[] {});
			Object isSupported = isDesktopSupportedMethod.invoke(null, new Object[] {});
			if (isSupported instanceof Boolean && ((Boolean) isSupported).booleanValue() == true)
				return desktopClass;
		}
		catch (Exception e)
		{
		}
		return null;
	}

	protected static Object getDesktopInstance(Class<?> desktopClass)
	{
		if (desktopClass != null)
		{
			try
			{
				Method getDesktopMethod = desktopClass.getMethod("getDesktop", new Class<?>[] {});
				Object desktopInstance = getDesktopMethod.invoke(null, new Object[] {});
				return desktopInstance;
			}
			catch (Exception e)
			{
			}
		}
		return null;
	}

	private static void macFileManagerOpen(URL url) throws Exception
	{
		Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
		Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
		openURL.invoke(null, new Object[] { url.toExternalForm() });
	}

	private static void linuxBrowserOpen(URL url) throws Exception
	{
		String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
		String browser = null;
		for (int count = 0; count < browsers.length && browser == null; count++)
			if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0)
				browser = browsers[count];
		if (browser == null)
			throw new Exception("Could not find web browser");
		else
		{
			Process process = Runtime.getRuntime().exec(new String[] { browser, url.toExternalForm() });
			new InputStreamGobbler(process.getInputStream());
			new InputStreamGobbler(process.getErrorStream());
		}
	}
}
