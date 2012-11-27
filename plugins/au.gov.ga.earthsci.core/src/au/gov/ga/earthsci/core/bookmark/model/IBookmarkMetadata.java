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

import java.util.Map;

/**
 * An interface for storing and retrieving metadata about an {@link IBookmark}. This is a dictionary
 * into which metadata can be stored relating to the associated bookmark.
 * <p/>
 * A number of standard keys are defined which can be used to store common pieces of information (description etc.).
 * <p/>
 * Where appropriate, data can be stored as HTML formatted strings for display purposes.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmarkMetadata extends Map<String, String>
{
	/**
	 * The key for the bookmark description
	 */
	String DESCRIPTION = "bookmark.metadata.description"; //$NON-NLS-1$
}
