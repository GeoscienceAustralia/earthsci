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

import org.eclipse.sapphire.modeling.IModelParticle;
import org.eclipse.sapphire.modeling.ModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Resource;

/**
 * A non-abstract version of {@link ModelElement}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ConcreteModelElement extends ModelElement
{
	public ConcreteModelElement(ModelElementType type, IModelParticle parent, ModelProperty parentProperty,
			Resource resource)
	{
		super(type, parent, parentProperty, resource);
	}

	public ConcreteModelElement(IModelParticle parent, ModelProperty parentProperty,
			Resource resource)
	{
		super(parentProperty.getType(), parent, parentProperty, resource);
	}
}
