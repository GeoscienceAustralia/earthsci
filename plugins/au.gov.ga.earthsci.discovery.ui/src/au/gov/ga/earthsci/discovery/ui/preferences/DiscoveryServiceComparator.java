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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import au.gov.ga.earthsci.discovery.IDiscoveryService;

/**
 * {@link ViewerComparator} used for sorting {@link IDiscoveryService} elements
 * in a {@link Viewer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryServiceComparator extends ViewerComparator
{
	private boolean descending = false;
	private DiscoveryServiceViewerColumn column = DiscoveryServiceViewerColumn.NAME;

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		if (!(e1 instanceof IDiscoveryService && e2 instanceof IDiscoveryService))
		{
			return super.compare(viewer, e1, e2);
		}

		IDiscoveryService d1 = (IDiscoveryService) e1;
		IDiscoveryService d2 = (IDiscoveryService) e2;
		return column.compare(d1, d2, descending);
	}

	public boolean isDescending()
	{
		return descending;
	}

	public void setDescending(boolean ascending)
	{
		this.descending = ascending;
	}

	public DiscoveryServiceViewerColumn getColumn()
	{
		return column;
	}

	public void setColumn(DiscoveryServiceViewerColumn column)
	{
		this.column = column;
	}
}
