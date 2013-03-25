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
package au.gov.ga.earthsci.bookmark.part.editor;


/**
 * A listener interface for classes that wish to be notified of events generated
 * by a {@link IBookmarkEditor}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarkEditorListener
{
	/**
	 * Notified when the editor detects it is in an invalid state.
	 * 
	 * @param editor The editor that is invalid
	 * @param messages Message associated with the validation failure
	 */
	void editorInvalid(IBookmarkEditor editor, IBookmarkEditorMessage[] messages);
	
	/**
	 * Notified when the editor detects it has entered a valid state.
	 * 
	 * @param editor The editor that is now valid
	 */
	void editorValid(IBookmarkEditor editor);
}
