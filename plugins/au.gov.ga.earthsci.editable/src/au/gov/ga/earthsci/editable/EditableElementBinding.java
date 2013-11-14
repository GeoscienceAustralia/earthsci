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

import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.modeling.ElementPropertyBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.editable.annotations.ElementBinder;

/**
 * {@link ElementBindingImpl} subclass used by the {@link EditableResource}
 * as the binding for {@link ElementProperty}s.
 * <p/>
 * Users can provide custom bindings that implement the {@link IElementBinder}
 * interface by adding the {@link ElementBinder} annotation to the
 * {@link ElementProperty} field in the element.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EditableElementBinding extends ElementPropertyBinding implements IRevertable
{
	private static final Logger logger = LoggerFactory.getLogger(EditableElementBinding.class);

	private final Object parent;
	private final ElementHandle<?> property;
	private final Resource parentResource;
	private final IElementBinder<Object> binder;
	private EditableResource<?> resource;
	private EditableResource<?> oldResource;
	private boolean oldValueSet = false;

	@SuppressWarnings("unchecked")
	public EditableElementBinding(Object parent, ElementHandle<?> property, Resource parentResource)
			throws InstantiationException, IllegalAccessException, IntrospectionException
	{
		this.parent = parent;
		this.property = property;
		this.parentResource = parentResource;

		ElementBinder binderAnnotation = property.definition().getAnnotation(ElementBinder.class);
		if (binderAnnotation != null && binderAnnotation.value() != null)
		{
			Class<? extends IElementBinder<?>> binderClass = binderAnnotation.value();
			this.binder = (IElementBinder<Object>) binderClass.newInstance();
		}
		else
		{
			this.binder = new BeanPropertyElementBinder();
		}
	}

	@Override
	public EditableResource<?> read()
	{
		if (resource == null)
		{
			Object object = binder.get(parent, property, property.element());
			if (object != null)
			{
				resource = new EditableResource<Object>(object, parentResource);
			}
		}
		return resource;
	}

	@Override
	public ElementType type(Resource resource)
	{
		return property.definition().getType();
	}

	@Override
	public Resource create(ElementType type)
	{
		storeOldValue();
		try
		{
			Object object = PropertyValueFactory.create(property, type, parent, binder.getNewType());
			if (object == null)
			{
				return null;
			}
			binder.set(object, parent, property, property.element());
			resource = new EditableResource<Object>(object, parentResource);
			return resource;
		}
		catch (Exception e)
		{
			logger.error("Error creating value for property: " + property.definition().name(), e); //$NON-NLS-1$
			return null;
		}
	}

	@Override
	public void remove()
	{
		storeOldValue();
		binder.set(null, parent, property, property.element());
		resource = null;
	}

	private void storeOldValue()
	{
		if (!oldValueSet)
		{
			oldResource = read();
			oldValueSet = true;
		}
	}

	@Override
	public void revert()
	{
		if (oldValueSet)
		{
			Object oldValue = oldResource == null ? null : oldResource.getObject();
			binder.set(oldValue, parent, property, property.element());
			oldResource.revert();
			oldResource = null;
			oldValueSet = false;
		}
	}
}
