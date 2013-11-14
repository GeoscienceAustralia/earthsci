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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.ListPropertyBinding;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.collection.adapter.IAdapter;
import au.gov.ga.earthsci.common.collection.adapter.IdentityHashMapAdapter;
import au.gov.ga.earthsci.common.collection.adapter.ListAdapter;
import au.gov.ga.earthsci.editable.annotations.ListBinder;

/**
 * {@link ListBindingImpl} subclass used by the {@link EditableModelResource} as
 * the binding for {@link ListProperty}s.
 * <p/>
 * Users can provide custom bindings that implement the {@link IListBinder}
 * interface by adding the {@link ListBinder} annotation to the
 * {@link ListProperty} field in the model.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EditableModelListBinding extends ListPropertyBinding implements IRevertable
{
	private static final Logger logger = LoggerFactory.getLogger(EditableModelListBinding.class);

	private final Object parent;
	private final ElementList<?> property;
	private final Resource parentResource;
	private final IListBinder<Object> binder;
	private List<Object> list;
	private IAdapter<EditableModelResource<?>, Object> adapter;
	private ListAdapter<EditableModelResource<?>, Object> listAdapter;
	private final List<Object> oldValue = new ArrayList<Object>();
	private boolean oldValueSet = false;

	@SuppressWarnings("unchecked")
	public EditableModelListBinding(Object parent, ElementList<?> property, final Resource parentResource)
			throws InstantiationException, IllegalAccessException, IntrospectionException
	{
		this.parent = parent;
		this.property = property;
		this.parentResource = parentResource;

		ListBinder binderAnnotation = property.definition().getAnnotation(ListBinder.class);
		if (binderAnnotation != null && binderAnnotation.value() != null)
		{
			Class<? extends IListBinder<?>> binderClass = binderAnnotation.value();
			binder = (IListBinder<Object>) binderClass.newInstance();
		}
		else
		{
			binder = new BeanPropertyListBinder();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(Property property)
	{
		super.init(property);

		list = (List<Object>) binder.get(parent, this.property, property.element());

		adapter = new IdentityHashMapAdapter<EditableModelResource<?>, Object>()
		{
			@Override
			protected EditableModelResource<?> createFrom(Object value)
			{
				return new EditableModelResource<Object>(value, parentResource);
			}

			@Override
			protected Object createTo(EditableModelResource<?> value)
			{
				return value.getObject();
			}
		};

		listAdapter = new ListAdapter<EditableModelResource<?>, Object>(list, adapter);
	}

	@Override
	public List<? extends Resource> read()
	{
		return listAdapter;
	}

	@Override
	public ElementType type(Resource resource)
	{
		return property.definition().getType();
	}

	@Override
	public Resource insert(ElementType type, int position)
	{
		storeOldValue();
		try
		{
			Object value = PropertyValueFactory.create(property, type, parent, null);
			if (value == null)
			{
				return null;
			}
			EditableModelResource<?> resource = new EditableModelResource<Object>(value, parentResource);
			listAdapter.add(resource);
			return resource;
		}
		catch (Exception e)
		{
			logger.error("Error creating value for property: " + property.name(), e); //$NON-NLS-1$
			return null;
		}
	}

	@Override
	public void move(Resource resource, int position)
	{
		storeOldValue();
		listAdapter.remove(resource);
		listAdapter.add(position, (EditableModelResource<?>) resource);
	}

	@Override
	public void remove(Resource resource)
	{
		storeOldValue();
		listAdapter.remove(resource);
	}

	private void storeOldValue()
	{
		if (!oldValueSet)
		{
			oldValue.addAll(list);
			oldValueSet = true;
		}
	}

	@Override
	public void revert()
	{
		if (oldValueSet)
		{
			list.clear();
			list.addAll(oldValue);
			oldValue.clear();
			oldValueSet = false;
		}

		for (Object object : list)
		{
			EditableModelResource<?> resource = adapter.adaptFrom(object);
			resource.revert();
		}
	}
}
