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

import gov.nasa.worldwind.layers.Layer;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Type;

import au.gov.ga.earthsci.editable.annotations.Accuracy;
import au.gov.ga.earthsci.editable.annotations.Sync;

/**
 * Sapphire {@link IModelElement} describing {@link Layer} instances.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@GenerateImpl(packageName = "au.gov.ga.earthsci.editable", className = "RevertableModelElement")
public interface LayerModelElement extends IModelElement
{
	ModelElementType TYPE = new ModelElementType(LayerModelElement.class);

	ValueProperty PROP_NAME = new ValueProperty(TYPE, "Name"); //$NON-NLS-1$

	@Type(base = Boolean.class)
	ValueProperty PROP_ENABLED = new ValueProperty(TYPE, "Enabled"); //$NON-NLS-1$

	@Type(base = Double.class)
	@NumericRange(min = "0.0", max = "1.0")
	@Accuracy(2)
	@Sync
	ValueProperty PROP_OPACITY = new ValueProperty(TYPE, "Opacity"); //$NON-NLS-1$
}
