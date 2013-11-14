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

import java.awt.Color;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.InitialValue;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Services;
import org.eclipse.sapphire.modeling.annotations.Type;

import au.gov.ga.earthsci.editable.annotations.Sync;
import au.gov.ga.earthsci.editable.serialization.ColorAwtToStringConversionService;
import au.gov.ga.earthsci.editable.serialization.StringToColorAwtConversionService;
import au.gov.ga.earthsci.layer.wrappers.SkyGradientLayerWrapper;

/**
 * Sapphire {@link IModelElement} describing {@link SkyGradientLayerWrapper}
 * instances.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface SkyGradientLayerWrapperModelElement extends LayerModelElement
{
	ElementType TYPE = new ElementType(SkyGradientLayerWrapperModelElement.class);

	@Sync
	@NumericRange(min = "0", max = "1e6")
	@Type(base = Double.class)
	@InitialValue(text = "1e5")
	ValueProperty PROP_ATMOSPHERE_THICKNESS = new ValueProperty(TYPE, "AtmosphereThickness"); //$NON-NLS-1$

	@Type(base = Color.class)
	@InitialValue(text = "#c2c2cc")
	@Services({ @Service(impl = StringToColorAwtConversionService.class),
			@Service(impl = ColorAwtToStringConversionService.class) })
	ValueProperty PROP_HORIZON_COLOR = new ValueProperty(TYPE, "HorizonColor"); //$NON-NLS-1$

	@Type(base = Color.class)
	@InitialValue(text = "#4278d4")
	@Services({ @Service(impl = StringToColorAwtConversionService.class),
			@Service(impl = ColorAwtToStringConversionService.class) })
	ValueProperty PROP_ZENITH_COLOR = new ValueProperty(TYPE, "ZenithColor"); //$NON-NLS-1$
}