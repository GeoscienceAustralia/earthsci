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
package au.gov.ga.earthsci.application.parts.retrieve;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

import au.gov.ga.earthsci.common.ui.util.SWTUtil;
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
	private final Set<Object> updatingElements = new HashSet<Object>();

	private Color downloadBackgroundColor;

	@Inject
	public void init(Composite parent, MPart part)
	{
		Color listBackground = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		downloadBackgroundColor =
				SWTUtil.shouldDarken(listBackground) ? SWTUtil.darker(listBackground) : SWTUtil.lighter(listBackground);

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
		downloadBackgroundColor.dispose();
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
		column.getColumn().setWidth(150);
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
				String text = positionString(retrieval);

				Rectangle bounds = ((TableItem) event.item).getBounds(event.index);
				int width = (int) ((bounds.width - 1) * percentage);
				gc.setBackground(downloadBackgroundColor);
				gc.fillRectangle(event.x, event.y, width, event.height);

				Point size = event.gc.textExtent(text);
				int offset = Math.max(0, (event.height - size.y) / 2);
				gc.setForeground(event.display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
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

	private static String positionString(IRetrieval retrieval)
	{
		long position = retrieval.getPosition();
		long length = retrieval.getLength();
		float percentage = retrieval.getPercentage();
		StringBuilder sb = new StringBuilder();
		String unknown = "unknown";

		long max = Math.max(position, length);
		if (max >= 0)
		{
			int unitNumber = max == 0 ? 0 : (int) (Math.log10(max) / Math.log10(1024));

			String prefixes = "kMGTPEZY"; //$NON-NLS-1$
			if (unitNumber > prefixes.length())
			{
				unitNumber = 0;
			}
			String unit = (unitNumber <= 0 ? "" : prefixes.charAt(unitNumber - 1)) + "B"; //$NON-NLS-1$ //$NON-NLS-2$

			long divisor = (long) Math.pow(1024, unitNumber);
			position /= divisor;
			length /= divisor;

			sb.append(position);
			sb.append(' ');
			sb.append(unit);

			sb.append(' ');
			sb.append("of");
			sb.append(' ');

			if (length <= 0)
			{
				sb.append(unknown);
			}
			else
			{
				sb.append(length);
				sb.append(' ');
				sb.append(unit);
			}
		}
		else
		{
			sb.append(unknown);
		}

		if (percentage >= 0)
		{
			sb.append(" ("); //$NON-NLS-1$
			sb.append((int) (percentage * 100));
			sb.append("%)"); //$NON-NLS-1$
		}

		return sb.toString();
	}

	@Override
	public void retrievalAdded(final IRetrieval retrieval)
	{
		synchronized (retrievals)
		{
			retrievals.add(retrieval);
		}
		retrieval.addListener(retrievalListener);
		if (viewer != null && !viewer.getControl().isDisposed())
		{
			viewer.getControl().getDisplay().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					if (!viewer.getControl().isDisposed())
					{
						viewer.add(retrieval);
					}
				}
			});
		}
	}

	@Override
	public void retrievalRemoved(final IRetrieval retrieval)
	{
		synchronized (retrievals)
		{
			retrievals.remove(retrieval);
		}
		retrieval.removeListener(retrievalListener);
		if (viewer != null && !viewer.getControl().isDisposed())
		{
			viewer.getControl().getDisplay().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					if (!viewer.getControl().isDisposed())
					{
						viewer.remove(retrieval);
					}
				}
			});
		}
	}

	private void asyncUpdate(final Object element)
	{
		//don't queue multiple updates for each retrieval, so we don't flood the UI thread with asyncExec's
		if (updatingElements.contains(element))
		{
			return;
		}

		if (viewer != null && !viewer.getControl().isDisposed())
		{
			updatingElements.add(element);
			viewer.getControl().getDisplay().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					if (!viewer.getControl().isDisposed())
					{
						viewer.update(element, null);
					}
					updatingElements.remove(element);
				}
			});
		}
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
