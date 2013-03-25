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
package au.gov.ga.earthsci.bookmark.model;

import java.util.HashMap;

/**
 * The default {@link IBookmarkMetadata} implementation which uses a {@link HashMap} for storing metadata
 * entries.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarkMetadata extends HashMap<String, String> implements IBookmarkMetadata
{

}
