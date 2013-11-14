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

import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyBinding;
import org.eclipse.sapphire.Resource;
import org.eclipse.sapphire.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Resource} subclass used by the {@link EditableModel}. Contains the
 * object being edited.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EditableModelResource<T> extends Resource implements IRevertable
{
	private static final Logger logger = LoggerFactory.getLogger(EditableModelResource.class);

	private final T object;
	private boolean dontCreateBinding = false;

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
	protected PropertyBinding createBinding(Property property)
	{
		if (dontCreateBinding)
		{
			return null;
		}

		PropertyBinding binding = null;
		try
		{
			if (property instanceof Value<?>)
			{
				Value<?> value = (Value<?>) property;
				binding = new EditableModelValueBinding(object, value);
			}
			else if (property instanceof ElementHandle<?>)
			{
				ElementHandle<?> elementHandle = (ElementHandle<?>) property;
				if (property.definition() instanceof ImpliedElementProperty)
				{
					binding = new EditableModelImpliedElementBinding(object, elementHandle, this);
				}
				else
				{
					binding = new EditableModelElementBinding(object, elementHandle, this);
				}
			}
			else if (property instanceof ElementList<?>)
			{
				ElementList<?> elementList = (ElementList<?>) property;
				binding = new EditableModelListBinding(object, elementList, this);
			}
		}
		catch (Exception e)
		{
			logger.error("Error binding to value property: " + property.name(), e); //$NON-NLS-1$
		}

		if (binding != null)
		{
			binding.init(property);
		}

		return binding;
	}

	protected PropertyBinding bindingIfExists(Property property)
	{
		dontCreateBinding = true;
		try
		{
			return binding(property);
		}
		catch (Exception e)
		{
			//ignore (binding() throws an exception if createBinding returns null)
			return null;
		}
		finally
		{
			dontCreateBinding = false;
		}
	}

	public T getObject()
	{
		return object;
	}

	@Override
	public void revert()
	{
		for (Property property : element().properties())
		{
			PropertyBinding binding = bindingIfExists(property);
			if (binding instanceof IRevertable)
			{
				((IRevertable) binding).revert();
			}
		}
	}
}
