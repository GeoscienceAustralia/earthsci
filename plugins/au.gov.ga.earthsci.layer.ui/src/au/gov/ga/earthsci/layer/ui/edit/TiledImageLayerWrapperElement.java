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
package au.gov.ga.earthsci.layer.ui.edit;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ValueProperty;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public interface TiledImageLayerWrapperElement extends LayerElement
{
	ElementType TYPE = new ElementType(TiledImageLayerWrapperElement.class);

	ValueProperty PROP_DATA_CACHE_NAME = new ValueProperty(TYPE, "DataCacheName");
}
