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
package au.gov.ga.earthsci.layer;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.ElevationModel;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.CompoundElevationModel;

/**
 * {@link Layer} that contains an elevation model; used by the LayerFactory so
 * that it can return an ElevationModel from
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ElevationModelLayer extends AbstractLayer implements IElevationModelLayer
{
	private final ElevationModel elevationModel;

	public ElevationModelLayer(ElevationModel elevationModel)
	{
		this.elevationModel = elevationModel;
	}

	@Override
	public ElevationModel getElevationModel()
	{
		return elevationModel;
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		//don't render anything!
	}

	@Override
	public boolean isEnabled()
	{
		return elevationModel.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		elevationModel.setEnabled(enabled);
	}

	@Override
	public String getName()
	{
		return getElevationModelName(elevationModel);
	}

	@Override
	public void setName(String name)
	{
		elevationModel.setName(name);
	}

	private static String getElevationModelName(ElevationModel elevationModel)
	{
		String name = elevationModel.getStringValue(AVKey.DISPLAY_NAME);
		if (name != null)
		{
			return name;
		}
		if (elevationModel instanceof CompoundElevationModel)
		{
			CompoundElevationModel cem = (CompoundElevationModel) elevationModel;
			for (ElevationModel em : cem.getElevationModels())
			{
				//recurse into children
				name = getElevationModelName(em);
				if (name != null)
				{
					return name;
				}
			}
		}
		return null;
	}
}
