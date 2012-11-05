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
package au.gov.ga.earthsci.worldwind.common.layers.curtain.delegate;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.formats.dds.DXTCompressionAttributes;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.ImageUtil;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.curtain.BasicTiledCurtainLayer;
import au.gov.ga.earthsci.worldwind.common.layers.curtain.CurtainLevel;
import au.gov.ga.earthsci.worldwind.common.layers.curtain.CurtainTextureTile;
import au.gov.ga.earthsci.worldwind.common.layers.curtain.Segment;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorLayer;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.ITileRequesterDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate.FileLockSharer;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.DDSUncompressor;

import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import javax.media.opengl.GLProfile;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

/**
 * {@link BasicTiledCurtainLayer} subclass that uses delegates provided by a
 * {@link CurtainDelegateKit} for the retrieval, transformation, and creation of
 * curtain data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DelegatorTiledCurtainLayer extends BasicTiledCurtainLayer implements
		IDelegatorLayer<DelegatorCurtainTextureTile>
{
	protected final Object fileLock;
	protected final URL context;
	protected final CurtainDelegateKit delegateKit;

	protected Globe currentGlobe;

	public DelegatorTiledCurtainLayer(AVList params)
	{
		super(params);

		Object o = params.getValue(AVKeyMore.DELEGATE_KIT);
		if (o != null && o instanceof CurtainDelegateKit)
			delegateKit = (CurtainDelegateKit) o;
		else
			delegateKit = new CurtainDelegateKit();

		o = params.getValue(AVKeyMore.CONTEXT_URL);
		if (o != null && o instanceof URL)
			context = (URL) o;
		else
			context = null;

		//Share the filelock with other layers with the same cache name. This allows
		//multiple layers to save and load from the same cache location.
		fileLock = FileLockSharer.getLock(getLevels().getFirstLevel().getCacheName());
	}

	public DelegatorTiledCurtainLayer(Element domElement, AVList params)
	{
		this(getParamsFromDocument(domElement, params));
	}

	protected static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		/*String serviceName = XMLUtil.getText(domElement, "Service/@serviceName");

		if (OGCConstants.WMS_SERVICE_NAME.equals(serviceName))
		{
			//if serviceName defines a WMS sources, then use the WMSTiledImageLayer to initialise params
			params = WMSTIL.wmsGetParamsFromDocument(domElement, params);
		}
		else*/
		{
			params = BasicTiledCurtainLayer.getParamsFromDocument(domElement, params);
		}

		//create the delegate kit from the XML element
		CurtainDelegateKit delegateKit = new CurtainDelegateKit().createFromXML(domElement, params);
		params.setValue(AVKeyMore.DELEGATE_KIT, delegateKit);

		return params;
	}

	@Override
	public void render(DrawContext dc)
	{
		if (!isEnabled())
			return;

		if (dc != null)
			currentGlobe = dc.getGlobe();

		delegateKit.preRender(dc);
		super.render(dc);
		delegateKit.postRender(dc);
	}

	@Override
	public boolean isTextureFileExpired(DelegatorCurtainTextureTile tile, URL textureURL, FileStore fileStore)
	{
		return super.isTextureFileExpired(tile, textureURL, fileStore);
	}

	@Override
	protected void forceTextureLoad(CurtainTextureTile tile)
	{
		validateTileClass(tile);

		//pass request to delegate
		delegateKit.forceTextureLoad((DelegatorCurtainTextureTile) tile, this);
	}

	@Override
	protected void requestTexture(DrawContext dc, CurtainTextureTile tile)
	{
		validateTileClass(tile);

		currentGlobe = dc.getGlobe();

		Vec4 centroid =
				getPath().getSegmentCenterPoint(dc, tile.getSegment(), getCurtainTop(), getCurtainBottom(),
						isFollowTerrain());
		Vec4 referencePoint = this.getReferencePoint(dc);
		if (referencePoint != null)
			tile.setPriority(centroid.distanceTo3(referencePoint));

		//pass request to delegate
		Runnable task = delegateKit.createRequestTask((DelegatorCurtainTextureTile) tile, this);

		//if returned task is null, the task has already been run by
		//the immediate delegates, so don't add to queue
		if (task != null)
		{
			this.getRequestQ().add(task);
		}
	}

	protected void validateTileClass(Object tile)
	{
		if (!(tile instanceof DelegatorCurtainTextureTile))
		{
			throw new IllegalArgumentException("Tile must be a " + DelegatorCurtainTextureTile.class.getName());
		}
	}

	/**
	 * Load a texture from a URL (must be file protocol) and set the tile's
	 * texture data to the loaded texture. Should be called by the
	 * {@link ITileRequesterDelegate}.
	 * 
	 * @param tile
	 *            Tile to set texture data
	 * @param textureURL
	 *            File URL of the texture
	 * @return true if the texture data was loaded successfully
	 */
	@Override
	public boolean loadTexture(DelegatorCurtainTextureTile tile, URL textureURL)
	{
		//public for delegate access

		TextureData textureData;

		synchronized (fileLock)
		{
			textureData = readTexture(tile, textureURL);
		}

		if (textureData == null)
			return false;

		tile.setTextureData(textureData);
		if (tile.getLevelNumber() != 0 || !isRetainLevelZeroTiles())
			addTileToCache(tile);

		return true;
	}

	@Override
	public void addTileToCache(IDelegatorTile tile)
	{
		TextureTile.getMemoryCache().add(tile.getTransformedTileKey(), tile);
	}

	@Override
	public TextureData readTexture(DelegatorCurtainTextureTile tile, URL url)
	{
		try
		{
			//if the file is a DDS file, just read it directly (skip all delegate readers/transformers)
			if (url.toString().toLowerCase().endsWith("dds"))
				return TextureIO.newTextureData(GLProfile.get(GLProfile.GL2), url, isUseMipMaps(), null);

			BufferedImage image = readImage(tile, url);

			if ("image/dds".equalsIgnoreCase(getTextureFormat()))
			{
				//if required to compress textures, then compress the image to a DDS image
				DXTCompressionAttributes attributes = DDSCompressor.getDefaultCompressionAttributes();
				attributes.setBuildMipmaps(isUseMipMaps());

				ByteBuffer buffer;
				if (image != null)
				{
					buffer = new DDSCompressor().compressImage(image, attributes);
				}
				else
				{
					buffer = DDSCompressor.compressImageURL(url, attributes);
				}

				//return the dds image as TextureData
				return TextureIO.newTextureData(GLProfile.get(GLProfile.GL2), WWIO.getInputStreamFromByteBuffer(buffer), isUseMipMaps(), null);
			}

			//return the image as TextureData
			return AWTTextureIO.newTextureData(GLProfile.get(GLProfile.GL2), image, isUseMipMaps());
		}
		catch (Exception e)
		{
			String msg = Logging.getMessage("layers.TextureLayer.ExceptionAttemptingToReadTextureFile", url);
			Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
		}
		return null;
	}

	/**
	 * Read image from a File URL and return it as a {@link BufferedImage}.
	 * 
	 * @param tile
	 *            Tile for which to read an image
	 * @param url
	 *            File URL to read from
	 * @return Image read from URL
	 * @throws IOException
	 *             If image could not be read
	 */
	protected BufferedImage readImage(DelegatorCurtainTextureTile tile, URL url) throws IOException
	{
		//first try to read the image via the ImageReaderDelegates
		BufferedImage image = delegateKit.readImage(tile, url, currentGlobe);
		if (image == null)
		{
			if (url.toString().toLowerCase().endsWith(".dds"))
			{
				ByteBuffer buffer = WWIO.readURLContentToBuffer(url, false);
				image = DDSUncompressor.readDxt3(buffer);
			}
			else
			{
				//if that doesn't work, just read it with ImageIO class
				image = ImageIO.read(url);
			}

			if (image == null)
			{
				throw new IOException("Could not read image");
			}
		}

		//perform any transformations on the image
		image = delegateKit.transformImage(image, tile);

		//manually do the TRANSPARENCY_COLORS transform, for compatibility with AbstractRetrievalPostProcessor
		int[] colors = (int[]) this.getValue(AVKey.TRANSPARENCY_COLORS);
		if (colors != null)
			image = ImageUtil.mapTransparencyColors(image, colors);

		return image;
	}

	/**
	 * Extension to superclass' DownloadPostProcessor which returns this class'
	 * fileLock instead of the superclass'.
	 * 
	 * @author Michael de Hoog
	 */
	protected static class DownloadPostProcessor extends BasicTiledCurtainLayer.DownloadPostProcessor
	{
		private final DelegatorTiledCurtainLayer layer;

		public DownloadPostProcessor(CurtainTextureTile tile, DelegatorTiledCurtainLayer layer)
		{
			super(tile, layer);
			this.layer = layer;
		}

		@Override
		protected Object getFileLock()
		{
			return layer.fileLock;
		}
	}

	@Override
	protected CurtainTextureTile createCurtainTextureTile(Segment segment, CurtainLevel level, int row, int col)
	{
		return delegateKit.createTextureTile(segment, level, row, col);
	}

	@Override
	public void unmarkResourceAbsent(DelegatorCurtainTextureTile tile)
	{
		getLevels().unmarkResourceAbsent(tile);
	}

	@Override
	public void markResourceAbsent(DelegatorCurtainTextureTile tile)
	{
		getLevels().markResourceAbsent(tile);
	}

	@Override
	public URL getContext()
	{
		return context;
	}

	/* **********************************************************************************************
	 * Below here is copied from BasicTiledImageLayer, with some modifications to use the delegates *
	 ********************************************************************************************** */

	@Override
	public void retrieveRemoteTexture(CurtainTextureTile tile,
			BasicTiledCurtainLayer.DownloadPostProcessor postProcessor)
	{
		createAndRunRetriever(tile, postProcessor);
	}

	@Override
	public void retrieveRemoteTexture(DelegatorCurtainTextureTile tile, RetrievalPostProcessor postProcessor)
	{
		createAndRunRetriever(tile, postProcessor);
	}

	protected void createAndRunRetriever(CurtainTextureTile tile, RetrievalPostProcessor postProcessor)
	{
		Retriever retriever = createRetriever(tile, postProcessor);
		if (retriever != null)
			WorldWind.getRetrievalService().runRetriever(retriever, tile.getPriority());
	}

	protected Retriever createRetriever(final CurtainTextureTile tile, RetrievalPostProcessor postProcessor)
	{
		//copied from BasicTiledImageLayer.downloadTexture(), with the following modifications:
		// - uses the delegateKit to instanciate the Retriever
		// - returns the Retriever instead of adding it to the RetrievalService

		if (!this.isNetworkRetrievalEnabled())
		{
			this.getLevels().markResourceAbsent(tile);
			return null;
		}

		if (!WorldWind.getRetrievalService().isAvailable())
			return null;

		java.net.URL url;
		try
		{
			//MODIFIED
			validateTileClass(tile);
			url = delegateKit.getRemoteTileURL((DelegatorCurtainTextureTile) tile, null);
			//MODIFIED
			if (url == null)
				return null;

			if (WorldWind.getNetworkStatus().isHostUnavailable(url))
			{
				this.getLevels().markResourceAbsent(tile);
				return null;
			}
		}
		catch (java.net.MalformedURLException e)
		{
			Logging.logger().log(java.util.logging.Level.SEVERE,
					Logging.getMessage("layers.TextureLayer.ExceptionCreatingTextureUrl", tile), e);
			return null;
		}

		Retriever retriever;

		if ("http".equalsIgnoreCase(url.getProtocol()) || "https".equalsIgnoreCase(url.getProtocol()))
		{
			if (postProcessor == null)
				postProcessor = new DownloadPostProcessor(tile, this);
			//MODIFIED
			//retriever = new HTTPRetriever(url, postProcessor);
			retriever = delegateKit.createRetriever(url, postProcessor);
			//MODIFIED
		}
		else
		{
			Logging.logger().severe(Logging.getMessage("layers.TextureLayer.UnknownRetrievalProtocol", url.toString()));
			return null;
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

		//MODIFIED
		//WorldWind.getRetrievalService().runRetriever(retriever, tile.getPriority());
		return retriever;
		//MODIFIED
	}
}
