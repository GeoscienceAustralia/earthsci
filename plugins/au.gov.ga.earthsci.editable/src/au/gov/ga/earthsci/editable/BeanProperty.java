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

import static java.util.Locale.ENGLISH;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.sapphire.modeling.ModelProperty;

/**
 * Helper class for setting a property's value on an object, using reflection.
 * Searches for the standard Java bean property names (is/get, and set).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BeanProperty
{
	private final Object object;
	private final Method getter;
	private final Method setter;

	public BeanProperty(Object object, ModelProperty property, boolean readOnly) throws IntrospectionException
	{
		this.object = object;

		//PropertyDescriptor automatically tries "get" prefix if "is" getter is not found
		String getterProperty = "is" + capitalize(property.getName()); //$NON-NLS-1$
		String setterProperty = readOnly ? null : "set" + capitalize(property.getName()); //$NON-NLS-1$
		PropertyDescriptor propertyDescriptor =
				new PropertyDescriptor(property.getName(), object.getClass(), getterProperty, setterProperty);
		getter = propertyDescriptor.getReadMethod();
		setter = propertyDescriptor.getWriteMethod();
	}

	public boolean isReadOnly()
	{
		return setter == null;
	}

	public Class<?> getGetterType()
	{
		return getter.getReturnType();
	}

	public Class<?> getSetterType()
	{
		return setter.getParameterTypes()[0];
	}

	public Object get() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		return getter.invoke(object);
	}

	public void set(Object value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		setter.invoke(object, value);
	}

	private static String capitalize(String name)
	{
		if (name == null || name.length() == 0)
		{
			return name;
		}
		return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
	}
}
