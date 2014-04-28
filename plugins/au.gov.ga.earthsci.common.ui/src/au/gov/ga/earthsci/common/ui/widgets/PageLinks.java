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
package au.gov.ga.earthsci.common.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;

/**
 * Widget that displays a number of numeric links which represent pages, with
 * previous and next buttons on either side. Can be used to support changing
 * pages on a paged result view.
 * <p/>
 * The number of page links shown depends on the page count and the available
 * width.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PageLinks extends Composite
{
	public final static int UNKNOWN_PAGE_COUNT = -1;

	private int pageCount = UNKNOWN_PAGE_COUNT;
	private int selectedPage = 0;
	private final List<PageListener> listeners = new ArrayList<PageListener>();
	private Control selectedPageControl;

	public PageLinks(Composite parent, int style)
	{
		super(parent, style);

		PageLinksLayout layout = new PageLinksLayout(SWT.HORIZONTAL);
		layout.pack = true;
		layout.justify = true;
		layout.spacing = 5;
		layout.wrap = false;
		layout.center = true;
		layout.fill = false;
		layout.marginBottom = layout.marginLeft = layout.marginRight = layout.marginTop = 0;
		setLayout(layout);

		addControlListener(new ControlAdapter()
		{
			@Override
			public void controlResized(ControlEvent e)
			{
				setupLinks();
			}
		});
		addPaintListener(new PaintListener()
		{
			@Override
			public void paintControl(PaintEvent e)
			{
				if (selectedPageControl == null || selectedPageControl.isDisposed())
				{
					return;
				}

				GC gc = e.gc;
				Color color = e.display.getSystemColor(SWT.COLOR_WIDGET_BORDER);
				gc.setForeground(color);
				Rectangle rect = selectedPageControl.getBounds();
				gc.drawRectangle(rect.x - 3, rect.y - 2, rect.width + 4, rect.height + 2);
			}
		});

		setupLinks();
	}

	/**
	 * Add a listener for page changes.
	 * 
	 * @param listener
	 *            Listener to add
	 */
	public void addPageListener(PageListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Remove the given page listener from the list of page listeners.
	 * 
	 * @param listener
	 *            Listener to remove
	 */
	public void removePageListener(PageListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * @return Number of pages that can be shown by this component.
	 */
	public int getPageCount()
	{
		return pageCount;
	}

	/**
	 * Set the number of pages that can be shown by this component.
	 * 
	 * @param pageCount
	 */
	public void setPageCount(int pageCount)
	{
		this.pageCount = pageCount;
		asyncSetupLinks();
	}

	/**
	 * @return The selected page.
	 */
	public int getSelectedPage()
	{
		return selectedPage;
	}

	/**
	 * Set the selected page.
	 * <p/>
	 * Note that listeners are not notified of this page change.
	 * 
	 * @param selectedPage
	 */
	public void setSelectedPage(int selectedPage)
	{
		setSelectedPage(selectedPage, false);
	}

	protected void setSelectedPage(int selectedPage, boolean notifyListeners)
	{
		if (this.selectedPage != selectedPage)
		{
			this.selectedPage = selectedPage;
			asyncSetupLinks();

			if (notifyListeners)
			{
				for (int i = listeners.size() - 1; i >= 0; i--)
				{
					listeners.get(i).pageChanged(selectedPage);
				}
			}
		}
	}

	/**
	 * @return The minimum spacing between page links.
	 */
	public int getSpacing()
	{
		return ((PageLinksLayout) getLayout()).spacing;
	}

	/**
	 * Set the minimum spacing between page links.
	 * 
	 * @param spacing
	 */
	public void setSpacing(int spacing)
	{
		((PageLinksLayout) getLayout()).spacing = spacing;
		asyncSetupLinks();
	}

	@Override
	public void dispose()
	{
		disposeChildren();
		super.dispose();
	}

	protected void disposeChildren()
	{
		Control[] children = getChildren();
		for (Control child : children)
		{
			child.dispose();
		}
	}

	/**
	 * Calls {@link #setupLinks()} on the UI thread.
	 */
	protected void asyncSetupLinks()
	{
		getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				setupLinks();
			}
		});
	}

	/**
	 * Create the child page link widgets.
	 */
	protected void setupLinks()
	{
		if (isDisposed())
		{
			return;
		}

		disposeChildren();

		int spacing = getSpacing();
		int availableWidth = getBounds().width - spacing;

		Button previous = new Button(this, SWT.FLAT);
		previous.setText("<"); //$NON-NLS-1$
		previous.pack();
		previous.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				setSelectedPage(selectedPage - 1, true);
			}
		});
		previous.setEnabled(selectedPage > 0);

		//reduce the available width by the width of the previous/next buttons (assume the next button has the same width)
		availableWidth -= previous.getBounds().width;
		availableWidth -= previous.getBounds().width;

		Link link = new Link(this, SWT.NONE);
		List<Integer> pages = new ArrayList<Integer>();
		int pageCount = this.pageCount == UNKNOWN_PAGE_COUNT ? Integer.MAX_VALUE : this.pageCount;
		int page = Math.min(pageCount - 1, this.selectedPage);
		while (availableWidth >= 0)
		{
			//has the first page been added to the array (don't add any before):
			boolean addedFirst = !pages.isEmpty() && pages.get(0) == 0;
			//has the last page been added to the array (don't add any after):
			boolean addedLast = !pages.isEmpty() && pages.get(pages.size() - 1) == pageCount - 1;
			//if both the first and the last have been added, we don't need to add any more
			if (addedFirst && addedLast)
			{
				break;
			}

			//calculate the width of the page link and subtract it from the available width
			link.setText(String.valueOf(page + 1));
			link.pack();
			int width = link.getBounds().width + spacing;
			if (width > availableWidth)
			{
				break;
			}
			availableWidth -= width;

			//should the new page be added at the start or end of the array?
			boolean before = !addedFirst && (addedLast || pages.size() % 2 == 0) && page != pageCount - 1;
			//add the page to the array:
			pages.add(before ? 0 : pages.size(), page);
			//increment or decrement the page:
			page += addedFirst ? 1 : addedLast ? -1 : pages.size() * (before ? 1 : -1);
		}
		link.dispose();

		selectedPageControl = null;
		int extraWidthPerPage = availableWidth / Math.max(pages.size(), 1) + 1;
		for (final Integer p : pages)
		{
			link = new Link(this, SWT.NONE);
			String text = String.valueOf(p + 1);
			if (selectedPage != p)
			{
				text = "<a>" + text + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			else
			{
				selectedPageControl = link;
			}
			link.setText(text);
			link.pack();
			link.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					setSelectedPage(p, true);
				}
			});

			//distribute the extra available width equally to the non selected link controls
			//(but not the selected page so it doesn't affect the border)
			if (selectedPage != p)
			{
				int extraWidth = Math.min(extraWidthPerPage, availableWidth);
				availableWidth -= extraWidth;
				//link.setLayoutData(new RowData(link.getBounds().width + extraWidth, SWT.DEFAULT));
			}
		}

		Button next = new Button(this, SWT.FLAT);
		next.setText(">"); //$NON-NLS-1$
		next.pack();
		next.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				setSelectedPage(selectedPage + 1, true);
			}
		});
		next.setEnabled(selectedPage < pageCount - 1);

		layout();
		redraw(); //repaint the selected page border
	}
}
