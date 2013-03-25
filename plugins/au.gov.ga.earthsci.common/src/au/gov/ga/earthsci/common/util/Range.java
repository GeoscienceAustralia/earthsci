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
package au.gov.ga.earthsci.common.util;

/**
 * Represents an immutable range of comparable values (eg. <code>[minValue, maxValue]</code>)
 * <p/>
 * The range may have open ends.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Range<C extends Comparable<C>> extends au.gov.ga.earthsci.worldwind.common.util.Range<C>
{

	// Delegate to the plugin version - this is done to reduce coupling between core and ww plugins
	
	public Range(C minValue, boolean includeMin, C maxValue, boolean includeMax)
	{
		super(minValue, includeMin, maxValue, includeMax);
	}

	public Range(C minValue, C maxValue)
	{
		super(minValue, maxValue);
	}

}
