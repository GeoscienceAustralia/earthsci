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
package au.gov.ga.earthsci.core;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.core.model.catalog.CatalogFactory;
import au.gov.ga.earthsci.core.model.layer.LayerFactory;
import au.gov.ga.earthsci.core.proxy.ProxyPreferences;
import au.gov.ga.earthsci.core.worldwind.WorldWindModel;
import au.gov.ga.earthsci.notification.NotificationManager;
import au.gov.ga.earthsci.worldwind.common.retrieve.ExtendedRetrievalService;

/**
 * Plugin's activator.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Activator implements BundleActivator
{
	private static BundleContext bundleContext;

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(Activator.class);
	
	@Override
	public void start(final BundleContext context) throws Exception
	{
		bundleContext = context;

		ProxyPreferences.preConfigureProxy();

		Configuration.setValue(AVKey.LAYER_FACTORY, LayerFactory.class.getName());
		Configuration.setValue(AVKey.MODEL_CLASS_NAME, WorldWindModel.class.getName());
		Configuration.setValue(AVKey.RETRIEVAL_SERVICE_CLASS_NAME, ExtendedRetrievalService.class.getName());

		loadExtensions(context);

		context.registerService(NotificationManager.class, NotificationManager.get(), null);
	}

	@Override
	public void stop(BundleContext context) throws Exception
	{
		bundleContext = null;
	}

	static BundleContext getContext()
	{
		return bundleContext;
	}

	public static String getBundleName()
	{
		return bundleContext.getBundle().getSymbolicName();
	}

	private void loadExtensions(final BundleContext context)
	{
		IExtensionRegistry registry = RegistryFactory.getRegistry();

		CatalogFactory.loadProvidersFromRegistry(registry);
		NotificationManager.loadReceivers(registry, context);
	}
}
