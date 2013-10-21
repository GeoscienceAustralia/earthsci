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
import org.eclipse.sapphire.modeling.IModelElement;

import au.gov.ga.earthsci.common.util.ExtensionPointHelper;

/**
 * Defines a mapping between a Sapphire model (interface that extends
 * {@link IModelElement}), and a class who's instances can be edited by the
 * model.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EditableModel<T>
{
	private final static String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$
	private final static String MODEL_ATTRIBUTE = "model"; //$NON-NLS-1$
	private final static String SDEF_CONTEXT_ATTRIBUTE = "sdef-context"; //$NON-NLS-1$
	private final static String SDEF_NAME_ATTRIBUTE = "sdef-name"; //$NON-NLS-1$

	private final Class<? extends T> type;
	private final Class<? extends IModelElement> model;
	private final Class<?> sdefContext;
	private final String sdefName;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public EditableModel(IConfigurationElement configurationElement) throws ClassNotFoundException, CoreException
	{
		type = (Class) ExtensionPointHelper.getClassForProperty(configurationElement, TYPE_ATTRIBUTE);
		model = (Class) ExtensionPointHelper.getClassForProperty(configurationElement, MODEL_ATTRIBUTE);
		sdefContext =
				configurationElement.getAttribute(SDEF_CONTEXT_ATTRIBUTE) != null ? ExtensionPointHelper
						.getClassForProperty(configurationElement, SDEF_CONTEXT_ATTRIBUTE) : null;
		sdefName = configurationElement.getAttribute(SDEF_NAME_ATTRIBUTE);
	}

	public Class<? extends T> getType()
	{
		return type;
	}

	public Class<? extends IModelElement> getModel()
	{
		return model;
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
