/*******************************************************************************
 * Copyright 2015 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.layers.worldmap;

import gov.nasa.worldwind.render.DrawContext;

import com.jogamp.opengl.util.texture.Texture;

/**
 * The original {@link gov.nasa.worldwind.layers.WorldMapLayer} has a bug where,
 * when multiple instances are created, only the first is rendered correctly,
 * due to the iconWidth and iconHeight variables not being initialized by the
 * initializeTexture() due to the texture being cached. This class fixes the
 * bug.
 *
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WorldMapLayer extends gov.nasa.worldwind.layers.WorldMapLayer
{
	@Override
	protected void drawIcon(DrawContext dc)
	{
		if (iconWidth == 0 || iconHeight == 0)
		{
			Texture iconTexture = dc.getTextureCache().getTexture(this.getIconFilePath());
			if (iconTexture != null)
			{
				iconWidth = iconTexture.getWidth();
				iconHeight = iconTexture.getHeight();
			}
		}

		super.drawIcon(dc);
	}
}
