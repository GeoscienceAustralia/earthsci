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

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.Property;

/**
 * Super-interface for custom bindings.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <T>
 *            Value type for getting/setting on the property
 * @param <E>
 *            Type of the object/element being edited
 * @param <P>
 *            Model property type
 */
interface IBinder<T, E, P extends Property>
{
	/**
	 * Get the value for the property from the given object.
	 * 
	 * @param object
	 *            Object to read the value from
	 * @param property
	 *            Property to read
	 * @return Value for the given property
	 */
	T get(E object, P property, Element element);

	/**
	 * Set the value for the property on the given object.
	 * 
	 * @param value
	 *            Value to set
	 * @param object
	 *            Object to set the value on
	 * @param property
	 *            Property to set
	 */
	void set(T value, E object, P property, Element element);
}
