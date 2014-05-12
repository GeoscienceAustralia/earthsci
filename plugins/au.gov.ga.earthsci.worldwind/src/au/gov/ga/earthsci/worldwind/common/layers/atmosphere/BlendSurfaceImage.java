/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.layers.atmosphere;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

/**
 * {@link SurfaceImage} that allows customisation of the blending function used
 * (via values passed to glBlendFunc).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BlendSurfaceImage extends SurfaceImage
{
	private int srcBlend = GL2.GL_ONE;
	private int dstBlend = GL2.GL_ONE_MINUS_SRC_ALPHA;

	public BlendSurfaceImage()
	{
		super();
	}

	public BlendSurfaceImage(Object imageSource, Iterable<? extends LatLon> corners)
	{
		super(imageSource, corners);
	}

	public BlendSurfaceImage(Object imageSource, Sector sector)
	{
		super(imageSource, sector);
	}

	public void resetBlendFunc()
	{
		srcBlend = GL2.GL_ONE;
		dstBlend = GL2.GL_ONE_MINUS_SRC_ALPHA;
	}

	public void setBlendFunc(int srcBlend, int dstBlend)
	{
		this.srcBlend = srcBlend;
		this.dstBlend = dstBlend;
	}

	@Override
	public void render(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull"); //$NON-NLS-1$
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (dc.isPickingMode() && !this.isPickEnabled())
		{
			return;
		}

		if (this.getSector() == null || !this.getSector().intersects(dc.getVisibleSector()))
		{
			return;
		}

		if (this.sourceTexture == null)
		{
			return;
		}

		GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
		try
		{
			if (!dc.isPickingMode())
			{
				double opacity = dc.getCurrentLayer() != null
						? this.getOpacity() * dc.getCurrentLayer().getOpacity() : this.getOpacity();

				if (opacity < 1)
				{
					gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_POLYGON_BIT | GL2.GL_CURRENT_BIT);
					// Enable blending using white premultiplied by the current opacity.
					gl.glColor4d(opacity, opacity, opacity, opacity);
				}
				else
				{
					gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_POLYGON_BIT);
				}
				gl.glEnable(GL.GL_BLEND);
				gl.glBlendFunc(srcBlend, dstBlend);
			}
			else
			{
				gl.glPushAttrib(GL2.GL_POLYGON_BIT);
			}

			gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
			gl.glEnable(GL.GL_CULL_FACE);
			gl.glCullFace(GL.GL_BACK);

			dc.getGeographicSurfaceTileRenderer().renderTiles(dc, this.thisList);
		}
		finally
		{
			gl.glPopAttrib();
		}
	}
}
