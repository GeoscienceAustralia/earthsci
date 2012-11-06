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
package au.gov.ga.earthsci.core.model.layer;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;

import org.w3c.dom.Element;

/**
 * Factory for creating {@link Layer}s from XML.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LayerFactory extends au.gov.ga.earthsci.worldwind.common.layers.LayerFactory
{
	@Override
	protected Layer createFromLayerDocument(Element domElement, AVList params)
	{
		//TODO add extendable mechanism that allows creation of layers from layer documents
		//(add the ability to define XML->layer translators via an Eclipse extension point)

		return super.createFromLayerDocument(domElement, params);
	}
}
