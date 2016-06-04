/*******************************************************************************
 * Copyright 2016 Geoscience Australia
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
package au.gov.ga.earthsci.layer.ui.wizard.gocad;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import au.gov.ga.earthsci.model.ui.raster.wizard.AbstractWizardPage;
import au.gov.ga.earthsci.model.ui.raster.wizard.ColorMapPage;
import au.gov.ga.earthsci.model.ui.raster.wizard.ProjectionPage;

/**
 * Wizard for collecting data required for displaying a GOCAD object as a layer.
 *
 * @author Michael de Hoog
 */
public class GOCADLayerParametersWizard extends Wizard
{
	private final GOCADLayerParameters params;
	private final String[] properties;

	public GOCADLayerParametersWizard(GOCADLayerParameters params, String[] properties)
	{
		this.params = params;
		this.properties = properties;

		setWindowTitle("Import GOCAD object as layer");
		setNeedsProgressMonitor(false);
	}

	@Override
	public void addPages()
	{
		addPage(new ProjectionPage(params, null));
		addPage(new ColorMapPage(params, false, properties));
	}

	@Override
	public boolean performFinish()
	{
		for (IWizardPage page : getPages())
		{
			if (page instanceof AbstractWizardPage)
			{
				((AbstractWizardPage<?>) page).bind();
			}
		}
		return true;
	}

	/**
	 * Get the parameters object populated with collected values
	 */
	public GOCADLayerParameters getParams()
	{
		return params;
	}
}
