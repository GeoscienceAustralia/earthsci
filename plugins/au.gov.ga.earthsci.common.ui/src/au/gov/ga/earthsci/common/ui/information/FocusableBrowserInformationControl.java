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
package au.gov.ga.earthsci.common.ui.information;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.jface.extras.information.html.BrowserInformationControl;

/**
 * Non-resizable {@link BrowserInformationControl} subclass that properly
 * implements {@link #getInformationPresenterControlCreator()}, returning a
 * resizable {@link BrowserInformationControl} with an optional
 * {@link ToolBarManager}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FocusableBrowserInformationControl extends BrowserInformationControl
{
	private final String symbolicFontName;
	private final ToolBarManager toolBarManager;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param symbolicFontName
	 *            the symbolic name of the font used for size computations
	 * @param statusFieldText
	 *            the text to be used in the optional status field or
	 *            <code>null</code> if the status field should be hidden
	 * @param toolBarManager
	 *            the manager or <code>null</code> if toolbar is not desired
	 *            (only used for the resizable control created by the creator
	 *            returned by {@link #getInformationPresenterControlCreator()})
	 * 
	 * @param toolBarManager
	 */
	public FocusableBrowserInformationControl(Shell parent, String symbolicFontName, String statusFieldText,
			ToolBarManager toolBarManager)
	{
		super(parent, symbolicFontName, false);
		this.symbolicFontName = symbolicFontName;
		this.toolBarManager = toolBarManager;
	}

	@Override
	public IInformationControlCreator getInformationPresenterControlCreator()
	{
		return new IInformationControlCreator()
		{
			@Override
			public IInformationControl createInformationControl(Shell parent)
			{
				return new BrowserInformationControl(parent, symbolicFontName, toolBarManager);
			}
		};
	}
}
