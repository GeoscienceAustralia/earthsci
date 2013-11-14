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

import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Property;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.editable.annotations.Factory;

/**
 * Factory that instantiates objects of a type compatible with a given property.
 * Can be used for {@link ElementProperty}s and {@link ListProperty}s, using the
 * {@link Factory} annotation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IFactory<T>
{
	/**
	 * Create a new instance of <code>T</code> that can be set for the given
	 * property on the parent object.
	 * 
	 * @param type
	 *            Model type
	 * @param property
	 *            Property that the new instance is for
	 * @param parent
	 *            Parent that the object will be set on
	 * @param shell
	 *            Shell to use as the parent for any UI elements required to
	 *            create the new instance (such as a input dialog)
	 * @return New instance
	 */
	T create(ElementType type, Property property, Object parent, Shell shell);
}
