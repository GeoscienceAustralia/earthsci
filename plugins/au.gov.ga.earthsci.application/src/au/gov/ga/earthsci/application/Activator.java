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
package au.gov.ga.earthsci.application;

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import au.gov.ga.earthsci.common.ui.util.KeyboardFocusManagerFix;

/**
 * Application bundle activator.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Activator extends AbstractUIPlugin
{
	private static BundleContext context;

	// The plug-in ID
	public static final String PLUGIN_ID = "au.gov.ga.earthsci.application"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	static BundleContext getContext()
	{
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception
	{
		super.start(bundleContext);
		plugin = this;
		Activator.context = bundleContext;

		//bugfix:
		KeyboardFocusManagerFix.initialize();

		//create the preference initializers
		DefaultScope.INSTANCE.getNode(getBundleName());
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception
	{
		super.stop(bundleContext);
		plugin = null;
		Activator.context = null;
	}

	public static String getBundleName()
	{
		return context.getBundle().getSymbolicName();
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault()
	{
		return plugin;
	}
}
