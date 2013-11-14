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
package au.gov.ga.earthsci.layer.wrappers;

import gov.nasa.worldwind.layers.TiledImageLayer;
import au.gov.ga.earthsci.common.util.XmlUtil;

/**
 * {@link XmlLayerWrapper} for editing {@link TiledImageLayer}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TiledImageLayerWrapper extends XmlLayerWrapper
{
	public String getDataCacheName()
	{
		return XmlUtil.getText(this.element, "DataCacheName"); //$NON-NLS-1$
	}

	public void setDataCacheName(String dataCacheName)
	{
		XmlUtil.setTextElement(this.element, "DataCacheName", dataCacheName); //$NON-NLS-1$
		reloadFromElement();
	}
}
