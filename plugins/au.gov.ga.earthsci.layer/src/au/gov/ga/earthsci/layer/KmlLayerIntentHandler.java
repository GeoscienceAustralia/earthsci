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
package au.gov.ga.earthsci.layer;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;

import java.io.File;
import java.net.URL;

import au.gov.ga.earthsci.core.intent.AbstractRetrieveIntentHandler;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.worldwind.common.layers.kml.KMLLayer;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * Intent handler for KML/KMZ layers.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class KmlLayerIntentHandler extends AbstractRetrieveIntentHandler
{
	@Override
	protected void handle(IRetrievalData data, URL url, Intent intent, IIntentCallback callback)
	{
		AVList params = new AVListImpl();
		params.setValue(AVKeyMore.CONTEXT_URL, url);
		try
		{
			KMLLayer layer = new KMLLayer(url, data.getInputStream(), params);
			String name = url.toString();
			if ("file".equalsIgnoreCase(url.getProtocol())) //$NON-NLS-1$
			{
				name = new File(url.toURI()).getName();
			}
			layer.setName(name);
			callback.completed(layer, intent);
		}
		catch (Exception e)
		{
			callback.error(e, intent);
		}
	}
}
