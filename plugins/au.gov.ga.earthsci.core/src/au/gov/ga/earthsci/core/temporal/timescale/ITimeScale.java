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

import java.util.List;

import au.gov.ga.earthsci.common.util.IDescribed;
import au.gov.ga.earthsci.common.util.INamed;
import au.gov.ga.earthsci.core.temporal.BigTime;

/**
 * A {@link ITimeScale} is a concept used to capture the various scales on which time can
 * be measured (e.g. Geological vs. human history vs. modern history etc.)
 * <p/>
 * {@link ITimeScale}s contain a hierarchy of {@link ITimePeriod}s which describe the structure
 * of the scale.
 * <p/>
 * A primary use of a {@link ITimeScale} is to generate a human-readable label for a
 * {@link BigTime} instance based on a particular scale.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ITimeScale extends INamed, IDescribed
{

	/**
	 * Return the unique identifier for this time scale
	 * 
	 * @return The unique identifier for this time scale
	 */
	String getId();
	
	/**
	 * Return the ordered list of levels that are used to define this {@link ITimeScale}.
	 * 
	 * @return The ordered list of levels that define this {@link ITimeScale}
	 */
	List<ITimeScaleLevel> getLevels();
	
	/**
	 * Return the top-level periods of this {@link ITimeScale} ordered from earliest to latest.
	 * 
	 * @return The ordered list of top-level periods for this {@link ITimeScale}, from earliest to latest.
	 */
	List<ITimePeriod> getPeriods();
	
	/**
	 * Return whether this time scale contains the provided time period
	 * 
	 * @param p The time period to test for
	 * 
	 * @return <code>true</code> if the time period exists in this time scale; <code>false</code>
	 * otherwise.
	 */
	boolean hasPeriod(ITimePeriod p);
}
