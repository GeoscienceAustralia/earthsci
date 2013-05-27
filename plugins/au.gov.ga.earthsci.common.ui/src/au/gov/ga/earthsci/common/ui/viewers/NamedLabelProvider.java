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
package au.gov.ga.earthsci.common.ui.viewers;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import au.gov.ga.earthsci.common.util.INamed;

/**
 * A simple {@link ILabelProvider} implementation that returns the name of
 * {@link INamed} objects
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class NamedLabelProvider extends LabelProvider implements ILabelProvider
{

	@Override
	public Image getImage(Object element)
	{
		return null;
	}

	@Override
	public String getText(Object element)
	{
		if (element == null)
		{
			return null;
		}
		return ((INamed) element).getName();
	}



}
