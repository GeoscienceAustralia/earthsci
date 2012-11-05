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
package au.gov.ga.earthsci.worldwind.common.layers.curtain;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.WWXML;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * Extension of the {@link DataConfigurationUtils} class that adds some extra
 * XML parsing for curtain layer definitions.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CurtainDataConfigurationUtils extends DataConfigurationUtils
{
	public static AVList getLevelSetConfigParams(Element domElement, AVList params)
	{
		params = DataConfigurationUtils.getLevelSetConfigParams(domElement, params);

		XPath xpath = WWXML.makeXPath();

		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.FULL_WIDTH, "FullSize/Dimension/@width", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.FULL_HEIGHT, "FullSize/Dimension/@height", xpath);

		return params;
	}
}
