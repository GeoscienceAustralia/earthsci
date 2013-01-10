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


/**
 * Represents a bookmark, used to store state that can be re-applied at a later date.
 * <p/>
 * Bookmarks contain zero or more {@link IBookmarkProperty}s, which capture the state stored by the
 * bookmark. 
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IBookmark
{

	/** 
	 * Return the name of this bookmark
	 * 
	 * @return The name of this bookmark 
	 */
	String getName();
	
	/**
	 * Set the name of this bookmark, to allow for user edits etc.
	 */
	void setName(String name);
	
	/**
	 * Return any metadata associated with this bookmark as a map of display strings keyed by 
	 *   
	 * @return The metadata associated with 
	 */
	IBookmarkMetadata getMetadata();
	
	/**
	 * Return the set of properties associated with this bookmark.
	 * 
	 * @return The set of properties associated with this bookmark.
	 */
	IBookmarkProperty[] getProperties(); 
	
	/**
	 * Return the property with the given type (if any).
	 * 
	 * @param type The key for the type of property to return
	 * 
	 * @return The property of the given type, or <code>null</code> if none exists.
	 */
	IBookmarkProperty getProperty(String type);
	
	/**
	 * Add the given property to this bookmark.
	 * 
	 * @param property The property to add.
	 */
	void addProperty(IBookmarkProperty property);
	
	/**
	 * Remove the property of the given type from this bookmark, if it exists
	 * 
	 * @param type The type of the property to remove
	 * 
	 * @return The removed property, or <code>null</code> if none is removed
	 */
	IBookmarkProperty removeProperty(String type);
	
	/**
	 * Remove the given property from this bookmark, if it exists
	 * 
	 * @param p The property to remove
	 * 
	 * @return <code>true</code> if the property was removed; <code>false</code> otherwise
	 */
	boolean removeProperty(IBookmarkProperty p);
	
	/**
	 * Return whether a property exists in this bookmark with the given type
	 * 
	 * @param type The property type to check for existence 
	 * 
	 * @return <code>true</code> if the property exists on this bookmark; <code>false</code> otherwise.
	 */
	boolean hasProperty(String type);
	
	/**
	 * Return the duration (in milliseconds) that the system should take when transitioning 
	 * to this bookmark state from the current world state.
	 * <p/>
	 * If the global default is to be used, this method will return <code>null</code>.
	 * 
	 * @return The duration (in milliseconds) that the system should take when transitioning to this bookmark
	 */
	Long getTransitionDuration();
}
