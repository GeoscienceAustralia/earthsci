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
package au.gov.ga.earthsci.editable;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;

import au.gov.ga.earthsci.editable.annotations.ElementType;
import au.gov.ga.earthsci.editable.annotations.Factory;

/**
 * Custom binding for {@link ElementProperty}s and
 * {@link ImpliedElementProperty}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IElementBinder<E> extends IBinder<Object, E, ElementProperty>
{
	/**
	 * @return Type that the property setter method accepts, used by the
	 *         {@link PropertyValueFactory} to instantiate a new instance of a
	 *         value for this property (if property doesn't have a
	 *         {@link Factory} or {@link ElementType} annotation). Return
	 *         <code>null</code> if unknown.
	 */
	Class<?> getNewType();
}
