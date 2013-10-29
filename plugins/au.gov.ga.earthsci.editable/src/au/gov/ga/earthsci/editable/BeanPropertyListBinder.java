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

import java.util.List;

import org.eclipse.sapphire.modeling.ListProperty;

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
public class BeanPropertyListBinder extends AbstractBeanPropertyBinder<List<?>, Object, ListProperty> implements
		IListBinder<Object>
{
	@SuppressWarnings("rawtypes")
	@Override
	protected List<?> convertTo(Object object, ListProperty property, BeanProperty beanProperty)
	{
		return (List) object;
	}

	@Override
	protected Conversion convertFrom(List<?> value, ListProperty property, BeanProperty beanProperty)
	{
		return new Conversion(value);
	}
}
