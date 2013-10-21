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

import java.beans.IntrospectionException;

import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class BeanPropertyValueBinder implements IValueBinder<Object>
{
	private static final Logger logger = LoggerFactory.getLogger(BeanPropertyValueBinder.class);

	private final BeanProperty beanProperty;

	public BeanPropertyValueBinder(Object object, ModelProperty property) throws IntrospectionException
	{
		beanProperty = new BeanProperty(object, property, property.getAnnotation(ReadOnly.class) != null);
	}

	@Override
	public String get(Object ignored, ValueProperty property)
	{
		Object value;
		try
		{
			value = beanProperty.get();
		}
		catch (Exception e)
		{
			logger.error("Error invoking '" + property.getName() + "' property getter", e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		return StringInstantiable.toString(value);
	}

	@Override
	public void set(String value, Object ignored, ValueProperty property)
	{
		if (beanProperty.isReadOnly())
		{
			return;
		}
		Class<?> type = beanProperty.getSetterType();
		if (type.isPrimitive() && (value == null || value.length() == 0))
		{
			//disable instantiating a primitive type with an empty string
			return;
		}
		Object object = StringInstantiable.newInstance(value, type);
		if (type.isPrimitive() && object == null)
		{
			//disable passing a null object as a primitive parameter
			return;
		}
		try
		{
			beanProperty.set(object);
		}
		catch (Exception e)
		{
			logger.error("Error invoking '" + property.getName() + "' property setter", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
