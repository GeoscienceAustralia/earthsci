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
import org.eclipse.sapphire.ui.def.DefinitionLoader;

/**
 * Container class used by the {@link EditableManager}. Stores an
 * {@link Element} and a {@link DefinitionLoader} for loading a Sapphire
 * definition.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ElementAndDefinition implements IRevertable
{
	private final Element element;
	private final DefinitionLoader loader;

	ElementAndDefinition(Element element, DefinitionLoader loader)
	{
		this.element = element;
		this.loader = loader;
	}

	public Element getElement()
	{
		return element;
	}

	public DefinitionLoader getLoader()
	{
		return loader;
	}

	@Override
	public void revert()
	{
		if (element.resource() instanceof IRevertable)
		{
			((IRevertable) element.resource()).revert();
		}
	}
}
