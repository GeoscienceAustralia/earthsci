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
import org.eclipse.sapphire.ElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to create a new model and resource that can be used to edit an object.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ElementFactory
{
	private static final Logger logger = LoggerFactory.getLogger(ElementFactory.class);

	/**
	 * Create a model that wraps the given object for editing.
	 * 
	 * @param object
	 *            Object to edit
	 * @param editable
	 *            Editable that defines the model that describes the object
	 * @return New model
	 */
	public static <T> Element createElement(T object, EditableElement<T> editable)
	{
		Class<? extends Element> modelInterface = editable.getElement();
		ElementType type = getType(modelInterface);
		EditableResource<T> resource = new EditableResource<T>(object);
		Element model = type.instantiate(null, resource);
		return model;
	}

	private static ElementType getType(Class<?> modelInterface)
	{
		try
		{
			return ElementType.read(modelInterface);
		}
		catch (Exception e)
		{
			logger.error("Error getting ModelElementType from model interface: " //$NON-NLS-1$
					+ modelInterface, e);
			return null;
		}
	}
}
