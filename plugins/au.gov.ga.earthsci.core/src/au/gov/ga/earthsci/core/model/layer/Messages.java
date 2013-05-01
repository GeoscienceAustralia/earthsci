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
package au.gov.ga.earthsci.core.model.layer;

import org.eclipse.osgi.util.NLS;

/**
 * @author u09145
 * 
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.core.model.layer.messages"; //$NON-NLS-1$
	public static String DefaultLayers_DefaultLabel;
	public static String IntentLayerLoader_FailedLoadNotificationDescription;
	public static String IntentLayerLoader_FailedLoadNotificationTitle;
	public static String IntentLayerLoader_LoadCanceledDescription;
	public static String IntentLayerLoader_UnknownLayerMessage;
	public static String IntentLayerLoader_UnknownLayerTitle;
	public static String LayerNode_FailedCopyNotificationDescription;
	public static String LayerNode_FailedCopyNotificationTitle;
	public static String LayerNodeDescriber_details;
	public static String LayerNodeDescriber_Folder;
	public static String LayerNodeDescriber_Label;
	public static String LayerNodeDescriber_Layer;
	public static String LayerNodeDescriber_Legend;
	public static String LayerNodeDescriber_Name;
	public static String URILayerLoadJob_FailedLoadNotificationDescription;
	public static String URILayerLoadJob_FailedLoadNotificationTitle;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
