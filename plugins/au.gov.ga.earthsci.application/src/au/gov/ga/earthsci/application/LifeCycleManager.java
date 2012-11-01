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

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;

import au.gov.ga.earthsci.core.retrieve.IRetrievalService;
import au.gov.ga.earthsci.core.retrieve.RetrievalService;
import au.gov.ga.earthsci.core.worldwind.TreeModel;
import au.gov.ga.earthsci.core.worldwind.WorldWindModel;
import au.gov.ga.earthsci.notification.NotificationManager;

/**
 * Registered as the product application 'lifeCycleURI' class, which gets called
 * by the injector at different points in the application lifecycle.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LifeCycleManager
{
	@Inject
	private IEclipseContext context;

	@Inject
	private ProxyConfigurator proxyConfigurator;

	@Inject
	private WorldWindConfigurator worldWindConfigurator;

	@Inject
	private NotificationManager notificationManager;

	@Inject
	private RetrievalService retrievalService;
	
	@PostContextCreate
	void postContextCreate()
	{
		context.set(TreeModel.class, new WorldWindModel());
		context.set(IRetrievalService.class, retrievalService);
	}

	@PreSave
	void preSave()
	{
		WorldWindModel model = (WorldWindModel) context.get(TreeModel.class);
		model.saveLayers();
	}
}
