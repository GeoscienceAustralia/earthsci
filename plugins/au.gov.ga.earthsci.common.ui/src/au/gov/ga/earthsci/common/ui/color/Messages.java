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
package au.gov.ga.earthsci.common.ui.color;

import org.eclipse.osgi.util.NLS;

/**
 * @author u09145
 *
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.common.ui.color.messages"; //$NON-NLS-1$
	public static String ColorMapEditor_AddEntryLabel;
	public static String ColorMapEditor_EntryAlphaLabel;
	public static String ColorMapEditor_EntryColorLabel;
	public static String ColorMapEditor_EntryValueLabel;
	public static String ColorMapEditor_ModeLabel;
	public static String ColorMapEditor_NoDataOptionLabel;
	public static String ColorMapEditor_NoDataOptionTooltip;
	public static String ColorMapEditor_RemoveEntryLabel;
	public static String ColorMapEditor_TableColorColumnLabel;
	public static String ColorMapEditor_TableValueColumnLabel;
	public static String ColorMapEditor_UsePercentagesLabel;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
