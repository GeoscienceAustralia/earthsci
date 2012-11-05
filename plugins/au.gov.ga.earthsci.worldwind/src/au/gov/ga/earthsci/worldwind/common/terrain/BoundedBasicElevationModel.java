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
package au.gov.ga.earthsci.worldwind.common.terrain;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.terrain.BasicElevationModel;
import au.gov.ga.earthsci.worldwind.common.layers.Bounded;

/**
 * Extension to {@link BasicElevationModel} that implements the {@link Bounded}
 * interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BoundedBasicElevationModel extends BasicElevationModel implements Bounded
{
	public BoundedBasicElevationModel(AVList params)
	{
		super(params);
	}

	@Override
	public Sector getSector()
	{
		return getLevels().getSector();
	}
}
