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

import org.eclipse.sapphire.modeling.ElementBindingImpl;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Resource;

import au.gov.ga.earthsci.editable.annotations.ElementBinder;

/**
 * {@link ElementBindingImpl} subclass used by the {@link EditableModelResource}
 * as the binding for {@link ImpliedElementProperty}s.
 * <p/>
 * Users can provide custom bindings that implement the {@link IElementBinder}
 * interface by adding the {@link ElementBinder} annotation to the
 * {@link ImpliedElementProperty} field in the model.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EditableModelImpliedElementBinding extends ElementBindingImpl implements IRevertable
{
	private final ImpliedElementProperty property;
	private final EditableModelResource<?> resource;

	@SuppressWarnings("unchecked")
	public EditableModelImpliedElementBinding(Object parent, ImpliedElementProperty property, Resource parentResource)
			throws InstantiationException, IllegalAccessException, IntrospectionException
	{
		this.property = property;

		IElementBinder<Object> binder;
		ElementBinder binderAnnotation = property.getAnnotation(ElementBinder.class);
		if (binderAnnotation != null && binderAnnotation.value() != null)
		{
			Class<? extends IElementBinder<?>> binderClass = binderAnnotation.value();
			binder = (IElementBinder<Object>) binderClass.newInstance();
		}
		else
		{
			binder = new BeanPropertyElementBinder(parent, property);
		}

		Object object = binder.get(parent, property);
		resource = new EditableModelResource<Object>(object, parentResource);
	}

	@Override
	public Resource read()
	{
		return resource;
	}

	@Override
	public ModelElementType type(Resource resource)
	{
		return property.getType();
	}

	@Override
	public void revert()
	{
		resource.revert();
	}
}
