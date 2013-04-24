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
package au.gov.ga.earthsci.catalog.wms;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;

import java.net.URL;

import au.gov.ga.earthsci.common.util.IInformationed;

/**
 * {@link WMSTiledImageLayer} subclass that implements {@link IInformationed}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class InformationedWMSTiledImageLayer extends WMSTiledImageLayer implements IInformationed
{
	private final URL informationURL;

	public InformationedWMSTiledImageLayer(WMSCapabilities caps, AVList params, URL informationURL)
	{
		super(caps, params);
		this.informationURL = informationURL;
	}

	@Override
	public URL getInformationURL()
	{
		return informationURL;
	}

	@Override
	public String getInformationString()
	{
		return null;
	}
}
