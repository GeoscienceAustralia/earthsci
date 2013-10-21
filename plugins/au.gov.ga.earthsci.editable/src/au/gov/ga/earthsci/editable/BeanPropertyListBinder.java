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

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.editable.annotations.ListBinder;

/**
 * {@link IListBinder} implementation that uses a {@link BeanProperty} for
 * getting/setting a property's value.
 * <p/>
 * Used as the default {@link IListBinder} by the
 * {@link EditableModelListBinding} (if no {@link ListBinder} is defined by the
 * model property).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BeanPropertyListBinder implements IListBinder<Object>
{
	private static final Logger logger = LoggerFactory.getLogger(BeanPropertyListBinder.class);

	private final BeanProperty beanProperty;

	public BeanPropertyListBinder(Object object, ModelProperty property) throws IntrospectionException
	{
		beanProperty = new BeanProperty(object, property, true);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<?> get(Object ignored, ListProperty property)
	{
		try
		{
			return (List) beanProperty.get();
		}
		catch (Exception e)
		{
			logger.error("Error invoking '" + property.getName() + "' property getter", e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	@Override
	public void set(List<?> value, Object ignored, ListProperty property)
	{
		if (beanProperty.isReadOnly())
		{
			return;
		}
		try
		{
			beanProperty.set(value);
		}
		catch (Exception e)
		{
			logger.error("Error invoking '" + property.getName() + "' property setter", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
