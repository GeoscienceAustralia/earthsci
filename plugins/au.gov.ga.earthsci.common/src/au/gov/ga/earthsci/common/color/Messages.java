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
package au.gov.ga.earthsci.common.color;

import org.eclipse.osgi.util.NLS;

/**
 * @author u09145
 *
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.common.color.messages"; //$NON-NLS-1$
	public static String ColorMap_DefaultColorMapName;
	public static String ColorMap_ExactMatchDescription;
	public static String ColorMap_ExactMatchName;
	public static String ColorMap_HueInterpolateDescription;
	public static String ColorMap_HueInterpolateName;
	public static String ColorMap_NearestMatchDescription;
	public static String ColorMap_NearestMatchName;
	public static String ColorMap_RGBInterpolateDescription;
	public static String ColorMap_RGBInterpolateName;
	public static String ColorMaps_RBGDescription;
	public static String ColorMaps_RBGName;
	public static String ColorMaps_RGBDescription;
	public static String ColorMaps_RGBName;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
