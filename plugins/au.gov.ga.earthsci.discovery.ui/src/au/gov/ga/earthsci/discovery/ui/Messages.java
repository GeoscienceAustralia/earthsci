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
package au.gov.ga.earthsci.discovery.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.discovery.ui.messages"; //$NON-NLS-1$
	public static String DiscoveryPart_Error;
	public static String DiscoveryPart_NoServicesDialogMessage;
	public static String DiscoveryPart_NoServicesDialogTitle;
	public static String DiscoveryPart_Search;
	public static String DiscoveryPart_SearchPlaceholder;
	public static String DiscoveryPreferencePage_Description;
	public static String DiscoveryPreferencePage_Title;
	public static String DiscoveryServicesPreferencePage_AddButton;
	public static String DiscoveryServicesPreferencePage_AddDialogTitle;
	public static String DiscoveryServicesPreferencePage_Description;
	public static String DiscoveryServicesPreferencePage_Disable;
	public static String DiscoveryServicesPreferencePage_DisableButton;
	public static String DiscoveryServicesPreferencePage_EditButton;
	public static String DiscoveryServicesPreferencePage_EditDialogTitle;
	public static String DiscoveryServicesPreferencePage_Enable;
	public static String DiscoveryServicesPreferencePage_Error;
	public static String DiscoveryServicesPreferencePage_ExportButton;
	public static String DiscoveryServicesPreferencePage_ExportError;
	public static String DiscoveryServicesPreferencePage_ImportButton;
	public static String DiscoveryServicesPreferencePage_ImportError;
	public static String DiscoveryServicesPreferencePage_RemoveButton;
	public static String DiscoveryServicesPreferencePage_SelectAllButton;
	public static String DiscoveryServicesPreferencePage_Title;
	public static String EditDiscoveryServiceDialog_LocationLabel;
	public static String EditDiscoveryServiceDialog_NameLabel;
	public static String EditDiscoveryServiceDialog_TypeLabel;
	public static String DiscoveryServiceViewerColumn_Disabled;
	public static String DiscoveryServiceViewerColumn_Enabled;
	public static String DiscoveryServiceViewerColumn_EnabledColumn;
	public static String DiscoveryServiceViewerColumn_LocationColumn;
	public static String DiscoveryServiceViewerColumn_NameColumn;
	public static String DiscoveryServiceViewerColumn_TypeColumn;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
