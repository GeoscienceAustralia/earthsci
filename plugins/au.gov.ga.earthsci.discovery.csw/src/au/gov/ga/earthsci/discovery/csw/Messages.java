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
package au.gov.ga.earthsci.discovery.csw;

import org.eclipse.osgi.util.NLS;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.discovery.csw.messages"; //$NON-NLS-1$
	public static String CSWDiscoveryResultHandler_URLSelectionDialogTitle;
	public static String CSWDiscoveryResultHandler_Error;
	public static String CSWDiscoveryResultHandler_ErrorNoURLs;
	public static String CSWDiscoveryResultHandler_ErrorOpeningURL;
	public static String CSWFormatProperty_FormatLabel;
	public static String CSWURLSelectionDialog_GetCapabilitiesButtonText;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
