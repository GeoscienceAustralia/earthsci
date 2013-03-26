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
package au.gov.ga.earthsci.bookmark.part.handlers;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;

import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmarkList;
import au.gov.ga.earthsci.bookmark.model.IBookmarks;
import au.gov.ga.earthsci.bookmark.part.IBookmarksController;

/**
 * Command handler that moves selected bookmarks to another bookmark list
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class MoveToListHandler
{
	public static final String COMMAND_ID = "au.gov.ga.earthsci.application.command.movetogroup"; //$NON-NLS-1$
	public static final String LIST_PARAMETER_ID =
			"au.gov.ga.earthsci.application.command.movetogroup.parameter.groupid"; //$NON-NLS-1$

	@Inject
	public IBookmarksController controller;

	@Inject
	public IBookmarks bookmarks;

	@Execute
	public void execute(@Optional @Named(LIST_PARAMETER_ID) String listId,
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) IBookmark[] selectedBookmarks)
	{
		IBookmarkList targetList = bookmarks.getListById(listId);
		if (targetList == null)
		{
			return;
		}
		controller.moveBookmarks(controller.getCurrentList(), selectedBookmarks, targetList, targetList.getBookmarks()
				.size());
	}

	@CanExecute
	public boolean canExecute(@Optional @Named(LIST_PARAMETER_ID) String listId,
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) IBookmark[] selectedBookmarks)
	{
		return selectedBookmarks != null && selectedBookmarks.length > 0
				&& !listId.equals(controller.getCurrentList().getId());
	}
}
