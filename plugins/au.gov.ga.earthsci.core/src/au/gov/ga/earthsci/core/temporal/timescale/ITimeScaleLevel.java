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
package au.gov.ga.earthsci.core.temporal.timescale;

import java.math.BigInteger;

import au.gov.ga.earthsci.common.util.IDescribed;
import au.gov.ga.earthsci.common.util.INamed;
import au.gov.ga.earthsci.core.temporal.BigTime;

/**
 * Represents a level in a {@link ITimeScale}.
 * <p/>
 * Levels are ordered, and have a resolution that gives an indication of the
 * scale at which time changes at this level of the time scale 
 * (e.g. millions of years, seconds etc.) 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ITimeScaleLevel extends INamed, IDescribed, Comparable<ITimeScaleLevel>
{

	/**
	 * Return the resolution, in nanoseconds, of this level
	 * <p/>
	 * See {@link BigTime} for some common/useful resolution constants.
	 * 
	 * @return The resolution, in nanoseconds, of this level.
	 */
	BigInteger getResolution();
	
	/**
	 * Return the order of this level within it's parent {@link ITimeScale}. Order
	 * is 0-indexed, with order 0 being the topmost level in a {@link ITimeScale}.
	 * 
	 * @return The order of this level within it's parent {@link ITimeScale}.
	 */
	int getOrder();
	
}
