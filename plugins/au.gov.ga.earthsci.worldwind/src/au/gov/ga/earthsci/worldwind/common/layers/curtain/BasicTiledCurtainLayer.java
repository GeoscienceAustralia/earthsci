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

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.formats.dds.DXTCompressionAttributes;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenCredit;
import gov.nasa.worldwind.retrieve.AbstractRetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.FileLockSharer;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import javax.media.opengl.GLProfile;

/**
 * Basic implementation subclass of the abstract {@link TiledCurtainLayer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BasicTiledCurtainLayer extends TiledCurtainLayer
{
	private final Object fileLock;

	public BasicTiledCurtainLayer(CurtainLevelSet levelSet)
	{
		super(levelSet);
		fileLock = FileLockSharer.getLock(getLevels().getFirstLevel().getCacheName());
	}

	public BasicTiledCurtainLayer(AVList params)
	{
		this(new CurtainLevelSet(params));

		String s = params.getStringValue(AVKey.DISPLAY_NAME);
		if (s != null)
			this.setName(s);

		String[] strings = (String[]) params.getValue(AVKey.AVAILABLE_IMAGE_FORMATS);
		if (strings != null && strings.length > 0)
			this.setAvailableImageFormats(strings);

		s = params.getStringValue(AVKey.TEXTURE_FORMAT);
		if (s != null)
			this.setTextureFormat(s);

		Double d = (Double) params.getValue(AVKey.OPACITY);
		if (d != null)
			this.setOpacity(d);

		d = (Double) params.getValue(AVKey.MAX_ACTIVE_ALTITUDE);
		if (d != null)
			this.setMaxActiveAltitude(d);

		d = (Double) params.getValue(AVKey.MIN_ACTIVE_ALTITUDE);
		if (d != null)
			this.setMinActiveAltitude(d);

		d = (Double) params.getValue(AVKey.MAP_SCALE);
		if (d != null)
			this.setValue(AVKey.MAP_SCALE, d);

		d = (Double) params.getValue(AVKey.DETAIL_HINT);
		if (d != null)
			this.setDetailHint(d);

		Boolean b = (Boolean) params.getValue(AVKey.FORCE_LEVEL_ZERO_LOADS);
		if (b != null)
			this.setForceLevelZeroLoads(b);

		b = (Boolean) params.getValue(AVKey.RETAIN_LEVEL_ZERO_TILES);
		if (b != null)
			this.setRetainLevelZeroTiles(b);

		b = (Boolean) params.getValue(AVKey.NETWORK_RETRIEVAL_ENABLED);
		if (b != null)
			this.setNetworkRetrievalEnabled(b);

		b = (Boolean) params.getValue(AVKey.USE_MIP_MAPS);
		if (b != null)
			this.setUseMipMaps(b);

		b = (Boolean) params.getValue(AVKey.USE_TRANSPARENT_TEXTURES);
		if (b != null)
			this.setUseTransparentTextures(b);

		Object o = params.getValue(AVKey.URL_CONNECT_TIMEOUT);
		if (o != null)
			this.setValue(AVKey.URL_CONNECT_TIMEOUT, o);

		o = params.getValue(AVKey.URL_READ_TIMEOUT);
		if (o != null)
			this.setValue(AVKey.URL_READ_TIMEOUT, o);

		o = params.getValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (o != null)
			this.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, o);

		ScreenCredit sc = (ScreenCredit) params.getValue(AVKey.SCREEN_CREDIT);
		if (sc != null)
			this.setScreenCredit(sc);

		if (params.getValue(AVKey.TRANSPARENCY_COLORS) != null)
			this.setValue(AVKey.TRANSPARENCY_COLORS, params.getValue(AVKey.TRANSPARENCY_COLORS));

		//curtain specific keys
		b = (Boolean) params.getValue(AVKeyMore.FOLLOW_TERRAIN);
		if (b != null)
			this.setFollowTerrain(b);

		d = (Double) params.getValue(AVKeyMore.CURTAIN_TOP);
		if (d != null)
			this.setCurtainTop(d);

		d = (Double) params.getValue(AVKeyMore.CURTAIN_BOTTOM);
		if (d != null)
			this.setCurtainBottom(d);

		Integer i = (Integer) params.getValue(AVKeyMore.SUBSEGMENTS);
		if (i != null)
			this.setSubsegments(i);

		Path path = (Path) params.getValue(AVKeyMore.PATH);
		if (path != null)
			this.setPath(path);


		this.setValue(AVKey.CONSTRUCTION_PARAMETERS, params.copy());

		// If any resources should be retrieved for this Layer, start a task to retrieve those resources, and initialize
		// this Layer once those resources are retrieved.
		//        if (this.isRetrieveResources())
		//        {
		//            this.startResourceRetrieval();
		//        }
	}

	public BasicTiledCurtainLayer(Document dom, AVList params)
	{
		this(dom.getDocumentElement(), params);
	}

	public BasicTiledCurtainLayer(Element domElement, AVList params)
	{
		this(getParamsFromDocument(domElement, params));
	}

	protected static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (domElement == null)
		{
			String message = Logging.getMessage("nullValue.DocumentIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (params == null)
			params = new AVListImpl();

		getTiledCurtainLayerConfigParams(domElement, params);
		setFallbacks(params);

		return params;
	}

	protected static void setFallbacks(AVList params)
	{
		if (params.getValue(AVKey.LEVEL_ZERO_TILE_DELTA) == null)
		{
			Angle delta = Angle.fromDegrees(36);
			params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(delta, delta));
		}

		if (params.getValue(AVKey.TILE_WIDTH) == null)
			params.setValue(AVKey.TILE_WIDTH, 512);

		if (params.getValue(AVKey.TILE_HEIGHT) == null)
			params.setValue(AVKey.TILE_HEIGHT, 512);

		if (params.getValue(AVKey.FORMAT_SUFFIX) == null)
			params.setValue(AVKey.FORMAT_SUFFIX, ".dds");

		if (params.getValue(AVKey.NUM_LEVELS) == null)
			params.setValue(AVKey.NUM_LEVELS, 19); // approximately 0.1 meters per pixel

		if (params.getValue(AVKey.NUM_EMPTY_LEVELS) == null)
			params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
	}

	@Override
	protected void forceTextureLoad(CurtainTextureTile tile)
	{
		final URL textureURL = this.getDataFileStore().findFile(tile.getPath(), true);

		if (textureURL != null && !this.isTextureFileExpired(tile, textureURL, this.getDataFileStore()))
		{
			this.loadTexture(tile, textureURL);
		}
	}

	@Override
	protected void requestTexture(DrawContext dc, CurtainTextureTile tile)
	{
		Vec4 centroid =
				getPath().getSegmentCenterPoint(dc, tile.getSegment(), getCurtainTop(), getCurtainBottom(),
						isFollowTerrain());
		Vec4 referencePoint = this.getReferencePoint(dc);
		if (referencePoint != null)
			tile.setPriority(centroid.distanceTo3(referencePoint));

		RequestTask task = new RequestTask(tile, this);
		this.getRequestQ().add(task);
	}

	protected boolean isTextureFileExpired(CurtainTextureTile tile, URL textureURL, FileStore fileStore)
	{
		if (!WWIO.isFileOutOfDate(textureURL, tile.getLevel().getExpiryTime()))
			return false;

		// The file has expired. Delete it.
		fileStore.removeFile(textureURL);
		String message = Logging.getMessage("generic.DataFileExpired", textureURL);
		Logging.logger().fine(message);
		return true;
	}

	private boolean loadTexture(CurtainTextureTile tile, java.net.URL textureURL)
	{
		TextureData textureData;

		synchronized (this.fileLock)
		{
			textureData = readTexture(textureURL, this.getTextureFormat(), this.isUseMipMaps());
		}

		if (textureData == null)
			return false;

		tile.setTextureData(textureData);
		if (tile.getLevelNumber() != 0 || !this.isRetainLevelZeroTiles())
			this.addTileToCache(tile);

		return true;
	}

	private void addTileToCache(CurtainTextureTile tile)
	{
		CurtainTextureTile.getMemoryCache().add(tile.getTileKey(), tile);
	}

	private static TextureData readTexture(java.net.URL url, String textureFormat, boolean useMipMaps)
	{
		try
		{
			// If the caller has enabled texture compression, and the texture data is not a DDS file, then use read the
			// texture data and convert it to DDS.
			if ("image/dds".equalsIgnoreCase(textureFormat) && !url.toString().toLowerCase().endsWith("dds"))
			{
				// Configure a DDS compressor to generate mipmaps based according to the 'useMipMaps' parameter, and
				// convert the image URL to a compressed DDS format.
				DXTCompressionAttributes attributes = DDSCompressor.getDefaultCompressionAttributes();
				attributes.setBuildMipmaps(useMipMaps);
				ByteBuffer buffer = DDSCompressor.compressImageURL(url, attributes);

				return TextureIO.newTextureData(GLProfile.get(GLProfile.GL2), WWIO.getInputStreamFromByteBuffer(buffer), useMipMaps, null);
			}
			// If the caller has disabled texture compression, or if the texture data is already a DDS file, then read
			// the texture data without converting it.
			else
			{
				return TextureIO.newTextureData(GLProfile.get(GLProfile.GL2), url, useMipMaps, null);
			}
		}
		catch (Exception e)
		{
			String msg = Logging.getMessage("layers.TextureLayer.ExceptionAttemptingToReadTextureFile", url);
			Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
			return null;
		}
	}

	private static class RequestTask implements Runnable, Comparable<RequestTask>
	{
		private final BasicTiledCurtainLayer layer;
		private final CurtainTextureTile tile;

		private RequestTask(CurtainTextureTile tile, BasicTiledCurtainLayer layer)
		{
			this.layer = layer;
			this.tile = tile;
		}

		@Override
		public void run()
		{
			// TODO: check to ensure load is still needed

			final java.net.URL textureURL = this.layer.getDataFileStore().findFile(tile.getPath(), false);
			if (textureURL != null && !this.layer.isTextureFileExpired(tile, textureURL, this.layer.getDataFileStore()))
			{
				if (this.layer.loadTexture(tile, textureURL))
				{
					layer.getLevels().unmarkResourceAbsent(this.tile);
					this.layer.firePropertyChange(AVKey.LAYER, null, this);
					return;
				}
				else
				{
					// Assume that something's wrong with the file and delete it.
					this.layer.getDataFileStore().removeFile(textureURL);
					String message = Logging.getMessage("generic.DeletedCorruptDataFile", textureURL);
					Logging.logger().info(message);
				}
			}

			this.layer.retrieveTexture(this.tile, new DownloadPostProcessor(this.tile, this.layer));
		}

		/**
		 * @param that
		 *            the task to compare
		 * 
		 * @return -1 if <code>this</code> less than <code>that</code>, 1 if
		 *         greater than, 0 if equal
		 * 
		 * @throws IllegalArgumentException
		 *             if <code>that</code> is null
		 */
		@Override
		public int compareTo(RequestTask that)
		{
			if (that == null)
			{
				String msg = Logging.getMessage("nullValue.RequestTaskIsNull");
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}
			return this.tile.getPriority() == that.tile.getPriority() ? 0 : this.tile.getPriority() < that.tile
					.getPriority() ? -1 : 1;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			final RequestTask that = (RequestTask) o;

			// Don't include layer in comparison so that requests are shared among layers
			return !(tile != null ? !tile.equals(that.tile) : that.tile != null);
		}

		@Override
		public int hashCode()
		{
			return (tile != null ? tile.hashCode() : 0);
		}

		@Override
		public String toString()
		{
			return this.tile.toString();
		}
	}

	protected void retrieveTexture(CurtainTextureTile tile, DownloadPostProcessor postProcessor)
	{
		//TODO add support for a Local Curtain RetrieverFactory

		/*if (this.getValue(AVKey.RETRIEVER_FACTORY_LOCAL) != null)
		    this.retrieveLocalTexture(tile, postProcessor);
		else*/
		// Assume it's remote, which handles the legacy cases.
		this.retrieveRemoteTexture(tile, postProcessor);
	}

	/*protected void retrieveLocalTexture(CurtainTextureTile tile, DownloadPostProcessor postProcessor)
	{
	    if (!WorldWind.getLocalRetrievalService().isAvailable())
	        return;

	    RetrieverFactory retrieverFactory = (RetrieverFactory) this.getValue(AVKey.RETRIEVER_FACTORY_LOCAL);
	    if (retrieverFactory == null)
	        return;

	    AVListImpl avList = new AVListImpl();
	    avList.setValue(AVKey.SECTOR, tile.getSector());
	    avList.setValue(AVKey.WIDTH, tile.getWidth());
	    avList.setValue(AVKey.HEIGHT, tile.getHeight());
	    avList.setValue(AVKey.FILE_NAME, tile.getPath());

	    Retriever retriever = retrieverFactory.createRetriever(avList, postProcessor);

	    WorldWind.getLocalRetrievalService().runRetriever(retriever, tile.getPriority());
	}*/

	protected void retrieveRemoteTexture(final CurtainTextureTile tile, DownloadPostProcessor postProcessor)
	{
		if (!this.isNetworkRetrievalEnabled())
		{
			this.getLevels().markResourceAbsent(tile);
			return;
		}

		if (!WorldWind.getRetrievalService().isAvailable())
			return;

		java.net.URL url;
		try
		{
			url = tile.getResourceURL();

			if (url == null)
				return;

			if (WorldWind.getNetworkStatus().isHostUnavailable(url))
			{
				this.getLevels().markResourceAbsent(tile);
				return;
			}
		}
		catch (java.net.MalformedURLException e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE,
					Logging.getMessage("layers.TextureLayer.ExceptionCreatingTextureUrl", tile), e);
			return;
		}

		Retriever retriever;

		if (postProcessor == null)
			postProcessor = new DownloadPostProcessor(tile, this);
		retriever = URLRetriever.createRetriever(url, postProcessor);

		if (retriever == null)
		{
			Logging.logger().severe(Logging.getMessage("layers.TextureLayer.UnknownRetrievalProtocol", url.toString()));
			return;
		}

		// Apply any overridden timeouts.
		Integer cto = AVListImpl.getIntegerValue(this, AVKey.URL_CONNECT_TIMEOUT);
		if (cto != null && cto > 0)
			retriever.setConnectTimeout(cto);
		Integer cro = AVListImpl.getIntegerValue(this, AVKey.URL_READ_TIMEOUT);
		if (cro != null && cro > 0)
			retriever.setReadTimeout(cro);
		Integer srl = AVListImpl.getIntegerValue(this, AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT);
		if (srl != null && srl > 0)
			retriever.setStaleRequestLimit(srl);

		WorldWind.getRetrievalService().runRetriever(retriever, tile.getPriority());
	}

	protected static class DownloadPostProcessor extends AbstractRetrievalPostProcessor
	{
		protected final CurtainTextureTile tile;
		protected final BasicTiledCurtainLayer layer;
		protected final FileStore fileStore;

		public DownloadPostProcessor(CurtainTextureTile tile, BasicTiledCurtainLayer layer)
		{
			this(tile, layer, null);
		}

		public DownloadPostProcessor(CurtainTextureTile tile, BasicTiledCurtainLayer layer, FileStore fileStore)
		{
			//noinspection RedundantCast
			super((AVList) layer);

			this.tile = tile;
			this.layer = layer;
			this.fileStore = fileStore;
		}

		protected FileStore getFileStore()
		{
			return this.fileStore != null ? this.fileStore : this.layer.getDataFileStore();
		}

		@Override
		protected void markResourceAbsent()
		{
			this.layer.getLevels().markResourceAbsent(this.tile);
		}

		@Override
		protected Object getFileLock()
		{
			return this.layer.fileLock;
		}

		@Override
		protected File doGetOutputFile()
		{
			return this.getFileStore().newFile(this.tile.getPath());
		}

		@Override
		protected ByteBuffer handleSuccessfulRetrieval()
		{
			ByteBuffer buffer = super.handleSuccessfulRetrieval();

			if (buffer != null)
			{
				// We've successfully cached data. Check if there's a configuration file for this layer, create one
				// if there's not.
				//this.layer.writeConfigurationFile(this.getFileStore()); //TODO implement

				// Fire a property change to denote that the layer's backing data has changed.
				this.layer.firePropertyChange(AVKey.LAYER, null, this);
			}

			return buffer;
		}

		@Override
		protected ByteBuffer handleTextContent() throws IOException
		{
			this.markResourceAbsent();

			return super.handleTextContent();
		}
	}
}
