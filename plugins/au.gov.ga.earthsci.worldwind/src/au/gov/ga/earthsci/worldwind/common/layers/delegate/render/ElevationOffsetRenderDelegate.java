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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IRenderDelegate;
import au.gov.ga.earthsci.worldwind.common.render.ExtendedDrawContext;

/**
 * Implementation of {@link IRenderDelegate} that uses the
 * {@link ExtendedDrawContext} class to offset the elevation model by a given
 * amount for the tiled image layer.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ElevationOffsetRenderDelegate implements IRenderDelegate
{
	protected final static String DEFINITION_STRING = "ElevationOffset";
	protected final double elevationOffset;
	protected double oldElevationOffset = 0;

	@SuppressWarnings("unused")
	private ElevationOffsetRenderDelegate()
	{
		this(0);
	}

	public ElevationOffsetRenderDelegate(double elevationOffset)
	{
		this.elevationOffset = elevationOffset;
	}

	@Override
	public void preRender(DrawContext dc)
	{
		if (dc instanceof ExtendedDrawContext)
		{
			oldElevationOffset = ((ExtendedDrawContext) dc).getGeographicSurfaceTileRenderer().getElevationOffset();
			((ExtendedDrawContext) dc).getGeographicSurfaceTileRenderer().setElevationOffset(elevationOffset);
		}
	}

	@Override
	public void postRender(DrawContext dc)
	{
		if (dc instanceof ExtendedDrawContext)
		{
			((ExtendedDrawContext) dc).getGeographicSurfaceTileRenderer().setElevationOffset(oldElevationOffset);
		}
	}

	@Override
	public String toDefinition(Element layerElement)
	{
		return DEFINITION_STRING + "(" + elevationOffset + ")";
	}

	@Override
	public IDelegate fromDefinition(String definition, Element layerElement, AVList params)
	{
		if (definition.toLowerCase().startsWith(DEFINITION_STRING.toLowerCase()))
		{
			Pattern pattern = Pattern.compile("(?:\\(([\\d.\\-]+)\\))");
			Matcher matcher = pattern.matcher(definition);
			if (matcher.find())
			{
				double elevationOffset = Double.parseDouble(matcher.group(1));
				return new ElevationOffsetRenderDelegate(elevationOffset);
			}
		}
		return null;
	}
}
