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
 * An interface for classes that can be uniquely identified by an ID
 * <p/>
 * It is up to the implementing class to decide 'how unique' the ID is (e.g. unique per session, 
 * globally unique, persistable etc.)
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public interface IIdentifiable
{
	/**
	 * Return the ID for this object.
	 * 
	 * @return the ID for this object; never <code>null</code>.
	 */
	String getId();
}
