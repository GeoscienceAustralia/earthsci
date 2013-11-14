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
package au.gov.ga.earthsci.catalog.wms.layer;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.eclipse.sapphire.modeling.annotations.Service;

import au.gov.ga.earthsci.layer.ui.edit.LayerModelElement;

/**
 * Sapphire {@link IModelElement} describing {@link WMSLayer} instances.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface WMSLayerModelElement extends LayerModelElement
{
	ElementType TYPE = new ElementType(WMSLayerModelElement.class);

	@ReadOnly
	@Label(standard = "Capabilities URL")
	ValueProperty PROP_CAPABILITIES_URI = new ValueProperty(TYPE, "CapabilitiesURI"); //$NON-NLS-1$

	@Service(impl = LayerNamePossibleValuesService.class)
	ValueProperty PROP_LAYER_NAME = new ValueProperty(TYPE, "LayerName"); //$NON-NLS-1$

	@Service(impl = StyleNamePossibleValuesService.class)
	ValueProperty PROP_STYLE_NAME = new ValueProperty(TYPE, "StyleName"); //$NON-NLS-1$
}