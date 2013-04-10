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

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.Curtain;
import gov.nasa.worldwind.render.airspaces.Geometry;
import gov.nasa.worldwind.util.OGLStackHandler;

import javax.media.opengl.GL2;

/**
 * An extension of the {@link Curtain} airspace that can render the generating
 * shape as a line at the upper and lower elevations.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ShapeOutlineCurtain extends Curtain implements ShapeOutlineAirspace
{
	private static final int GEOMETRY_TYPE_ELEMENT = 1;
	private static final int GEOMETRY_TYPE_VERTEX = 2;

	boolean drawCurtain = true;
	boolean drawUpperShapeOutline = false;
	boolean drawLowerShapeOutline = false;

	public ShapeOutlineCurtain()
	{
		super();
	}

	public ShapeOutlineCurtain(AirspaceAttributes attributes)
	{
		super(attributes);
	}

	public ShapeOutlineCurtain(Iterable<? extends LatLon> locations)
	{
		super(locations);
	}

	@Override
	protected void doRenderGeometry(DrawContext dc, String drawStyle)
	{
		setExpiryTime(0);
		if (drawCurtain)
		{
			super.doRenderGeometry(dc, drawStyle);
		}
		if (drawUpperShapeOutline)
		{
			renderUpperShapeOutline(dc);
		}
		if (drawLowerShapeOutline)
		{
			renderLowerShapeOutline(dc);
		}
	}

	private void renderUpperShapeOutline(DrawContext dc)
	{
		Vec4 refCenter = computeReferenceCenter(dc);
		Geometry vertexGeometry = getCurtainGeometry(dc, refCenter).getVertexGeometry();

		int count = vertexGeometry.getCount(GEOMETRY_TYPE_VERTEX) - 2;
		int[] shapeIndices = new int[count];
		int i = 0;
		while (i < count)
		{
			shapeIndices[i] = i + 1;
			shapeIndices[i + 1] = i + 3;
			i += 2;
		}

		Geometry shapeOutlineElementGeometry = new Geometry();
		shapeOutlineElementGeometry.setElementData(GL2.GL_LINES, count, shapeIndices);

		drawShapeOutline(dc, refCenter, vertexGeometry, shapeOutlineElementGeometry);
	}

	private void renderLowerShapeOutline(DrawContext dc)
	{
		Vec4 refCenter = computeReferenceCenter(dc);
		Geometry vertexGeometry = getCurtainGeometry(dc, refCenter).getVertexGeometry();

		int count = vertexGeometry.getCount(GEOMETRY_TYPE_VERTEX) - 2;
		int[] shapeIndices = new int[count];
		int i = 0;
		while (i < count)
		{
			shapeIndices[i] = i;
			shapeIndices[i + 1] = i + 2;
			i += 2;
		}

		Geometry shapeOutlineElementGeometry = new Geometry();
		shapeOutlineElementGeometry.setElementData(GL2.GL_LINES, count, shapeIndices);

		drawShapeOutline(dc, refCenter, vertexGeometry, shapeOutlineElementGeometry);
	}

	private void drawShapeOutline(DrawContext dc, Vec4 refCenter, Geometry vertexGeometry,
			Geometry shapeOutlineElementGeometry)
	{

		dc.getView().pushReferenceCenter(dc, refCenter);
		GL2 gl = dc.getGL().getGL2();
		OGLStackHandler stack = new OGLStackHandler();
		stack.pushAttrib(gl, GL2.GL_CURRENT_BIT | GL2.GL_HINT_BIT | GL2.GL_ENABLE_BIT | GL2.GL_DEPTH_BUFFER_BIT
				| GL2.GL_POINT_BIT | GL2.GL_COLOR_BUFFER_BIT | GL2.GL_LIGHTING_BIT | GL2.GL_POINT_BIT);
		stack.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);
		try
		{
			// Points are drawn over the line to prevent gaps forming when 
			// antialiasing and smoothing is applied to the line
			setupDrawParams(dc, gl);

			gl.glDepthMask(false);
			drawShapeOutlineAsLines(dc, shapeOutlineElementGeometry, vertexGeometry);
			drawShapeOutlineAsPoints(dc, shapeOutlineElementGeometry, vertexGeometry);

			gl.glDepthMask(true);
			drawShapeOutlineAsLines(dc, shapeOutlineElementGeometry, vertexGeometry);

		}
		finally
		{
			dc.getView().popReferenceCenter(dc);
			stack.pop(gl);
		}
	}

	private void setupDrawParams(DrawContext dc, GL2 gl)
	{
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glEnable(GL2.GL_LINE_SMOOTH);
		gl.glEnable(GL2.GL_POINT_SMOOTH);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
		gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);
		getAttributes().applyOutline(dc, false);
		gl.glPointSize((float) getAttributes().getOutlineWidth());
	}

	private void drawShapeOutlineAsLines(DrawContext dc, Geometry shapeOutlineElementGeometry, Geometry vertexGeometry)
	{
		getRenderer().drawGeometry(dc, shapeOutlineElementGeometry, vertexGeometry);
	}

	private void drawShapeOutlineAsPoints(DrawContext dc, Geometry shapeOutlineElementGeometry, Geometry vertexGeometry)
	{
		getRenderer().drawGeometry(dc, GL2.GL_POINTS, shapeOutlineElementGeometry.getCount(GEOMETRY_TYPE_ELEMENT),
				shapeOutlineElementGeometry.getGLType(GEOMETRY_TYPE_ELEMENT),
				shapeOutlineElementGeometry.getBuffer(GEOMETRY_TYPE_ELEMENT), vertexGeometry);
	}

	private CurtainGeometry getCurtainGeometry(DrawContext dc, Vec4 refCenter)
	{
		return getCurtainGeometry(dc, locations.size(), locations.toArray(new LatLon[locations.size()]), pathType,
				splitThreshold, getAltitudes(), isTerrainConforming(), refCenter);
	}

	public void setDrawCurtain(boolean drawCurtain)
	{
		this.drawCurtain = drawCurtain;
	}

	@Override
	public void setDrawUpperShapeOutline(boolean drawUpperShapeOutline)
	{
		this.drawUpperShapeOutline = drawUpperShapeOutline;
	}

	@Override
	public void setDrawLowerShapeOutline(boolean drawLowerShapeOutline)
	{
		this.drawLowerShapeOutline = drawLowerShapeOutline;
	}
}
