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
package au.gov.ga.earthsci.intent.util;

import java.lang.annotation.Annotation;

import javax.inject.Scope;
import javax.inject.Singleton;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;

/**
 * Wrapper around the {@link ContextInjectionFactory}'s methods that ensures
 * thread safety.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ContextInjectionFactoryThreadSafe
{
	private final static Object mutex = new Object();

	/**
	 * Injects a context into a domain object. See the class comment for details
	 * on the injection algorithm that is used.
	 * 
	 * @param object
	 *            The object to perform injection on
	 * @param context
	 *            The context to obtain injected values from
	 * @throws InjectionException
	 *             if an exception occurred while performing this operation
	 */
	static public void inject(Object object, IEclipseContext context) throws InjectionException
	{
		synchronized (mutex)
		{
			ContextInjectionFactory.inject(object, context);
		}
	}

	/**
	 * Call a method, injecting the parameters from the context.
	 * <p>
	 * If no matching method is found on the class, an InjectionException will
	 * be thrown.
	 * </p>
	 * 
	 * @param object
	 *            The object to perform injection on
	 * @param qualifier
	 *            the annotation tagging method to be called
	 * @param context
	 *            The context to obtain injected values from
	 * @return the return value of the method call, might be <code>null</code>
	 * @throws InjectionException
	 *             if an exception occurred while performing this operation
	 */
	static public Object invoke(Object object, Class<? extends Annotation> qualifier, IEclipseContext context)
			throws InjectionException
	{
		synchronized (mutex)
		{
			return ContextInjectionFactory.invoke(object, qualifier, context);
		}
	}

	/**
	 * Call a method, injecting the parameters from the context.
	 * <p>
	 * If no matching method is found on the class, the defaultValue will be
	 * returned.
	 * </p>
	 * 
	 * @param object
	 *            The object to perform injection on
	 * @param qualifier
	 *            the annotation tagging method to be called
	 * @param context
	 *            The context to obtain injected values from
	 * @param defaultValue
	 *            A value to be returned if the method cannot be called, might
	 *            be <code>null</code>
	 * @return the return value of the method call, might be <code>null</code>
	 * @throws InjectionException
	 *             if an exception occurred while performing this operation
	 */
	static public Object invoke(Object object, Class<? extends Annotation> qualifier, IEclipseContext context,
			Object defaultValue) throws InjectionException
	{
		synchronized (mutex)
		{
			return ContextInjectionFactory.invoke(object, qualifier, context, defaultValue);
		}
	}

	/**
	 * Call a method, injecting the parameters from two contexts. This method is
	 * useful when the method needs to receive some values not present in the
	 * context. In this case a local context can be created and populated with
	 * additional values.
	 * <p>
	 * If values for the same key present in both the context and the local
	 * context, the values from the local context are injected.
	 * </p>
	 * <p>
	 * If no matching method is found on the class, the defaultValue will be
	 * returned.
	 * </p>
	 * 
	 * @param object
	 *            The object to perform injection on
	 * @param qualifier
	 *            the annotation tagging method to be called
	 * @param context
	 *            The context to obtain injected values from
	 * @param localContext
	 *            The context to obtain addition injected values from
	 * @param defaultValue
	 *            A value to be returned if the method cannot be called, might
	 *            be <code>null</code>
	 * @return the return value of the method call, might be <code>null</code>
	 * @throws InjectionException
	 *             if an exception occurred while performing this operation
	 */
	static public Object invoke(Object object, Class<? extends Annotation> qualifier, IEclipseContext context,
			IEclipseContext localContext, Object defaultValue) throws InjectionException
	{
		synchronized (mutex)
		{
			return ContextInjectionFactory.invoke(object, qualifier, context, localContext, defaultValue);
		}
	}

	/**
	 * Un-injects the context from the object. The un-injection requires that
	 * all injected values were marked as optional, or the un-injection will
	 * fail.
	 * 
	 * @param object
	 *            The domain object previously injected with the context
	 * @param context
	 *            The context previously injected into the object
	 * @throws InjectionException
	 *             if an exception occurred while performing this operation
	 */
	static public void uninject(Object object, IEclipseContext context) throws InjectionException
	{
		synchronized (mutex)
		{
			ContextInjectionFactory.uninject(object, context);
		}
	}

	/**
	 * Obtain an instance of the specified class and inject it with the context.
	 * <p>
	 * Class'es scope dictates if a new instance of the class will be created,
	 * or existing instance will be reused.
	 * </p>
	 * 
	 * @param clazz
	 *            The class to be instantiated
	 * @param context
	 *            The context to obtain injected values from
	 * @return an instance of the specified class
	 * @throws InjectionException
	 *             if an exception occurred while performing this operation
	 * @see Scope
	 * @see Singleton
	 */
	static public <T> T make(Class<T> clazz, IEclipseContext context) throws InjectionException
	{
		synchronized (mutex)
		{
			return ContextInjectionFactory.make(clazz, context);
		}
	}

	/**
	 * Obtain an instance of the specified class and inject it with the context.
	 * This method allows extra values that don't need to be tracked to be
	 * passed to the object using staticContext.
	 * <p>
	 * If values for the same key present in both the context and the static
	 * context, the values from the static context are injected.
	 * </p>
	 * <p>
	 * Class'es scope dictates if a new instance of the class will be created,
	 * or existing instance will be reused.
	 * </p>
	 * 
	 * @param clazz
	 *            The class to be instantiated
	 * @param context
	 *            The context to obtain injected values from
	 * @param staticContext
	 *            The context containing extra values; not tracked
	 * @return an instance of the specified class
	 * @throws InjectionException
	 *             if an exception occurred while performing this operation
	 * @see #make(Class, IEclipseContext)
	 */
	static public <T> T make(Class<T> clazz, IEclipseContext context, IEclipseContext staticContext)
			throws InjectionException
	{
		synchronized (mutex)
		{
			return ContextInjectionFactory.make(clazz, context, staticContext);
		}
	}
}
