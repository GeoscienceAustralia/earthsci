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
package au.gov.ga.earthsci.worldwind.common.layers.geometry;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;

import java.util.ArrayList;
import java.util.List;

import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * A default implementation of the {@link Shape} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicShapeImpl implements Shape
{
	private String id;
	private Type type;
	private List<ShapePoint> points = new ArrayList<ShapePoint>();
	
	/**
	 * Create a new shape of the provided type
	 */
	public BasicShapeImpl(String id, Type type)
	{
		Validate.notBlank(id, "An ID must be provided");
		Validate.notNull(type, "A type must be provided");
		
		this.type = type;
		this.id = id;
	}
	
	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public List<? extends ShapePoint> getPoints()
	{
		return points;
	}

	@Override
	public void addPoint(ShapePoint p)
	{
		if (p == null)
		{
			return;
		}
		points.add(p);
	}
	
	@Override
	public void addPoint(Position p, AVList attributeValues)
	{
		if (p == null)
		{
			return;
		}
		points.add(new ShapePoint(p, attributeValues));
	}

	@Override
	public Type getType()
	{
		return type;
	}

}
