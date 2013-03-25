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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.bookmark.part.editor.messages"; //$NON-NLS-1$
	public static String CameraPropertyEditor_EditorDescription;
	public static String CameraPropertyEditor_EditorTitle;
	public static String CameraPropertyEditor_EyePositionLabel;
	public static String CameraPropertyEditor_InvalidEyePositionMessage;
	public static String CameraPropertyEditor_InvalidLookatPositionMessage;
	public static String CameraPropertyEditor_InvalidUpVectorMessage;
	public static String CameraPropertyEditor_LookatPositionLabel;
	public static String CameraPropertyEditor_UpVectorLabel;
	public static String LayersPropertyEditor_EditorDescription;
	public static String LayersPropertyEditor_EditorName;
	public static String LayersPropertyEditor_InvalidOpacityMessage;
	public static String LayersPropertyEditor_LayerStateLabel;
	public static String LayersPropertyEditor_LayerStateLabelColumnLabel;
	public static String LayersPropertyEditor_LayerStateOpacityColumnLabel;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
