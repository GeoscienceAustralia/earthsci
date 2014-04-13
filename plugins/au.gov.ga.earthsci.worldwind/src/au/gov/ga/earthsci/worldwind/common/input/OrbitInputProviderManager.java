/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.input;

import gov.nasa.worldwind.view.orbit.OrbitView;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a list of {@link IOrbitInputProvider}s that provide input to the
 * {@link IProviderOrbitViewInputHandler} associated with {@link OrbitView}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OrbitInputProviderManager
{
	private final static OrbitInputProviderManager INSTANCE = new OrbitInputProviderManager();

	public static OrbitInputProviderManager getInstance()
	{
		return INSTANCE;
	}

	private final List<IOrbitInputProvider> providers = new ArrayList<IOrbitInputProvider>();

	/**
	 * Add an input provider.
	 * 
	 * @param provider
	 */
	public void addProvider(IOrbitInputProvider provider)
	{
		providers.add(provider);
	}

	/**
	 * Remove an input provider.
	 * 
	 * @param provider
	 */
	public void removeProvider(IOrbitInputProvider provider)
	{
		providers.remove(provider);
	}

	/**
	 * Apply the input providers to the given input handler. Should be called by
	 * the {@link IProviderOrbitViewInputHandler} implementation.
	 * 
	 * @param inputHandler
	 */
	public void apply(IProviderOrbitViewInputHandler inputHandler)
	{
		for (int i = providers.size() - 1; i >= 0; i--)
		{
			providers.get(i).apply(inputHandler);
		}
	}
}
