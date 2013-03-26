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
package au.gov.ga.earthsci.bookmark.part.editor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import au.gov.ga.earthsci.bookmark.model.IBookmark;

/**
 * An interface for editors able to participate in editing of {@link IBookmark}s
 * and their properties.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarkEditor
{
	/**
	 * Used to indicate the user has pressed 'OK'. Implementing classes should
	 * bind user input to the target as appropriate.
	 */
	void okPressed();

	/**
	 * Used to indicate the user has pressed 'Cancel'. Implementing classes
	 * should abandon user input.
	 */
	void cancelPressed();

	/**
	 * Restore the original values held by the target being edited
	 */
	void restoreOriginalValues();

	/**
	 * Create the editor controls on the given parent.
	 * 
	 * @param parent
	 *            The parent on which to attach the controls.
	 * 
	 * @return the created control
	 */
	Control createControl(Composite parent);

	/**
	 * @return the control for this editor
	 */
	Control getControl();

	/**
	 * Return the (localised) display name to use for this editor.
	 */
	String getName();

	/**
	 * Return the short (localised) description text to use for tooltips etc.
	 */
	String getDescription();

	/**
	 * Return whether this editor is in a valid state (i.e. User-provided values
	 * are valid and could be applied to the target if required)
	 * 
	 * @return <code>true</code> if the editor is in a valid state;
	 *         <code>false</code> otherwise.
	 */
	boolean isValid();

	/**
	 * Return any messages associated with this editor
	 */
	IBookmarkEditorMessage[] getMessages();

	/**
	 * Add a listener to this editor
	 */
	void addListener(IBookmarkEditorListener listener);

	/**
	 * Remove the listener from this editor.
	 */
	void removeListener(IBookmarkEditorListener listener);
}
