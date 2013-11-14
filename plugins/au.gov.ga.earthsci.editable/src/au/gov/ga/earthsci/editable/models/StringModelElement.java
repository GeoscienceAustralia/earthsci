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
package au.gov.ga.earthsci.editable.models;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;

import au.gov.ga.earthsci.editable.annotations.Factory;
import au.gov.ga.earthsci.editable.annotations.InstanceElementType;
import au.gov.ga.earthsci.editable.annotations.ValueBinder;
import au.gov.ga.earthsci.editable.binders.StringBinder;
import au.gov.ga.earthsci.editable.factories.StringFactory;

/**
 * {@link IModelElement} for string objects.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@InstanceElementType(String.class)
@Factory(StringFactory.class)
public interface StringModelElement extends Element
{
	ElementType TYPE = new ElementType(StringModelElement.class);

	@ReadOnly
	@ValueBinder(StringBinder.class)
	ValueProperty PROP_VALUE = new ValueProperty(TYPE, "Value"); //$NON-NLS-1$
}
