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
package au.gov.ga.earthsci.worldwind.common.util;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.FlatGlobe;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;

import javax.media.opengl.GL2;

/**
 * Uses the OpenGL clipping planes to clip the geometry around a given sector.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SectorClipPlanes
{
	private Sector sector = null;
	private boolean dirty = false;
	private double[] planes;

	/**
	 * Setup the clipping planes to clip around the given sector.
	 * 
	 * @param sector
	 */
	public void clipSector(Sector sector)
	{
		this.sector = sector;
		dirty = true;
	}

	/**
	 * Clear the sector clipping planes so that no geometry is clipped.
	 */
	public void clear()
	{
		this.sector = null;
		dirty = true;
	}

	/**
	 * Enable the clipping planes.
	 * 
	 * @param dc
	 */
	public void enableClipping(DrawContext dc)
	{
		if (dirty)
		{
			if (sector == null)
			{
				planes = null;
			}
			else
			{
				planes = computeSectorClippingPlanes(dc.getGlobe(), sector);
			}
			dirty = false;
		}

		if (planes != null)
		{
			GL2 gl = dc.getGL();

			gl.glClipPlane(GL2.GL_CLIP_PLANE0, planes, 0);
			gl.glClipPlane(GL2.GL_CLIP_PLANE1, planes, 4);
			gl.glClipPlane(GL2.GL_CLIP_PLANE2, planes, 8);
			gl.glClipPlane(GL2.GL_CLIP_PLANE3, planes, 12);

			gl.glEnable(GL2.GL_CLIP_PLANE0);
			gl.glEnable(GL2.GL_CLIP_PLANE1);
			gl.glEnable(GL2.GL_CLIP_PLANE2);
			gl.glEnable(GL2.GL_CLIP_PLANE3);
		}
	}

	/**
	 * Disable the clipping planes.
	 * 
	 * @param dc
	 */
	public void disableClipping(DrawContext dc)
	{
		GL2 gl = dc.getGL();

		gl.glDisable(GL2.GL_CLIP_PLANE0);
		gl.glDisable(GL2.GL_CLIP_PLANE1);
		gl.glDisable(GL2.GL_CLIP_PLANE2);
		gl.glDisable(GL2.GL_CLIP_PLANE3);
	}

	/**
	 * Calculate 4 clipping planes which clip the geomtry around the provided
	 * sector.
	 * 
	 * @param globe
	 *            Current globe
	 * @param sector
	 *            Sector to clip around
	 * @return An array of length 16, containing 4x 4-value vectors representing
	 *         4 clipping planes
	 */
	protected static double[] computeSectorClippingPlanes(Globe globe, Sector sector)
	{
		if (globe instanceof FlatGlobe)
		{
			//TODO implement:
			//Clipping on flat earth: instead of all planes going through (0,0,0) (D = 0), calculate
			//distance of min/max lat/lon from 0,0,0, and use that for the plane's D value (plane
			//normals will be (1,0,0),(-1,0,0),(0,1,0),(0,-1,0) for left,right,top,bottom respectively).
			throw new UnsupportedOperationException("Sector clipping does not support FlatGlobe's");
		}
		else
		{
			double[] planes = new double[16];

			LatLon centroid = sector.getCentroid();
			LatLon minLon = new LatLon(centroid.latitude, sector.getMinLongitude());
			LatLon maxLon = new LatLon(centroid.latitude, sector.getMaxLongitude());
			LatLon minLat = new LatLon(sector.getMinLatitude(), centroid.longitude);
			LatLon maxLat = new LatLon(sector.getMaxLatitude(), centroid.longitude);

			Vec4 center = globe.computePointFromLocation(centroid).normalize3();
			Vec4 minX = globe.computePointFromLocation(minLon).normalize3();
			Vec4 maxX = globe.computePointFromLocation(maxLon).normalize3();
			Vec4 minY = globe.computePointFromLocation(minLat).normalize3();
			Vec4 maxY = globe.computePointFromLocation(maxLat).normalize3();

			Vec4 up = Vec4.UNIT_Y;
			Vec4 left = center.cross3(up);
			Vec4 leftPlaneNormal = up.cross3(minX);
			Vec4 rightPlaneNormal = maxX.cross3(up);
			Vec4 topPlaneNormal = left.cross3(minY);
			Vec4 bottomPlaneNormal = maxY.cross3(left);

			planes[0] = leftPlaneNormal.x;
			planes[1] = leftPlaneNormal.y;
			planes[2] = leftPlaneNormal.z;
			planes[3] = 0;
			planes[4] = rightPlaneNormal.x;
			planes[5] = rightPlaneNormal.y;
			planes[6] = rightPlaneNormal.z;
			planes[7] = 0;
			planes[8] = topPlaneNormal.x;
			planes[9] = topPlaneNormal.y;
			planes[10] = topPlaneNormal.z;
			planes[11] = 0;
			planes[12] = bottomPlaneNormal.x;
			planes[13] = bottomPlaneNormal.y;
			planes[14] = bottomPlaneNormal.z;
			planes[15] = 0;

			return planes;
		}
	}
}
