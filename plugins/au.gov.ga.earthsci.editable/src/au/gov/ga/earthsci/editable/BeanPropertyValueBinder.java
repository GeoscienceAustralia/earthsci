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
import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.Value;

import au.gov.ga.earthsci.common.util.StringInstantiable;
import au.gov.ga.earthsci.editable.annotations.ValueBinder;

/**
 * {@link IValueBinder} implementation that uses a {@link BeanProperty} for
 * getting/setting a property's value.
 * <p/>
 * Used as the default {@link IValueBinder} by the
 * {@link EditableModelValueBinding} (if no {@link ValueBinder} is defined by
 * the model property).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BeanPropertyValueBinder extends AbstractBeanPropertyValueBinder
{
	@Override
	protected String convertTo(Object value, Value<?> property, BeanProperty beanProperty, Element element)
	{
		//first try string instantiable:
		String instantiable = StringInstantiable.toString(value);
		if (instantiable != null)
		{
			return instantiable;
		}

		//next try a ValueSerializationService:
		return element.property(property.definition()).service(MasterConversionService.class)
				.convert(value, String.class);
	}

	@Override
	protected au.gov.ga.earthsci.editable.AbstractBeanPropertyBinder.Conversion convertFrom(String value,
			Value<?> property, BeanProperty beanProperty, Element element)
	{
		Class<?> type = beanProperty.getSetterType();
		if (type.isPrimitive() && (value == null || value.length() == 0))
		{
			//disable instantiating a primitive type with an empty string
			return null;
		}
		Object object = StringInstantiable.newInstance(value, type);
		if (object == null)
		{
			//not string instantiable, so try the ValueSerializationService
			object =
					element.property(property.definition()).service(MasterConversionService.class).convert(value, type);
		}
		if (type.isPrimitive() && object == null)
		{
			//disable passing a null object as a primitive parameter
			return null;
		}
		return new Conversion(object);
	}
}
