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
package au.gov.ga.earthsci.common.collection.adapter;

/**
 * Responsible for converting a type to another type, and back.
 * <p/>
 * Adapting must be commutative, ie the following must be true:
 * <ul>
 * <li><code>adapter.adaptFrom(adapter.adaptTo(value)) == value</code></li>
 * <li><code>adapter.adaptTo(adapter.adaptFrom(value)) == value</code></li>
 * </ul>
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IAdapter<E, A>
{
	/**
	 * Adapt this value.
	 * 
	 * @param value
	 * @return Adapted value
	 */
	A adaptTo(E value);

	/**
	 * Adapt this value.
	 * 
	 * @param value
	 * @return Adapted value
	 */
	E adaptFrom(A value);
}
