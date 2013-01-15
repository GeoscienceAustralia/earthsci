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
package au.gov.ga.earthsci.bookmark.part;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.bookmark.part.messages"; //$NON-NLS-1$
	public static String BookmarkEditorDialog_BookmarkNameFieldLabel;
	public static String BookmarkEditorDialog_DialogTitle;
	public static String BookmarkEditorDialog_EmptyBookmarkNameMessage;
	public static String BookmarkEditorDialog_FillFromCurrentLabel;
	public static String BookmarkEditorDialog_GeneralEditorDescription;
	public static String BookmarkEditorDialog_GeneralEditorTitle;
	public static String BookmarkEditorDialog_IncludeInBookmarkLabel;
	public static String BookmarkEditorDialog_ResetValuesLabel;
	public static String BookmarksPreferencesPage_DefaultPropertiesLabel;
	public static String BookmarksPreferencesPage_Description;
	public static String BookmarksPreferencesPage_PropertyColumn;
	public static String BookmarksPreferencesPage_Title;
	public static String BookmarksPreferencesPage_TransitionDurationFieldTitle;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
