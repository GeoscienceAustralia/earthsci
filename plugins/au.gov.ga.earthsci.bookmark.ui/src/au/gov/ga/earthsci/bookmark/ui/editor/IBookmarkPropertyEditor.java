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
package au.gov.ga.earthsci.bookmark.ui.editor;

import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;

/**
 * An interface for classes that create controls for displaying and editing
 * values of an {@link IBookmarkProperty}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarkPropertyEditor extends IBookmarkEditor
{

	/**
	 * Set the property that this editor is editing.
	 * <p/>
	 * Calling this method will re-populate the controls etc. with the new
	 * values and abandon any user edits.
	 * 
	 * @param property
	 *            The property to edit with this editor
	 */
	void setProperty(IBookmarkProperty property);

	/**
	 * Retrieve the property this editor is editing
	 * 
	 * @return The property being edited, or <code>null</code> if none exist
	 */
	IBookmarkProperty getProperty();

	/**
	 * Fill the fields of this editor from the current world state (as
	 * appropriate), but (importantly) do not apply them to the backing
	 * property.
	 * <p/>
	 * Actual binding to the backing property should be done in
	 * {@link #okPressed()}
	 */
	void fillFromCurrent();

	/**
	 * Return whether the property being edited by this editor should be
	 * included in the current bookmark.
	 * 
	 * @return <code>true</code> if the property is to be included;
	 *         <code>false</code> otherwise.
	 */
	boolean isIncludedInBookmark();

	/**
	 * Set whether the property being edited by this editor should be included
	 * in the current bookmark
	 * 
	 * @param included
	 *            Whether or not to include the property in the current bookmark
	 */
	void setIncludedInBookmark(boolean included);
}
