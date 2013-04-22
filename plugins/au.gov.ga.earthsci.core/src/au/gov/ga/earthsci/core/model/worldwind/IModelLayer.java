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
package au.gov.ga.earthsci.core.model.worldwind;

import gov.nasa.worldwind.layers.Layer;

import java.util.List;

import au.gov.ga.earthsci.model.IModel;
import au.gov.ga.earthsci.model.render.IModelGeometryRenderer;

/**
 * A {@link Layer} that renders one or more {@link IModel} instances using their
 * configured {@link IModelGeometryRenderer}s
 * <p/>
 * This class acts as bridge between the Model representation type and the World
 * Wind {@link Layer} concept.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IModelLayer extends Layer
{
	/**
	 * Get the models associated with this layer
	 * 
	 * @return The models associated with this layer
	 */
	List<IModel> getModels();

}
