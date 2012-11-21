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
package au.gov.ga.earthsci.core.context;

import au.gov.ga.earthsci.core.model.catalog.CatalogPersister;
import au.gov.ga.earthsci.core.model.catalog.ICatalogModel;
import au.gov.ga.earthsci.core.worldwind.WorldWindModel;
import au.gov.ga.earthsci.core.worldwind.WorldWindView;

/**
 * The default immutable implementation of the {@link IPlatformContext}
 * interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class PlatformContext implements IPlatformContext
{
	private boolean isStarted = false;
	private boolean isShutdown = false;

	private final ICatalogModel catalogModel;
	private final WorldWindModel wwModel;
	private final WorldWindView wwView;

	public PlatformContext(ICatalogModel catalogModel, WorldWindModel wwModel, WorldWindView wwView)
	{
		this.catalogModel = catalogModel;
		this.wwModel = wwModel;
		this.wwView = wwView;
	}

	@Override
	public ICatalogModel getCatalogModel()
	{
		return catalogModel;
	}

	@Override
	public WorldWindModel getWorldWindModel()
	{
		return wwModel;
	}

	@Override
	public WorldWindView getWorldWindView()
	{
		return wwView;
	}

	@Override
	public synchronized void startup()
	{
		if (isStarted)
		{
			return;
		}

		wwModel.loadLayers();
	}

	@Override
	public synchronized void shutdown()
	{
		if (isShutdown)
		{
			return;
		}

		CatalogPersister.saveToWorkspace(catalogModel);
		wwModel.saveLayers();
	}
}
