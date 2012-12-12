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
package au.gov.ga.earthsci.core.math.interpolation;

import au.gov.ga.earthsci.core.math.vector.Vector;

/**
 * An interface for interpolators that are able to provide interpolated values at a given 
 * percentage between a start and end value.
 * <p/>
 * Subclasses might include linear or bezier interpolators and may 
 * require specific initialisation.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface Interpolator<V extends Vector<?>>
{
	
	/**
	 * Compute the interpolated value at the provided percentage 
	 * along the interpolation
	 * 
	 * @param percent The percentage along the interpolation the value is required for. 
	 * 				  In range <code>[0,1]</code>
	 * 
	 * @return The computed interpolated value at the provided percentage
	 */
	V computeValue(double percent);
	
}
