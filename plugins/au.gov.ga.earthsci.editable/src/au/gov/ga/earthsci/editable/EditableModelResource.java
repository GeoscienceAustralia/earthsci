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

import org.eclipse.sapphire.modeling.BindingImpl;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Resource;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Resource} subclass used by the {@link EditableModel}. Contains the
 * object being edited.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EditableModelResource<T> extends Resource
{
	private static final String[] EMPTY_PARAMS = new String[0];
	private static final Logger logger = LoggerFactory.getLogger(EditableModelResource.class);

	private final T object;

	public EditableModelResource(T object)
	{
		this(object, null);
	}

	public EditableModelResource(T object, Resource parent)
	{
		super(parent);
		this.object = object;
	}

	@Override
	protected BindingImpl createBinding(ModelProperty property)
	{
		BindingImpl binding = null;
		try
		{
			if (property instanceof ValueProperty)
			{
				binding = new EditableModelValueBinding(object, (ValueProperty) property);
			}
			else if (property instanceof ImpliedElementProperty)
			{
				binding = new EditableModelImpliedElementBinding(object, (ImpliedElementProperty) property, this);
			}
			else if (property instanceof ElementProperty)
			{
				binding = new EditableModelElementBinding(object, (ElementProperty) property, this);
			}
			else if (property instanceof ListProperty)
			{
				binding = new EditableModelListBinding(object, (ListProperty) property, this);
			}
		}
		catch (Exception e)
		{
			logger.error("Error binding to value property: " + property.getName(), e); //$NON-NLS-1$
		}

		if (binding != null)
		{
			binding.init(element(), property, EMPTY_PARAMS);
		}

		return binding;
	}

	public T getObject()
	{
		return object;
	}
}
