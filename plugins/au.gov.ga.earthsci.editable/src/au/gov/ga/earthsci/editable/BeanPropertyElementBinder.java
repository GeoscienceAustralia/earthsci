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

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.editable.annotations.ElementBinder;

/**
 * {@link IElementBinder} implementation that uses a {@link BeanProperty} for
 * getting/setting a property's value.
 * <p/>
 * Used as the default {@link IElementBinder} by the
 * {@link EditableModelElementBinding} and
 * {@link EditableModelImpliedElementBinding} (if no {@link ElementBinder} is
 * defined by the model property).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BeanPropertyElementBinder implements IElementBinder<Object>
{
	private static final Logger logger = LoggerFactory.getLogger(BeanPropertyElementBinder.class);

	private final BeanProperty beanProperty;

	public BeanPropertyElementBinder(Object object, ModelProperty property) throws IntrospectionException
	{
		beanProperty = new BeanProperty(object, property, property.getAnnotation(ReadOnly.class) != null);
	}

	@Override
	public Object get(Object ignored, ElementProperty property)
	{
		try
		{
			return beanProperty.get();
		}
		catch (Exception e)
		{
			logger.error("Error invoking '" + property.getName() + "' property getter", e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	@Override
	public void set(Object value, Object ignored, ElementProperty property)
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

	@Override
	public Class<?> getSetterType()
	{
		return beanProperty.getSetterType();
	}
}
