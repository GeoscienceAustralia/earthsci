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
package au.gov.ga.earthsci.worldwind.common.render.fastshape;

import gov.nasa.worldwind.render.DrawContext;

/**
 * Listener interface for {@link FastShape} render events.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface FastShapeRenderListener
{
	/**
	 * Called by the given {@link FastShape} prior to rendering itself. Note
	 * that this is not called if the shape adds itself to the
	 * {@link DrawContext}'s ordered renderable list.
	 * 
	 * @param dc
	 *            {@link DrawContext}
	 * @param shape
	 *            {@link FastShape} about to be rendered.
	 */
	void shapePreRender(DrawContext dc, FastShape shape);

	/**
	 * Called by the given {@link FastShape} after rendering itself.
	 * 
	 * @param dc
	 *            {@link DrawContext}
	 * @param shape
	 *            {@link FastShape} that was just rendered.
	 */
	void shapePostRender(DrawContext dc, FastShape shape);

	/**
	 * Empty implementation of the {@link FastShapeRenderListener}.
	 */
	public abstract class FastShapeRenderAdapter implements FastShapeRenderListener
	{
		@Override
		public void shapePreRender(DrawContext dc, FastShape shape)
		{
		}

		@Override
		public void shapePostRender(DrawContext dc, FastShape shape)
		{
		}
	}
}
