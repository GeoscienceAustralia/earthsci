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
package au.gov.ga.earthsci.bookmark.part.editor;

/**
 * A simple immutable implementation of the {@link IBookmarkEditorMessage}
 * interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarkEditorMessage implements IBookmarkEditorMessage
{
	private final Level level;
	private final String code;
	private final String message;

	public BookmarkEditorMessage(Level level, String code, String message)
	{
		super();
		this.level = level;
		this.code = code;
		this.message = message;
	}

	@Override
	public Level getLevel()
	{
		// TODO Auto-generated method stub
		return level;
	}

	@Override
	public String getCode()
	{
		return code;
	}

	@Override
	public String getMessage()
	{
		return message;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof IBookmarkEditorMessage))
		{
			return false;
		}
		return code.equals(((IBookmarkEditorMessage) obj).getCode());
	}

	@Override
	public int hashCode()
	{
		return code.hashCode();
	}

}
