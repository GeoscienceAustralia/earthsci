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

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerShape;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

/**
 * Disk marker shape.
 *
 * @author Michael de Hoog
 */
public class DiskMarkerShape extends BasicMarkerShape
{
	public static final String DISK = "gov.nasa.worldwind.render.markers.Disk";

	public static MarkerShape createShapeInstance(String shapeType)
	{
		if (shapeType == DISK)
		{
			return new Disk();
		}
		return BasicMarkerShape.createShapeInstance(shapeType);
	}

	public static class Disk extends Shape
	{
		@Override
		protected void initialize(DrawContext dc)
		{
			super.initialize(dc);

			this.name = "Disk";
			this.shapeType = DiskMarkerShape.DISK;
			this.isInitialized = true;
		}

		@Override
		protected void doRender(DrawContext dc, Marker marker, Vec4 point, double size, int[] dlResource)
		{
			Vec4 orientation = dc.getGlobe().computeSurfaceNormalAtPoint(point);

			// Heading only applies to cylinder if pitch is also specified. A heading without pitch spins the cylinder
			// around its axis. A heading with pitch spins the cylinder, and then tilts it in the direction of the
			// heading.
			if (this.isApplyOrientation() && marker.getPitch() != null)
			{
				orientation = this.computeOrientationVector(dc, point, orientation,
						marker.getHeading() != null ? marker.getHeading() : Angle.ZERO,
						marker.getPitch());
			}

			// This performs the same operation as Vec4.axisAngle() but with a "v2" of <0, 0, 1>.
			// Compute rotation angle
			Angle angle = Angle.fromRadians(Math.acos(orientation.z));
			// Compute the direction cosine factors that define the rotation axis
			double A = -orientation.y;
			double B = orientation.x;
			double L = Math.sqrt(A * A + B * B);

			GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
			gl.glRotated(angle.degrees, A / L, B / L, 0); // rotate to proper heading and pitch

			gl.glScaled(size, size, size); // scale
			gl.glCallList(dlResource[0]);
		}

		@Override
		protected int drawShape(DrawContext dc, double radius)
		{
			int slices = 20;
			int loops = 1;
			GLU glu = dc.getGLU();
			glu.gluDisk(quadric, 0d, 1d, slices, loops);
			return slices * 2 * 2 * 4; //vertices and normals, inner and outer, assume float coords (4 bytes)
		}
	}
}
