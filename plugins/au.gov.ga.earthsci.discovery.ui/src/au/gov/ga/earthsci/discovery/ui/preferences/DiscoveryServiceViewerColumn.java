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

import au.gov.ga.earthsci.discovery.IDiscoveryProvider;
import au.gov.ga.earthsci.discovery.IDiscoveryService;

/**
 * Enumeration of columns to use for a {@link Viewer} displaying a list of
 * {@link IDiscoveryService}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public enum DiscoveryServiceViewerColumn
{
	/**
	 * {@link IDiscoveryService#getName()}
	 */
	NAME("Name")
	{
		@Override
		public String getText(IDiscoveryService discoveryService)
		{
			return discoveryService.getName();
		}

		@Override
		public int compare(IDiscoveryService d1, IDiscoveryService d2, boolean descending)
		{
			return compare(d1, d2, descending, this, TYPE, URL);
		}
	},

	/**
	 * {@link IDiscoveryProvider#getName()}
	 */
	TYPE("Type")
	{
		@Override
		public String getText(IDiscoveryService discoveryService)
		{
			return discoveryService.getProvider().getName();
		}

		@Override
		public int compare(IDiscoveryService d1, IDiscoveryService d2, boolean descending)
		{
			return compare(d1, d2, descending, this, NAME, URL);
		}
	},

	/**
	 * {@link IDiscoveryService#getServiceURL()}
	 */
	URL("Location")
	{
		@Override
		public String getText(IDiscoveryService discoveryService)
		{
			return discoveryService.getServiceURL().toString();
		}

		@Override
		public int compare(IDiscoveryService d1, IDiscoveryService d2, boolean descending)
		{
			return compare(d1, d2, descending, this, NAME, TYPE);
		}
	},

	/**
	 * {@link IDiscoveryService#isEnabled()}
	 */
	ENABLED("Enabled")
	{
		@Override
		public String getText(IDiscoveryService discoveryService)
		{
			return discoveryService.isEnabled() ? "Enabled" : "Disabled";
		}

		@Override
		public int compare(IDiscoveryService d1, IDiscoveryService d2, boolean descending)
		{
			return compare(d1, d2, descending, this, NAME, TYPE, URL);
		}

		@Override
		protected int compare(IDiscoveryService d1, IDiscoveryService d2)
		{
			//"Disabled" should come after "Enabled", so reverse:
			return -super.compare(d1, d2);
		}
	};

	private final String label;

	private DiscoveryServiceViewerColumn(String label)
	{
		this.label = label;
	}

	/**
	 * @return Label to use for the column header.
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * Calculate the text to use for a viewer cell in this column for the given
	 * {@link IDiscoveryService}.
	 * 
	 * @param discoveryService
	 * @return Text to display in this column for the given service
	 */
	public abstract String getText(IDiscoveryService discoveryService);

	/**
	 * Compare the two services. Does a case-insensitive comparison on the two
	 * strings returned by {@link #getText(IDiscoveryService)} for both
	 * services.
	 * 
	 * @param d1
	 * @param d2
	 * @return Text for d1 compared with text for d2
	 */
	protected int compare(IDiscoveryService d1, IDiscoveryService d2)
	{
		String t1 = getText(d1);
		String t2 = getText(d2);
		if (t1 == null)
		{
			return 1;
		}
		if (t2 == null)
		{
			return -1;
		}
		return t1.compareToIgnoreCase(t2);
	}

	/**
	 * Compare the two services. Does a case-insensitive comparison on the two
	 * strings returned by {@link #getText(IDiscoveryService)} for both
	 * services. If the values for this column are equal, the next column is
	 * used for comparison.
	 * 
	 * @param d1
	 * @param d2
	 * @param descending
	 *            Should the values be sorted in descending order?
	 * @return Text for d1 compared with text for d2
	 */
	public abstract int compare(IDiscoveryService d1, IDiscoveryService d2, boolean descending);

	/**
	 * Compare the two services using the given list of columns. The first
	 * non-zero comparison result for the columns is returned.
	 * 
	 * @param d1
	 * @param d2
	 * @param reverseFirstColumn
	 *            Should the first column's comparison result be reversed? (can
	 *            be used for sorting the first column in descending order)
	 * @param columns
	 *            Columns to use to comparison, in order
	 * @return Comparison result between d1 and d2
	 */
	public static int compare(IDiscoveryService d1, IDiscoveryService d2, boolean reverseFirstColumn,
			DiscoveryServiceViewerColumn... columns)
	{
		boolean first = true;
		for (DiscoveryServiceViewerColumn column : columns)
		{
			int result = column.compare(d1, d2);
			if (result != 0)
			{
				return first && reverseFirstColumn ? -result : result;
			}
			first = false;
		}
		return 0;
	}
}
