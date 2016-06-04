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

import au.gov.ga.earthsci.common.color.ColorMap;
import au.gov.ga.earthsci.model.core.parameters.IColorMapParameters;
import au.gov.ga.earthsci.model.core.parameters.ISourceProjectionParameters;

/**
 * Parameters required for creating a layer from a GOCAD object.
 *
 * @author Michael de Hoog
 */
public class GOCADLayerParameters implements IColorMapParameters, ISourceProjectionParameters
{
	private String sourceProjection;
	private ColorMap colorMap;
	private String paintedVariable;

	@Override
	public String getSourceProjection()
	{
		return sourceProjection;
	}

	@Override
	public void setSourceProjection(String sourceProjection)
	{
		this.sourceProjection = sourceProjection;
	}

	@Override
	public ColorMap getColorMap()
	{
		return colorMap;
	}

	@Override
	public void setColorMap(ColorMap colorMap)
	{
		this.colorMap = colorMap;
	}

	@Override
	public String getPaintedVariable()
	{
		return paintedVariable;
	}

	@Override
	public void setPaintedVariable(String paintedVariable)
	{
		this.paintedVariable = paintedVariable;
	}
}
