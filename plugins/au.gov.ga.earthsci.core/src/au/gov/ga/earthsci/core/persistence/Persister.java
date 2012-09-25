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
package au.gov.ga.earthsci.core.persistence;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import au.gov.ga.earthsci.core.util.AnnotationUtil;
import au.gov.ga.earthsci.core.util.StringInstantiable;
import au.gov.ga.earthsci.core.util.Util;
import au.gov.ga.earthsci.core.util.XmlUtil;

/**
 * Persists annotated {@link Exportable} types.
 * 
 * @see Exportable
 * @see Persistant
 * @see Adapter
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class Persister
{
	protected final static String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$
	protected final static String NULL_ATTRIBUTE = "null"; //$NON-NLS-1$
	protected final static String DEFAULT_ARRAY_ELEMENT_NAME = "element"; //$NON-NLS-1$

	protected final Map<String, Class<?>> nameToExportable = new HashMap<String, Class<?>>();
	protected final Map<Class<?>, String> exportableToName = new HashMap<Class<?>, String>();
	protected final Map<Class<?>, IPersistantAdapter<?>> adapters = new HashMap<Class<?>, IPersistantAdapter<?>>();

	/**
	 * Register a name for a given {@link Exportable} type. This name is used
	 * for the top level XML element name (instead of the canonical class name)
	 * when persisting objects of this type.
	 * 
	 * @param type
	 *            Class of the type to name
	 * @param name
	 *            XML element name to use when persisting type objects
	 */
	public void registerNamedExportable(Class<?> type, String name)
	{
		assertIsExportable(type);
		if (Util.isEmpty(name))
		{
			throw new IllegalArgumentException("name must not be empty"); //$NON-NLS-1$
		}
		nameToExportable.put(name, type);
		exportableToName.put(type, name);
	}

	/**
	 * Unregister a named {@link Exportable} type.
	 * 
	 * @param type
	 *            Type to unregister
	 * @see #registerNamedExportable(Class, String)
	 */
	public void unregisterNamedExportable(Class<?> type)
	{
		String name = exportableToName.remove(type);
		nameToExportable.remove(name);
	}

	/**
	 * Unregister a named {@link Exportable} name.
	 * 
	 * @param name
	 *            Name to unregister
	 * @see #registerNamedExportable(Class, String)
	 */
	public void unregisterNamedExportable(String name)
	{
		Class<?> type = nameToExportable.remove(name);
		exportableToName.remove(type);
	}

	/**
	 * Register an {@link IPersistantAdapter} to use when persisting objects of
	 * the given type. This overrides any {@link Adapter} annotations for
	 * fields/methods of this type.
	 * 
	 * @param type
	 *            Type that the adapter supports
	 * @param adapter
	 *            Adapter used for persisting type objects
	 */
	public <E> void registerAdapter(Class<E> type, IPersistantAdapter<E> adapter)
	{
		adapters.put(type, adapter);
	}

	/**
	 * Unregister the {@link IPersistantAdapter} registered for the given type.
	 * 
	 * @param type
	 *            Type of {@link IPersistantAdapter} to unregister
	 * @see #registerAdapter(Class, IPersistantAdapter)
	 */
	public void unregisterAdapter(Class<?> type)
	{
		adapters.remove(type);
	}

	/**
	 * Save the given {@link Exportable} object to XML under the given parent.
	 * 
	 * @param o
	 *            Object to save/persist
	 * @param parent
	 *            XML element to save inside
	 * @param context
	 */
	public void save(Object o, Element parent, URI context)
	{
		if (o == null)
		{
			throw new NullPointerException("Object cannot be null"); //$NON-NLS-1$
		}
		if (parent == null)
		{
			throw new NullPointerException("Parent element cannot be null"); //$NON-NLS-1$
		}

		assertIsExportable(o.getClass());
		String elementName = getNameFromType(o.getClass());

		Element element = parent.getOwnerDocument().createElement(elementName);
		parent.appendChild(element);

		persistMethods(o, element, context);
		persistFields(o, element, context);
	}

	/**
	 * Persist the {@link Persistant} methods of the given object.
	 * 
	 * @param o
	 *            Object whose method values should be persisted
	 * @param element
	 *            XML element to save inside
	 * @param context
	 */
	protected void persistMethods(Object o, Element element, URI context)
	{
		Method[] methods = AnnotationUtil.getAnnotatedMethods(o.getClass(), Persistant.class);
		for (Method method : methods)
		{
			method.setAccessible(true);
			Persistant persistant = AnnotationUtil.getAnnotation(method, Persistant.class);
			String name = checkAndGetPersistantName(method, persistant);
			Object value;
			try
			{
				value = method.invoke(o);
			}
			catch (Exception e)
			{
				throw new IllegalStateException(e);
			}

			Adapter adapter = AnnotationUtil.getAnnotation(method, Adapter.class);
			persist(value, method.getReturnType(), name, element, context, persistant, adapter);
		}
	}

	/**
	 * Persist the {@link Persistant} fields of the given object.
	 * 
	 * @param o
	 *            Object whose fields should be persisted
	 * @param element
	 *            XML element to save inside
	 * @param context
	 */
	protected void persistFields(Object o, Element element, URI context)
	{
		Field[] fields = AnnotationUtil.getAnnotatedFields(o.getClass(), Persistant.class);
		for (Field field : fields)
		{
			field.setAccessible(true);
			Persistant persistant = AnnotationUtil.getAnnotation(field, Persistant.class);
			String name = checkAndGetPersistantName(field, persistant);
			Object value;
			try
			{
				value = field.get(o);
			}
			catch (Exception e)
			{
				throw new IllegalStateException(e);
			}

			Adapter adapter = AnnotationUtil.getAnnotation(field, Adapter.class);
			persist(value, field.getType(), name, element, context, persistant, adapter);
		}
	}

	/**
	 * Persist a value into an element (or attribute) with the given name.
	 * <p/>
	 * If the type of the value is a subclass of the given baseType, then the
	 * classname is also persisted. This allows the {@link Persister} to know
	 * what type to instantiate when loading.
	 * 
	 * @param value
	 *            Value to persist
	 * @param baseType
	 *            Type specified by the method/field (can be null)
	 * @param name
	 *            XML element (or attribute) name to save to
	 * @param element
	 *            XML element to save inside
	 * @param context
	 * @param persistant
	 *            Field/method's {@link Persistant} annotation
	 * @param adapter
	 *            Field/method's {@link Adapter} annotation
	 */
	protected void persist(Object value, Class<?> baseType, String name, Element element, URI context,
			Persistant persistant, Adapter adapter)
	{
		Element nameElement = element.getOwnerDocument().createElement(name);
		element.appendChild(nameElement);

		//if the value is null, mark it as such with an attribute on the element, and return
		if (value == null)
		{
			nameElement.setAttribute(NULL_ATTRIBUTE, Boolean.TRUE.toString());
			return;
		}

		IPersistantAdapter<?> persistantAdapter = getAdapter(value.getClass(), adapter);
		boolean isExportable = AnnotationUtil.getAnnotation(value.getClass(), Exportable.class) != null;

		//if the value type isn't the same as the type specified by the field/method, and
		//it isn't a boxed version, then save the type as an attribute on the element
		boolean classNameSaved = false;
		if (!value.getClass().equals(baseType))
		{
			boolean boxed =
					baseType != null && baseType.isPrimitive()
							&& Util.primitiveClassToBoxed(baseType).equals(value.getClass());
			if (!boxed && !(persistantAdapter == null && isExportable))
			{
				nameElement.setAttribute(TYPE_ATTRIBUTE, getNameFromType(value.getClass()));
				classNameSaved = true;
			}
		}

		//If the value is an array or Collection, save each element as a separate XML element
		if (value.getClass().isArray() || value instanceof Collection<?>)
		{
			if (persistant.attribute())
			{
				throw new IllegalArgumentException("Array or collection Persistant cannot be an attribute"); //$NON-NLS-1$
			}

			String arrayElementName = getArrayElementName(persistant);

			if (value.getClass().isArray())
			{
				for (int i = 0; i < Array.getLength(value); i++)
				{
					Object arrayElement = Array.get(value, i);
					Class<?> componentType = baseType == null ? null : baseType.getComponentType();
					persist(arrayElement, componentType, arrayElementName, nameElement, context, persistant, adapter);
				}
			}
			else
			{
				Collection<?> collection = (Collection<?>) value;
				for (Object collectionElement : collection)
				{
					persist(collectionElement, null, arrayElementName, nameElement, context, persistant, adapter);
				}
			}
			return;
		}

		if (persistantAdapter != null)
		{
			//if there's a IPersistantAdapter for this object's type, use it to create the XML
			@SuppressWarnings("unchecked")
			IPersistantAdapter<Object> objectAdapter = (IPersistantAdapter<Object>) persistantAdapter;
			objectAdapter.toXML(value, nameElement, context);
		}
		else if (isExportable)
		{
			//if the object is itself exportable, recurse
			save(value, nameElement, context);
		}
		else
		{
			//once here, the only objects supported for persistance are those that are StringInstantiable
			assertIsStringInstantiable(value.getClass());
			String stringValue = StringInstantiable.toString(value);

			if (persistant.attribute() && !classNameSaved)
			{
				element.removeChild(nameElement);
				element.setAttribute(name, stringValue);
			}
			else
			{
				Text text = nameElement.getOwnerDocument().createTextNode(stringValue);
				nameElement.appendChild(text);
			}
		}
	}

	/**
	 * Load an {@link Exportable} object from an XML element.
	 * 
	 * @param element
	 *            Element to load from
	 * @param context
	 * @return New object loaded from XML
	 */
	public Object load(Element element, URI context)
	{
		if (element == null)
		{
			throw new NullPointerException("Element cannot be null"); //$NON-NLS-1$
		}

		Class<?> c = getTypeFromName(element.getTagName());
		assertIsExportable(c);
		Constructor<?> constructor = null;
		try
		{
			constructor = c.getDeclaredConstructor();
		}
		catch (NoSuchMethodException e)
		{
			//impossible; already checked
		}
		constructor.setAccessible(true);

		Object o;
		try
		{
			o = constructor.newInstance();
		}
		catch (Exception e)
		{
			throw new IllegalStateException(e);
		}

		unpersistMethods(o, element, context);
		unpersistFields(o, element, context);

		return o;
	}

	/**
	 * Unpersist the {@link Persistant} methods on the given object from an XML
	 * element.
	 * 
	 * @param o
	 *            Object to unpersist methods to
	 * @param element
	 *            XML element to unpersist
	 * @param context
	 */
	protected void unpersistMethods(Object o, Element element, URI context)
	{
		Method[] methods = AnnotationUtil.getAnnotatedMethods(o.getClass(), Persistant.class);
		for (Method method : methods)
		{
			method.setAccessible(true);
			Persistant persistant = AnnotationUtil.getAnnotation(method, Persistant.class);
			String name = checkAndGetPersistantName(method, persistant);
			Class<?> type = method.getReturnType();
			Method setter = getSetter(o.getClass(), name, type, persistant);

			Adapter adapter = AnnotationUtil.getAnnotation(method, Adapter.class);
			Object value = unpersist(0, element, name, type, context, persistant, adapter);
			try
			{
				setter.invoke(o, value);
			}
			catch (Exception e)
			{
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * Unpersist the {@link Persistant} fields for the given object from an XML
	 * element.
	 * 
	 * @param o
	 *            Object to unpersist fields to
	 * @param element
	 *            XML element to unpersist
	 * @param context
	 */
	protected void unpersistFields(Object o, Element element, URI context)
	{
		Field[] fields = AnnotationUtil.getAnnotatedFields(o.getClass(), Persistant.class);
		for (Field field : fields)
		{
			field.setAccessible(true);
			Persistant persistant = AnnotationUtil.getAnnotation(field, Persistant.class);
			String name = checkAndGetPersistantName(field, persistant);
			Class<?> type = field.getType();

			Adapter adapter = AnnotationUtil.getAnnotation(field, Adapter.class);
			Object value = unpersist(0, element, name, type, context, persistant, adapter);
			try
			{
				field.set(o, value);
			}
			catch (Exception e)
			{
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * Load/unpersist an object from an XML element (or attribute) with the
	 * given name.
	 * 
	 * @param index
	 *            Index of the element within the list of direct child elements
	 *            of parent with the given tag name
	 * @param parent
	 *            XML element in which to search for child elements (or an
	 *            attribute) with the given tag name
	 * @param name
	 *            XML element (or attribute) name that stores the value to
	 *            unpersist
	 * @param type
	 *            Type to unpersist to (can be null if the element has an
	 *            attribute which specifies the type)
	 * @param context
	 * @param persistant
	 *            Field/method's {@link Persistant} annotation
	 * @param adapter
	 *            Field/method's {@link Adapter} annotation
	 * @return New object loaded from XML
	 */
	protected Object unpersist(int index, Element parent, String name, Class<?> type, URI context,
			Persistant persistant, Adapter adapter)
	{
		//get the index'th named element of parent
		Element element = XmlUtil.getChildElementByTagName(index, name, parent);
		Attr attribute = parent.getAttributeNode(name);

		if (element != null)
		{
			//if the null attribute is set, return null
			String nullAttribute = element.getAttribute(NULL_ATTRIBUTE);
			if (new Boolean(nullAttribute))
			{
				return null;
			}

			//the className attribute can override the type (to support subclasses)
			String classNameAttribute = element.getAttribute(TYPE_ATTRIBUTE);
			if (!Util.isEmpty(classNameAttribute))
			{
				//for each [] at the end of the class name, increment the array depth
				int arrayDepth = 0;
				while (classNameAttribute.endsWith("[]")) //$NON-NLS-1$
				{
					classNameAttribute = classNameAttribute.substring(0, classNameAttribute.length() - 2);
					arrayDepth++;
				}
				//load the class from the name
				type = getTypeFromName(classNameAttribute);
				//make the type an array type with the correct depth
				while (arrayDepth > 0)
				{
					type = Array.newInstance(type, 0).getClass();
					arrayDepth--;
				}
			}
		}

		if (type == null)
		{
			Element firstChild = element == null ? null : XmlUtil.getFirstChildElement(element);
			if (firstChild == null)
			{
				throw new NullPointerException("Unpersist type is null"); //$NON-NLS-1$
			}

			//if the type isn't defined, assume the first child element is exportable
			type = getTypeFromName(firstChild.getTagName());
			assertIsExportable(type);
		}

		//handle array/collection types
		if (type.isArray() || Collection.class.isAssignableFrom(type))
		{
			if (element == null)
			{
				throw new NullPointerException("Could not find element for name: " + name); //$NON-NLS-1$
			}

			//calculate the array length from the number of child elements
			String arrayElementName = getArrayElementName(persistant);
			int length = XmlUtil.getCountChildElementsByTagName(arrayElementName, element);

			if (type.isArray())
			{
				//create an array and unpersist the elements into it
				Object array = Array.newInstance(type.getComponentType(), length);
				for (int i = 0; i < length; i++)
				{
					//recurse
					Object o =
							unpersist(i, element, arrayElementName, type.getComponentType(), context, persistant,
									adapter);
					Array.set(array, i, o);
				}
				return array;
			}
			else
			{
				//instantiate the collection impementation
				String collectionClassName = element.getAttribute(TYPE_ATTRIBUTE);
				if (Util.isEmpty(collectionClassName))
				{
					throw new IllegalStateException("Collection class not specified"); //$NON-NLS-1$
				}
				Collection<Object> collection;
				try
				{
					Class<?> collectionType = getTypeFromName(collectionClassName);
					Constructor<?> constructor = collectionType.getConstructor();
					@SuppressWarnings("unchecked")
					Collection<Object> objectCollection = (Collection<Object>) constructor.newInstance();
					collection = objectCollection;
				}
				catch (Exception e)
				{
					throw new IllegalStateException("Error instantiating collection", e); //$NON-NLS-1$
				}
				//unpersist the collection's elements
				for (int i = 0; i < length; i++)
				{
					//recurse
					//we don't know the type for collection elements (they must specify the className attribute)
					Object o = unpersist(i, element, arrayElementName, null, context, persistant, adapter);
					collection.add(o);
				}
				return collection;
			}
		}

		String stringValue = null;
		IPersistantAdapter<?> persistantAdapter = getAdapter(type, adapter);
		if (element != null)
		{
			if (persistantAdapter != null)
			{
				//if there's a IPersistantAdapter for this object's type, use it to load the XML
				@SuppressWarnings("unchecked")
				IPersistantAdapter<Object> objectAdapter = (IPersistantAdapter<Object>) persistantAdapter;
				return objectAdapter.fromXML(element, context);
			}
			else if (AnnotationUtil.getAnnotation(type, Exportable.class) != null)
			{
				//if the type is exportable, recurse
				Element child = XmlUtil.getFirstChildElement(element);
				return load(child, context);
			}
			else
			{
				Text text = XmlUtil.getFirstChildText(element);
				stringValue = text.getData();
			}
		}
		else if (attribute != null)
		{
			stringValue = attribute.getValue();
		}

		//once here, the only objects supported for unpersistance are those that are StringInstantiable
		if (stringValue != null)
		{
			assertIsStringInstantiable(type);
			return StringInstantiable.newInstance(stringValue, type);
		}

		throw new IllegalStateException("Could not unpersist Persistable: " + name); //$NON-NLS-1$
	}

	/**
	 * Check that the method is persistable (no parameters, and a non-void
	 * return type), and calculate the element/attribute name to save to.
	 * 
	 * @param method
	 *            Method that will be persisted
	 * @param persistant
	 *            Method's {@link Persistant} annotation
	 * @return Element/attribute name for the given method
	 */
	protected String checkAndGetPersistantName(Method method, Persistant persistant)
	{
		if (method.getParameterTypes().length > 0)
		{
			throw new IllegalArgumentException("Cannot persist parameterized methods: " + method); //$NON-NLS-1$
		}
		if (void.class.equals(method.getReturnType()))
		{
			throw new IllegalArgumentException("Cannot persist methods with no return type: " + method); //$NON-NLS-1$
		}
		String name = persistant.name();
		if (Util.isEmpty(name))
		{
			name = removeGetter(method);
		}
		if (Util.isEmpty(name))
		{
			throw new IllegalArgumentException("Could not determine name for method: " + method); //$NON-NLS-1$
		}
		return name;
	}

	/**
	 * Calculate the element/attribute name to save the given field to.
	 * 
	 * @param field
	 *            Field that will be persisted
	 * @param persistant
	 *            Field's {@link Persistant} annotation
	 * @return Element/attribute name for the given field
	 */
	protected String checkAndGetPersistantName(Field field, Persistant persistant)
	{
		String name = persistant.name();
		if (Util.isEmpty(name))
		{
			name = field.getName();
		}
		if (Util.isEmpty(name))
		{
			throw new IllegalArgumentException("Could not determine name for field: " + field); //$NON-NLS-1$
		}
		return name;
	}

	/**
	 * Remove the 'get' (or 'is' for boolean return types) method name prefix
	 * (if it exists), and lowercase the first character (if the prefix was
	 * present).
	 * 
	 * @param method
	 *            Method from which to remove the 'get'/'is' prefix from
	 * @return Method name without the 'get'/'is' prefix
	 */
	protected String removeGetter(Method method)
	{
		String name = method.getName();
		if (boolean.class.equals(method.getReturnType()) && name.length() > 2 && name.startsWith("is")) //$NON-NLS-1$
		{
			name = name.substring(2, 3).toLowerCase() + name.substring(3);
		}
		else if (name.length() > 3 && name.startsWith("get")) //$NON-NLS-1$
		{
			name = name.substring(3, 4).toLowerCase() + name.substring(4);
		}
		return name;
	}

	/**
	 * Find the setter method in the class for the given property name. If the
	 * {@link Persistant} annotation defines the setter property, then return
	 * the method with that name.
	 * 
	 * @param c
	 *            Class in which to find the setter method
	 * @param name
	 *            Name of the property to find a setter for (ignored the
	 *            {@link Persistant} annotation defines the setter)
	 * @param parameterType
	 *            Type that the setter method should have a single parameter for
	 * @param persistant
	 *            {@link Persistant} annotation for the corresponding getter
	 * @return
	 */
	protected Method getSetter(Class<?> c, String name, Class<?> parameterType, Persistant persistant)
	{
		if (!Util.isEmpty(persistant.setter()))
		{
			try
			{
				return getSetterMethod(c, persistant.setter(), parameterType);
			}
			catch (NoSuchMethodException e)
			{
				throw new IllegalArgumentException("Cannot find matching Persistant setter: " + persistant.setter() //$NON-NLS-1$
						+ " in class " + c); //$NON-NLS-1$
			}
		}

		if (Util.isEmpty(name))
		{
			throw new IllegalArgumentException("Persistant setter name is empty"); //$NON-NLS-1$
		}

		//first find a method with the property name and a 'set' prefix (ie if property = name, setter = setName)
		String setName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1); //$NON-NLS-1$
		try
		{
			return getSetterMethod(c, setName, parameterType);
		}
		catch (NoSuchMethodException e)
		{
		}

		//next try and find a method that is just named the property name
		try
		{
			return getSetterMethod(c, name, parameterType);
		}
		catch (NoSuchMethodException e)
		{
		}

		throw new IllegalArgumentException("Cannot find matching Persistant setter: " + setName + " in class " + c); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Find a setter method declared in a class with the given name. If not
	 * found in the class, recurses to search the class' superclasses and
	 * implemented interfaces.
	 * 
	 * @param c
	 *            Class to search for the setter method
	 * @param name
	 *            Name of the setter method
	 * @param parameterType
	 *            Single parameter type that the setter accepts
	 * @return Setter method
	 * @throws NoSuchMethodException
	 *             If a corresponding setter method could not be found
	 */
	protected Method getSetterMethod(Class<?> c, String name, Class<?> parameterType) throws NoSuchMethodException
	{
		NoSuchMethodException noSuchMethodException;
		try
		{
			Method m = c.getDeclaredMethod(name, parameterType);
			m.setAccessible(true);
			return m;
		}
		catch (NoSuchMethodException e)
		{
			noSuchMethodException = e;
		}
		//search super class
		if (c.getSuperclass() != null)
		{
			try
			{
				return getSetterMethod(c.getSuperclass(), name, parameterType);
			}
			catch (NoSuchMethodException e)
			{
			}
		}
		//search interfaces
		for (Class<?> i : c.getInterfaces())
		{
			try
			{
				return getSetterMethod(i, name, parameterType);
			}
			catch (NoSuchMethodException e)
			{
			}
		}
		//could not be found, throw the original exception
		throw noSuchMethodException;
	}

	/**
	 * Calculate the XML element name to use for array elements. If the
	 * {@link Persistant} attribute defines an element name, return that,
	 * otherwise return the default.
	 * 
	 * @param persistant
	 *            {@link Persistant} annotation that may define the element name
	 * @return The XML element name to use for array elements
	 */
	protected String getArrayElementName(Persistant persistant)
	{
		String arrayElementName = persistant.elementName();
		if (Util.isEmpty(arrayElementName))
		{
			arrayElementName = DEFAULT_ARRAY_ELEMENT_NAME;
		}
		return arrayElementName;
	}

	/**
	 * Get the {@link IPersistantAdapter} used to persist the given type to XML.
	 * If the type already has a registered adapter (from
	 * {@link #registerAdapter(Class, IPersistantAdapter)}), return that;
	 * otherwise, if the {@link Adapter} annotation is non-null, instantiate and
	 * return an object of the class defined in the annotation.
	 * 
	 * @param type
	 *            Type for which to get an adapter for
	 * @param adapter
	 *            Adapter annotation
	 * @return {@link IPersistantAdapter} used to persist the given type
	 */
	protected IPersistantAdapter<?> getAdapter(Class<?> type, Adapter adapter)
	{
		IPersistantAdapter<?> persistantAdapter = adapters.get(type);
		if (persistantAdapter == null && adapter != null)
		{
			Class<? extends IPersistantAdapter<?>> adapterClass = adapter.value();
			if (adapterClass != null)
			{
				try
				{
					Constructor<? extends IPersistantAdapter<?>> constructor = adapterClass.getConstructor();
					constructor.setAccessible(true);
					persistantAdapter = constructor.newInstance();
				}
				catch (Exception e)
				{
					throw new IllegalStateException(e);
				}
			}
		}
		return persistantAdapter;
	}

	/**
	 * Calculate the type for the given name. If the name has been registered
	 * using {@link #registerNamedExportable(Class, String)}, that type is
	 * returned. Otherwise {@link Class#forName(String)} is used.
	 * 
	 * @param name
	 *            Name to calculate type for
	 * @return Type for name
	 */
	protected Class<?> getTypeFromName(String name)
	{
		Class<?> c = nameToExportable.get(name);
		if (c == null)
		{
			try
			{
				c = Class.forName(name);
			}
			catch (ClassNotFoundException e)
			{
				throw new IllegalArgumentException("Could not determine type for name: " + name); //$NON-NLS-1$
			}
		}
		return c;
	}

	/**
	 * Calculate the name for the given type. If the type is marked as
	 * {@link Exportable} and a named exportable has been registered using
	 * {@link #registerNamedExportable(Class, String)}, that name is returned.
	 * Otherwise the canonical class name is returned.
	 * 
	 * @param type
	 *            Type to calculate name for
	 * @return Name of type
	 */
	protected String getNameFromType(Class<?> type)
	{
		String name = exportableToName.get(type);
		if (Util.isEmpty(name))
		{
			if (type.isArray())
			{
				return getNameFromType(type.getComponentType()) + "[]"; //$NON-NLS-1$
			}
			name = type.getCanonicalName();
		}
		if (Util.isEmpty(name))
		{
			throw new IllegalArgumentException("Could not determine name for type: " + type); //$NON-NLS-1$
		}
		return name;
	}

	/**
	 * Throws an {@link IllegalArgumentException} if the given type is not
	 * {@link Exportable} (or doesn't have a default constructor).
	 * 
	 * @param type
	 *            Type to test
	 */
	protected void assertIsExportable(Class<?> type)
	{
		if (AnnotationUtil.getAnnotation(type, Exportable.class) == null)
		{
			throw new IllegalArgumentException(type + " is not marked " + Exportable.class); //$NON-NLS-1$
		}
		try
		{
			type.getDeclaredConstructor();
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException(type + " does not have a default constructor"); //$NON-NLS-1$
		}
	}

	/**
	 * Throws an {@link IllegalArgumentException} if the given type is not
	 * {@link StringInstantiable#isInstantiable(Class)}.
	 * 
	 * @param type
	 *            Type to test
	 */
	protected void assertIsStringInstantiable(Class<?> type)
	{
		if (!StringInstantiable.isInstantiable(type))
		{
			throw new IllegalArgumentException("Cannot persist type: " + type); //$NON-NLS-1$
		}
	}
}
