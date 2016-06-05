/*******************************************************************************
 * Copyright 2016 Geoscience Australia
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
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.markers.MarkerRenderer;

/**
 * {@link MarkerRenderer} that supports picking of subsurface markers.
 *
 * @author Michael de Hoog
 */
public class DeepPickingMarkerRenderer extends MarkerRenderer
{
	private boolean oldDeepPicking;

	@Override
	protected boolean intersectsFrustum(DrawContext dc, Vec4 point, double radius)
	{
		return dc.getView().getFrustumInModelCoordinates().contains(point);
	}

	@Override
	protected void begin(DrawContext dc)
	{
		if (dc.isPickingMode())
		{
			oldDeepPicking = dc.isDeepPickingEnabled();
			dc.setDeepPickingEnabled(true);
		}
		super.begin(dc);
	}

	@Override
	protected void end(DrawContext dc)
	{
		super.end(dc);
		if (dc.isPickingMode())
		{
			dc.setDeepPickingEnabled(oldDeepPicking);
		}
	}
}
