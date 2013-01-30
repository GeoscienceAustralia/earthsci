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
package au.gov.ga.earthsci.bookmark.part;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.bookmark.model.IBookmark;

/**
 * Drop adapter for the {@link BookmarksPart}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
class BookmarksDropAdapter extends ViewerDropAdapter
{

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(BookmarksDropAdapter.class);
	
	private IBookmarksController controller;
	
	protected BookmarksDropAdapter(Viewer viewer, IBookmarksController controller)
	{
		super(viewer);
		this.controller = controller;
	}

	@Override
	public boolean performDrop(Object data)
	{
		if (data == null)
		{
			return false;
		}
		
		if (isBookmarkDrop())
		{
			return doBookmarkDrop(data);
		}
		
		return false;
	}

	private boolean isBookmarkDrop()
	{
		return BookmarkTransfer.getInstance().isSupportedType(getCurrentEvent().currentDataType);
	}
	
	private boolean doBookmarkDrop(Object data)
	{
		BookmarkTransferData btd = (BookmarkTransferData)data;
		
		int index = getDropIndex();
		
		if (getCurrentOperation() == DND.DROP_MOVE)
		{
			controller.moveTo(btd.getBookmarks(), index);
			return true;
		}
		else if (getCurrentOperation() == DND.DROP_COPY)
		{
			controller.copyTo(btd.getBookmarks(), index);
			return true;
		}
		
		return false;
	}
	
	private int getDropIndex()
	{
		IBookmark target = (IBookmark) getCurrentTarget();
		if (target == null)
		{
			return controller.getCurrentList().getBookmarks().size();
		}
		
		int location = getCurrentLocation();
		if (location == LOCATION_NONE)
		{
			return controller.getCurrentList().getBookmarks().size();
		}
				
		int indexOf = controller.getCurrentList().getBookmarks().indexOf(target);
		return location == LOCATION_BEFORE ? indexOf : indexOf + 1;
	}
	
	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType)
	{
		return BookmarkTransfer.getInstance().isSupportedType(transferType);
	}

}
