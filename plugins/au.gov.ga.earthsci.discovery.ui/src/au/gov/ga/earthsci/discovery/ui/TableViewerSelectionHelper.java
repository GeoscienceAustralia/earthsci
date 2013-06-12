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

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;

/**
 * Class that provides listeners for a {@link ColumnViewer} that notify an
 * abstract method when an item in the viewer's control has been selected.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class TableViewerSelectionHelper<T>
{
	public TableViewerSelectionHelper(final TableViewer viewer, final Class<T> selectionType)
	{
		viewer.getControl().addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDown(MouseEvent e)
			{
				ViewerCell cell = viewer.getCell(new Point(e.x, e.y));
				if (cell == null)
				{
					viewer.setSelection(StructuredSelection.EMPTY);
				}
			}
		});

		viewer.getTable().addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				Object s = selection.getFirstElement();
				if (s == null)
				{
					itemSelected(null);
				}
				else if (selectionType.isInstance(s))
				{
					T t = selectionType.cast(s);
					itemSelected(t);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				Object s = selection.getFirstElement();
				if (selectionType.isInstance(s))
				{
					T t = selectionType.cast(s);
					itemDefaultSelected(t);
				}
			}
		});
	}

	/**
	 * Called when the given item is selected in the viewer.
	 * 
	 * @param selection
	 *            Selected item
	 */
	protected abstract void itemSelected(T selection);

	/**
	 * Called when the given item is default selected in the viewer. This is a
	 * strong selection, such as a double-click by the user's mouse or the
	 * RETURN/ENTER key pressed.
	 * 
	 * @param selection
	 *            Selected item
	 */
	protected abstract void itemDefaultSelected(T selection);
}
