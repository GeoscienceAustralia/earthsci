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
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;

import java.util.List;

/**
 * A {@link Shape} is a collection of {@link Position}s that has an ID unique for a given {@link GeometryLayer}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface Shape
{
	/**
	 * @return The ID of this shape. Shape IDs are unique within a single {@link GeometryLayer}.
	 */
	String getId();
	
	/**
	 * @return The ordered list of points associated with this shape
	 */
	List<? extends ShapePoint> getPoints();
	
	/**
	 * Add the provided position to the end of this shape's list of points.
	 */
	void addPoint(Position p, AVList attributeValues);
	
	/**
	 * Add the provided point to the end of this shape's list of points
	 */
	void addPoint(ShapePoint p);
	
	/**
	 * @return The type of shape this is
	 */
	Type getType();
	
	/**
	 * The type of shape this is
	 */
	enum Type
	{
		POINT, LINE, POLYGON
	}
	
	/**
	 * A point in a shape. Contains a {@link Position} and a list of attribute values 
	 * that may be used during rendering etc. 
	 */
	class ShapePoint extends Position
	{
		private AVList attributeValues = new AVListImpl();
		
		public ShapePoint(Position position, AVList attributeValues)
		{
			super(position.getLatitude(), position.getLongitude(), position.getElevation());
			if (attributeValues != null)
			{
				this.attributeValues.setValues(attributeValues);
			}
		}
		
		public ShapePoint(Angle latitude, Angle longitude, double elevation, AVList attributeValues)
		{
			super(latitude, longitude, elevation);
			if (attributeValues != null)
			{
				this.attributeValues.setValues(attributeValues);
			}
		}

		public ShapePoint(LatLon latLon, double elevation, AVList attributeValues)
		{
			super(latLon, elevation);
			if (attributeValues != null)
			{
				this.attributeValues.setValues(attributeValues);
			}
		}
		
		public Object getAttributeValue(String attributeName)
		{
			return attributeValues.getValue(attributeName);
		}
		
		public void setAttributeValue(String attributeName, Object value)
		{
			attributeValues.setValue(attributeName, value);
		}
	}
}
