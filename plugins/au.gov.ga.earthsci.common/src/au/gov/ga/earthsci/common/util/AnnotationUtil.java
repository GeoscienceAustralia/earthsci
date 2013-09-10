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
package au.gov.ga.earthsci.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class with methods for getting annotations from super-interfaces and
 * super-methods.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AnnotationUtil
{
	/**
	 * Return the provided Class' annotation for the specified type if such an
	 * annotation is present, else null. Searches super-interfaces, and returns
	 * the first annotation found.
	 * 
	 * @param type
	 *            Class to test
	 * @param annotationClass
	 *            The Class object corresponding to the annotation type
	 * @return The annotation for the specified annotation type if present on
	 *         the given Class, or any of its implemented interfaces, else null
	 * @see Class#getAnnotation(Class)
	 */
	public static <T extends Annotation> T getAnnotation(Class<?> type, Class<T> annotationClass)
	{
		T t = type.getAnnotation(annotationClass);
		if (t != null)
		{
			return t;
		}
		for (Class<?> i : type.getInterfaces())
		{
			if ((t = getAnnotation(i, annotationClass)) != null)
			{
				return t;
			}
		}
		return null;
	}

	/**
	 * Return the provided Method's annotation for the specified type if such an
	 * annotation is present, else null. Also searches super-classes and
	 * super-interfaces for a matching method signature with the annotation, and
	 * returns the first annotation found.
	 * 
	 * @param method
	 *            Method to test
	 * @param annotationClass
	 *            The Class object corresponding to the annotation type
	 * @return The annotation for the specified annotation type if present on
	 *         the given Method, or any super-methods, else null.
	 * @see Method#getAnnotation(Class)
	 */
	public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass)
	{
		T t = method.getAnnotation(annotationClass);
		if (t != null)
		{
			return t;
		}

		Class<?> type = method.getDeclaringClass();
		for (Class<?> i : type.getInterfaces())
		{
			try
			{
				Method m = i.getDeclaredMethod(method.getName(), method.getParameterTypes());
				if (m.getReturnType().equals(method.getReturnType()) && (t = getAnnotation(m, annotationClass)) != null)
				{
					return t;
				}
			}
			catch (NoSuchMethodException e)
			{
			}
		}
		return null;
	}

	/**
	 * Return the provided Field's annotation for the specified type if such an
	 * annotation is present, else null.
	 * 
	 * @param field
	 *            Field to test
	 * @param annotationClass
	 *            The Class object corresponding to the annotation type
	 * @return The annotation for the specified annotation type if present on
	 *         the given Field, else null.
	 * @see Field#getAnnotation(Class)
	 */
	public static <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClass)
	{
		return field.getAnnotation(annotationClass);
	}

	/**
	 * Return an array of {@link Method}s that are annotated with the specified
	 * type. Also searches for annotated methods in the given type's class
	 * hierarchy, including both superclasses and implemented interfaces.
	 * 
	 * @param type
	 *            Class to search for annotated methods
	 * @param annotationClass
	 *            The Class object corresponding to the annotation type
	 * @return Array of methods annotated by the specified annotation type,
	 *         including those declared in super-classes and implemented
	 *         interfaces.
	 */
	public static <T extends Annotation> Method[] getAnnotatedMethods(Class<?> type, Class<T> annotationClass)
	{
		List<Method> methods = new ArrayList<Method>();
		addAnnotatedMethodsToList(type, annotationClass, methods);
		return methods.toArray(new Method[methods.size()]);
	}

	/**
	 * Add methods declared in the given Class object that are annotated with
	 * the specified annotation type to the given list. Also searches
	 * super-classes and implemented interfaces.
	 * 
	 * @param type
	 * @param annotationClass
	 * @param methodList
	 */
	protected static <T extends Annotation> void addAnnotatedMethodsToList(Class<?> type, Class<T> annotationClass,
			List<Method> methodList)
	{
		if (type == null)
		{
			return;
		}
		Method[] methods = type.getDeclaredMethods();
		for (Method method : methods)
		{
			if (getAnnotation(method, annotationClass) != null)
			{
				methodList.add(method);
			}
		}
		addAnnotatedMethodsToList(type.getSuperclass(), annotationClass, methodList);
		for (Class<?> i : type.getInterfaces())
		{
			addAnnotatedMethodsToList(i, annotationClass, methodList);
		}
	}

	/**
	 * Return an array of {@link Fields}s that are annotated with the specified
	 * type. Also searches fields declared by classes in the given type's class
	 * hierarchy (ie super-classes).
	 * 
	 * @param type
	 *            Class to search for annotated fields
	 * @param annotationClass
	 *            The Class object corresponding to the annotation type
	 * @return Array of fields annotated by the specified annotation type,
	 *         including those declared in super-classes.
	 */
	public static <T extends Annotation> Field[] getAnnotatedFields(Class<?> type, Class<T> annotationClass)
	{
		List<Field> fields = new ArrayList<Field>();
		addAnnotatedFieldsToList(type, annotationClass, fields);
		return fields.toArray(new Field[fields.size()]);
	}

	/**
	 * Add fields declared in the given Class object that are annotated with the
	 * specified annotation type to the given list. Also searches super-classes.
	 * 
	 * @param type
	 * @param annotationClass
	 * @param fieldList
	 */
	protected static <T extends Annotation> void addAnnotatedFieldsToList(Class<?> type, Class<T> annotationClass,
			List<Field> fieldList)
	{
		if (type == null)
		{
			return;
		}
		Field[] fields = type.getDeclaredFields();
		for (Field field : fields)
		{
			if (getAnnotation(field, annotationClass) != null)
			{
				fieldList.add(field);
			}
		}
		addAnnotatedFieldsToList(type.getSuperclass(), annotationClass, fieldList);
	}
}
