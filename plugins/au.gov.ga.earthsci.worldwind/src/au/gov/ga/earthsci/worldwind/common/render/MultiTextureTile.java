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

import gov.nasa.worldwind.render.DrawContext;

/**
 * Represents a texture tile that supports multi-texturing, using the
 * {@link MultiTextureSurfaceTileRenderer}. If one tile in a particular layer
 * implements this interface, all others tiles in that layer should also.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface MultiTextureTile
{
	/**
	 * @return The number of <b>extra</b> textures associated with this tile
	 *         (for multitexturing). If this tile only has a single texture,
	 *         return 0. All tiles associated with a particular layer should
	 *         return the same value from this function.
	 */
	int extraTextureCount();

	/**
	 * Bind this texture. If any extra textures are required, they should be
	 * bound at the texture units starting with the unit given in
	 * <code>firstExtraTextureUnit</code>.
	 * 
	 * @param dc
	 *            {@link DrawContext}
	 * @param firstExtraTextureUnit
	 *            If extra textures are required, bind them to successive
	 *            texture units starting at this unit.
	 * @param remainingExtraTextureUnits
	 *            Number of texture units available for <b>extra</b> textures.
	 * @return True if bind succeeds.
	 */
	boolean bind(DrawContext dc, int firstExtraTextureUnit, int remainingExtraTextureUnits);

	/**
	 * Apply the texture transform to this tile for the given texture.
	 * 
	 * @param dc
	 *            {@link DrawContext}
	 * @param textureIdentityActive
	 *            Does the current texture matrix contain the identity?
	 * @param texture
	 *            Texture index to apply the internal transform for. This
	 *            function will be called the number of times returned by
	 *            {@link #extraTextureCount()}, incremeting this value each time
	 *            (starting from 0).
	 */
	void applyInternalTransform(DrawContext dc, boolean textureIdentityActive, int texture);
}
