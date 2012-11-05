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
package au.gov.ga.earthsci.worldwind.common.layers.geometry.types.airspace;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.AbstractAirspace;
import gov.nasa.worldwind.render.airspaces.Airspace;
import gov.nasa.worldwind.render.airspaces.Polygon;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import au.gov.ga.earthsci.worldwind.common.layers.geometry.GeometryLayer;
import au.gov.ga.earthsci.worldwind.common.layers.geometry.Shape;
import au.gov.ga.earthsci.worldwind.common.layers.geometry.types.GeometryLayerBase;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StyleAndText;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * An implementation of the {@link GeometryLayer} interface that renders each
 * shape as a separate {@link Airspace}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AirspaceGeometryLayer extends GeometryLayerBase implements GeometryLayer
{
	private List<AirspaceShape> airspaceShapes = new ArrayList<AirspaceShape>();

	public AirspaceGeometryLayer(AVList params)
	{
		super(params);
	}

	@Override
	public Iterable<? extends Shape> getShapes()
	{
		return airspaceShapes;
	}

	@Override
	public void addShape(Shape shape)
	{
		synchronized (airspaceShapes)
		{
			if (shape == null)
			{
				return;
			}
			airspaceShapes.add(new AirspaceShape(shape));
		}
	}

	@Override
	public void renderGeometry(DrawContext dc)
	{
		synchronized (airspaceShapes)
		{
			for (AirspaceShape airspaceShape : airspaceShapes)
			{
				airspaceShape.render(dc);
			}
		}
	}

	/**
	 * A container class that associates a {@link Shape} with a renderable
	 * {@link Airspace}.
	 */
	private class AirspaceShape implements Shape, Renderable
	{
		private AtomicBoolean dirty = new AtomicBoolean(true);
		private AbstractAirspace airspace;
		private Shape shapeDelegate;

		public AirspaceShape(Shape shape)
		{
			Validate.notNull(shape, "A shape is required");
			this.shapeDelegate = shape;
		}

		@Override
		public String getId()
		{
			return shapeDelegate.getId();
		}

		@Override
		public List<? extends ShapePoint> getPoints()
		{
			return shapeDelegate.getPoints();
		}

		@Override
		public void addPoint(Position p, AVList attributeValues)
		{
			shapeDelegate.addPoint(p, attributeValues);
			dirty.set(true);
		}

		@Override
		public void addPoint(ShapePoint p)
		{
			shapeDelegate.addPoint(p);
			dirty.set(true);
		}

		@Override
		public Type getType()
		{
			return shapeDelegate.getType();
		}

		/**
		 * Generates the airspace used to render the associated shape
		 */
		private void generateAirspace()
		{
			switch (shapeDelegate.getType())
			{
			case LINE:
			{
				airspace = new ShapeOutlineCurtain(getPoints());
				break;
			}
			case POLYGON:
			{
				airspace = new Polygon(getPoints());
				break;
			}
			}

			applyStyleToAirspace();

			dirty.set(false);
		}

		private void applyStyleToAirspace()
		{
			StyleAndText style = getStyleProvider().getStyle(AirspaceGeometryLayer.this);
			if (style == null)
			{
				applyDefaultStyleToAirspace();
			}
			else
			{
				style.style.setPropertiesFromAttributes((URL) getValue(AVKeyMore.CONTEXT_URL),
						AirspaceGeometryLayer.this, airspace, airspace.getAttributes(), airspace.getRenderer());
			}
		}

		/**
		 * Apply some sensible defaults to the airspace in case no style was
		 * found
		 */
		private void applyDefaultStyleToAirspace()
		{
			airspace.setTerrainConforming(true, false);
			airspace.setAltitudes(0, 100000);
			airspace.getAttributes().setOpacity(0.5);
			airspace.getAttributes().setOutlineWidth(2.0d);
			airspace.getRenderer().setEnableAntialiasing(true);
		}

		@Override
		public void render(DrawContext dc)
		{
			if (dirty.get())
			{
				generateAirspace();
			}
			airspace.render(dc);
		}
	}

}
