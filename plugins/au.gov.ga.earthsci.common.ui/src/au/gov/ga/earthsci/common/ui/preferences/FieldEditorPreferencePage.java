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
package au.gov.ga.earthsci.common.ui.preferences;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;

/**
 * {@link org.eclipse.jface.preference.FieldEditorPreferencePage} that
 * implements {@link IPreferencePage} empty methods.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class FieldEditorPreferencePage extends org.eclipse.jface.preference.FieldEditorPreferencePage
		implements IPreferencePage
{
	public FieldEditorPreferencePage()
	{
		super();
	}

	protected FieldEditorPreferencePage(int style)
	{
		super(style);
	}

	protected FieldEditorPreferencePage(String title, ImageDescriptor image, int style)
	{
		super(title, image, style);
	}

	protected FieldEditorPreferencePage(String title, int style)
	{
		super(title, style);
	}

	@Override
	public void init(IWorkbench workbench)
	{
	}
}
