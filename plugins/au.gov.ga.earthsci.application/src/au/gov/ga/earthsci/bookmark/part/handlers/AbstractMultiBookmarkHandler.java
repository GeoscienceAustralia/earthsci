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

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;

import au.gov.ga.earthsci.bookmark.model.IBookmark;

/**
 * An abstract command handler that has a {@link #canExecute(IBookmark[])}
 * method that only enables the command when AT LEAST ONE bookmark is selected.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class AbstractMultiBookmarkHandler
{

	@Execute
	public void execute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) IBookmark[] selectedBookmarks)
	{
		doExecute(selectedBookmarks);
	}

	/**
	 * Implement handler specific execution logic here
	 * 
	 * @param selectedBookmarks
	 *            The currently selected bookmarks
	 */
	protected abstract void doExecute(IBookmark[] selectedBookmarks);

	@CanExecute
	public boolean canExecute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) IBookmark[] selectedBookmarks)
	{
		return selectedBookmarks != null && selectedBookmarks.length > 0;
	}

}
