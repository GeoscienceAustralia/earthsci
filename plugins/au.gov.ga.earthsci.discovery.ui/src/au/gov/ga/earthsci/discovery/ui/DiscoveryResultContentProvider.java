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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import au.gov.ga.earthsci.discovery.DiscoveryIndexOutOfBoundsException;
import au.gov.ga.earthsci.discovery.DiscoveryResultNotFoundException;
import au.gov.ga.earthsci.discovery.IDiscovery;
import au.gov.ga.earthsci.discovery.IDiscoveryListener;
import au.gov.ga.earthsci.discovery.IDiscoveryResult;

/**
 * {@link IStructuredContentProvider} implementation that provides
 * {@link IDiscoveryResult}s to a JFace viewer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryResultContentProvider implements IStructuredContentProvider, IDiscoveryListener
{
	private Viewer viewer;
	private IDiscovery discovery;
	private int page = 0;

	/**
	 * @return The page of results of the {@link IDiscovery} that this content
	 *         provider is providing.
	 */
	public int getPage()
	{
		return page;
	}

	/**
	 * Set the page of results of the {@link IDiscovery} that this content
	 * provider should provide.
	 * 
	 * @param page
	 *            Page number of results to provide
	 */
	public void setPage(int page)
	{
		this.page = page;
		refreshViewer(viewer);
	}

	@Override
	public void dispose()
	{
		if (discovery != null)
		{
			discovery.removeListener(this);
		}
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		this.viewer = viewer;
		if (discovery == newInput)
		{
			//input hasn't changed
			return;
		}
		if (discovery != null)
		{
			discovery.removeListener(this);
		}
		if (newInput instanceof IDiscovery)
		{
			discovery = (IDiscovery) newInput;
			discovery.addListener(this);
		}
	}

	@Override
	public Object[] getElements(Object inputElement)
	{
		if (!(inputElement instanceof IDiscovery))
		{
			return null;
		}
		List<IDiscoveryResult> results = new ArrayList<IDiscoveryResult>();
		IDiscovery discovery = (IDiscovery) inputElement;
		int pageSize = discovery.getPageSize();
		int resultCount = discovery.getResultCount();
		if (resultCount == IDiscovery.UNKNOWN)
		{
			resultCount = Integer.MAX_VALUE;
		}
		if (pageSize == 0)
		{
			for (int i = 0; i < resultCount; i++)
			{
				try
				{
					IDiscoveryResult result = discovery.getResult(i);
					if (result == null)
					{
						//assume, if we hit a loading result in unpaged results, there will be no records after this one yet
						break;
					}
					results.add(result);
				}
				catch (DiscoveryResultNotFoundException e)
				{
					//ignore for unpaged results
				}
				catch (DiscoveryIndexOutOfBoundsException e)
				{
					break;
				}
			}
		}
		else
		{
			int firstIndex = page * pageSize;
			for (int i = 0; i < pageSize; i++)
			{
				int index = firstIndex + i;
				if (index >= resultCount)
				{
					break;
				}
				try
				{
					//add null results, and display them as loading
					IDiscoveryResult result = discovery.getResult(index);
					if (result != null)
					{
						results.add(result);
					}
					else
					{
						//TODO add loading node
					}
				}
				catch (DiscoveryResultNotFoundException e)
				{
					//TODO for now ignore, is this correct?
				}
				catch (DiscoveryIndexOutOfBoundsException e)
				{
					break;
				}
			}
		}

		return results.toArray();
	}

	@Override
	public void resultCountChanged(IDiscovery discovery)
	{
	}

	@Override
	public void resultAdded(IDiscovery discovery, IDiscoveryResult result)
	{
		int pageSize = discovery.getPageSize();
		boolean refresh = pageSize == 0;
		if (pageSize != 0)
		{
			int startIndex = pageSize * page;
			int endIndex = startIndex + pageSize - 1;
			int index = result.getIndex();
			refresh = startIndex <= index && index <= endIndex;
		}
		if (refresh)
		{
			refreshViewer(viewer);
		}
	}

	private static void refreshViewer(final Viewer viewer)
	{
		if (viewer != null && !viewer.getControl().isDisposed())
		{
			viewer.getControl().getDisplay().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					if (!viewer.getControl().isDisposed())
					{
						viewer.refresh();
					}
				}
			});
		}
	}
}
