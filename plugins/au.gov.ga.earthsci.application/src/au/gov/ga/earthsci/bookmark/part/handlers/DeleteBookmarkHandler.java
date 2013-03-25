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

import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.part.IBookmarksController;

/**
 * A command handler for deleting a selected bookmark
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DeleteBookmarkHandler extends AbstractMultiBookmarkHandler
{

	@Inject
	private IBookmarksController controller;

	@Override
	protected void doExecute(IBookmark[] selectedBookmarks)
	{
		controller.delete(selectedBookmarks);
	}
	
	public void setController(IBookmarksController controller)
	{
		this.controller = controller;
	}
}
