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

import java.lang.reflect.Field;

/**
 * Utility class for disabling direct draw. This is required to run WorldWind
 * without the UI flickering. Currently this is done in the webstart JNLP file,
 * but this can be used when running a standalone instance of WorldWind.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DirectDraw
{
	public static void disableDirectDraw()
	{
		String noddraw = System.getProperty("sun.java2d.noddraw");
		if (noddraw == null || !noddraw.equalsIgnoreCase("true"))
		{
			System.setProperty("sun.java2d.noddraw", "true");

			Class<?> windowsFlags = null;
			try
			{
				windowsFlags = Class.forName("sun.awt.WindowsFlags");
			}
			catch (ClassNotFoundException e1)
			{
				try
				{
					windowsFlags = Class.forName("sun.java2d.windows.WindowsFlags");
				}
				catch (ClassNotFoundException e2)
				{
				}
			}
			if (windowsFlags != null)
			{
				try
				{
					Field field = windowsFlags.getDeclaredField("ddEnabled");
					field.setAccessible(true);
					field.set(null, false);
				}
				catch (Exception e)
				{
				}
			}
		}
	}
}
