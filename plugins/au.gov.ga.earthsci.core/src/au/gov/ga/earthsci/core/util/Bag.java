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
package au.gov.ga.earthsci.core.util;

import java.util.HashMap;

/**
 * A simple Bag implementation backed by a HashMap for {@code O(1)} contains operations 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Bag<E>
{

	private HashMap<E, Integer> map = new HashMap<E, Integer>();
	
	/**
	 * Add the provided object to the bag and return the new count for the object.
	 * 
	 * @param obj The object to add
	 * 
	 * @return The new count of the object in the bag after the addition
	 */
	public int add(E obj)
	{
		int count = 1;
		if (map.containsKey(obj))
		{
			count += map.get(obj);
		}
		map.put(obj, count);
		return count;
	}
	
	/**
	 * Remove the provided object from the bag and return the new count for the object.
	 * 
	 * @param obj The object to remove
	 * 
	 * @return The new count of the object in the bag after the removal
	 */
	public int remove(E obj)
	{
		int count = 0;
		if (map.containsKey(obj))
		{
			count = map.get(obj) - 1;
		}
		if (count == 0)
		{
			map.remove(obj);
		}
		else
		{
			map.put(obj, count);
		}
		return count;
	}
	
	/**
	 * Determine if the object exists in this bag
	 * <p/>
	 * This is the same as testing if {@link #count(Object)}{@code > 0}.
	 * 
	 * @param obj The object to test
	 * 
	 * @return <code>true</code> if the object exists in this bag; <code>false</code> otherwise.
	 */
	public boolean contains(E obj)
	{
		return count(obj) > 0;
	}
	
	/**
	 * Return the count of the given object in this bag
	 * 
	 * @param obj The object to count
	 * 
	 * @return The count of the object in the bag
	 */
	public int count(E obj)
	{
		if (!map.containsKey(obj))
		{
			return 0;
		}
		return map.get(obj);
	}
	
	@Override
	public String toString()
	{
		return map.keySet().toString();
	}
}
