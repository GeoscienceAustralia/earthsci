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

import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.TileKey;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * Extension of the {@link CurtainTile} class which contains texture data for
 * the tile.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CurtainTextureTile extends CurtainTile
{
	private volatile TextureData textureData; // if non-null, then must be converted to a Texture
	private CurtainTextureTile fallbackTile = null; // holds texture to use if own texture not available
	private boolean hasMipmapData = false;
	private long updateTime = 0;

	public static synchronized MemoryCache getMemoryCache()
	{
		//share TextureTile memory cache for now
		return gov.nasa.worldwind.layers.TextureTile.getMemoryCache();
	}

	public CurtainTextureTile(Segment segment, CurtainLevel level, int row, int column)
	{
		super(segment, level, row, column);
	}

	@Override
	public final long getSizeInBytes()
	{
		long size = super.getSizeInBytes();

		if (this.textureData != null)
		{
			size += this.textureData.getEstimatedMemorySize();
		}

		return size;
	}

	public CurtainTextureTile getFallbackTile()
	{
		return this.fallbackTile;
	}

	public void setFallbackTile(CurtainTextureTile fallbackTile)
	{
		this.fallbackTile = fallbackTile;
	}

	/**
	 * Returns the texture data most recently specified for the tile. New
	 * texture data is typically specified when a new image is read, either
	 * initially or in response to image expiration.
	 * <p/>
	 * If texture data is non-null, a new texture is created from the texture
	 * data when the tile is next bound or otherwise initialized. The texture
	 * data field is then set to null. Subsequently setting texture data to be
	 * non-null causes a new texture to be created when the tile is next bound
	 * or initialized.
	 * 
	 * @return the texture data, which may be null.
	 */
	public TextureData getTextureData()
	{
		return this.textureData;
	}

	/**
	 * Specifies new texture data for the tile. New texture data is typically
	 * specified when a new image is read, either initially or in response to
	 * image expiration.
	 * <p/>
	 * If texture data is non-null, a new texture is created from the texture
	 * data when the tile is next bound or otherwise initialized. The texture
	 * data field is then set to null. Subsequently setting texture data to be
	 * non-null causes a new texture to be created when the tile is next bound
	 * or initialized.
	 * <p/>
	 * When a texture is created from the texture data, the texture data field
	 * is set to null to indicate that the data has been converted to a texture
	 * and its resources may be released.
	 * 
	 * @param textureData
	 *            the texture data, which may be null.
	 */
	public void setTextureData(TextureData textureData)
	{
		this.textureData = textureData;
		if (textureData.getMipmapData() != null)
		{
			this.hasMipmapData = true;
		}
	}

	public Texture getTexture(GpuResourceCache tc)
	{
		if (tc == null)
		{
			String message = Logging.getMessage("nullValue.TextureCacheIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		return tc.getTexture(this.getTileKey());
	}

	public boolean isTextureInMemory(GpuResourceCache tc)
	{
		if (tc == null)
		{
			String message = Logging.getMessage("nullValue.TextureCacheIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		return this.getTexture(tc) != null || this.getTextureData() != null;
	}

	public boolean isTextureExpired()
	{
		return this.isTextureExpired(this.getLevel().getExpiryTime());
	}

	public boolean isTextureExpired(long expiryTime)
	{
		return this.updateTime > 0 && this.updateTime < expiryTime;
	}

	public void setTexture(GpuResourceCache tc, Texture texture)
	{
		if (tc == null)
		{
			String message = Logging.getMessage("nullValue.TextureCacheIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		tc.put(this.getTileKey(), texture);
		this.updateTime = System.currentTimeMillis();

		// No more need for texture data; allow garbage collector and memory cache to reclaim it.
		// This also signals that new texture data has been converted.
		this.textureData = null;
		this.updateMemoryCache();
	}

	//	public Vec4 getCentroidPoint(Globe globe)
	//	{
	//		if (globe == null)
	//		{
	//			String msg = Logging.getMessage("nullValue.GlobeIsNull");
	//			Logging.logger().severe(msg);
	//			throw new IllegalArgumentException(msg);
	//		}
	//
	//		return globe.computePointFromLocation(this.getSector().getCentroid());
	//	}
	//
	//	public Extent getExtent(DrawContext dc)
	//	{
	//		if (dc == null)
	//		{
	//			String msg = Logging.getMessage("nullValue.DrawContextIsNull");
	//			Logging.logger().severe(msg);
	//			throw new IllegalArgumentException(msg);
	//		}
	//
	//		return Sector.computeBoundingBox(dc.getGlobe(), dc.getVerticalExaggeration(),
	//				this.getSector());
	//	}

	public CurtainTextureTile[] createSubTiles(CurtainLevel nextLevel)
	{
		int[] columns, rows;
		int rowMultiplier, columnMultiplier;
		if (this.getLevel().getRowCount() == nextLevel.getRowCount())
		{
			columns = new int[] { 0, 1, 2, 3 };
			rows = new int[] { 0, 0, 0, 0 };
			columnMultiplier = 4;
			rowMultiplier = 1;
		}
		else if (this.getLevel().getColumnCount() == nextLevel.getColumnCount())
		{
			columns = new int[] { 0, 0, 0, 0 };
			rows = new int[] { 0, 1, 2, 3 };
			columnMultiplier = 1;
			rowMultiplier = 4;
		}
		else
		{
			columns = new int[] { 0, 1, 0, 1 };
			rows = new int[] { 0, 0, 1, 1 };
			columnMultiplier = 2;
			rowMultiplier = 2;
		}

		String nextLevelCacheName = nextLevel.getCacheName();
		int nextLevelNum = nextLevel.getLevelNumber();

		CurtainTextureTile[] subTiles = new CurtainTextureTile[4];
		for (int i = 0; i < 4; i++)
		{
			int row = this.getRow() * rowMultiplier + rows[i];
			int column = this.getColumn() * columnMultiplier + columns[i];
			CurtainTileKey key = new CurtainTileKey(nextLevelNum, row, column, nextLevelCacheName);
			CurtainTextureTile subTile = this.getTileFromMemoryCache(key);

			if (subTile == null)
			{
				Segment segment = nextLevel.computeSegmentForKey(key);
				subTile = createTextureTile(segment, nextLevel, key.getRow(), key.getColumn());
			}

			subTiles[i] = subTile;
		}

		return subTiles;
	}

	protected CurtainTextureTile createTextureTile(Segment segment, CurtainLevel level, int row, int column)
	{
		return new CurtainTextureTile(segment, level, row, column);
	}

	protected CurtainTextureTile getTileFromMemoryCache(TileKey tileKey)
	{
		return (CurtainTextureTile) getMemoryCache().getObject(tileKey);
	}

	protected void updateMemoryCache()
	{
		if (this.getTileFromMemoryCache(this.getTileKey()) != null)
		{
			getMemoryCache().add(this.getTileKey(), this);
		}
	}

	protected Texture initializeTexture(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		Texture t = this.getTexture(dc.getTextureCache());
		// Return texture if found and there is no new texture data
		if (t != null && this.getTextureData() == null)
		{
			return t;
		}

		if (this.getTextureData() == null) // texture not in cache yet texture data is null, can't initialize
		{
			String msg = Logging.getMessage("nullValue.TextureDataIsNull");
			Logging.logger().severe(msg);
			throw new IllegalStateException(msg);
		}

		try
		{
			t = TextureIO.newTexture(this.getTextureData());
		}
		catch (Exception e)
		{
			String msg = Logging.getMessage("layers.TextureLayer.ExceptionAttemptingToReadTextureFile", "");
			Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
			return null;
		}

		this.setTexture(dc.getTextureCache(), t);
		t.bind(dc.getGL());

		this.setTextureParameters(dc, t);

		return t;
	}

	protected void setTextureParameters(DrawContext dc, Texture t)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		GL2 gl = dc.getGL().getGL2();

		// Use a mipmap minification filter when either of the following is true:
		// a. The texture has mipmap data. This is typically true for formats with embedded mipmaps, such as DDS.
		// b. The texture is setup to have GL automatically generate mipmaps. This is typically true when a texture is
		//    loaded from a standard image type, such as PNG or JPEG, and the caller instructed JOGL to generate
		//     mipmaps.

		boolean useMipmapFilter = (this.hasMipmapData || t.isUsingAutoMipmapGeneration());

		// Set the texture minification filter. If the texture qualifies for mipmaps, apply a minification filter that
		// will access the mipmap data using the highest quality algorithm. If the anisotropic texture filter is
		// available, we will enable it. This will sharpen the appearance of the mipmap filter when the textured
		// surface is at a high slope to the eye.
		if (useMipmapFilter)
		{
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);

			// If the maximum degree of anisotropy is 2.0 or greater, then we know this graphics context supports
			// the anisotropic texture filter.
			double maxAnisotropy = dc.getGLRuntimeCapabilities().getMaxTextureAnisotropy();
			if (dc.getGLRuntimeCapabilities().isUseAnisotropicTextureFilter() && maxAnisotropy >= 2.0)
			{
				gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, (float) maxAnisotropy);
			}
		}
		// If the texture does not qualify for mipmaps, then apply a linear minification filter.
		else
		{
			gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		}

		// Set the texture magnification filter to a linear filter. This will blur the texture as the eye gets very
		// near, but this is still a better choice than nearest neighbor filtering.
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);

		// Set the S and T wrapping modes to clamp to the texture edge. This way no border pixels will be sampled by
		// either the minification or magnification filters.
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
	}

	public boolean bind(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		// Reinitialize texture if new texture data
		if (this.getTextureData() != null)
		{
			Texture t = this.initializeTexture(dc);
			if (t != null)
			{
				return true; // texture was bound during initialization.
			}
		}

		Texture t = this.getTexture(dc.getTextureCache());

		if (t == null && this.getFallbackTile() != null)
		{
			CurtainTextureTile resourceTile = this.getFallbackTile();
			t = resourceTile.getTexture(dc.getTextureCache());
			if (t == null)
			{
				t = resourceTile.initializeTexture(dc);
				if (t != null)
				{
					return true; // texture was bound during initialization.
				}
			}
		}

		if (t != null)
		{
			t.bind(dc.getGL());
		}

		return t != null;
	}

	public void applyInternalTransform(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		Texture t;
		if (this.getTextureData() != null)
		{
			t = this.initializeTexture(dc);
		}
		else
		{
			t = this.getTexture(dc.getTextureCache()); // Use the tile's texture if available
		}

		if (t != null)
		{
			if (t.getMustFlipVertically())
			{
				GL2 gl = GLContext.getCurrent().getGL().getGL2();
				gl.glMatrixMode(GL2.GL_TEXTURE);
				gl.glLoadIdentity();
				gl.glScaled(1, -1, 1);
				gl.glTranslated(0, -1, 0);
			}
			return;
		}

		// Use the tile's fallback texture if its primary texture is not available.
		CurtainTextureTile resourceTile = this.getFallbackTile();
		if (resourceTile == null)
		{
			return;
		}

		t = resourceTile.getTexture(dc.getTextureCache());
		if (t == null && resourceTile.getTextureData() != null)
		{
			t = resourceTile.initializeTexture(dc);
		}

		if (t == null)
		{
			return;
		}

		// Apply necessary transforms to the fallback texture.
		GL2 gl = GLContext.getCurrent().getGL().getGL2();
		gl.glMatrixMode(GL2.GL_TEXTURE);
		gl.glLoadIdentity();

		if (t.getMustFlipVertically())
		{
			gl.glScaled(1, -1, 1);
			gl.glTranslated(0, -1, 0);
		}

		this.applyResourceTextureTransform(dc);
	}

	private void applyResourceTextureTransform(DrawContext dc)
	{
		if (dc == null)
		{
			String message = Logging.getMessage("nullValue.DrawContextIsNull");
			Logging.logger().severe(message);
			throw new IllegalStateException(message);
		}

		if (this.getLevel() == null)
		{
			return;
		}

		int levelDelta = this.getLevelNumber() - this.getFallbackTile().getLevelNumber();
		if (levelDelta <= 0)
		{
			return;
		}

		Segment segment = getSegment();
		Segment fallbackSegment = this.getFallbackTile().getSegment();
		double fhd = fallbackSegment.getHorizontalDelta();
		double fvd = fallbackSegment.getVerticalDelta();
		double shd = segment.getHorizontalDelta();
		double svd = segment.getVerticalDelta();

		double xScale = shd / fhd;
		double yScale = svd / fvd;
		double xShift = (segment.getStart() - fallbackSegment.getStart()) / fhd;
		double yShift = (segment.getBottom() - fallbackSegment.getBottom()) / fvd;

		dc.getGL().getGL2().glTranslated(xShift, yShift, 0);
		dc.getGL().getGL2().glScaled(xScale, yScale, 1);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		final CurtainTextureTile tile = (CurtainTextureTile) o;

		return !(this.getTileKey() != null ? !this.getTileKey().equals(tile.getTileKey()) : tile.getTileKey() != null);
	}

	@Override
	public int hashCode()
	{
		return (this.getTileKey() != null ? this.getTileKey().hashCode() : 0);
	}

	@Override
	public String toString()
	{
		return super.toString() + " " + this.getSegment().toString();
	}
}
