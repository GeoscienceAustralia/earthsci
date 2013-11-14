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

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;

import au.gov.ga.earthsci.editable.annotations.ElementBinder;

/**
 * {@link IElementBinder} implementation that uses a {@link BeanProperty} for
 * getting/setting a property's value.
 * <p/>
 * Used as the default {@link IElementBinder} by the
 * {@link EditableElementBinding} and
 * {@link EditableImpliedElementBinding} (if no {@link ElementBinder} is
 * defined by the model property).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BeanPropertyElementBinder extends AbstractBeanPropertyBinder<Object, Object, ElementHandle<?>> implements
		IElementBinder<Object>
{
	@Override
	protected Object convertTo(Object value, ElementHandle<?> property, BeanProperty beanProperty, Element element)
	{
		return value;
	}

	@Override
	protected Conversion convertFrom(Object value, ElementHandle<?> property, BeanProperty beanProperty,
			Element element)
	{
		return new Conversion(value);
	}

	@Override
	public Class<?> getNewType()
	{
		if (getBeanProperty() == null)
		{
			return null;
		}
		return getBeanProperty().getSetterType();
	}
}
