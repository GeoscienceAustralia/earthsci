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
package au.gov.ga.earthsci.application.console;

import org.eclipse.osgi.util.NLS;

/**
 * @author u09145
 *
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.application.console.messages"; //$NON-NLS-1$
	public static String LoggingPreferencePage_DebugLevel;
	public static String LoggingPreferencePage_ErrorLevel;
	public static String LoggingPreferencePage_FatalLevel;
	public static String LoggingPreferencePage_InfoLevel;
	public static String LoggingPreferencePage_LogLevelTitle;
	public static String LoggingPreferencePage_PageDescription;
	public static String LoggingPreferencePage_PageTitle;
	public static String LoggingPreferencePage_TraceLevel;
	public static String LoggingPreferencePage_WarnLevel;
	public static String StandardOutConsole_Name;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
