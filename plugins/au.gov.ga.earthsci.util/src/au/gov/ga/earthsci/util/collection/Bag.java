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
package au.gov.ga.earthsci.util.collection;

import java.util.Collection;

/**
 * Collection that represents a bag, which allows for {@code O(1)} contains and
 * count operations for any element.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface Bag<E> extends Collection<E>
{
	/**
	 * Return the count of the given object in this bag
	 * 
	 * @param o
	 *            The object to count
	 * 
	 * @return The count of the object in the bag
	 */
	int count(Object o);

	/**
	 * @return The number of unique objects in this bag
	 */
	int countUnique();
}
