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
package au.gov.ga.earthsci.core.temporal;

import java.math.BigInteger;

import au.gov.ga.earthsci.core.util.Range;


/**
 * An interface for classes that can provide temporal data, or that have a temporal aspect to them.
 * <p/>
 * The 'time' referred to here is real-world time (rather than animation time etc.). Implementations
 * may be applicable at any time that can be stored in a {@link BigTime} instance - that is geological
 * through to nanosecond scale.
 * 
 * @see Chronos
 * @see BigTime
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface Temporal
{

	/**
	 * Return the range of time, in real-world time, over which this temporal object is meaningful.
	 * 
	 * @return The range of time over which this temporal object is meaningful.
	 */
	Range<BigTime> getRange();
	
	/**
	 * Return an indication of the resolution of the temporal data represented by this instance,
	 * expressed in nanoseconds.
	 * <p/>
	 * See {@link BigTime#resolution} for more information on how resolution should be interpreted.
	 * 
	 * @return The resolution of the temporal data represented by this instance.
	 */
	BigInteger getResolution();
	
	/**
	 * Determine whether this temporal object can be applied at the given real-world time instant.
	 * <p/>
	 * In most cases this will be equivalent to {@code getRange().contains(time);}. However, 
	 * implementations may use a more fine-grained test and so client code should use this method
	 * in preference to the above. 
	 * 
	 * @param time The real-world time instant to test for. 
	 * 
	 * @return <code>true</code> if this temporal object can be applied at the given time instant;
	 * <code>false</code> otherwise.
	 */
	boolean isApplicableAt(BigTime time);
	
	/**
	 * Apply this temporal object at the given real-world time instant.
	 * <p/>
	 * This method will have no effect if {@link #isApplicableAt(BigTime)} returns <code>false</code>
	 * for the given time instant.
	 * 
	 * @param time The real-world time instant to apply 
	 */
	void apply(BigTime time);
}
