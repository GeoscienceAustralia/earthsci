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
import au.gov.ga.earthsci.common.util.Range;
import au.gov.ga.earthsci.core.temporal.BigTime;

/**
 * A representation of a time period within a {@link ITimeScale}.
 * <p/>
 * {@link ITimePeriod}s may contain one or more child {@link ITimePeriod}s,
 * which allows a hierarchy of time periods to be created.
 * <p/>
 * A {@link ITimePeriod} can be used to generate a human-readable label for a
 * given {@link BigTime} instance if it falls within the range of the period.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface ITimePeriod extends INamed, IDescribed, Comparable<ITimePeriod>
{

	/**
	 * Return the (unique) ID of this time period
	 * 
	 * @return The unique ID of this time period
	 */
	String getId();

	/**
	 * Return the level of this period within it's parent {@link ITimeScale}
	 * 
	 * @return The level of this period within it's parent {@link ITimeScale}
	 */
	ITimeScaleLevel getLevel();

	/**
	 * Return whether or not this period has sub-periods.
	 * 
	 * @return <code>true</code> if this period has sub-periods;
	 *         <code>false</code> otherwise.
	 */
	boolean hasSubPeriods();

	/**
	 * Return whether or not the provided period is a sub-period of this time
	 * period.
	 * 
	 * @param p
	 *            The sub-period to test for
	 * 
	 * @return <code>true</code> if the provided period is a sub-period of this
	 *         time period; <code>false</code> otherwise.
	 */
	boolean hasSubPeriod(ITimePeriod p);

	/**
	 * Return the ordered list of sub-periods that comprise this period, ordered
	 * from earliest to latest.
	 * <p/>
	 * If there are no sub-periods will return the empty list.
	 * 
	 * @return The ordered list of sub-periods that comprise this period; or the
	 *         empty list if there are no sub-periods.
	 */
	List<ITimePeriod> getSubPeriods();

	/**
	 * Return any sub-periods of this period that contain the time instant
	 * provided.
	 * <p/>
	 * This method returns a list to support the ability to have overlapping
	 * periods where period boundaries are not clearly defined / agreed on. In
	 * this case, client code may then implement their own heuristics to resolve
	 * a time to a single period, as required.
	 * <p/>
	 * Implementations may choose to use basic in-range tests (e.g.
	 * {@code contains(t)}) to select sub-periods, or may provide their own
	 * heuristics to resolve overlapping sub-periods.
	 * 
	 * @param t
	 *            The time instant to obtain a sub-period for
	 * @return
	 */
	List<ITimePeriod> getSubPeriod(BigTime t);

	/**
	 * Return the time range this period encompasses.
	 * 
	 * @return The time range this period encompasses.
	 */
	Range<BigTime> getRange();

	/**
	 * Return whether the provided time instant falls within this period
	 * 
	 * @param t
	 *            The time instant to test
	 * 
	 * @return <code>true</code> if the given time instant falls within this
	 *         period; <code>false</code> otherwise.
	 */
	boolean contains(BigTime t);

	/**
	 * Return a localised human-readable label for the given time instant for
	 * this period in the {@link ITimeScale}.
	 * <p/>
	 * If the given time instant does not fall within this period, will return
	 * <code>null</code>.
	 * 
	 * @param t
	 *            The time instant for which a label is required
	 * 
	 * @return The localised human-readable label for the given time instant for
	 *         this period, or <code>null</code> if the time instant does not
	 *         fall within this period.
	 */
	String getLabel(BigTime t);

}
