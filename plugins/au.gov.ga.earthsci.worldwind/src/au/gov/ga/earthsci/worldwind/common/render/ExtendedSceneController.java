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
package au.gov.ga.earthsci.worldwind.common.render;

import gov.nasa.worldwind.AbstractSceneController;
import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.terrain.Tessellator;

import java.util.ConcurrentModificationException;

/**
 * {@link SceneController} that uses a separate {@link Tessellator} to generate
 * a separate set of flat geometry, used by layers that are rendered onto a flat
 * surface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class ExtendedSceneController extends AbstractSceneController
{
	private FlatRectangularTessellator flatTessellator = new FlatRectangularTessellator();

	public ExtendedSceneController()
	{
		dc.dispose();
		dc = new ExtendedDrawContext();
	}

	@Override
	protected void createTerrain(DrawContext dc)
	{
		super.createTerrain(dc);

		if (dc instanceof ExtendedDrawContext)
		{
			ExtendedDrawContext edc = (ExtendedDrawContext) dc;
			if (edc.getFlatSurfaceGeometry() == null)
			{
				if (dc.getModel() != null && dc.getModel().getGlobe() != null)
				{
					SectorGeometryList sgl = flatTessellator.tessellate(dc);
					edc.setFlatSurfaceGeometry(sgl);
				}
			}
		}
	}

	@Override
	protected void pickTerrain(DrawContext dc)
	{
		try
		{
			super.pickTerrain(dc);
		}
		catch (ConcurrentModificationException e)
		{
			//ignore CME, seems to be a bug in the SectorGeometryList
		}
	}

	@Override
	protected void pickLayers(DrawContext dc)
	{
		super.pickLayers(dc);
		afterPickLayers(dc);
	}

	@Override
	protected void preRenderOrderedSurfaceRenderables(DrawContext dc)
	{
		//preRenderOrderedSurfaceRenderables is called immediately after prerendering the layer list, so we
		//can inject our overridable function here
		afterPreRenderLayers(dc);
		super.preRenderOrderedSurfaceRenderables(dc);
	}

	@Override
	protected void drawOrderedSurfaceRenderables(DrawContext dc)
	{
		//drawOrderedSurfaceRenderables is called immediately after drawing the layer list, so we can inject
		//our overridable function here
		afterDrawLayers(dc);
		super.drawOrderedSurfaceRenderables(dc);
	}

	/**
	 * Called immediately after the layer list is prerendered. Subclasses can
	 * override to add custom functionality.
	 * 
	 * @param dc
	 */
	protected void afterPreRenderLayers(DrawContext dc)
	{
	}

	/**
	 * Called immediately after the layer list is drawn. Subclasses can override
	 * to add custom functionality.
	 * 
	 * @param dc
	 */
	protected void afterDrawLayers(DrawContext dc)
	{
	}

	/**
	 * Called immediately after the layer list is picked. Subclasses can
	 * override to add custom functionality.
	 * 
	 * @param dc
	 */
	protected void afterPickLayers(DrawContext dc)
	{
	}
}
