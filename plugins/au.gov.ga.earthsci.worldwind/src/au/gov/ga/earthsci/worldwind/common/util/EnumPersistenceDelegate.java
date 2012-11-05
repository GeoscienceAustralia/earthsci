/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.util;

import java.beans.BeanInfo;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.IntrospectionException;
import java.beans.Introspector;

/**
 * Helper class for making enum classes persistent when serializing classes
 * containing an enum property. Each enum must be registered with the class
 * using the {@link EnumPersistenceDelegate#installFor(Enum[])} method.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EnumPersistenceDelegate extends DefaultPersistenceDelegate
{
	private static EnumPersistenceDelegate INSTANCE = new EnumPersistenceDelegate();

	/**
	 * Install the {@link EnumPersistenceDelegate} for each of the provided enum
	 * values.
	 * 
	 * @param values
	 */
	public static void installFor(Enum<?>[] values)
	{
		Class<?> declaringClass = values[0].getDeclaringClass();
		installFor(declaringClass);

		for (Enum<?> e : values)
			if (e.getClass() != declaringClass)
				installFor(e.getClass());
	}

	protected static void installFor(Class<?> enumClass)
	{
		try
		{
			BeanInfo info = Introspector.getBeanInfo(enumClass);
			info.getBeanDescriptor().setValue("persistenceDelegate", INSTANCE);
		}
		catch (IntrospectionException exception)
		{
			throw new RuntimeException("Unable to persist enumerated type " + enumClass, exception);
		}
	}

	@Override
	protected Expression instantiate(Object oldInstance, Encoder out)
	{
		Enum<?> e = (Enum<?>) oldInstance;
		return new Expression(e.getDeclaringClass(), "valueOf", new Object[] { e.name() });
	}

	@Override
	protected boolean mutatesTo(Object oldInstance, Object newInstance)
	{
		return oldInstance == newInstance;
	}
}
