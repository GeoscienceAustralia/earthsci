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
package au.gov.ga.earthsci.worldwind.common.render;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DeclutterableText;
import gov.nasa.worldwind.render.DeclutteringTextRenderer;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Subclass of {@link DeclutteringTextRenderer} that fixes the text draw point
 * computation for viewports with a non-zero origin.
 *
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DeclutteringTextViewportRenderer extends DeclutteringTextRenderer
{
	private Rectangle viewport;

	@Override
	protected Point2D.Float computeDrawPoint(Rectangle2D rect, Vec4 screenPoint)
	{
		Point2D.Float point = super.computeDrawPoint(rect, screenPoint);
		if (viewport != null)
		{
			point.x -= viewport.x;
			point.y -= viewport.y;
		}
		return point;
	}

	@Override
	protected Vec4 drawText(DrawContext dc, DeclutterableText uText, double scale, double opacity) throws Exception
	{
		viewport = dc.getView().getViewport();
		return super.drawText(dc, uText, scale, opacity);
	}
}
