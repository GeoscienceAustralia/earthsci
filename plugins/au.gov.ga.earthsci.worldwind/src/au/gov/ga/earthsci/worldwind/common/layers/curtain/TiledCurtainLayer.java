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
package au.gov.ga.earthsci.worldwind.common.layers.curtain;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLTextRenderer;
import gov.nasa.worldwind.util.PerformanceStatistic;
import gov.nasa.worldwind.util.WWXML;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import javax.media.opengl.GL2;
import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.Bounded;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.exaggeration.VerticalExaggerationAccessor;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * {@link Layer} which renders a textured surface along a horizontal line (ie a
 * curtain).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class TiledCurtainLayer extends AbstractLayer implements Bounded
{
	//TODO where should this live
	protected CurtainTileRenderer renderer = new CurtainTileRenderer();

	protected Sector boundingSector;
	protected Path path;
	protected double curtainTop = 0;
	protected double curtainBottom = -10000;
	protected boolean followTerrain = false; //TODO how do we do this?
	protected int subsegments = 1;

	// Infrastructure
	protected static final LevelComparer levelComparer = new LevelComparer();
	protected final CurtainLevelSet levels;
	protected List<CurtainTextureTile> topLevels;
	protected boolean forceLevelZeroLoads = false;
	protected boolean levelZeroLoaded = false;
	protected boolean retainLevelZeroTiles = false;
	protected String tileCountName;
	protected double detailHintOrigin = 2.8;
	protected double detailHint = 0;
	protected boolean useMipMaps = true;
	protected boolean useTransparentTextures = false;
	protected List<String> supportedImageFormats = new ArrayList<String>();
	protected String textureFormat;

	// Diagnostic flags
	protected boolean drawTileBoundaries = false;
	protected boolean drawTileIDs = false;
	protected boolean drawBoundingVolumes = false;

	// Stuff computed each frame
	protected List<CurtainTextureTile> currentTiles = new ArrayList<CurtainTextureTile>();
	protected CurtainTextureTile currentResourceTile;
	protected boolean atMaxResolution = false;
	protected PriorityBlockingQueue<Runnable> requestQ = new PriorityBlockingQueue<Runnable>(200);

	abstract protected void requestTexture(DrawContext dc, CurtainTextureTile tile);

	abstract protected void forceTextureLoad(CurtainTextureTile tile);

	public TiledCurtainLayer(CurtainLevelSet levelSet)
	{
		if (levelSet == null)
		{
			String message = Logging.getMessage("nullValue.LevelSetIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		this.levels = new CurtainLevelSet(levelSet); // the caller's levelSet may change internally, so we copy it.

		this.setPickEnabled(false); // textures are assumed to be terrain unless specifically indicated otherwise.
		this.tileCountName = this.getName() + " Tiles";
	}

	@Override
	public Object setValue(String key, Object value)
	{
		// Offer it to the level set
		if (this.getLevels() != null)
			this.getLevels().setValue(key, value);

		return super.setValue(key, value);
	}

	@Override
	public Object getValue(String key)
	{
		Object value = super.getValue(key);

		return value != null ? value : this.getLevels().getValue(key); // see if the level set has it
	}

	@Override
	public void setName(String name)
	{
		super.setName(name);
		this.tileCountName = this.getName() + " Tiles";
	}

	public Path getPath()
	{
		return path;
	}

	public void setPath(Path path)
	{
		this.path = path;
	}

	public double getCurtainTop()
	{
		return curtainTop;
	}

	public void setCurtainTop(double curtainTop)
	{
		this.curtainTop = curtainTop;
	}

	public double getCurtainBottom()
	{
		return curtainBottom;
	}

	public void setCurtainBottom(double curtainBottom)
	{
		this.curtainBottom = curtainBottom;
	}

	public boolean isFollowTerrain()
	{
		return followTerrain;
	}

	public void setFollowTerrain(boolean followTerrain)
	{
		this.followTerrain = followTerrain;
	}

	public int getSubsegments()
	{
		return subsegments;
	}

	public void setSubsegments(int subsegments)
	{
		this.subsegments = subsegments;
	}

	public boolean isForceLevelZeroLoads()
	{
		return this.forceLevelZeroLoads;
	}

	public void setForceLevelZeroLoads(boolean forceLevelZeroLoads)
	{
		this.forceLevelZeroLoads = forceLevelZeroLoads;
	}

	public boolean isRetainLevelZeroTiles()
	{
		return retainLevelZeroTiles;
	}

	public void setRetainLevelZeroTiles(boolean retainLevelZeroTiles)
	{
		this.retainLevelZeroTiles = retainLevelZeroTiles;
	}

	public boolean isDrawTileIDs()
	{
		return drawTileIDs;
	}

	public void setDrawTileIDs(boolean drawTileIDs)
	{
		this.drawTileIDs = drawTileIDs;
	}

	public boolean isDrawTileBoundaries()
	{
		return drawTileBoundaries;
	}

	public void setDrawTileBoundaries(boolean drawTileBoundaries)
	{
		this.drawTileBoundaries = drawTileBoundaries;
	}

	public boolean isDrawBoundingVolumes()
	{
		return drawBoundingVolumes;
	}

	public void setDrawBoundingVolumes(boolean drawBoundingVolumes)
	{
		this.drawBoundingVolumes = drawBoundingVolumes;
	}

	/**
	 * Indicates the layer's detail hint, which is described in
	 * {@link #setDetailHint(double)}.
	 * 
	 * @return the detail hint
	 * 
	 * @see #setDetailHint(double)
	 */
	public double getDetailHint()
	{
		return this.detailHint;
	}

	/**
	 * Modifies the default relationship of image resolution to screen
	 * resolution as the viewing altitude changes. Values greater than 0 cause
	 * imagery to appear at higher resolution at greater altitudes than normal,
	 * but at an increased performance cost. Values less than 0 decrease the
	 * default resolution at any given altitude. The default value is 0. Values
	 * typically range between -0.5 and 0.5.
	 * <p/>
	 * Note: The resolution-to-height relationship is defined by a scale factor
	 * that specifies the approximate size of discernable lengths in the image
	 * relative to eye distance. The scale is specified as a power of 10. A
	 * value of 3, for example, specifies that 1 meter on the surface should be
	 * distinguishable from an altitude of 10^3 meters (1000 meters). The
	 * default scale is 1/10^2.8, (1 over 10 raised to the power 2.8). The
	 * detail hint specifies deviations from that default. A detail hint of 0.2
	 * specifies a scale of 1/1000, i.e., 1/10^(2.8 + .2) = 1/10^3. Scales much
	 * larger than 3 typically cause the applied resolution to be higher than
	 * discernable for the altitude. Such scales significantly decrease
	 * performance.
	 * 
	 * @param detailHint
	 *            the degree to modify the default relationship of image
	 *            resolution to screen resolution with changing view altitudes.
	 *            Values greater than 1 increase the resolution. Values less
	 *            than zero decrease the resolution. The default value is 0.
	 */
	public void setDetailHint(double detailHint)
	{
		this.detailHint = detailHint;
	}

	protected CurtainLevelSet getLevels()
	{
		return levels;
	}

	protected PriorityBlockingQueue<Runnable> getRequestQ()
	{
		return requestQ;
	}

	@Override
	public boolean isMultiResolution()
	{
		return this.getLevels() != null && this.getLevels().getNumLevels() > 1;
	}

	@Override
	public boolean isAtMaxResolution()
	{
		return this.atMaxResolution;
	}

	/**
	 * Returns the format used to store images in texture memory, or null if
	 * images are stored in their native format.
	 * 
	 * @return the texture image format; null if images are stored in their
	 *         native format.
	 * 
	 * @see {@link #setTextureFormat(String)}
	 */
	public String getTextureFormat()
	{
		return this.textureFormat;
	}

	/**
	 * Specifies the format used to store images in texture memory, or null to
	 * store images in their native format. Suppported texture formats are as
	 * follows:
	 * <ul>
	 * <li><code>image/dds</code> - Stores images in the compressed DDS format.
	 * If the image is already in DDS format it's stored as-is.</li>
	 * </ul>
	 * 
	 * @param textureFormat
	 *            the texture image format; null to store images in their native
	 *            format.
	 */
	public void setTextureFormat(String textureFormat)
	{
		this.textureFormat = textureFormat;
	}

	public boolean isUseMipMaps()
	{
		return useMipMaps;
	}

	public void setUseMipMaps(boolean useMipMaps)
	{
		this.useMipMaps = useMipMaps;
	}

	public boolean isUseTransparentTextures()
	{
		return this.useTransparentTextures;
	}

	public void setUseTransparentTextures(boolean useTransparentTextures)
	{
		this.useTransparentTextures = useTransparentTextures;
	}

	/**
	 * Specifies the time of the layer's most recent dataset update, beyond
	 * which cached data is invalid. If greater than zero, the layer ignores and
	 * eliminates any in-memory or on-disk cached data older than the time
	 * specified, and requests new information from the data source. If zero,
	 * the default, the layer applies any expiry times associated with its
	 * individual levels, but only for on-disk cached data. In-memory cached
	 * data is expired only when the expiry time is specified with this method
	 * and is greater than zero. This method also overwrites the expiry times of
	 * the layer's individual levels if the value specified to the method is
	 * greater than zero.
	 * 
	 * @param expiryTime
	 *            the expiry time of any cached data, expressed as a number of
	 *            milliseconds beyond the epoch. The default expiry time is
	 *            zero.
	 * 
	 * @see System#currentTimeMillis() for a description of milliseconds beyond
	 *      the epoch.
	 */
	@Override
	public void setExpiryTime(long expiryTime) // Override this method to use intrinsic level-specific expiry times
	{
		super.setExpiryTime(expiryTime);

		if (expiryTime > 0)
			this.levels.setExpiryTime(expiryTime); // remove this in sub-class to use level-specific expiry times
	}

	public List<CurtainTextureTile> getTopLevels()
	{
		if (this.topLevels == null)
			this.createTopLevelTiles();

		return topLevels;
	}

	protected void createTopLevelTiles()
	{
		CurtainLevel level = levels.getFirstLevel();
		int rowCount = level.getRowCount();
		int colCount = level.getColumnCount();

		this.topLevels = new ArrayList<CurtainTextureTile>(rowCount * colCount);

		for (int row = 0; row < rowCount; row++)
		{
			for (int col = 0; col < colCount; col++)
			{
				Segment segment = level.computeSegmentForRowColumn(row, col);
				CurtainTextureTile tile = createCurtainTextureTile(segment, level, row, col);
				this.topLevels.add(tile);
			}
		}
	}

	protected CurtainTextureTile createCurtainTextureTile(Segment segment, CurtainLevel level, int row, int col)
	{
		return new CurtainTextureTile(segment, level, row, col);
	}

	protected void loadAllTopLevelTextures(DrawContext dc)
	{
		for (CurtainTextureTile tile : this.getTopLevels())
		{
			if (!tile.isTextureInMemory(dc.getTextureCache()))
			{
				this.forceTextureLoad(tile);
			}
		}

		this.levelZeroLoaded = true;
	}

	@Override
	public Sector getSector()
	{
		if (boundingSector == null)
		{
			boundingSector = path == null ? null : path.getBoundingSector();
		}
		return boundingSector;
	}

	// ============== Tile Assembly ======================= //
	// ============== Tile Assembly ======================= //
	// ============== Tile Assembly ======================= //

	protected void assembleTiles(DrawContext dc)
	{
		this.currentTiles.clear();

		for (CurtainTextureTile tile : this.getTopLevels())
		{
			if (this.isTileVisible(dc, tile))
			{
				this.currentResourceTile = null;
				this.addTileOrDescendants(dc, tile);
			}
		}
	}

	protected void addTileOrDescendants(DrawContext dc, CurtainTextureTile tile)
	{
		if (this.meetsRenderCriteria(dc, tile))
		{
			this.addTile(dc, tile);
			return;
		}

		// The incoming tile does not meet the rendering criteria, so it must be subdivided and those
		// subdivisions tested against the criteria.

		// All tiles that meet the selection criteria are drawn, but some of those tiles will not have
		// textures associated with them either because their texture isn't loaded yet or because they
		// are finer grain than the layer has textures for. In these cases the tiles use the texture of
		// the closest ancestor that has a texture loaded. This ancestor is called the currentResourceTile.
		// A texture transform is applied during rendering to align the sector's texture coordinates with the
		// appropriate region of the ancestor's texture.

		CurtainTextureTile ancestorResource = null;

		try
		{
			// TODO: Revise this to reflect that the parent layer is only requested while the algorithm continues
			// to search for the layer matching the criteria.
			// At this point the tile does not meet the render criteria but it may have its texture in memory.
			// If so, register this tile as the resource tile. If not, then this tile will be the next level
			// below a tile with texture in memory. So to provide progressive resolution increase, add this tile
			// to the draw list. That will cause the tile to be drawn using its parent tile's texture, and it will
			// cause it's texture to be requested. At some future call to this method the tile's texture will be in
			// memory, it will not meet the render criteria, but will serve as the parent to a tile that goes
			// through this same process as this method recurses. The result of all this is that a tile isn't rendered
			// with its own texture unless all its parents have their textures loaded. In addition to causing
			// progressive resolution increase, this ensures that the parents are available as the user zooms out, and
			// therefore the layer remains visible until the user is zoomed out to the point the layer is no longer
			// active.
			if (tile.isTextureInMemory(dc.getTextureCache()) || tile.getLevelNumber() == 0)
			{
				ancestorResource = this.currentResourceTile;
				this.currentResourceTile = tile;
			}
			else if (!tile.getLevel().isEmpty())
			{
				//                this.addTile(dc, tile);
				//                return;

				// Issue a request for the parent before descending to the children.
				//                if (tile.getLevelNumber() < this.levels.getNumLevels())
				//                {
				//                    // Request only tiles with data associated at this level
				//                    if (!this.levels.isResourceAbsent(tile))
				//                        this.requestTexture(dc, tile);
				//                }
			}

			CurtainTextureTile[] subTiles = tile.createSubTiles(this.levels.getLevel(tile.getLevelNumber() + 1));
			for (CurtainTextureTile child : subTiles)
			{
				if (this.isTileVisible(dc, child))
				{
					this.addTileOrDescendants(dc, child);
				}
			}
		}
		finally
		{
			if (ancestorResource != null) // Pop this tile as the currentResource ancestor
			{
				this.currentResourceTile = ancestorResource;
			}
		}
	}

	protected void addTile(DrawContext dc, CurtainTextureTile tile)
	{
		tile.setFallbackTile(null);

		if (tile.isTextureInMemory(dc.getTextureCache()))
		{
			this.addTileToCurrent(tile);
			return;
		}

		// Level 0 loads may be forced
		if (tile.getLevelNumber() == 0 && this.forceLevelZeroLoads && !tile.isTextureInMemory(dc.getTextureCache()))
		{
			this.forceTextureLoad(tile);
			if (tile.isTextureInMemory(dc.getTextureCache()))
			{
				this.addTileToCurrent(tile);
				return;
			}
		}

		// Tile's texture isn't available, so request it
		if (tile.getLevelNumber() < this.levels.getNumLevels())
		{
			// Request only tiles with data associated at this level
			if (!this.levels.isResourceAbsent(tile))
				this.requestTexture(dc, tile);
		}

		// Set up to use the currentResource tile's texture
		if (this.currentResourceTile != null)
		{
			if (this.currentResourceTile.getLevelNumber() == 0 && this.forceLevelZeroLoads
					&& !this.currentResourceTile.isTextureInMemory(dc.getTextureCache())
					&& !this.currentResourceTile.isTextureInMemory(dc.getTextureCache()))
				this.forceTextureLoad(this.currentResourceTile);

			if (this.currentResourceTile.isTextureInMemory(dc.getTextureCache()))
			{
				tile.setFallbackTile(currentResourceTile);
				this.addTileToCurrent(tile);
			}
		}
	}

	protected void addTileToCurrent(CurtainTextureTile tile)
	{
		this.currentTiles.add(tile);
	}

	protected boolean isTileVisible(DrawContext dc, CurtainTextureTile tile)
	{
		//		Segment segment = tile.getSegment();
		//		Extent extent = path.getSegmentExtent(dc, segment, curtainTop, curtainBottom, subsegments, followTerrain);

		// TODO: Fix this - see CWW-129
		Sector pathBoundingSector = getSector();
		Extent extent =
				Sector.computeBoundingBox(dc.getGlobe(),
						VerticalExaggerationAccessor.getGlobalVerticalExaggeration(dc), pathBoundingSector);

		return extent.intersects(dc.getView().getFrustumInModelCoordinates());
	}

	protected boolean meetsRenderCriteria(DrawContext dc, CurtainTextureTile tile)
	{
		return this.levels.isFinalLevel(tile.getLevelNumber()) || !needToSplit(dc, tile);
	}

	protected double getDetailFactor()
	{
		return this.detailHintOrigin + this.getDetailHint();
	}

	protected boolean needToSplit(DrawContext dc, CurtainTextureTile tile)
	{
		Vec4[] points =
				path.getPointsInSegment(dc, tile.getSegment(), curtainTop, curtainBottom, subsegments, followTerrain);
		Vec4 centerPoint = path.getSegmentCenterPoint(dc, tile.getSegment(), curtainTop, curtainBottom, followTerrain);

		View view = dc.getView();
		Vec4 eyePoint = view.getEyePoint();

		double texelSize = tile.getLevel().getTexelSize();
		double minDistance = eyePoint.distanceTo3(centerPoint);
		double cellHeight = centerPoint.getLength3() * texelSize;

		for (Vec4 point : points)
		{
			double distance = eyePoint.distanceTo3(point);
			if (distance < minDistance)
			{
				minDistance = distance;
				cellHeight = point.getLength3() * texelSize;
			}
		}

		// Split when the cell height (length of a texel) becomes greater than the specified fraction of the eye
		// distance. The fraction is specified as a power of 10. For example, a detail factor of 3 means split when the
		// cell height becomes more than one thousandth of the eye distance. Another way to say it is, use the current
		// tile if its cell height is less than the specified fraction of the eye distance.
		//
		// NOTE: It's tempting to instead compare a screen pixel size to the texel size, but that calculation is
		// window-size dependent and results in selecting an excessive number of tiles when the window is large.
		return cellHeight > minDistance * Math.pow(10, -this.getDetailFactor());
	}

	// ============== Rendering ======================= //
	// ============== Rendering ======================= //
	// ============== Rendering ======================= //

	@Override
	public void render(DrawContext dc)
	{
		//this.atMaxResolution = this.atMaxLevel(dc); //TODO calculate
		super.render(dc);
	}

	@Override
	protected final void doRender(DrawContext dc)
	{
		if (this.forceLevelZeroLoads && !this.levelZeroLoaded)
			this.loadAllTopLevelTextures(dc);
		if (dc.getSurfaceGeometry() == null || dc.getSurfaceGeometry().size() < 1)
			return;

		//TODO add tile boundary rendering to the curtain tile renderer
		//dc.getGeographicSurfaceTileRenderer().setShowImageTileOutlines(this.showImageTileOutlines);

		draw(dc);
	}

	protected void draw(DrawContext dc)
	{
		this.assembleTiles(dc); // Determine the tiles to draw.

		if (this.currentTiles.size() >= 1)
		{
			if (this.getScreenCredit() != null)
			{
				dc.addScreenCredit(this.getScreenCredit());
			}

			CurtainTextureTile[] sortedTiles = new CurtainTextureTile[this.currentTiles.size()];
			sortedTiles = this.currentTiles.toArray(sortedTiles);
			Arrays.sort(sortedTiles, levelComparer);

			GL2 gl = dc.getGL();

			if (this.isUseTransparentTextures() || this.getOpacity() < 1)
			{
				gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_POLYGON_BIT | GL2.GL_CURRENT_BIT);
				this.setBlendingFunction(dc);
			}
			else
			{
				gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_POLYGON_BIT);
			}

			gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
			gl.glEnable(GL2.GL_CULL_FACE);
			gl.glCullFace(GL2.GL_BACK);

			dc.setPerFrameStatistic(PerformanceStatistic.IMAGE_TILE_COUNT, this.tileCountName, this.currentTiles.size());
			renderer.renderTiles(dc, this.currentTiles, getPath(), getCurtainTop(), getCurtainBottom(),
					getSubsegments(), isFollowTerrain());

			gl.glPopAttrib();

			if (this.drawTileIDs)
				this.drawTileIDs(dc, this.currentTiles);

			if (this.drawBoundingVolumes)
				this.drawBoundingVolumes(dc, this.currentTiles);

			// Check texture expiration. Memory-cached textures are checked for expiration only when an explicit,
			// non-zero expiry time has been set for the layer. If none has been set, the expiry times of the layer's
			// individual levels are used, but only for images in the local file cache, not textures in memory. This is
			// to avoid incurring the overhead of checking expiration of in-memory textures, a very rarely used feature.
			if (this.getExpiryTime() > 0 && this.getExpiryTime() < System.currentTimeMillis())
				this.checkTextureExpiration(dc, this.currentTiles);

			this.currentTiles.clear();
		}

		this.sendRequests();
		this.requestQ.clear();
	}

	protected void checkTextureExpiration(DrawContext dc, List<CurtainTextureTile> tiles)
	{
		for (CurtainTextureTile tile : tiles)
		{
			if (tile.isTextureExpired())
				this.requestTexture(dc, tile);
		}
	}

	protected void setBlendingFunction(DrawContext dc)
	{
		// Set up a premultiplied-alpha blending function. Any texture read by JOGL will have alpha-premultiplied color
		// components, as will any DDS file created by World Wind or the World Wind WMS. We'll also set up the base
		// color as a premultiplied color, so that any incoming premultiplied color will be properly combined with the
		// base color.

		GL2 gl = dc.getGL();

		double alpha = this.getOpacity();
		gl.glColor4d(alpha, alpha, alpha, alpha);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE_MINUS_SRC_ALPHA);
	}

	protected void sendRequests()
	{
		Runnable task = this.requestQ.poll();
		while (task != null)
		{
			if (!WorldWind.getTaskService().isFull())
			{
				WorldWind.getTaskService().addTask(task);
			}
			task = this.requestQ.poll();
		}
	}

	@Override
	public boolean isLayerInView(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (dc.getView() == null)
		{
			String message = Logging.getMessage("layers.AbstractLayer.NoViewSpecifiedInDrawingContext");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		Extent extent = path.getSegmentExtent(dc, Segment.FULL, curtainTop, curtainBottom, 1, followTerrain);
		return extent.intersects(dc.getView().getFrustumInModelCoordinates());

		/*return dc.getVisibleSector() == null
				|| dc.getVisibleSector().intersectsSegment(path.getPercentLatLon(0d),
						path.getPercentLatLon(1d));*/
	}

	protected Vec4 computeReferencePoint(DrawContext dc)
	{
		if (dc.getViewportCenterPosition() != null)
			return dc.getGlobe().computePointFromPosition(dc.getViewportCenterPosition());

		java.awt.geom.Rectangle2D viewport = dc.getView().getViewport();
		int x = (int) viewport.getWidth() / 2;
		for (int y = (int) (0.5 * viewport.getHeight()); y >= 0; y--)
		{
			Position pos = dc.getView().computePositionFromScreenPoint(x, y);
			if (pos == null)
				continue;

			return dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(), 0d);
		}

		return null;
	}

	protected Vec4 getReferencePoint(DrawContext dc)
	{
		return this.computeReferencePoint(dc);
	}

	protected static class LevelComparer implements Comparator<CurtainTextureTile>
	{
		@Override
		public int compare(CurtainTextureTile ta, CurtainTextureTile tb)
		{
			int la = ta.getFallbackTile() == null ? ta.getLevelNumber() : ta.getFallbackTile().getLevelNumber();
			int lb = tb.getFallbackTile() == null ? tb.getLevelNumber() : tb.getFallbackTile().getLevelNumber();

			return la < lb ? -1 : la == lb ? 0 : 1;
		}
	}

	protected void drawTileIDs(DrawContext dc, List<CurtainTextureTile> tiles)
	{
		java.awt.Rectangle viewport = dc.getView().getViewport();
		TextRenderer textRenderer =
				OGLTextRenderer.getOrCreateTextRenderer(dc.getTextRendererCache(),
						java.awt.Font.decode("Arial-Plain-13"));

		dc.getGL().glDisable(GL2.GL_DEPTH_TEST);
		dc.getGL().glDisable(GL2.GL_BLEND);
		dc.getGL().glDisable(GL2.GL_TEXTURE_2D);

		textRenderer.beginRendering(viewport.width, viewport.height);
		textRenderer.setColor(java.awt.Color.YELLOW);
		for (CurtainTextureTile tile : tiles)
		{
			String tileLabel = tile.getLabel();

			if (tile.getFallbackTile() != null)
				tileLabel += "/" + tile.getFallbackTile().getLabel();

			Vec4 pt = path.getSegmentCenterPoint(dc, tile.getSegment(), curtainTop, curtainBottom, followTerrain);
			pt = dc.getView().project(pt);
			textRenderer.draw(tileLabel, (int) pt.x, (int) pt.y);
		}
		textRenderer.setColor(java.awt.Color.WHITE);
		textRenderer.endRendering();
	}

	protected void drawBoundingVolumes(DrawContext dc, List<CurtainTextureTile> tiles)
	{
		float[] previousColor = new float[4];
		dc.getGL().glGetFloatv(GL2.GL_CURRENT_COLOR, previousColor, 0);
		dc.getGL().glColor3d(0, 1, 0);

		for (CurtainTextureTile tile : tiles)
		{
			Extent extent =
					path.getSegmentExtent(dc, tile.getSegment(), curtainTop, curtainBottom, subsegments, followTerrain);
			if (extent instanceof Renderable)
				((Renderable) extent).render(dc);

			/*dc.getGL().glBegin(GL.GL_POINTS);
			Vec4[] points = path.getPointsInSegment(dc, tile.getSegment(), top, bottom);
			for (Vec4 point : points)
			{
				dc.getGL().glVertex3d(point.x, point.y, point.z);
			}
			dc.getGL().glEnd();*/
		}

		dc.getGL().glColor4fv(previousColor, 0);
	}

	//**************************************************************//
	//********************  Configuration  *************************//
	//**************************************************************//

	public static AVList getTiledCurtainLayerConfigParams(Element domElement, AVList params)
	{
		if (domElement == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		// Common layer properties.
		AbstractLayer.getLayerConfigParams(domElement, params);

		// LevelSet properties.
		CurtainDataConfigurationUtils.getLevelSetConfigParams(domElement, params);

		// Service properties.
		WWXML.checkAndSetStringParam(domElement, params, AVKey.SERVICE_NAME, "Service/@serviceName", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, AVKey.RETRIEVE_PROPERTIES_FROM_SERVICE,
				"RetrievePropertiesFromService", xpath);

		// Image format properties.
		WWXML.checkAndSetStringParam(domElement, params, AVKey.IMAGE_FORMAT, "ImageFormat", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKey.TEXTURE_FORMAT, "TextureFormat", xpath);
		WWXML.checkAndSetUniqueStringsParam(domElement, params, AVKey.AVAILABLE_IMAGE_FORMATS,
				"AvailableImageFormats/ImageFormat", xpath);

		// Optional behavior properties.
		WWXML.checkAndSetBooleanParam(domElement, params, AVKey.FORCE_LEVEL_ZERO_LOADS, "ForceLevelZeroLoads", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, AVKey.RETAIN_LEVEL_ZERO_TILES, "RetainLevelZeroTiles", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, AVKey.USE_MIP_MAPS, "UseMipMaps", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, AVKey.USE_TRANSPARENT_TEXTURES, "UseTransparentTextures",
				xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKey.DETAIL_HINT, "DetailHint", xpath);
		WWXML.checkAndSetColorArrayParam(domElement, params, AVKey.TRANSPARENCY_COLORS, "TransparencyColors/Color",
				xpath);

		// Retrieval properties. Convert the Long time values to Integers, because BasicTiledImageLayer is expecting
		// Integer values.
		WWXML.checkAndSetTimeParamAsInteger(domElement, params, AVKey.URL_CONNECT_TIMEOUT,
				"RetrievalTimeouts/ConnectTimeout/Time", xpath);
		WWXML.checkAndSetTimeParamAsInteger(domElement, params, AVKey.URL_READ_TIMEOUT,
				"RetrievalTimeouts/ReadTimeout/Time", xpath);
		WWXML.checkAndSetTimeParamAsInteger(domElement, params, AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT,
				"RetrievalTimeouts/StaleRequestLimit/Time", xpath);

		// Curtain specific properties.
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.CURTAIN_TOP, "CurtainTop", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.CURTAIN_BOTTOM, "CurtainBottom", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, AVKeyMore.FOLLOW_TERRAIN, "FollowTerrain", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKeyMore.SUBSEGMENTS, "Subsegments", xpath);

		// Curtain path
		List<LatLon> positions = new ArrayList<LatLon>();
		Element[] latlons = WWXML.getElements(domElement, "Path/LatLon", xpath);
		for (Element latlon : latlons)
		{
			LatLon ll = WWXML.getLatLon(latlon, null, xpath);
			positions.add(ll);
		}
		Path path = new Path(positions);
		params.setValue(AVKeyMore.PATH, path);

		// Parse the legacy configuration parameters. This enables TiledImageLayer to recognize elements from previous
		// versions of configuration documents.
		getLegacyTiledImageLayerConfigParams(domElement, params);

		return params;
	}

	/**
	 * Parses TiledImageLayer configuration parameters from previous versions of
	 * configuration documents. This writes output as key-value pairs to params.
	 * If a parameter from the XML document already exists in params, that
	 * parameter is ignored. Supported key and parameter names are:
	 * <table>
	 * <tr>
	 * <th>Parameter</th>
	 * <th>Element Path</th>
	 * <th>Type</th>
	 * </tr>
	 * <tr>
	 * <td>{@link AVKey#TEXTURE_FORMAT}</td>
	 * <td>CompressTextures</td>
	 * <td>"image/dds" if CompressTextures is "true"; null otherwise</td>
	 * </tr>
	 * </table>
	 * 
	 * @param domElement
	 *            the XML document root to parse for legacy TiledImageLayer
	 *            configuration parameters.
	 * @param params
	 *            the output key-value pairs which recieve the TiledImageLayer
	 *            configuration parameters. A null reference is permitted.
	 * 
	 * @return a reference to params, or a new AVList if params is null.
	 * 
	 * @throws IllegalArgumentException
	 *             if the document is null.
	 */
	protected static AVList getLegacyTiledImageLayerConfigParams(Element domElement, AVList params)
	{
		if (domElement == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		Object o = params.getValue(AVKey.TEXTURE_FORMAT);
		if (o == null)
		{
			Boolean b = WWXML.getBoolean(domElement, "CompressTextures", xpath);
			if (b != null && b)
				params.setValue(AVKey.TEXTURE_FORMAT, "image/dds");
		}

		return params;
	}

	// ============== Image Composition ======================= //
	// ============== Image Composition ======================= //
	// ============== Image Composition ======================= //

	public List<String> getAvailableImageFormats()
	{
		return new ArrayList<String>(this.supportedImageFormats);
	}

	public boolean isImageFormatAvailable(String imageFormat)
	{
		return imageFormat != null && this.supportedImageFormats.contains(imageFormat);
	}

	public String getDefaultImageFormat()
	{
		return this.supportedImageFormats.size() > 0 ? this.supportedImageFormats.get(0) : null;
	}

	protected void setAvailableImageFormats(String[] formats)
	{
		this.supportedImageFormats.clear();

		if (formats != null)
			this.supportedImageFormats.addAll(Arrays.asList(formats));
	}
}
