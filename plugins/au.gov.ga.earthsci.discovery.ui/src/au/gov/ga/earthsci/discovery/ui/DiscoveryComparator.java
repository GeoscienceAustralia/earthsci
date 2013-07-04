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
package au.gov.ga.earthsci.discovery.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.discovery.IDiscovery;
import au.gov.ga.earthsci.discovery.IDiscoveryService;

/**
 * {@link ViewerComparator} used to sort {@link IDiscovery}s in the discovery
 * part.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryComparator extends ViewerComparator
{
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (e1 instanceof IDiscovery && e2 instanceof IDiscovery)
		{
			IDiscovery d1 = (IDiscovery) e1;
			IDiscovery d2 = (IDiscovery) e2;
			IDiscoveryService s1 = d1.getService();
			IDiscoveryService s2 = d2.getService();
			boolean empty1 = Util.isEmpty(s1.getName());
			boolean empty2 = Util.isEmpty(s2.getName());
			if (empty1 && empty2)
			{
				if (s1.getServiceURL() != null && s2.getServiceURL() != null)
				{
					return s1.getServiceURL().toString().compareToIgnoreCase(s2.getServiceURL().toString());
				}
			}
			if (empty1)
			{
				return 1;
			}
			if (empty2)
			{
				return -1;
			}
			return s1.getName().compareToIgnoreCase(s2.getName());
		}
		return super.compare(viewer, e1, e2);
	}
}
