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
package au.gov.ga.earthsci.worldwind.common.layers.point;

import au.gov.ga.earthsci.worldwind.common.layers.data.DataProvider;

/**
 * Represents a point source for the {@link PointLayer}. Handles loading points
 * from the data source, and adding them to the layer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface PointProvider extends DataProvider<PointLayer>
{
}
