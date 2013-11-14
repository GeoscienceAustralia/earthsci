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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.common.collection.ArrayListTreeMap;
import au.gov.ga.earthsci.common.collection.ListSortedMap;

/**
 * Manages the list of {@link EditableElement}s, defined via the
 * <code>au.gov.ga.earthsci.editable.elements</code> extension point.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EditableManager
{
	private static final String ELEMENTS_ID = "au.gov.ga.earthsci.editable.elements"; //$NON-NLS-1$
	private static final String RENDERERS_ID = "au.gov.ga.earthsci.editable.renderers"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(EditableManager.class);
	private static EditableManager instance = new EditableManager();

	/**
	 * @return An instance of the intent manager
	 */
	public static EditableManager getInstance()
	{
		return instance;
	}

	private final Map<Class<?>, EditableElement<?>> typeElements = new HashMap<Class<?>, EditableElement<?>>();

	private EditableManager()
	{
		IConfigurationElement[] elements = RegistryFactory.getRegistry().getConfigurationElementsFor(ELEMENTS_ID);
		for (IConfigurationElement element : elements)
		{
			try
			{
				EditableElement<?> typeElement = new EditableElement<Object>(element);
				this.typeElements.put(typeElement.getType(), typeElement);
			}
			catch (Exception e)
			{
				logger.error("Error processing editable element", e); //$NON-NLS-1$
			}
		}

		ListSortedMap<Integer, PropertyEditorPresentationFactory> factories =
				new ArrayListTreeMap<Integer, PropertyEditorPresentationFactory>();
		elements = RegistryFactory.getRegistry().getConfigurationElementsFor(RENDERERS_ID);
		for (IConfigurationElement element : elements)
		{
			try
			{
				PropertyEditorPresentationFactory factory =
						(PropertyEditorPresentationFactory) element.createExecutableExtension("factory"); //$NON-NLS-1$
				String stringPriority = element.getAttribute("priority"); //$NON-NLS-1$
				int priority =
						stringPriority == null || stringPriority.length() == 0 ? 0 : Integer.parseInt(stringPriority);
				factories.putSingle(priority, factory);
			}
			catch (Exception e)
			{
				logger.error("Error processing renderer factory", e); //$NON-NLS-1$
			}
		}

		try
		{
			Field FACTORIES_field = PropertyEditorPart.class.getDeclaredField("FACTORIES"); //$NON-NLS-1$
			FACTORIES_field.setAccessible(true);
			@SuppressWarnings({ "unchecked", "rawtypes" })
			List<PropertyEditorPresentationFactory> list = (List) FACTORIES_field.get(null);
			for (Entry<Integer, List<PropertyEditorPresentationFactory>> entry : factories.entrySet())
			{
				for (PropertyEditorPresentationFactory factory : entry.getValue())
				{
					//add factories before all default ones, inserting them at the front so that highest priority is at index 0
					list.add(0, factory);
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Error adding factory", e); //$NON-NLS-1$
		}
	}

	public <E> Element createElement(E object)
	{
		EditableElement<? super E> editable = getEditable(object);
		if (editable == null)
		{
			return null;
		}
		return ElementFactory.createElement(object, editable);
	}

	public <E> ElementAndDefinition edit(E object)
	{
		EditableElement<? super E> editable = getEditable(object);
		if (editable == null)
		{
			return null;
		}
		Element element = createElement(object);
		DefinitionLoader loader =
				DefinitionLoader.context(editable.getSdefContext()).sdef(editable.getSdefName());
		return new ElementAndDefinition(element, loader);
	}

	public <E> EditableElement<? super E> getEditable(E element)
	{
		Map<EditableElement<?>, Integer> distances = new HashMap<EditableElement<?>, Integer>();
		calculateEditableDistances(element.getClass(), 0, distances);
		int minDistance = Integer.MAX_VALUE;
		EditableElement<? super E> editable = null;
		for (Entry<EditableElement<?>, Integer> entry : distances.entrySet())
		{
			if (entry.getValue() < minDistance)
			{
				minDistance = entry.getValue();
				@SuppressWarnings({ "rawtypes", "unchecked" })
				EditableElement<? super E> e = (EditableElement) entry.getKey();
				editable = e;
			}
		}
		return editable;
	}

	private void calculateEditableDistances(Class<?> c, int distance, Map<EditableElement<?>, Integer> distances)
	{
		if (c == null)
		{
			return;
		}
		EditableElement<?> editable = typeElements.get(c);
		if (editable != null)
		{
			Integer lastDistance = distances.get(editable);
			if (lastDistance == null || distance < lastDistance)
			{
				distances.put(editable, distance);
			}
		}
		calculateEditableDistances(c.getSuperclass(), distance + 1, distances);
		for (Class<?> i : c.getInterfaces())
		{
			calculateEditableDistances(i, distance + 1, distances);
		}
	}
}
