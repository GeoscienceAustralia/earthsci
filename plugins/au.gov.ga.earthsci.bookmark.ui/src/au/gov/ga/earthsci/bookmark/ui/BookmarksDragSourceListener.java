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
package au.gov.ga.earthsci.bookmark.ui;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.bookmark.model.IBookmark;

/**
 * {@link DragSourceListener} for the {@link BookmarksPart}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarksDragSourceListener implements DragSourceListener
{

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(BookmarksDragSourceListener.class);

	private TableViewer bookmarkListView;

	public BookmarksDragSourceListener(TableViewer bookmarkListView)
	{
		this.bookmarkListView = bookmarkListView;
	}

	@Override
	public void dragStart(DragSourceEvent event)
	{
		event.doit = getSelectedBookmarks().length > 0;
	}

	@Override
	public void dragSetData(DragSourceEvent event)
	{
		if (BookmarkTransfer.getInstance().isSupportedType(event.dataType))
		{
			doBookmarkTransfer(event);
			return;
		}
	}

	private void doBookmarkTransfer(DragSourceEvent event)
	{
		BookmarkTransferData data = BookmarkTransferData.fromBookmarks(getSelectedBookmarks());
		event.data = data;
	}

	@Override
	public void dragFinished(DragSourceEvent event)
	{
		if (!event.doit)
		{
			return;
		}
	}

	private IBookmark[] getSelectedBookmarks()
	{
		IStructuredSelection selection = (IStructuredSelection) bookmarkListView.getSelection();
		List<?> selectionList = selection.toList();
		IBookmark[] bookmarks = selectionList.toArray(new IBookmark[selectionList.size()]);
		return bookmarks;
	}

}
