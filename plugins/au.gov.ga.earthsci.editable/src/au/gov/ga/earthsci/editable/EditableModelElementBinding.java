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
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.editable.annotations.ElementBinder;

/**
 * {@link ElementBindingImpl} subclass used by the {@link EditableModelResource}
 * as the binding for {@link ElementProperty}s.
 * <p/>
 * Users can provide custom bindings that implement the {@link IElementBinder}
 * interface by adding the {@link ElementBinder} annotation to the
 * {@link ElementProperty} field in the model.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EditableModelElementBinding extends ElementBindingImpl
{
	private static final Logger logger = LoggerFactory.getLogger(EditableModelElementBinding.class);

	private final Object parent;
	private final ElementProperty property;
	private final Resource parentResource;
	private final IElementBinder<Object> binder;
	private EditableModelResource<?> resource;

	@SuppressWarnings("unchecked")
	public EditableModelElementBinding(Object parent, ElementProperty property, Resource parentResource)
			throws InstantiationException, IllegalAccessException, IntrospectionException
	{
		this.parent = parent;
		this.property = property;
		this.parentResource = parentResource;

		ElementBinder binderAnnotation = property.getAnnotation(ElementBinder.class);
		if (binderAnnotation != null && binderAnnotation.value() != null)
		{
			Class<? extends IElementBinder<?>> binderClass = binderAnnotation.value();
			this.binder = (IElementBinder<Object>) binderClass.newInstance();
		}
		else
		{
			this.binder = new BeanPropertyElementBinder(parent, property);
		}
	}

	@Override
	public Resource read()
	{
		if (resource == null)
		{
			Object object = binder.get(parent, property);
			if (object != null)
			{
				resource = new EditableModelResource<Object>(object, parentResource);
			}
		}
		return resource;
	}

	@Override
	public ModelElementType type(Resource resource)
	{
		return property.getType();
	}

	@Override
	public Resource create(ModelElementType type)
	{
		try
		{
			Object object = PropertyValueFactory.create(property, type, parent, binder.getSetterType());
			if (object == null)
			{
				return null;
			}
			binder.set(object, parent, property);
			resource = new EditableModelResource<Object>(object, parentResource);
			return resource;
		}
		catch (Exception e)
		{
			logger.error("Error creating value for property: " + property.getName(), e); //$NON-NLS-1$
			return null;
		}
	}

	@Override
	public void remove()
	{
		binder.set(null, parent, property);
		resource = null;
	}

	@Override
	public boolean removable()
	{
		return true;
	}
}
