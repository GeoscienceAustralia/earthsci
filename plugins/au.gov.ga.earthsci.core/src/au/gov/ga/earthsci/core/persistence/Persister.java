package au.gov.ga.earthsci.core.persistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import au.gov.ga.earthsci.core.util.StringInstantiable;
import au.gov.ga.earthsci.core.util.Util;

public class Persister
{
	protected final static String COLLECTION_ELEMENT = "collection"; //$NON-NLS-1$
	protected final static String CLASS_NAME_ATTRIBUTE = "className"; //$NON-NLS-1$
	protected final static String NULL_ATTRIBUTE = "null"; //$NON-NLS-1$

	private final Map<String, Class<?>> nameToExportable = new HashMap<String, Class<?>>();
	private final Map<Class<?>, String> exportableToName = new HashMap<Class<?>, String>();
	private final Map<Class<?>, IPersistantAdapter<?>> adapters = new HashMap<Class<?>, IPersistantAdapter<?>>();

	public void registerNamedExportable(String name, Class<?> type)
	{
		assertIsExportable(type);
		if (Util.isEmpty(name))
		{
			throw new IllegalArgumentException("name must not be empty"); //$NON-NLS-1$
		}
		nameToExportable.put(name, type);
		exportableToName.put(type, name);
	}

	public <E> void registerAdapter(Class<E> type, IPersistantAdapter<E> adapter)
	{
		adapters.put(type, adapter);
	}

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
		String elementName = exportableToName.get(o.getClass());
		if (Util.isEmpty(elementName))
		{
			elementName = o.getClass().getCanonicalName();
		}
		if (Util.isEmpty(elementName))
		{
			throw new IllegalArgumentException("Could not determine element name for object: " + o); //$NON-NLS-1$
		}

		Element element = parent.getOwnerDocument().createElement(elementName);
		parent.appendChild(element);

