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
package au.gov.ga.earthsci.worldwind.common.terrain;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.RectangularTessellator;
import gov.nasa.worldwind.terrain.RectangularTessellatorAccessible;
import gov.nasa.worldwind.terrain.SectorGeometry;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.media.opengl.GL2;

/**
 * Subclass of the {@link RectangularTessellator} that adds several features:
 * <ul>
 * <li>Ability to enable/disable depth testing for the elevation wireframe.</li>
 * <li>Ability to disable backface culling for tiled image layers.</li>
 * <li>Smart skirts: instead of skirts that go down to the minimum elevation,
 * smart skirts are skirts generated from the vertices of neighbouring tiles,
 * ensuring that no gaps exist, but also ensuring that skirts don't get in the
 * way of sub-surface navigation.</li>
 * </ul>
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WireframeRectangularTessellator extends RectangularTessellatorAccessible
{
	private boolean wireframeDepthTesting = true;
	private boolean backfaceCulling = false;
	private boolean smartSkirts = true;

	/**
	 * @return Is depth testing enabled for the elevation model wireframe?
	 */
	public boolean isWireframeDepthTesting()
	{
		return wireframeDepthTesting;
	}

	/**
	 * Enable/disable depth testing for the elevation model wireframe.
	 * 
	 * @param wireframeDepthTesting
	 */
	public void setWireframeDepthTesting(boolean wireframeDepthTesting)
	{
		this.wireframeDepthTesting = wireframeDepthTesting;
	}

	/**
	 * @return Is backface culling enabled for surface tiles (tiled image
	 *         layers)?
	 */
	public boolean isBackfaceCulling()
	{
		return backfaceCulling;
	}

	/**
	 * Enable/disable backface culling for surface tiles.
	 * 
	 * @param backfaceCulling
	 */
	public void setBackfaceCulling(boolean backfaceCulling)
	{
		this.backfaceCulling = backfaceCulling;
	}

	/**
	 * @return Are smart skirts enabled? Smart skirts are skirts that use
	 *         neighbouring tile vertices as skirts, instead of skirts that go
	 *         down to the minimum elevation. This helps sub-surface rendering.
	 */
	public boolean isSmartSkirts()
	{
		return smartSkirts;
	}

	/**
	 * Enable/disable smart skirts.
	 * 
	 * @param smartSkirts
	 */
	public void setSmartSkirts(boolean smartSkirts)
	{
		this.smartSkirts = smartSkirts;
	}

	@Override
	protected void renderWireframe(DrawContext dc, RectTile tile, boolean showTriangles, boolean showTileBoundary)
	{
		if (dc == null)
		{
			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		RenderInfo ri = tile.getRi();

		if (ri == null)
		{
			String msg = Logging.getMessage("nullValue.RenderInfoIsNull");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		dc.getView().pushReferenceCenter(dc, ri.getReferenceCenter());

		GL2 gl = dc.getGL().getGL2();
		gl.glPushAttrib(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_POLYGON_BIT | GL2.GL_TEXTURE_BIT | GL2.GL_ENABLE_BIT
				| GL2.GL_CURRENT_BIT);
		//gl.glEnable(GL.GL_BLEND);
		//gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
		//gl.glDisable(javax.media.opengl.GL.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_CULL_FACE);
		gl.glCullFace(GL2.GL_BACK);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glColor4d(0.6, 0.8, 0.8, 1.0);
		gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);

		if (isWireframeDepthTesting())
		{
			gl.glEnable(GL2.GL_POLYGON_OFFSET_LINE);
			gl.glPolygonOffset(-1, 1);
		}
		else
		{
			gl.glDisable(GL2.GL_DEPTH_TEST);
		}

		if (showTriangles)
		{
			OGLStackHandler ogsh = new OGLStackHandler();

			try
			{
				ogsh.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);

				gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);

				FloatBuffer vertices = ri.getVertices();
				IntBuffer indices = ri.getIndices();
				gl.glVertexPointer(3, GL2.GL_FLOAT, 0, vertices.rewind());
				gl.glDrawElements(GL2.GL_TRIANGLE_STRIP, indices.limit(), GL2.GL_UNSIGNED_INT, indices.rewind());
			}
			finally
			{
				ogsh.pop(gl);
			}
		}

		dc.getView().popReferenceCenter(dc);

		gl.glPopAttrib();

		if (showTileBoundary)
		{
			this.renderPatchBoundary(dc, tile);
		}
	}

	@Override
	protected long render(DrawContext dc, RectTile tile, int numTextureUnits)
	{
		if (!backfaceCulling)
		{
			dc.getGL().glDisable(GL2.GL_CULL_FACE);
		}
		return super.render(dc, tile, numTextureUnits);
	}

	@Override
	public synchronized SectorGeometryList tessellate(DrawContext dc)
	{
		SectorGeometryList currentTiles = super.tessellate(dc);

		if (isMakeTileSkirts() && smartSkirts)
		{
			Map<RectTileKey, RowColRectTile> tileMap = new HashMap<RectTileKey, RowColRectTile>();
			for (SectorGeometry t : currentTiles)
			{
				RowColRectTile tile = (RowColRectTile) t;
				RectTileKey tileKey = new RectTileKey(tile.getLevel(), tile.getRow(), tile.getColumn());
				tileMap.put(tileKey, tile);
			}
			for (SectorGeometry tile : currentTiles)
			{
				fixSkirts(dc, (RowColRectTile) tile, tileMap);
			}
		}

		//sort tiles from closest to eye to furthest away
		try
		{
			SortedMap<Double, SectorGeometry> sortedGeometry = new TreeMap<Double, SectorGeometry>();
			for (SectorGeometry tile : currentTiles)
			{
				double distanceToEyeSquared =
						tile.getExtent().getCenter().distanceToSquared3(dc.getView().getEyePoint());
				sortedGeometry.put(distanceToEyeSquared, tile);
			}
			currentTiles.clear();
			currentTiles.addAll(sortedGeometry.values());
		}
		catch (Exception e)
		{
		}

		return currentTiles;
	}

	@Override
	protected void makeVerts(DrawContext dc, RectTile tile)
	{
		//vertices are rebuilt if required in the super method
		((RowColRectTile) tile).rebuiltVertices = false;
		super.makeVerts(dc, tile);
	}

	@Override
	public boolean buildVerts(DrawContext dc, RectTile tile, boolean makeSkirts)
	{
		//mark the tile's vertices as rebuilt
		((RowColRectTile) tile).rebuiltVertices = true;
		return super.buildVerts(dc, tile, false);
	}

	@Override
	protected ArrayList<LatLon> computeLocations(RectTile tile)
	{
		//Changed to remove the latMax/lonMax calculations, as the small difference in the double
		//lat/lon locations between the skirts and the tile edges were causing large differences
		//in the returned elevation. Perhaps an ElevationModel bug?

		int density = tile.getDensity();
		int numVertices = (density + 3) * (density + 3);

		Sector sector = tile.getSector();
		Angle dLat = sector.getDeltaLat().divide(density);
		Angle lat = sector.getMinLatitude();

		Angle lonMin = sector.getMinLongitude();
		Angle dLon = sector.getDeltaLon().divide(density);

		ArrayList<LatLon> latlons = new ArrayList<LatLon>(numVertices);
		for (int j = 0; j <= density + 2; j++)
		{
			Angle lon = lonMin;
			for (int i = 0; i <= density + 2; i++)
			{
				latlons.add(new LatLon(lat, lon));

				if (i != 0 && i <= density)
				{
					lon = lon.add(dLon);
				}

				if (lon.degrees < -180)
				{
					lon = Angle.NEG180;
				}
				else if (lon.degrees > 180)
				{
					lon = Angle.POS180;
				}
			}

			if (j != 0 && j <= density)
			{
				lat = lat.add(dLat);
			}
		}

		return latlons;
	}

	protected void fixSkirts(DrawContext dc, RowColRectTile tile, Map<RectTileKey, RowColRectTile> tileMap)
	{
		int row = tile.getRow();
		int column = tile.getColumn();
		int level = tile.getLevel();
		int sRow = row / 2;
		int sColumn = column / 2;
		int sLevel = level - 1;
		boolean topHalf = row % 2 == 0;
		boolean leftHalf = column % 2 == 0;

		RowColRectTile sLeft = leftHalf ? tileMap.get(new RectTileKey(sLevel, sRow, sColumn - 1)) : null;
		RowColRectTile sRight = !leftHalf ? tileMap.get(new RectTileKey(sLevel, sRow, sColumn + 1)) : null;
		RowColRectTile sTop = topHalf ? tileMap.get(new RectTileKey(sLevel, sRow - 1, sColumn)) : null;
		RowColRectTile sBottom = !topHalf ? tileMap.get(new RectTileKey(sLevel, sRow + 1, sColumn)) : null;

		RowColRectTile left = sLeft == null ? tileMap.get(new RectTileKey(level, row, column - 1)) : null;
		RowColRectTile top = sTop == null ? tileMap.get(new RectTileKey(level, row - 1, column)) : null;

		boolean anyRebuilt =
				tile.rebuiltVertices || (sLeft != null && sLeft.rebuiltVertices)
						|| (sRight != null && sRight.rebuiltVertices) || (sTop != null && sTop.rebuiltVertices)
						|| (sBottom != null && sBottom.rebuiltVertices) || (left != null && left.rebuiltVertices)
						|| (top != null && top.rebuiltVertices);
		if (!anyRebuilt)
		{
			return;
		}

		FloatBuffer vertices = tile.getRi().getVertices();
		Vec4 refCenter = tile.getRi().getReferenceCenter();
		int density = tile.getDensity();
		int size = density + 3;

		if (sLeft != null)
		{
			FloatBuffer leftVertices = sLeft.getRi().getVertices();
			Vec4 leftRefCenter = sLeft.getRi().getReferenceCenter();
			int srcStart = topHalf ? 1 : density / 2 + 1;
			subdivideVerticesFromNeighboringSuperTile(leftVertices, vertices, leftRefCenter, refCenter, size, srcStart,
					size - 1, 0, true);
		}
		else if (left != null)
		{
			FloatBuffer leftVertices = left.getRi().getVertices();
			Vec4 leftRefCenter = left.getRi().getReferenceCenter();
			copyVerticesFromNeighboringTile(leftVertices, vertices, leftRefCenter, refCenter, size, size - 1, 0, true);
		}
		if (sRight != null)
		{
			FloatBuffer rightVertices = sRight.getRi().getVertices();
			Vec4 rightRefCenter = sRight.getRi().getReferenceCenter();
			int srcStart = topHalf ? 1 : density / 2 + 1;
			subdivideVerticesFromNeighboringSuperTile(rightVertices, vertices, rightRefCenter, refCenter, size,
					srcStart, 0, size - 1, true);
		}
		if (sTop != null)
		{
			FloatBuffer topVertices = sTop.getRi().getVertices();
			Vec4 topRefCenter = sTop.getRi().getReferenceCenter();
			int srcStart = leftHalf ? 1 : density / 2 + 1;
			subdivideVerticesFromNeighboringSuperTile(topVertices, vertices, topRefCenter, refCenter, size, srcStart,
					size - 1, 0, false);
		}
		else if (top != null)
		{
			FloatBuffer topVertices = top.getRi().getVertices();
			Vec4 topRefCenter = top.getRi().getReferenceCenter();
			copyVerticesFromNeighboringTile(topVertices, vertices, topRefCenter, refCenter, size, size - 1, 0, false);
		}
		if (sBottom != null)
		{
			FloatBuffer bottomVertices = sBottom.getRi().getVertices();
			Vec4 bottomRefCenter = sBottom.getRi().getReferenceCenter();
			int srcStart = leftHalf ? 1 : density / 2 + 1;
			subdivideVerticesFromNeighboringSuperTile(bottomVertices, vertices, bottomRefCenter, refCenter, size,
					srcStart, 0, size - 1, false);
		}

		if (dc.getGLRuntimeCapabilities().isUseVertexBufferObject())
		{
			GL2 gl = dc.getGL().getGL2();
			OGLStackHandler ogsh = new OGLStackHandler();

			try
			{
				ogsh.pushClientAttrib(gl, GL2.GL_CLIENT_VERTEX_ARRAY_BIT);

				int[] vboIds = (int[]) dc.getGpuResourceCache().get(tile.getRi().getVboCacheKey());
				if (vboIds != null)
				{
					gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vboIds[0]);
					gl.glBufferData(GL2.GL_ARRAY_BUFFER, vertices.limit() * 4, vertices.rewind(), GL2.GL_DYNAMIC_DRAW);
				}
			}
			finally
			{
				ogsh.pop(gl);
			}
		}
	}

	private void subdivideVerticesFromNeighboringSuperTile(FloatBuffer src, FloatBuffer dst, Vec4 srcRefCenter,
			Vec4 dstRefCenter, int size, int srcStart, int srcRC, int dstRC, boolean column)
	{
		int offsetFactor = (column ? 1 : size) * 3;
		int srcOffset = srcRC * offsetFactor;
		int dstOffset = dstRC * offsetFactor;
		int stride = (column ? size : 1) * 3;

		Vec4 last = null;
		for (int di = 1, si = srcStart; di < size - 1; di += 2, si++)
		{
			int srcIndex = srcOffset + si * stride;
			Vec4 current = new Vec4(src.get(srcIndex), src.get(srcIndex + 1), src.get(srcIndex + 2));
			current = current.add3(srcRefCenter).subtract3(dstRefCenter);

			Vec4 previous = last == null ? current : last.add3(current).divide3(2);
			last = current;

			int dstIndex = dstOffset + (di - 1) * stride;
			dst.put(dstIndex, (float) previous.x).put(dstIndex + 1, (float) previous.y)
					.put(dstIndex + 2, (float) previous.z);

			dstIndex += stride;
			dst.put(dstIndex, (float) current.x).put(dstIndex + 1, (float) current.y)
					.put(dstIndex + 2, (float) current.z);

			if (di >= size - 2)
			{
				dstIndex += stride;
				dst.put(dstIndex, (float) current.x).put(dstIndex + 1, (float) current.y)
						.put(dstIndex + 2, (float) current.z);
			}
		}
	}

	private void copyVerticesFromNeighboringTile(FloatBuffer src, FloatBuffer dst, Vec4 srcRefCenter,
			Vec4 dstRefCenter, int size, int srcRC, int dstRC, boolean column)
	{
		int offsetFactor = (column ? 1 : size) * 3;
		int srcOffset = srcRC * offsetFactor;
		int dstOffset = dstRC * offsetFactor;
		int stride = (column ? size : 1) * 3;

		for (int i = 0; i < size; i++)
		{
			//don't use skirts to copy from
			int srcIndex = srcOffset + i * stride;
			int dstIndex = dstOffset + i * stride;
			dst.put(dstIndex, src.get(srcIndex) + (float) (srcRefCenter.x - dstRefCenter.x));
			dst.put(dstIndex + 1, src.get(srcIndex + 1) + (float) (srcRefCenter.y - dstRefCenter.y));
			dst.put(dstIndex + 2, src.get(srcIndex + 2) + (float) (srcRefCenter.z - dstRefCenter.z));
		}
	}

	@Override
	protected RectTile[] split(DrawContext dc, RectTile tile)
	{
		//override the split() function to speed up the row/column calculation
		//we don't need to override the createTopLevelTiles() function, as it's only called once

		Sector[] sectors = tile.getSector().subdivide();

		int row = ((RowColRectTile) tile).getRow() * 2;
		int column = ((RowColRectTile) tile).getColumn() * 2;

		RectTile[] subTiles = new RectTile[4];
		subTiles[0] = this.createTile(dc, sectors[0], tile.getLevel() + 1, row, column);
		subTiles[1] = this.createTile(dc, sectors[1], tile.getLevel() + 1, row, column + 1);
		subTiles[2] = this.createTile(dc, sectors[2], tile.getLevel() + 1, row + 1, column);
		subTiles[3] = this.createTile(dc, sectors[3], tile.getLevel() + 1, row + 1, column + 1);

		return subTiles;
	}

	@Override
	protected RectTile createTile(DrawContext dc, Sector tileSector, int level)
	{
		double deltaLat = 180d / DEFAULT_NUM_LAT_SUBDIVISIONS;
		double deltaLon = 360d / DEFAULT_NUM_LON_SUBDIVISIONS;

		LatLon centroid = tileSector.getCentroid();
		int row = getTileY(centroid.latitude, Angle.NEG90, level, deltaLat);
		int column = getTileX(centroid.longitude, Angle.NEG180, level, deltaLon);

		return createTile(dc, tileSector, level, row, column);
	}

	protected RectTile createTile(DrawContext dc, Sector tileSector, int level, int row, int column)
	{
		Extent extent = Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(), tileSector);
		double cellSize = tileSector.getDeltaLatRadians() * dc.getGlobe().getRadius() / this.density;

		return new RowColRectTile(this, extent, level, this.density, tileSector, cellSize, row, column);
	}

	protected static int getTileX(Angle longitude, Angle longitudeOrigin, int level, double lztsd)
	{
		double layerpow = Math.pow(0.5, level);
		double X = (longitude.degrees - longitudeOrigin.degrees) / (lztsd * layerpow);
		return (int) X;
	}

	protected static int getTileY(Angle latitude, Angle latitudeOrigin, int level, double lztsd)
	{
		double layerpow = Math.pow(0.5, level);
		double Y = (latitude.degrees - latitudeOrigin.degrees) / (lztsd * layerpow);
		return (int) Y;
	}

	/**
	 * Subclass of {@link RectTile} that store the tile's row/column, so that
	 * neighbouring tiles can easily be calculated for the smart skirts.
	 * 
	 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
	 */
	protected static class RowColRectTile extends RectTile
	{
		protected boolean rebuiltVertices = false;
		protected final int row;
		protected final int column;

		public RowColRectTile(RectangularTessellator tessellator, Extent extent, int level, int density, Sector sector,
				double cellSize, int row, int column)
		{
			super(tessellator, extent, level, density, sector, cellSize);
			this.row = row;
			this.column = column;
		}

		@Override
		public int getLevel()
		{
			return level;
		}

		public int getRow()
		{
			return row;
		}

		public int getColumn()
		{
			return column;
		}

		@Override
		public String toString()
		{
			return "(" + level + "," + row + "," + column + ")";
		}
	}

	protected static class RectTileKey
	{
		protected final int level;
		protected final int row;
		protected final int column;

		public RectTileKey(int level, int row, int column)
		{
			this.level = level;
			this.row = row;
			this.column = column;
		}

		@Override
		public int hashCode()
		{
			int result;
			result = level;
			result = 29 * result + row;
			result = 29 * result + column;
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof RectTileKey)
			{
				RectTileKey key = (RectTileKey) obj;
				return key.level == this.level && key.column == this.column && key.row == this.row;
			}
			return false;
		}
	}
}
