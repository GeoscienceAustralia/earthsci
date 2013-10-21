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
import java.util.List;

import org.eclipse.sapphire.modeling.ListBindingImpl;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Resource;
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
public class EditableModelListBinding extends ListBindingImpl
{
	private static final Logger logger = LoggerFactory.getLogger(EditableModelListBinding.class);

	private final Object parent;
	private final ListProperty property;
	private final Resource parentResource;
	private final List<Object> list;
	private final IAdapter<Resource, Object> adapter;
	private final ListAdapter<Resource, Object> listAdapter;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public EditableModelListBinding(Object parent, ListProperty property, final Resource parentResource)
			throws InstantiationException, IllegalAccessException, IntrospectionException
	{
		this.parent = parent;
		this.property = property;
		this.parentResource = parentResource;

		IListBinder<Object> binder;
		ListBinder binderAnnotation = property.getAnnotation(ListBinder.class);
		if (binderAnnotation != null && binderAnnotation.value() != null)
		{
			Class<? extends IListBinder<?>> binderClass = binderAnnotation.value();
			binder = (IListBinder<Object>) binderClass.newInstance();
		}
		else
		{
			binder = new BeanPropertyListBinder(parent, property);
		}

		list = (List) binder.get(parent, property);

		adapter = new IdentityHashMapAdapter<Resource, Object>()
		{
			@Override
			protected Resource createFrom(Object value)
			{
				return new EditableModelResource<Object>(value, parentResource);
			}

			@Override
			protected Object createTo(Resource value)
			{
				return ((EditableModelResource<Object>) value).getObject();
			}
		};

		listAdapter = new ListAdapter<Resource, Object>(list, adapter);
	}

	@Override
	public List<? extends Resource> read()
	{
		return listAdapter;
	}

	@Override
	public ModelElementType type(Resource resource)
	{
		return property.getType();
	}

	@Override
	public Resource insert(ModelElementType type, int position)
	{
		try
		{
			Object value = PropertyValueFactory.create(property, type, parent, null);
			if (value == null)
			{
				return null;
			}
			Resource resource = new EditableModelResource<Object>(value, parentResource);
			listAdapter.add(resource);
			return resource;
		}
		catch (Exception e)
		{
			logger.error("Error creating value for property: " + property.getName(), e); //$NON-NLS-1$
			return null;
		}
	}

	@Override
	public void move(Resource resource, int position)
	{
		listAdapter.remove(resource);
		listAdapter.add(position, resource);
	}

	@Override
	public void remove(Resource resource)
	{
		listAdapter.remove(resource);
	}
}
