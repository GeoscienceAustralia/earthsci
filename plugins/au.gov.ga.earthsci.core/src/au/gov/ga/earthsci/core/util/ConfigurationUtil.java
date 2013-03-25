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
package au.gov.ga.earthsci.core.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;

/**
 * Utility class used to calculation configuration file locations.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ConfigurationUtil
{
	/**
	 * Create a URL pointing to a file in the application's workspace storage
	 * area.
	 * 
	 * @param subpath
	 *            Path within the workspace area
	 * @return URL for subpath in the workspace
	 */
	public static URL getWorkspaceURL(String subpath)
	{
		try
		{
			URL url = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().toURI().toURL();
			return new URL(url, subpath);
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Create a File object pointing to a subpath in the application's workspace
	 * storage area.
	 * 
	 * @param subpath
	 *            Path within the workspace area
	 * @return File for subpath in the workspace
	 */
	public static File getWorkspaceFile(String subpath)
	{
		try
		{
			return new File(getWorkspaceURL(subpath).toURI());
		}
		catch (URISyntaxException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Create a URL pointing to a file in the user's home area (by default the
	 * System property 'user.home').
	 * 
	 * @param subpath
	 *            Path within the user directory
	 * @return URL for subpath in the user directory
	 */
	public static URL getUserURL(String subpath)
	{
		try
		{
			return new URL(Platform.getUserLocation().getDataArea(subpath).toString().replace(" ", "%20")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Create a File object pointing to a subpath in the user's home area (by
	 * default the System property 'user.home').
	 * 
	 * @param subpath
	 *            Path within the user directory
	 * @return File for subpath in the user directory
	 */
	public static File getUserFile(String subpath)
	{
		try
		{
			return new File(getUserURL(subpath).toURI());
		}
		catch (URISyntaxException e)
		{
			throw new IllegalArgumentException(e);
		}
	}
}
