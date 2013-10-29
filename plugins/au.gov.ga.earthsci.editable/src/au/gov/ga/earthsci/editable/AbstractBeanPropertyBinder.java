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

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract {@link IBinder} implementation that uses a {@link BeanProperty} to
 * get/set property values using reflection.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractBeanPropertyBinder<T, E, P extends ModelProperty> implements IBinder<T, E, P>
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractBeanPropertyBinder.class);

	private BeanProperty beanProperty;
	private boolean beanPropertyInited = false;

	/**
	 * Convert the given object read from the bean property to a type supported
	 * by the Sapphire property system (eg String for value properties, list for
	 * list properties, etc).
	 * 
	 * @param value
	 *            Value read from the bean property
	 * @param property
	 *            Sapphire property being read
	 * @param beanProperty
	 *            Bean property that was used to read the value
	 * @return Converted value
	 */
	protected abstract T convertTo(Object value, P property, BeanProperty beanProperty);

	/**
	 * Convert the given value from Sapphire to an object to be used for setting
	 * on the bean property via reflection. The returned type must support the
	 * bean property's setter type.
	 * 
	 * @param value
	 *            Value to convert
	 * @param property
	 *            Sapphire property that was set
	 * @param beanProperty
	 *            Bean property that will be used to set the returned value
	 * @return Converted value, <code>null</code> if the setter shouldn't be
	 *         invoked
	 */
	protected abstract Conversion convertFrom(T value, P property, BeanProperty beanProperty);

	protected BeanProperty getBeanProperty()
	{
		return beanProperty;
	}

	@Override
	public T get(E object, P property)
	{
		initBeanPropertyIfRequired(object, property);
		if (beanProperty == null)
		{
			return null;
		}

		Object value;
		try
		{
			value = beanProperty.get();
		}
		catch (Exception e)
		{
			logger.error("Error invoking '" + property.getName() + "' property getter", e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}

		return convertTo(value, property, beanProperty);
	}

	@Override
	public void set(T value, E object, P property)
	{
		initBeanPropertyIfRequired(object, property);
		if (beanProperty == null || beanProperty.isReadOnly())
		{
			return;
		}

		Conversion conversion = convertFrom(value, property, beanProperty);
		if (conversion == null)
		{
			return;
		}

		try
		{
			beanProperty.set(conversion.result);
		}
		catch (Exception e)
		{
			logger.error("Error invoking '" + property.getName() + "' property setter", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private void initBeanPropertyIfRequired(E object, P property)
	{
		if (!beanPropertyInited)
		{
			//use a flag instead of a null check so that, if an exception occurs during instatiation, it is not retried
			beanPropertyInited = true;
			try
			{
				beanProperty =
						new BeanProperty(object, property, property.getAnnotation(ReadOnly.class) != null
								|| property instanceof ListProperty);
			}
			catch (IntrospectionException e)
			{
				logger.error("Error initializing bean property", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Helper class returned from the
	 * {@link AbstractBeanPropertyBinder#convertFrom} method.
	 */
	protected static class Conversion
	{
		public final Object result;

		public Conversion(Object result)
		{
			this.result = result;
		}
	}
}
