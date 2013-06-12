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
package au.gov.ga.earthsci.discovery;

import java.util.ArrayList;

/**
 * Helper class for creating a list of {@link IDiscoveryListener}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryListenerList extends ArrayList<IDiscoveryListener> implements IDiscoveryListener
{
	@Override
	public void resultCountChanged(IDiscovery discovery)
	{
		for (int i = size() - 1; i >= 0; i--)
		{
			get(i).resultCountChanged(discovery);
		}
	}

	@Override
	public void resultAdded(IDiscovery discovery, IDiscoveryResult result)
	{
		for (int i = size() - 1; i >= 0; i--)
		{
			get(i).resultAdded(discovery, result);
		}
	}
}
