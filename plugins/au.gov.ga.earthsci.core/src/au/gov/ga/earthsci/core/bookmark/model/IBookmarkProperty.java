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
package au.gov.ga.earthsci.core.bookmark.model;

/**
 * Represents a single property stored on a bookmark. A bookmark property stores state
 * about a specific aspect of the model/view that can be persisted between application invocations
 * and later re-applied. 
 * <p/>
 * Types of properties are uniquely identified by a type, and a bookmark may have at most one property
 * of each type.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarkProperty
{
	/**
	 * Return the key that identifies the type of this property.
	 * 
	 * @return The key that identifies the type of this property
	 */
	String getType();
	
	/**
	 * Return the key to use for looking up the name for this property; or the name itself.
	 * 
	 * @return the key to use for looking up the name for this property; or the name itself.
	 */
	String getName();
}
