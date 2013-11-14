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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.sapphire.Element;

import au.gov.ga.earthsci.common.util.ExtensionPointHelper;

/**
 * Defines a mapping between a Sapphire element (interface that extends
 * {@link Element}), and a class who's instances can be edited by the element.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EditableElement<T>
{
	private final static String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$
	private final static String ELEMENT_ATTRIBUTE = "element"; //$NON-NLS-1$
	private final static String SDEF_CONTEXT_ATTRIBUTE = "sdef-context"; //$NON-NLS-1$
	private final static String SDEF_NAME_ATTRIBUTE = "sdef-name"; //$NON-NLS-1$

	private final Class<? extends T> type;
	private final Class<? extends Element> element;
	private final Class<?> sdefContext;
	private final String sdefName;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public EditableElement(IConfigurationElement configurationElement) throws ClassNotFoundException, CoreException
	{
		type = (Class) ExtensionPointHelper.getClassForProperty(configurationElement, TYPE_ATTRIBUTE);
		element = (Class) ExtensionPointHelper.getClassForProperty(configurationElement, ELEMENT_ATTRIBUTE);
		sdefContext =
				configurationElement.getAttribute(SDEF_CONTEXT_ATTRIBUTE) != null ? ExtensionPointHelper
						.getClassForProperty(configurationElement, SDEF_CONTEXT_ATTRIBUTE) : null;
		sdefName = configurationElement.getAttribute(SDEF_NAME_ATTRIBUTE);
	}

	public Class<? extends T> getType()
	{
		return type;
	}

	public Class<? extends Element> getElement()
	{
		return element;
	}

	public Class<?> getSdefContext()
	{
		return sdefContext;
	}

	public String getSdefName()
	{
		return sdefName;
	}
}
