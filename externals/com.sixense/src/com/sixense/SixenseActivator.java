/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package com.sixense;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activator that loads the native Sixense library.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SixenseActivator implements BundleActivator
{
	private static BundleContext context;
	private static boolean libraryLoaded = false;
	private static Logger logger = Logger.getLogger(SixenseActivator.class.getName());

	static BundleContext getContext()
	{
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception
	{
		SixenseActivator.context = bundleContext;

		try
		{
			Error error = null;
			try
			{
				System.loadLibrary("sixense"); //$NON-NLS-1$
				System.loadLibrary("sixense_utils"); //$NON-NLS-1$
			}
			catch (Error e)
			{
				error = e;
			}
			if (error != null)
			{
				//loading 32-bit didn't work, try 64-bit
				try
				{
					System.loadLibrary("sixense_x64"); //$NON-NLS-1$
					System.loadLibrary("sixense_utils_x64"); //$NON-NLS-1$
					error = null;
				}
				catch (Error e)
				{
				}
			}
			if (error != null)
			{
				throw error;
			}
			System.loadLibrary("SixenseJava"); //$NON-NLS-1$
		}
		catch (Throwable e)
		{
			logger.severe("Unable to load SixenseJava library: " + e.getLocalizedMessage()); //$NON-NLS-1$
			return;
		}
		libraryLoaded = true;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception
	{
		SixenseActivator.context = null;
	}

	public static String getBundleName()
	{
		return context.getBundle().getSymbolicName();
	}

	/**
	 * @return Is the Sixense native library loaded?
	 */
	public static boolean isLibraryLoaded()
	{
		return libraryLoaded;
	}
}