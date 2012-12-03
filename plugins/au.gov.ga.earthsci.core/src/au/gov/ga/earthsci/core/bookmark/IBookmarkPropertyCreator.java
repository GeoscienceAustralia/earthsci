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
package au.gov.ga.earthsci.core.bookmark;

import java.util.Map;

import au.gov.ga.earthsci.core.bookmark.model.IBookmarkProperty;

/**
 * An interface for classes that are able to create {@link IBookmarkProperty} instances
 * from a context containing the required key-value pairs.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarkPropertyCreator
{
	/**
	 * Returns the property types supported by this creator
	 * 
	 * @return The property types supported by this creator
	 */
	String[] getSupportedTypes();
	
	/**
	 * Creates and returns a new {@link IBookmarkProperty} from the provided
	 * context.
	 * 
	 * @param context A map containing key-value pairs used to re-create the property
	 * 
	 * @return The {@link IBookmarkProperty} created from the given context
	 * 
	 * @throws IllegalArgumentException If the context is <code>null</code> or does not
	 * contain the required keys for this creator
	 */
	IBookmarkProperty create(Map<String, String> context);
}
