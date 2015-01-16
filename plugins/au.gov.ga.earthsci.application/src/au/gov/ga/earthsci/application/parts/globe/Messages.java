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
package au.gov.ga.earthsci.application.parts.globe;

import org.eclipse.osgi.util.NLS;

/**
 * @author u09145
 *
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.application.parts.globe.messages"; //$NON-NLS-1$
	public static String FullscreenHandler_FullscreenLabel;
	public static String GlobeExaggerationToolControl_ToolTip0;
	public static String GlobePart_ToggleHUDTooltip;
	public static String GlobePreferencePage_Description;
	public static String GlobePreferencePage_FPS;
	public static String GlobePreferencePage_Stereo;
	public static String GlobePreferencePage_Title;
	public static String GotoCoordinateDialog_EnterCoordinates;
	public static String GotoCoordinateDialog_GotoCoordinates;
	public static String GotoCoordinateDialog_InvalidCoordinates;
	public static String GotoCoordinateDialog_Supports;
	public static String GotoCoordinateDialog_TypeCoordinates;
	public static String StereoHandler_StereoDialogMessage;
	public static String StereoHandler_StereoDialogTitle;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
