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
package au.gov.ga.earthsci.catalog.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.catalog.part.messages"; //$NON-NLS-1$
	public static String CatalogBrowserController_DialogDontAsk;
	public static String CatalogBrowserController_AddNodePathDialogMessage;
	public static String CatalogBrowserController_AddNodePathDialogTitle;
	public static String CatalogBrowserController_DeleteEmptyFoldersDialogTitle;
	public static String CatalogBrowserController_DeleteEmptyFoldersMessage;
	public static String CatalogBrowserPreferencesPage_AddNodeStructureMessage;
	public static String CatalogBrowserPreferencesPage_AskOptionLabel;
	public static String CatalogBrowserPreferencesPage_AwlaysOptionLabel;
	public static String CatalogBrowserPreferencesPage_DeleteEmptyFoldersMessage;
	public static String CatalogBrowserPreferencesPage_NeverOptionLabel;
	public static String CatalogBrowserPreferencesPage_PageDescription;
	public static String CatalogBrowserPreferencesPage_PageTitle;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
