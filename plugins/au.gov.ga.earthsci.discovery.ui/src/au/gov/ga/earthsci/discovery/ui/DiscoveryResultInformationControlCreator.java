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
package au.gov.ga.earthsci.discovery.ui;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.common.ui.information.FocusableBrowserInformationControl;
import au.gov.ga.earthsci.discovery.IDiscoveryResult;
import au.gov.ga.earthsci.jface.extras.information.html.BrowserInformationControl;

/**
 * {@link IInformationControlCreator} that creates information controls for
 * {@link IDiscoveryResult} values.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DiscoveryResultInformationControlCreator implements IInformationControlCreator
{
	@Override
	public IInformationControl createInformationControl(Shell parent)
	{
		if (BrowserInformationControl.isAvailable(parent))
		{
			return new FocusableBrowserInformationControl(parent, JFaceResources.DIALOG_FONT, null, null);
		}
		return new DefaultInformationControl(parent, false);
	}
}
