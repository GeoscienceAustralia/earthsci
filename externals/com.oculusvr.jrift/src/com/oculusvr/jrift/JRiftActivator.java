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
package com.oculusvr.jrift;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activator that loads the native JRift library.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class JRiftActivator implements BundleActivator
{
	private static BundleContext context;
	private static boolean libraryLoaded = false;
	private static Logger logger = Logger.getLogger(JRiftActivator.class.getName());

	static BundleContext getContext()
	{
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception
	{
		JRiftActivator.context = bundleContext;

		try
		{
			System.loadLibrary("JRiftLibrary"); //$NON-NLS-1$
			libraryLoaded = true;
		}
		catch (Throwable e)
		{
			logger.severe("Unable to load JRiftLibrary: " + e.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception
	{
		JRiftActivator.context = null;
	}

	public static String getBundleName()
	{
		return context.getBundle().getSymbolicName();
	}

	/**
	 * @return Is the JRift native library loaded?
	 */
	public static boolean isLibraryLoaded()
	{
		return libraryLoaded;
	}
}