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
package au.gov.ga.earthsci.bookmark.part.editor;

import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;

/**
 * An abstract base class for {@link IBookmarkPropertyEditor} implementations.
 * <p/>
 * Provides convenience implementations of some methods.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class AbstractBookmarkPropertyEditor extends AbstractBookmarkEditor implements IBookmarkPropertyEditor
{
	private IBookmarkProperty property;
	private boolean includedInBookmark;
	
	@Override
	public void setProperty(IBookmarkProperty property) 
	{
		this.property = property;
	};
	
	/**
	 * @return the property this editor is backed by
	 */
	@Override
	public IBookmarkProperty getProperty()
	{
		return property;
	}
	
	@Override
	public void setIncludedInBookmark(boolean included)
	{
		this.includedInBookmark = included;
		if (this.includedInBookmark && this.property == null)
		{
			this.property = createPropertyFromCurrent();
			fillFromCurrent();
		}
	}
	
	@Override
	public boolean isIncludedInBookmark()
	{
		return includedInBookmark;
	}
	
	@Override
	public void restoreOriginalValues()
	{
		fillFieldsFromProperty(getProperty());
	}

	@Override
	public void fillFromCurrent()
	{
		fillFieldsFromProperty(createPropertyFromCurrent());
	}
	
	/**
	 * Create and return a new property from the current world state
	 */
	protected abstract IBookmarkProperty createPropertyFromCurrent();

	/**
	 * Fill this editor's fields with the values in the given property, or clear the fields
	 * if <code>null</code>
	 */
	protected abstract void fillFieldsFromProperty(IBookmarkProperty property);
}
