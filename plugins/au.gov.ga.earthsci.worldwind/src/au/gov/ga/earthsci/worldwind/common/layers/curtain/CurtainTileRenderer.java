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
package au.gov.ga.earthsci.worldwind.common.layers.curtain;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.logging.Level;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.render.ExtendedDrawContext;

/**
 * Helper class used to render a curtain's {@link CurtainTile}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CurtainTileRenderer
{
	public void renderTile(DrawContext dc, CurtainTextureTile tile, Path path, double top, double bottom,
			int subsegments, boolean followTerrain)
	{
		if (tile == null)
		{
			String message = Logging.getMessage("nullValue.TileIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		ArrayList<CurtainTextureTile> al = new ArrayList<CurtainTextureTile>(1);
		al.add(tile);
		this.renderTiles(dc, al, path, top, bottom, subsegments, followTerrain);
		al.clear();
	}

	public void renderTiles(DrawContext dc, Iterable<? extends CurtainTextureTile> tiles, Path path, double top,
			double bottom, int subsegments, boolean followTerrain)
	{
		if (tiles == null)
		{
			String message = Logging.getMessage("nullValue.TileIterableIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		ExtendedDrawContext.applyWireframePolygonMode(dc);

		GL2 gl = dc.getGL();

		gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT // for alpha func
				| GL2.GL_ENABLE_BIT | GL2.GL_CURRENT_BIT | GL2.GL_DEPTH_BUFFER_BIT // for depth func
				| GL2.GL_TEXTURE_BIT // for texture env
				| GL2.GL_TRANSFORM_BIT);

		try
		{
			gl.glDisable(GL2.GL_CULL_FACE);

			gl.glEnable(GL2.GL_DEPTH_TEST);
			gl.glDepthFunc(GL2.GL_LEQUAL);

			gl.glEnable(GL2.GL_ALPHA_TEST);
			gl.glAlphaFunc(GL2.GL_GREATER, 0.01f);

			gl.glActiveTexture(GL2.GL_TEXTURE0);
			gl.glEnable(GL2.GL_TEXTURE_2D);
			gl.glMatrixMode(GL2.GL_TEXTURE);
			gl.glPushMatrix();
			if (!dc.isPickingMode())
			{
				gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
			}
			else
			{
				gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);
				gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_SRC0_RGB, GL2.GL_PREVIOUS);
				gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GL2.GL_REPLACE);
			}

			for (CurtainTextureTile tile : tiles)
			{
				if (tile.bind(dc))
				{
					gl.glMatrixMode(GL2.GL_TEXTURE);
					gl.glLoadIdentity();
					tile.applyInternalTransform(dc);

					SegmentGeometry geometry = path.getGeometry(dc, tile, top, bottom, subsegments, followTerrain);
					geometry.render(dc, 1);
				}
			}

			gl.glActiveTexture(GL2.GL_TEXTURE0);
			gl.glMatrixMode(GL2.GL_TEXTURE);
			gl.glPopMatrix();
			gl.glDisable(GL2.GL_TEXTURE_2D);
		}
		catch (Exception e)
		{
			Logging.logger().log(Level.SEVERE,
					Logging.getMessage("generic.ExceptionWhileRenderingLayer", this.getClass().getName()), e);
		}
		finally
		{
			// TODO: pop matrix stack too, for all texture units
			gl.glPopAttrib();
		}
	}
}
