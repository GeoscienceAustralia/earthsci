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
package au.gov.ga.earthsci.core.model.catalog;

/**
 * A threadsafe factory class used to load the {@link ICatalogModel}
 * 
 * @author James Navin (jame.navin@ga.gov.au)
 */
public class CatalogModelLoader
{

	private CatalogModelLoader() {}
	
	private static ICatalogModel loadedModel;
	
	/**
	 * Load the persisted catalog model, if one exists, or initialise
	 * a new instance.
	 * 
	 * @return The catalog model to use
	 */
	public synchronized static ICatalogModel loadCatalogModel()
	{
		if (loadedModel != null)
		{
			return loadedModel;
		}
		
		// TODO: Load from persisted model
		CatalogModel model = new CatalogModel();
		loadedModel = model;
		return model;
	}
	
}
