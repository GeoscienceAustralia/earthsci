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
package au.gov.ga.earthsci.worldwind.common.layers.tiled.image.delegate;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.formats.dds.DXTCompressionAttributes;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.ogc.OGCConstants;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.ImageUtil;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileKey;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.Bounded;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorLayer;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.IDelegatorTile;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.ITileRequesterDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.tiled.image.URLTransformerBasicTiledImageLayer;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.DDSUncompressor;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import javax.media.opengl.GLProfile;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

/**
 * TiledImageLayer which uses delegates to perform the various tiled image layer
 * functions such as downloading, saving, loading, and image transforming. This
 * allows full customisation of different functions of the layer.
 * <p>
 * It also uses the {@link FileLockSharer} to create/share the fileLock object.
 * This is so that multiple layers can point and write to the same data cache
 * name and synchronize with each other on the same fileLock object. (Note: this
 * has not yet been added to Bulk Download facility).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DelegatorTiledImageLayer extends URLTransformerBasicTiledImageLayer implements Bounded,
		IDelegatorLayer<DelegatorTextureTile>
{
	protected final Object fileLock;
	protected final URL context;
	protected final ImageDelegateKit delegateKit;
	protected boolean extractZipEntry = false;

	protected Globe currentGlobe;

	public DelegatorTiledImageLayer(AVList params)
	{
		super(params);

		Object o = params.getValue(AVKeyMore.DELEGATE_KIT);
		if (o != null && o instanceof ImageDelegateKit)
			delegateKit = (ImageDelegateKit) o;
		else
			delegateKit = new ImageDelegateKit();

		o = params.getValue(AVKeyMore.CONTEXT_URL);
		if (o != null && o instanceof URL)
			context = (URL) o;
		else
			context = null;

		Boolean b = (Boolean) params.getValue(AVKeyMore.EXTRACT_ZIP_ENTRY);
		if (b != null)
			this.setExtractZipEntry(b);

		//Share the filelock with other layers with the same cache name. This allows
		//multiple layers to save and load from the same cache location.
		fileLock = FileLockSharer.getLock(getLevels().getFirstLevel().getCacheName());
	}

	public DelegatorTiledImageLayer(Element domElement, AVList params)
	{
		this(getParamsFromDocument(domElement, params));
	}

	protected static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		String serviceName = XMLUtil.getText(domElement, "Service/@serviceName");

		if (OGCConstants.WMS_SERVICE_NAME.equals(serviceName))
		{
			//if serviceName defines a WMS sources, then use the WMSTiledImageLayer to initialise params
			params = WMSTIL.wmsGetParamsFromDocument(domElement, params);
		}
		else
		{
			params = BasicTiledImageLayer.getParamsFromDocument(domElement, params);
		}

		//create the delegate kit from the XML element
		ImageDelegateKit delegateKit = new ImageDelegateKit().createFromXML(domElement, params);
		params.setValue(AVKeyMore.DELEGATE_KIT, delegateKit);

		XPath xpath = WWXML.makeXPath();
		WWXML.checkAndSetBooleanParam(domElement, params, AVKeyMore.EXTRACT_ZIP_ENTRY, "ExtractZipEntry", xpath);

		return params;
	}

	public boolean isExtractZipEntry()
	{
		return extractZipEntry;
	}

	public void setExtractZipEntry(boolean extractZipEntry)
	{
		this.extractZipEntry = extractZipEntry;
	}

	/**
	 * Extension of {@link WMSTiledImageLayer} that provides access to the
	 * wmsGetParamsFromDocument function.
	 * 
	 * @author Michael de Hoog
	 */
	protected static class WMSTIL extends WMSTiledImageLayer
	{
		private WMSTIL()
		{
			super("");
		}

		public static AVList wmsGetParamsFromDocument(Element domElement, AVList params)
		{
			return WMSTiledImageLayer.wmsGetParamsFromDocument(domElement, params);
		}
	}

	/**
	 * Create a new XML document describing a {@link DelegatorTiledImageLayer}
	 * from an AVList.
	 * 
	 * @param params
	 * @return New XML document
	 */
	public static Document createDelegatorTiledImageLayerConfigDocument(AVList params)
	{
		Document document = BasicTiledImageLayer.createTiledImageLayerConfigDocument(params);
		Element context = document.getDocumentElement();
		createDelegatorTiledImageLayerConfigElements(params, context);
		return document;
	}

	/**
	 * Add XML elements specific to the {@link DelegatorTiledImageLayer} from an
	 * AVList to an XML element.
	 * 
	 * @param params
	 * @param context
	 *            XML element to add to
	 * @return context
	 */
	public static Element createDelegatorTiledImageLayerConfigElements(AVList params, Element context)
	{
		Object o = params.getValue(AVKeyMore.DELEGATE_KIT);
		if (o != null && o instanceof ImageDelegateKit)
		{
			((ImageDelegateKit) o).saveToXML(context);
		}
		return context;
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
	public Sector getSector()
	{
		return getLevels().getSector();
	}

	@Override
	public boolean isTextureFileExpired(DelegatorTextureTile tile, URL textureURL, FileStore fileStore)
	{
		return super.isTextureFileExpired(tile, textureURL, fileStore);
	}

	@Override
	protected void forceTextureLoad(TextureTile tile)
	{
		validateTileClass(tile);

		//pass request to delegate
		delegateKit.forceTextureLoad((DelegatorTextureTile) tile, this);
	}

	@Override
	protected void requestTexture(DrawContext dc, TextureTile tile)
	{
		//Only request textures for tiles that intersect the layer's sector.
		//This makes perfect sense, and am unsure why the TiledImageLayer doesn't do this. 
		if (!tile.getSector().intersects(getSector()))
		{
			markResourceAbsent(tile);
			return;
		}

		validateTileClass(tile);

		currentGlobe = dc.getGlobe();

		Vec4 centroid = tile.getCentroidPoint(currentGlobe);
		Vec4 referencePoint = this.getReferencePoint(dc);
		if (referencePoint != null)
			tile.setPriority(centroid.distanceTo3(referencePoint));

		//pass request to delegate
		Runnable task = delegateKit.createRequestTask((DelegatorTextureTile) tile, this);

		//if returned task is null, the task has already been run by
		//the immediate delegates, so don't add to queue
		if (task != null)
		{
			this.getRequestQ().add(task);
		}
	}

	protected void validateTileClass(Object tile)
	{
		if (!(tile instanceof DelegatorTextureTile))
		{
			throw new IllegalArgumentException("Tile must be a " + DelegatorTextureTile.class.getName());
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
	public boolean loadTexture(DelegatorTextureTile tile, URL textureURL)
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
		{
			addTileToCache((IDelegatorTile) tile);
		}

		return true;
	}

	@Override
	public void addTileToCache(IDelegatorTile tile)
	{
		TextureTile.getMemoryCache().add(tile.getTransformedTileKey(), tile);
	}

	@Override
	public TextureData readTexture(DelegatorTextureTile tile, URL url)
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
	protected BufferedImage readImage(DelegatorTextureTile tile, URL url) throws IOException
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
	protected static class DownloadPostProcessor extends BasicTiledImageLayer.DownloadPostProcessor
	{
		private final DelegatorTiledImageLayer layer;

		public DownloadPostProcessor(TextureTile tile, DelegatorTiledImageLayer layer)
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
	protected BufferedImage requestImage(TextureTile tile, String mimeType) throws URISyntaxException,
			InterruptedIOException, MalformedURLException
	{
		validateTileClass(tile);

		return requestImage((DelegatorTextureTile) tile, mimeType);
	}

	protected BufferedImage requestImage(DelegatorTextureTile tile, String mimeType) throws URISyntaxException,
			InterruptedIOException, MalformedURLException
	{
		//ignores mimeType parameter

		URL url = delegateKit.getLocalTileURL(tile, this, false);
		if (url != null)
		{
			try
			{
				return readImage(tile, url);
			}
			catch (IOException e)
			{
				String msg = Logging.getMessage("layers.TextureLayer.ExceptionAttemptingToReadTextureFile", url);
				Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
			}
		}
		return null;
	}

	@Override
	protected void downloadImage(TextureTile tile, String mimeType, int timeout) throws Exception
	{
		//ignores mimeType parameter

		Retriever retriever = createRetriever(tile, null);
		retriever.setConnectTimeout(10000);
		retriever.setReadTimeout(timeout);
		retriever.call();
	}

	/* **********************************************************************************************
	 * Below here is copied from BasicTiledImageLayer, with some modifications to use the delegates *
	 ********************************************************************************************** */

	@Override
	protected void createTopLevelTiles()
	{
		Sector sector = this.getLevels().getSector();

		Level level = this.getLevels().getFirstLevel();
		Angle dLat = level.getTileDelta().getLatitude();
		Angle dLon = level.getTileDelta().getLongitude();
		Angle latOrigin = this.getLevels().getTileOrigin().getLatitude();
		Angle lonOrigin = this.getLevels().getTileOrigin().getLongitude();

		// Determine the row and column offset from the common World Wind global tiling origin.
		int firstRow = Tile.computeRow(dLat, sector.getMinLatitude(), latOrigin);
		int firstCol = Tile.computeColumn(dLon, sector.getMinLongitude(), lonOrigin);
		int lastRow = Tile.computeRow(dLat, sector.getMaxLatitude(), latOrigin);
		int lastCol = Tile.computeColumn(dLon, sector.getMaxLongitude(), lonOrigin);

		int nLatTiles = lastRow - firstRow + 1;
		int nLonTiles = lastCol - firstCol + 1;

		this.topLevels = new ArrayList<TextureTile>(nLatTiles * nLonTiles);

		Angle p1 = Tile.computeRowLatitude(firstRow, dLat, latOrigin);
		for (int row = firstRow; row <= lastRow; row++)
		{
			Angle p2;
			p2 = p1.add(dLat);

			Angle t1 = Tile.computeColumnLongitude(firstCol, dLon, lonOrigin);
			for (int col = firstCol; col <= lastCol; col++)
			{
				Angle t2;
				t2 = t1.add(dLon);

				//MODIFIED
				this.topLevels.add(delegateKit.createTextureTile(new Sector(p1, p2, t1, t2), level, row, col));
				//MODIFIED
				t1 = t2;
			}
			p1 = p2;
		}
	}

	@Override
	public TextureTile[][] getTilesInSector(Sector sector, int levelNumber)
	{
		if (sector == null)
		{
			String msg = Logging.getMessage("nullValue.SectorIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		Level targetLevel = this.getLevels().getLastLevel();
		if (levelNumber >= 0)
		{
			for (int i = levelNumber; i < this.getLevels().getLastLevel().getLevelNumber(); i++)
			{
				if (this.getLevels().isLevelEmpty(i))
					continue;

				targetLevel = this.getLevels().getLevel(i);
				break;
			}
		}

		// Collect all the tiles intersecting the input sector.
		LatLon delta = targetLevel.getTileDelta();
		LatLon origin = this.getLevels().getTileOrigin();
		final int nwRow = Tile.computeRow(delta.getLatitude(), sector.getMaxLatitude(), origin.getLatitude());
		final int nwCol = Tile.computeColumn(delta.getLongitude(), sector.getMinLongitude(), origin.getLongitude());
		final int seRow = Tile.computeRow(delta.getLatitude(), sector.getMinLatitude(), origin.getLatitude());
		final int seCol = Tile.computeColumn(delta.getLongitude(), sector.getMaxLongitude(), origin.getLongitude());

		int numRows = nwRow - seRow + 1;
		int numCols = seCol - nwCol + 1;
		TextureTile[][] sectorTiles = new TextureTile[numRows][numCols];

		for (int row = nwRow; row >= seRow; row--)
		{
			for (int col = nwCol; col <= seCol; col++)
			{
				TileKey key = new TileKey(targetLevel.getLevelNumber(), row, col, targetLevel.getCacheName());
				Sector tileSector = this.getLevels().computeSectorForKey(key);
				//MODIFIED
				sectorTiles[nwRow - row][col - nwCol] =
						delegateKit.createTextureTile(tileSector, targetLevel, row, col); //new TextureTile(tileSector, targetLevel, row, col);
				//MODIFIED
			}
		}

		return sectorTiles;
	}

	@Override
	public void retrieveRemoteTexture(TextureTile tile, BasicTiledImageLayer.DownloadPostProcessor postProcessor)
	{
		createAndRunRetriever(tile, postProcessor);
	}

	@Override
	public void retrieveRemoteTexture(DelegatorTextureTile tile, RetrievalPostProcessor postProcessor)
	{
		createAndRunRetriever(tile, postProcessor);
	}

	protected void createAndRunRetriever(TextureTile tile, RetrievalPostProcessor postProcessor)
	{
		Retriever retriever = createRetriever(tile, postProcessor);
		if (retriever != null)
			WorldWind.getRetrievalService().runRetriever(retriever, tile.getPriority());
	}

	protected Retriever createRetriever(final TextureTile tile, RetrievalPostProcessor postProcessor)
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
			url = delegateKit.getRemoteTileURL((DelegatorTextureTile) tile, null);
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
		if (isExtractZipEntry())
		{
			retriever.setValue(URLRetriever.EXTRACT_ZIP_ENTRY, "true");
		}
		//WorldWind.getRetrievalService().runRetriever(retriever, tile.getPriority());
		return retriever;
		//MODIFIED
	}

	@Override
	protected void writeConfigurationParams(FileStore fileStore, AVList params)
	{
		// Determine what the configuration file name should be based on the configuration parameters. Assume an XML
		// configuration document type, and append the XML file suffix.
		String fileName = DataConfigurationUtils.getDataConfigFilename(params, ".xml");
		if (fileName == null)
		{
			String message = Logging.getMessage("nullValue.FilePathIsNull");
			Logging.logger().severe(message);
			throw new WWRuntimeException(message);
		}

		// Check if this component needs to write a configuration file. This happens outside of the synchronized block
		// to improve multithreaded performance for the common case: the configuration file already exists, this just
		// need to check that it's there and return. If the file exists but is expired, do not remove it -  this
		// removes the file inside the synchronized block below.
		if (!this.needsConfigurationFile(fileStore, fileName, params, false))
			return;

		synchronized (this.fileLock)
		{
			// Check again if the component needs to write a configuration file, potentially removing any existing file
			// which has expired. This additional check is necessary because the file could have been created by
			// another thread while we were waiting for the lock.
			if (!this.needsConfigurationFile(fileStore, fileName, params, true))
				return;

			this.doWriteConfigurationParams(fileStore, fileName, params);
		}
	}

	@Override
	public void unmarkResourceAbsent(DelegatorTextureTile tile)
	{
		getLevels().unmarkResourceAbsent(tile);
	}

	@Override
	public void markResourceAbsent(DelegatorTextureTile tile)
	{
		getLevels().markResourceAbsent(tile);
	}

	public void markResourceAbsent(TextureTile tile)
	{
		getLevels().markResourceAbsent(tile);
	}

	@Override
	public URL getContext()
	{
		return context;
	}
}
