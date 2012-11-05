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
package au.gov.ga.earthsci.worldwind.common.layers.delegate.render;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.render.DrawContext;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IRenderDelegate;
import au.gov.ga.earthsci.worldwind.common.render.ExtendedDrawContext;

/**
 * Implementation of {@link IRenderDelegate} that uses the
 * {@link ExtendedDrawContext} to disable the elevation model for this layer,
 * causing the layer to be rendered on a flat surface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class IgnoreElevationRenderDelegate implements IRenderDelegate
{
	protected final static String DEFINITION_STRING = "IgnoreElevation";
	protected boolean oldValue = false;

	public IgnoreElevationRenderDelegate()
	{
	}

	@Override
	public void preRender(DrawContext dc)
	{
		if (dc instanceof ExtendedDrawContext)
		{
			oldValue = ((ExtendedDrawContext) dc).getGeographicSurfaceTileRenderer().isIgnoreElevation();
			((ExtendedDrawContext) dc).getGeographicSurfaceTileRenderer().setIgnoreElevation(true);
		}
	}

	@Override
	public void postRender(DrawContext dc)
	{
		if (dc instanceof ExtendedDrawContext)
		{
			((ExtendedDrawContext) dc).getGeographicSurfaceTileRenderer().setIgnoreElevation(oldValue);
		}
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING;
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.equalsIgnoreCase(DEFINITION_STRING))
		{
			return new IgnoreElevationRenderDelegate();
		}
		return null;
	}
}
