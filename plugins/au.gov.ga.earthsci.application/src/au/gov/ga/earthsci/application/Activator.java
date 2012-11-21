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

import org.eclipse.e4.core.di.InjectorFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import au.gov.ga.earthsci.application.util.KeyboardFocusManagerFix;
import au.gov.ga.earthsci.catalog.part.CatalogBrowserController;
import au.gov.ga.earthsci.catalog.part.ICatalogBrowserController;
import au.gov.ga.earthsci.catalog.part.preferences.CatalogBrowserPreferences;
import au.gov.ga.earthsci.catalog.part.preferences.ICatalogBrowserPreferences;

/**
 * Application bundle activator.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Activator implements BundleActivator
{
	private static BundleContext context;

	static BundleContext getContext()
	{
		return context;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception
	{
		Activator.context = bundleContext;
		KeyboardFocusManagerFix.initialize();
		InjectorFactory.getDefault().addBinding(ICatalogBrowserController.class).implementedBy(CatalogBrowserController.class);
		InjectorFactory.getDefault().addBinding(ICatalogBrowserPreferences.class).implementedBy(CatalogBrowserPreferences.class);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception
	{
		Activator.context = null;
	}
	
	public static String getBundleName()
	{
		return context.getBundle().getSymbolicName();
	}
}
