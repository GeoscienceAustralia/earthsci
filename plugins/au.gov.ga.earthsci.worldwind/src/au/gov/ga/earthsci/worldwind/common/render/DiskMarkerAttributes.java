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

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerShape;
import au.gov.ga.earthsci.worldwind.common.render.DiskMarkerShape.Disk;

/**
 * {@link MarkerAttributes} that support a {@link Disk} shape.
 *
 * @author Michael de Hoog
 */
public class DiskMarkerAttributes extends BasicMarkerAttributes
{
	public DiskMarkerAttributes()
	{
		setShapeType(DiskMarkerShape.DISK);
	}

	@Override
	public MarkerShape getShape(DrawContext dc)
	{
		String shapeType = getShapeType();
		MarkerShape shape = (MarkerShape) dc.getValue(shapeType);

		if (shape == null)
		{
			shape = DiskMarkerShape.createShapeInstance(shapeType);
			dc.setValue(shapeType, shape);
		}

		return shape;
	}
}
