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
package au.gov.ga.earthsci.core.model.layer;

import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.Layer;

/**
 * Represents a {@link Layer} that wraps an elevation model. A layer of this
 * type can be returned by the {@link LayerFactory} when a source representing
 * an elevation model is passed to the factory.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IElevationModelLayer extends Layer
{
	/**
	 * @return The elevation model wrapped by this object
	 */
	ElevationModel getElevationModel();
}
