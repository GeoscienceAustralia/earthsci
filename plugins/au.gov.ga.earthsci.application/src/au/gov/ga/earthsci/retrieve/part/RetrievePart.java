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
package au.gov.ga.earthsci.retrieve.part;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

import au.gov.ga.earthsci.core.retrieve.IRetrieval;
import au.gov.ga.earthsci.core.retrieve.IRetrievalListener;
import au.gov.ga.earthsci.core.retrieve.IRetrievalService;
import au.gov.ga.earthsci.core.retrieve.IRetrievalServiceListener;
import au.gov.ga.earthsci.core.retrieve.RetrievalAdapter;

/**
 * Part that displays progress of {@link IRetrieval}s running in the
 * {@link IRetrievalService}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RetrievePart implements IRetrievalServiceListener
{
	@Inject
	private IRetrievalService retrievalService;

	private TableViewer viewer;

	private final List<IRetrieval> retrievals = new ArrayList<IRetrieval>();

	@Inject
	public void init(Composite parent, MPart part)
	{
		retrievalService.addListener(this);

		viewer = new TableViewer(parent, SWT.V_SCROLL);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(retrievals);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		/*viewer.setSorter(new ViewerSorter()
		{
			@Override
			public int category(Object element)
			{
				return ((JobInfo) element).job.getState();
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2)
			{
				return ((JobInfo) e1).job.getPriority() - ((JobInfo) e2).job.getPriority();
			}
		});
		viewer.addFilter(new ViewerFilter()
		{
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element)
			{
				return ((JobInfo) element).job.getState() != Job.SLEEPING;
			}

		});*/

		createColumns();
	}

	@PreDestroy
	public void packup()
	{
		retrievalService.removeListener(this);
	}

	private void createColumns()
	{
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("URL");
		column.getColumn().setWidth(300);
		column.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				IRetrieval retrieval = (IRetrieval) element;
				return retrieval.getURL().toString();
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("Caller(s)");
		column.getColumn().setWidth(200);
		column.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				IRetrieval retrieval = (IRetrieval) element;
				StringBuilder sb = new StringBuilder();
				for (Object caller : retrieval.getCallers())
				{
					sb.append(", " + caller.toString()); //$NON-NLS-1$
				}
				if (sb.length() == 0)
				{
					return sb.toString();
				}
				return sb.substring(2);
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText("Progress");
		column.getColumn().setWidth(100);
		column.setLabelProvider(new OwnerDrawLabelProvider()
		{
			@Override
			protected void paint(Event event, Object element)
			{
				GC gc = event.gc;
				Color foreground = gc.getForeground();
				Color background = gc.getBackground();

				IRetrieval retrieval = (IRetrieval) element;
				float percentage = Math.max(0, retrieval.getPercentage());
				String text = (int) (percentage * 100) + "%"; //$NON-NLS-1$

				Rectangle bounds = ((TableItem) event.item).getBounds(event.index);
				int width = (int) ((bounds.width - 1) * percentage);
				gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION));
				gc.fillRectangle(event.x, event.y, width, event.height);

				Point size = event.gc.textExtent(text);
				int offset = Math.max(0, (event.height - size.y) / 2);
				gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
				gc.drawText(text, event.x + 2, event.y + offset, true);

				gc.setForeground(background);
				gc.setBackground(foreground);
			}

			@Override
			protected void measure(Event event, Object element)
			{
			}
		});
	}

	@Override
	public void retrievalAdded(IRetrieval retrieval)
	{
		synchronized (retrievals)
		{
			retrievals.add(retrieval);
			retrieval.addListener(retrievalListener);
			asyncRefresh();
		}
	}

	@Override
	public void retrievalRemoved(IRetrieval retrieval)
	{
		synchronized (retrievals)
		{
			retrievals.remove(retrieval);
			retrieval.removeListener(retrievalListener);
			asyncRefresh();
		}
	}

	private void asyncRefresh()
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (!viewer.getTable().isDisposed())
				{
					viewer.refresh();
				}
			}
		});
	}

	private void asyncUpdate(final Object element)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (!viewer.getTable().isDisposed())
				{
					viewer.update(element, null);
				}
			}
		});
	}

	private IRetrievalListener retrievalListener = new RetrievalAdapter()
	{
		@Override
		public void progress(IRetrieval retrieval)
		{
			asyncUpdate(retrieval);
		}

		@Override
		public void callersChanged(IRetrieval retrieval)
		{
			asyncUpdate(retrieval);
		}
	};
}
