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

import au.gov.ga.earthsci.core.model.catalog.ICatalogModel;
import au.gov.ga.earthsci.core.worldwind.WorldWindModel;

/**
 * A typesafe context for accessing the core components of the Earthsci platform
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IPlatformContext
{
	/**
	 * Invoke to perform required startup processing.
	 * <p/>
	 * This method is idempotent - multiple calls should not result in the same processing occurring multiple times in one session.
	 * <p/>
	 * Normally this method should only be called from the plugin activator in response to a bundle lifecyle event.
	 */
	void startup();
	
	/**
	 * Invoke to perform required shutdown processing.
	 * <p/>
	 * This method is idempotent - multiple calls should not result in the same processing occurring multiple times in one session.
	 * <p/>
	 * Normally this method should only be called from the plugin activator in response to a bundle lifecyle event.
	 */
	void shutdown();
	
	/**
	 * Return the current catalog model
	 * 
	 * @return the current catalog model, or <code>null</code> if it hasn't been initialised yet
	 */
	ICatalogModel getCatalogModel();
	
	/**
	 * Return the current worldwind model
	 * 
	 * @return The current worldwind model
	 */
	WorldWindModel getWorldWindModel();
}
