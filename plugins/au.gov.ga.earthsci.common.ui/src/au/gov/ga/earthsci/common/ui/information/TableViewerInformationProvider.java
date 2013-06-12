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
package au.gov.ga.earthsci.common.ui.information;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableItem;

/**
 * An {@link IInformationProvider} that provides information about the
 * {@link TableItem}s of a {@link TableViewer}.
 */
public class TableViewerInformationProvider implements IInformationProvider
{
	private final TableViewer viewer;

	/**
	 * Creates a new information provider for the given table viewer.
	 * 
	 * @param viewer
	 *            the table viewer
	 */
	public TableViewerInformationProvider(TableViewer viewer)
	{
		this.viewer = viewer;
	}

	@Override
	public Object getInformation(Point location)
	{
		Item item = viewer.getTable().getItem(location);
		if (item != null)
		{
			return item.getData();
		}
		return null;
	}

	@Override
	public Rectangle getArea(Point location)
	{
		TableItem item = viewer.getTable().getItem(location);
		if (item != null)
		{
			return item.getBounds();
		}
		return null;
	}
}
