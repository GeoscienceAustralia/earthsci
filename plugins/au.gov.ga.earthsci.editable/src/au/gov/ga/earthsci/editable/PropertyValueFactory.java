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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.editable.annotations.ElementType;
import au.gov.ga.earthsci.editable.annotations.Factory;

/**
 * Helper class that is used to instantiate a new object used as the value for a
 * property.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PropertyValueFactory
{
	/**
	 * Create a new instance of <code>T</code> that can be set for the given
	 * property on the parent object.
	 * <p/>
	 * The logic is as follows:
	 * <ol>
	 * <li>The property is checked for a {@link Factory} annotation. If found,
	 * the {@link IFactory} defined is used to create the instance.</li>
	 * <li>The property is checked for an {@link ElementType} annotation. If
	 * found, the element type is instantiated and returned.</li>
	 * <li>The type is checked for a {@link Factory} annotation. If found, the
	 * {@link IFactory} defined is used to create the instance.</li>
	 * <li>The type is checked for an {@link ElementType} annotation. If found,
	 * the element type is instantiated and returned.</li>
	 * <li>If the <code>fallbackType</code> is non-null, it is instantiated and
	 * returned.</li>
	 * <li>Otherwise <code>null</code> is returned.</li>
	 * </ol>
	 * 
	 * @param property
	 *            Property that the new instance is for
	 * @param type
	 *            Model type of the new instance
	 * @param parent
	 *            Parent object that the new instance will be set on
	 * @param fallbackType
	 *            Type of object to create if both the property and type don't
	 *            define a {@link Factory} or an {@link ElementType}
	 * @return New instance
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Object create(ModelProperty property, ModelElementType type, Object parent, Class<?> fallbackType)
			throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException,
			IllegalAccessException, InvocationTargetException
	{
		//first see if the property has factory or type annotations
		Factory factoryAnnotation = property.getAnnotation(Factory.class);
		ElementType elementTypeAnnotation = property.getAnnotation(ElementType.class);
		if (factoryAnnotation == null && elementTypeAnnotation == null)
		{
			//if the property has no relevant annotations, see if the type does
			factoryAnnotation = type.getAnnotation(Factory.class);
			elementTypeAnnotation = type.getAnnotation(ElementType.class);
		}

		if (factoryAnnotation != null)
		{
			//a factory is defined, instantiate it and use it to create a property value
			Class<? extends IFactory<?>> factoryClass = factoryAnnotation.value();

			//first find a constructor (prefer a constructor that takes the annotation
			//as a parameter, otherwise just use the empty constructor)
			Constructor<? extends IFactory<?>> constructor;
			boolean constructorTakesAnnotation;
			try
			{
				constructor = factoryClass.getConstructor(Factory.class);
				constructorTakesAnnotation = true;
			}
			catch (Exception e)
			{
				constructor = factoryClass.getConstructor();
				constructorTakesAnnotation = false;
			}
			constructor.setAccessible(true);

			//instantiate the factory
			IFactory<?> factory =
					constructorTakesAnnotation ? constructor.newInstance(factoryAnnotation) : constructor
							.newInstance();

			//use the factory to create a new property value
			Shell shell = Display.getDefault().getActiveShell();
			return factory.create(type, property, parent, shell);
		}
		else if (elementTypeAnnotation != null)
		{
			//if an element type is defined, create an instance and return
			Class<?> elementType = elementTypeAnnotation.value();
			return elementType.newInstance();
		}
		else if (fallbackType != null)
		{
			//otherwise, if the fallback type is defined, use it
			return fallbackType.newInstance();
		}
		return null;
	}
}