		persistMethods(o, element, context);
		persistFields(o, element, context);
	}

	protected void persistMethods(Object o, Element element, URI context)
	{
		Method[] methods = o.getClass().getDeclaredMethods();
		for (Method method : methods)
		{
			method.setAccessible(true);
			Persistant persistant = getAnnotation(method, Persistant.class);
			if (persistant == null)
				continue;

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

			Adapter adapter = getAnnotation(method, Adapter.class);
			persist(value, name, element, context, persistant, adapter);
		}
	}

	protected void persistFields(Object o, Element element, URI context)
	{
		Field[] fields = o.getClass().getDeclaredFields();
		for (Field field : fields)
		{
			field.setAccessible(true);
			Persistant persistant = getAnnotation(field, Persistant.class);
			if (persistant == null)
				continue;

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

			Adapter adapter = getAnnotation(field, Adapter.class);
			persist(value, name, element, context, persistant, adapter);
		}
	}

	public static void main(String[] args) throws ClassNotFoundException
	{
		System.out.println(Collection.class.isAssignableFrom(List.class));
		System.out.println(List.class.isAssignableFrom(Collection.class));

		int[][] array = new int[][] {};
		System.out.println(array.getClass());
		System.out.println(array.getClass().getSimpleName());
		System.out.println(array.getClass().getCanonicalName());
		System.out.println(Element.class.getCanonicalName());
	}

	protected void persist(Object value, String name, Element element, URI context, Persistant persistant,
			Adapter adapter)
	{
		persist(value, name, element, context, persistant, adapter, false);
	}

	protected void persist(Object value, String name, Element element, URI context, Persistant persistant,
			Adapter adapter, boolean saveClassName)
	{
		Element nameElement = element.getOwnerDocument().createElement(name);
		element.appendChild(nameElement);

		if (value == null)
		{
			nameElement.setAttribute(NULL_ATTRIBUTE, Boolean.TRUE.toString());
			return;
		}
		if (saveClassName)
		{
			nameElement.setAttribute(CLASS_NAME_ATTRIBUTE, value.getClass().getCanonicalName());
		}

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
					boolean saveElementClassName = false;
					if (arrayElement != null)
					{
						Class<?> componentType = value.getClass().getComponentType();
						saveElementClassName =
								!(componentType.equals(arrayElement.getClass()) || (componentType.isPrimitive() && Util
										.primitiveClassToBoxed(componentType).equals(arrayElement.getClass())));
					}
					persist(arrayElement, arrayElementName, nameElement, context, persistant, adapter,
							saveElementClassName);
				}
			}
			else
			{
				Collection<?> collection = (Collection<?>) value;
				nameElement.setAttribute(CLASS_NAME_ATTRIBUTE, collection.getClass().getCanonicalName());
				for (Object collectionElement : collection)
				{
					persist(collectionElement, arrayElementName, nameElement, context, persistant, adapter, true);
				}
			}
			return;
		}

		IPersistantAdapter<?> persistantAdapter = getAdapter(value.getClass(), adapter);
		if (persistantAdapter != null)
		{
			@SuppressWarnings("unchecked")
			IPersistantAdapter<Object> objectAdapter = (IPersistantAdapter<Object>) persistantAdapter;
			objectAdapter.toXML(value, nameElement, context);
		}
		else if (getAnnotation(value.getClass(), Exportable.class) != null)
		{
			//if the object is itself exportable, recurse
			save(value, nameElement, context);
		}
		else
		{
			assertIsStringInstantiable(value.getClass());
			String stringValue = StringInstantiable.toString(value);

			if (persistant.attribute())
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

	public Object load(Element element, URI context)
	{
		if (element == null)
		{
			throw new NullPointerException("Element cannot be null"); //$NON-NLS-1$
		}

		String elementName = element.getTagName();
		Class<?> c = nameToExportable.get(elementName);
		if (c == null)
		{
			try
			{
				c = Class.forName(elementName);
			}
			catch (ClassNotFoundException e)
			{
				throw new IllegalArgumentException("Could not determine class for element tag name: " + elementName); //$NON-NLS-1$
			}
		}

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

	protected void unpersistMethods(Object o, Element parent, URI context)
	{
		Method[] methods = o.getClass().getDeclaredMethods();
		for (Method method : methods)
		{
			method.setAccessible(true);
			Persistant persistant = getAnnotation(method, Persistant.class);
			if (persistant == null)
				continue;

			String name = checkAndGetPersistantName(method, persistant);
			Class<?> type = method.getReturnType();
			Method setter = getSetter(o.getClass(), name, type, persistant);

			Adapter adapter = getAnnotation(method, Adapter.class);
			Object value = unpersist(parent, name, type, context, persistant, adapter);
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

	protected void unpersistFields(Object o, Element parent, URI context)
	{
		Field[] fields = o.getClass().getDeclaredFields();
		for (Field field : fields)
		{
			field.setAccessible(true);
			Persistant persistant = getAnnotation(field, Persistant.class);
			if (persistant == null)
				continue;

			String name = checkAndGetPersistantName(field, persistant);
			Class<?> type = field.getType();

			Adapter adapter = getAnnotation(field, Adapter.class);
			Object value = unpersist(parent, name, type, context, persistant, adapter);
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

	protected Object unpersist(Element parent, String name, Class<?> type, URI context, Persistant persistant,
			Adapter adapter)
	{
		return unpersist(0, parent, name, type, context, persistant, adapter);
	}

	protected Object unpersist(int index, Element parent, String name, Class<?> type, URI context,
			Persistant persistant, Adapter adapter)
	{
		Element element = null;
		Attr attribute = null;

		NodeList nodes = parent.getElementsByTagName(name);
		if (nodes != null && nodes.getLength() > 0)
		{
			element = (Element) nodes.item(index);
		}
		else
		{
			attribute = parent.getAttributeNode(name);
		}

		if (element != null)
		{
			String nullAttribute = element.getAttribute(NULL_ATTRIBUTE);
			Boolean b = new Boolean(nullAttribute);
			if (b)
			{
				return null;
			}

			//the className attribute can override the type (for subclasses)
			String classNameAttribute = element.getAttribute(CLASS_NAME_ATTRIBUTE);
			if (!Util.isEmpty(classNameAttribute))
			{
				//TODO parse [] from class name attribute to create array types
				try
				{
					type = Class.forName(classNameAttribute);
				}
				catch (ClassNotFoundException e)
				{
					throw new IllegalStateException("Unknown class name: " + classNameAttribute); //$NON-NLS-1$
				}
			}
		}

		if (type == null)
		{
			throw new NullPointerException("Unpersist type is null"); //$NON-NLS-1$
		}

		if (type.isArray() || Collection.class.isAssignableFrom(type))
		{
			if (element == null)
			{
				throw new NullPointerException("Could not find element for name: " + name); //$NON-NLS-1$
			}

			String arrayElementName = getArrayElementName(persistant);
			NodeList arrayElementElements = element.getElementsByTagName(arrayElementName);
			int length = arrayElementElements == null ? 0 : arrayElementElements.getLength();

			if (type.isArray())
			{
				Object array = Array.newInstance(type.getComponentType(), length);
				for (int i = 0; i < length; i++)
				{
					Object o =
							unpersist(i, element, arrayElementName, type.getComponentType(), context, persistant,
									adapter);
					Array.set(array, i, o);
				}
				return array;
			}
			else
			{
				String collectionClassName = element.getAttribute(CLASS_NAME_ATTRIBUTE);
				if (Util.isEmpty(collectionClassName))
				{
					throw new IllegalStateException("Collection class not specified"); //$NON-NLS-1$
				}
				Collection<Object> collection;
				try
				{
					@SuppressWarnings("unchecked")
					Collection<Object> objectCollection =
							(Collection<Object>) Class.forName(collectionClassName).newInstance();
					collection = objectCollection;
				}
				catch (Exception e)
				{
					throw new IllegalStateException("Error instantiating collection", e); //$NON-NLS-1$
				}
				for (int i = 0; i < length; i++)
				{
					//don't know the type for collection elements; they must specify the className attribute
					Object o = unpersist(i, element, arrayElementName, null, context, persistant, adapter);
					collection.add(o);
				}
				return collection;
			}
		}

		IPersistantAdapter<?> persistantAdapter = getAdapter(type, adapter);
		if (element != null)
		{
			if (persistantAdapter != null)
			{
				@SuppressWarnings("unchecked")
				IPersistantAdapter<Object> objectAdapter = (IPersistantAdapter<Object>) persistantAdapter;
				return objectAdapter.fromXML(element, context);
			}
			else if (getAnnotation(type, Exportable.class) != null)
			{
				//if the type is exportable, recurse
				Element child = getFirstNodeImplementing(element, Element.class);
				return load(child, context);
			}
			else
			{
				Text text = getFirstNodeImplementing(element, Text.class);
				assertIsStringInstantiable(type);
				String stringValue = text.getData();
				return StringInstantiable.newInstance(stringValue, type);
			}
		}
		else if (attribute != null)
		{
			assertIsStringInstantiable(type);
			String stringValue = attribute.getValue();
			return StringInstantiable.newInstance(stringValue, type);
		}

		throw new IllegalStateException("Could not unpersist Persistable: " + name); //$NON-NLS-1$
	}

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

	protected Method getSetter(Class<?> c, String name, Class<?> parameterType, Persistant persistant)
	{
		if (!Util.isEmpty(persistant.setter()))
		{
			try
			{
				Method m = c.getDeclaredMethod(persistant.setter(), parameterType);
				m.setAccessible(true);
				return m;
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

		try
		{
			Method m = c.getDeclaredMethod(name, parameterType);
			m.setAccessible(true);
			return m;
		}
		catch (NoSuchMethodException e)
		{
		}

		name = "set" + name.substring(0, 1).toUpperCase() + name.substring(1); //$NON-NLS-1$
		try
		{
			Method m = c.getDeclaredMethod(name, parameterType);
			m.setAccessible(true);
			return m;
		}
		catch (NoSuchMethodException e)
		{
		}

		throw new IllegalArgumentException("Cannot find matching Persistant setter: " + name + " in class " + c); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected String getArrayElementName(Persistant persistant)
	{
		String arrayElementName = persistant.elementName();
		if (Util.isEmpty(arrayElementName))
		{
			arrayElementName = "element"; //$NON-NLS-1$
		}
		return arrayElementName;
	}

	protected IPersistantAdapter<?> getAdapter(Class<?> type, Adapter adapter)
	{
		IPersistantAdapter<?> persistantAdapter = adapters.get(type);
		if (persistantAdapter == null && adapter != null)
		{
			Class<? extends IPersistantAdapter<?>> adapterClass = adapter.adapter();
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

	protected void assertIsExportable(Class<?> c)
	{
		if (getAnnotation(c, Exportable.class) == null)
		{
			throw new IllegalArgumentException(c + " is not marked " + Exportable.class); //$NON-NLS-1$
		}
		try
		{
			c.getDeclaredConstructor();
		}
		catch (NoSuchMethodException e)
		{
			throw new IllegalArgumentException(c + " does not have a default constructor"); //$NON-NLS-1$
		}
	}

	protected void assertIsStringInstantiable(Class<?> type)
	{
		if (!StringInstantiable.isInstantiable(type))
		{
			throw new IllegalArgumentException("Type is not string instantiable: " + type); //$NON-NLS-1$
		}
	}

	protected static <T extends Annotation> T getAnnotation(Class<?> type, Class<T> annotationClass)
	{
		T t = type.getAnnotation(annotationClass);
		if (t != null)
			return t;
		for (Class<?> i : type.getInterfaces())
			if ((t = getAnnotation(i, annotationClass)) != null)
				return t;
		return null;
	}

	protected static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass)
	{
		T t = method.getAnnotation(annotationClass);
		if (t != null)
			return t;

		Class<?> type = method.getDeclaringClass();
		for (Class<?> i : type.getInterfaces())
		{
			try
			{
				Method m = i.getDeclaredMethod(method.getName(), method.getParameterTypes());
				if (m.getReturnType().equals(method.getReturnType()) && (t = getAnnotation(m, annotationClass)) != null)
					return t;
			}
			catch (NoSuchMethodException e)
			{
			}
		}
		return null;
	}

	protected static <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClass)
	{
		return field.getAnnotation(annotationClass);
	}

	protected static <N extends Node> N getFirstNodeImplementing(Node parent, Class<N> nodeType)
	{
		NodeList children = parent.getChildNodes();
		if (children != null)
		{
			for (int i = 0; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				if (nodeType.isAssignableFrom(node.getClass()))
				{
					@SuppressWarnings("unchecked")
					N n = (N) node;
					return n;
				}
			}
		}
		return null;
	}
}
