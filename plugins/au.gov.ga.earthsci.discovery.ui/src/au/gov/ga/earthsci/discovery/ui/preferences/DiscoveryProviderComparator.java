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
package au.gov.ga.earthsci.discovery.ui.preferences;

import java.util.Comparator;

import au.gov.ga.earthsci.discovery.IDiscoveryProvider;

/**
 * {@link Comparator} implementation for {@link IDiscoveryProvider}s, using
 * case-insensitive sorting by name.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryProviderComparator implements Comparator<IDiscoveryProvider>
{
	@Override
	public int compare(IDiscoveryProvider d1, IDiscoveryProvider d2)
	{
		if (d1.getName() == d2.getName()) //handles double null case
		{
			return 0;
		}
		if (d1.getName() == null)
		{
			return 1;
		}
		if (d2.getName() == null)
		{
			return -1;
		}
		return d1.getName().compareToIgnoreCase(d2.getName());
	}
}
