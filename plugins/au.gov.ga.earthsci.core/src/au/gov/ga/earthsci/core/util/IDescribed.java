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
package au.gov.ga.earthsci.core.util;

/**
 * An interface for objects that can provide a localised human-readable
 * description of themselves.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IDescribed
{

	/**
	 * Return the (localised) human-readable description of this instance.
	 * 
	 * @return the (localised) human-readable description of this instance.
	 */
	String getDescription();
}
