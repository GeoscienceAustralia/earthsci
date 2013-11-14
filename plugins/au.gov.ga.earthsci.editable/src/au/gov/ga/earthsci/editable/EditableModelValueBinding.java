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

import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.ValuePropertyBinding;

import au.gov.ga.earthsci.editable.annotations.ValueBinder;

/**
 * {@link ValueBindingImpl} subclass used by the {@link EditableModelResource}
 * as the binding for {@link ValueProperty}s.
 * <p/>
 * Users can provide custom bindings that implement the {@link IValueBinder}
 * interface by adding the {@link ValueBinder} annotation to the
 * {@link ValueProperty} field in the model.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EditableModelValueBinding extends ValuePropertyBinding implements IRevertable
{
	private final Object object;
	private final Value<?> property;
	private final IValueBinder<Object> binder;
	private String oldValue;
	private boolean oldValueSet = false;

	@SuppressWarnings("unchecked")
	public EditableModelValueBinding(Object object, Value<?> property) throws IntrospectionException,
			InstantiationException, IllegalAccessException
	{
		this.object = object;
		this.property = property;

		ValueBinder binderAnnotation = property.definition().getAnnotation(ValueBinder.class);
		if (binderAnnotation != null && binderAnnotation.value() != null)
		{
			Class<? extends IValueBinder<?>> binderClass = binderAnnotation.value();
			this.binder = (IValueBinder<Object>) binderClass.newInstance();
		}
		else
		{
			this.binder = new BeanPropertyValueBinder();
		}
	}

	@Override
	public String read()
	{
		return binder.get(object, property, property().element());
	}

	@Override
	public void write(String value)
	{
		if (!oldValueSet)
		{
			oldValue = read();
			oldValueSet = true;
		}
		binder.set(value, object, property, property().element());
	}

	@Override
	public void revert()
	{
		if (oldValueSet)
		{
			write(oldValue);
			oldValue = null;
			oldValueSet = false;
		}
	}
}
