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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;

/**
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CameraPropertyEditor implements IBookmarkPropertyEditor
{

	@Override
	public void setProperty(IBookmarkProperty property)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void okPressed()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void cancelPressed()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreOriginalValues()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Control createControl(Composite parent)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Control getControl()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean isValid()
	{
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public IBookmarkEditorMessage[] getMessages()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getName()
	{
		return "Camera";
	}
	
	@Override
	public String getDescription()
	{
		return "Edit camera properties (position, viewing direction etc.)";
	}
}
