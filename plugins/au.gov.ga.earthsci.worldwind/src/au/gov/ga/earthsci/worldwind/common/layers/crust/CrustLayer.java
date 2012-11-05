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
package au.gov.ga.earthsci.worldwind.common.layers.crust;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.downloader.Downloader;
import au.gov.ga.earthsci.worldwind.common.downloader.RetrievalHandler;
import au.gov.ga.earthsci.worldwind.common.downloader.RetrievalResult;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.Loader;

import com.jogamp.common.nio.Buffers;

/**
 * A specialised sub-surface layer that displays crustal elevation data
 * read from a simple comma- or whitespace-separated data file.
 * <p/>
 * The data file (referenced via the {@link #url} field) should contain elevations
 * expressed as doubles (in metres) in a row-major ordering of dimensions {@link #width} x {@link #height}.
 * The datafile can be contaied within a zip file to minimise bandwidth requirements.
 * <p/>
 * The crust layer will be rendered as a surface deformed by the elevation data and
 * coloured using a colour map based on min and max elevation values.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class CrustLayer extends AbstractLayer implements Loader
{
	private static final String WHITESPACE_COMMA_REGEX = "(\\s*,\\s*)|\\s+";
	private final static int MAX_DOWNLOAD_ATTEMPTS = 3;

	private final URL url;

	private final int width;
	private final int height;
	private final double scale;
	private final Sector sector;

	private boolean loaded = false;
	private int loadAttempts = 0;

	private boolean loading = false;
	private final List<LoadingListener> loadingListeners = new ArrayList<LoadingListener>();

	private final Object elevationLock = new Object();
	private DoubleBuffer elevations;
	private DoubleBuffer vertices;
	private DoubleBuffer colors;
	private IntBuffer indices;

	private double minElevation = Double.MAX_VALUE;
	private double maxElevation = -Double.MAX_VALUE;
	private double lastVerticalExaggeration = -1;
	private Globe lastGlobe = null;

	public CrustLayer(AVList params)
	{
		URL url = null;
		try
		{
			URL context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
			url = new URL(context, params.getStringValue(AVKey.URL));
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException(e);
		}
		this.url = url;

		this.width = (Integer) params.getValue(AVKey.WIDTH);
		this.height = (Integer) params.getValue(AVKey.HEIGHT);
		this.sector = (Sector) params.getValue(AVKey.SECTOR);
		
		if (width <= 1 || height <= 1)
		{
			throw new IllegalArgumentException("Illegal width or height");
		}

		double scale = 1;
		if (params.getValue(AVKeyMore.SCALE) != null)
		{
			scale = (Double) params.getValue(AVKeyMore.SCALE);
		}
		this.scale = scale;

		boolean wrap = false;
		if (params.getValue(AVKeyMore.WRAP) != null)
		{
			wrap = (Boolean) params.getValue(AVKeyMore.WRAP);
		}
		
		indices = generateTriStripIndices(width, height, wrap);
		vertices = Buffers.newDirectDoubleBuffer(width * height * 3);
		colors = Buffers.newDirectDoubleBuffer(width * height * 4);
	}

	public CrustLayer(Document dom, AVList params)
	{
		this(dom.getDocumentElement(), params);
	}

	public CrustLayer(Element domElement, AVList params)
	{
		this(getParamsFromDocument(domElement, params));
	}

	protected static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (params == null)
		{
			params = new AVListImpl();
		}

		XPath xpath = WWXML.makeXPath();

		// Common layer properties.
		AbstractLayer.getLayerConfigParams(domElement, params);

		WWXML.checkAndSetStringParam(domElement, params, AVKey.URL, "URL", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKey.WIDTH, "Size/@width", xpath);
		WWXML.checkAndSetIntegerParam(domElement, params, AVKey.HEIGHT, "Size/@height", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.SCALE, "Scale", xpath);
		WWXML.checkAndSetSectorParam(domElement, params, AVKey.SECTOR, "Sector", xpath);
		WWXML.checkAndSetBooleanParam(domElement, params, AVKeyMore.WRAP, "Wrap", xpath);

		return params;
	}

	private void recalculateVertices(Globe globe, double verticalExaggeration)
	{
		synchronized (elevationLock)
		{
			if (elevations != null)
			{
				elevations.rewind();
				vertices.rewind();
				Angle minlon = sector.getMinLongitude();
				Angle minlat = sector.getMaxLatitude();
				double lonstep = sector.getDeltaLonDegrees() / (width - 1);
				double latstep = sector.getDeltaLatDegrees() / (height - 1);
				for (int y = 0; y < height; y++)
				{
					Angle lat = minlat.subtractDegrees(latstep * y);
					for (int x = 0; x < width; x++)
					{
						Angle lon = minlon.addDegrees(lonstep * x);
						double elev = elevations.get() * scale * verticalExaggeration;
						Vec4 point = globe.computePointFromPosition(lat, lon, elev);
						vertices.put(point.x).put(point.y).put(point.z);
					}
				}
			}
		}
	}

	private void recalculateColors()
	{
		synchronized (elevationLock)
		{
			if (elevations != null)
			{
				elevations.rewind();
				colors.rewind();
				for (int i = 0; i < width * height; i++)
				{
					double[] color = chroma((elevations.get() - minElevation) / (maxElevation - minElevation), getOpacity());
					colors.put(color);
				}
			}
		}
	}

	@Override
	public void setOpacity(double opacity)
	{
		super.setOpacity(opacity);
		recalculateColors();
	}

	protected static IntBuffer generateTriStripIndices(int width, int height, boolean wrapWidth)
	{
		int w = width - 1;
		if (!wrapWidth)
		{
			width--;
		}
		height--;
		int indexCount = 2 * width * height + 4 * width - 2;
		IntBuffer buffer = Buffers.newDirectIntBuffer(indexCount);
		int k = 0;
		for (int i = 0; i < width; i++)
		{
			buffer.put(k);
			if (i > 0)
			{
				buffer.put(++k);
				buffer.put(k);
			}

			if (i % 2 == 0) // even
			{
				buffer.put(++k);
				for (int j = 0; j < height; j++)
				{
					k += w;
					buffer.put(k);
					buffer.put(++k);
				}
			}
			else
			// odd
			{
				buffer.put(--k);
				for (int j = 0; j < height; j++)
				{
					k -= w;
					buffer.put(k);
					buffer.put(--k);
				}
			}
		}

		if (wrapWidth)
		{
			boolean even = width % 2 == 0;
			int fixLast = 2 * height + 3 - (even ? 0 : 1);
			for (int i = indexCount - fixLast + 1; i < indexCount; i += 2)
			{
				buffer.put(i, buffer.get(i) - width);
			}
			if (even)
			{
				buffer.put(fixLast, buffer.get(fixLast) - width);
			}
		}

		return buffer;
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		if (!loaded)
		{
			loaded = true;
			loadAttempts++;
			downloadData();
		}

		if (lastVerticalExaggeration != dc.getVerticalExaggeration() || lastGlobe != dc.getGlobe())
		{
			lastVerticalExaggeration = dc.getVerticalExaggeration();
			lastGlobe = dc.getGlobe();
			recalculateVertices(lastGlobe, lastVerticalExaggeration);
			recalculateColors();
		}

		GL2 gl = dc.getGL();

		int push = GL2.GL_CLIENT_VERTEX_ARRAY_BIT;
		if (colors != null)
		{
			push |= GL2.GL_COLOR_BUFFER_BIT;
		}
		if (getOpacity() < 1.0)
		{
			push |= GL2.GL_CURRENT_BIT;
		}
		gl.glPushClientAttrib(push);

		if (colors != null)
		{
			gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
			gl.glColorPointer(4, GL2.GL_DOUBLE, 0, colors.rewind());
		}
		if (getOpacity() < 1.0)
		{
			setBlendingFunction(dc);
		}

		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glVertexPointer(3, GL2.GL_DOUBLE, 0, vertices.rewind());

		gl.glDrawElements(GL2.GL_TRIANGLE_STRIP, indices.limit(),
				GL2.GL_UNSIGNED_INT, indices.rewind());

		gl.glColor4d(1, 1, 1, 1);
		gl.glPopClientAttrib();
	}

	protected void downloadData()
	{
		//run download in separate thread, so that data loading from download
		//cache doesn't freeze up the render thread

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				RetrievalHandler handler = new RetrievalHandler()
				{
					@Override
					public void handle(RetrievalResult result)
					{
						if (result.hasData())
						{
							loadData(result.getAsString());
						}
						else if (result.getError() != null)
						{
							result.getError().printStackTrace();
						}
					}
				};

				Downloader.downloadIfModified(url, handler, handler, true);
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	protected void loadData(String s)
	{
		try
		{
			double[] doubles = parseDoubles(s);

			if (doubles.length != width * height)
				throw new IOException("File doesn't contain width x height (" + (width * height) + ") values");

			DoubleBuffer buffer = Buffers.newDirectDoubleBuffer(width * height);
			buffer.put(doubles);
			buffer.rewind();
			
			for (int i = 0; i < width * height; i++)
			{
				double elev = buffer.get();
				minElevation = Math.min(minElevation, elev);
				maxElevation = Math.max(maxElevation, elev);
			}

			synchronized (elevationLock)
			{
				this.elevations = buffer;
			}
			//force a recalculate
			lastGlobe = null;
			firePropertyChange(AVKey.LAYER, null, this);
		}
		catch (IOException e)
		{
			if (loadAttempts < MAX_DOWNLOAD_ATTEMPTS)
			{
				loaded = false;
				Downloader.removeCache(url);
				Logging.logger().warning("Deleted corrupt cached data file for " + url);
			}
			else
			{
				e.printStackTrace();
			}
		}
	}

	protected void setBlendingFunction(DrawContext dc)
	{
		GL2 gl = dc.getGL();
		double alpha = this.getOpacity();
		gl.glColor4d(alpha, alpha, alpha, alpha);
		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	}

	private static double[] chroma(double depth, double opacity)
	{
		double r = 2.0 - depth * 4.0;
		double b = depth * 4.0 - 2.0;
		double g = depth * 4.0;
		if (g >= 2.0)
		{
			g = 4.0 - g;
		}
		return new double[] { clamp(r, 0, 1), clamp(g, 0, 1), clamp(b, 0, 1), opacity };
	}

	private static double clamp(double value, double min, double max)
	{
		return value > max ? max : value < min ? min : value;
	}

	private static double[] parseDoubles(String string)
	{
		String[] split = string.trim().split(WHITESPACE_COMMA_REGEX);
		double[] array = new double[split.length];
		try
		{
			for (int i = 0; i < array.length; i++)
			{
				array[i] = Double.parseDouble(split[i]);
			}
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		return array;
	}

	protected void fireLoadingStateChanged()
	{
		for (int i = loadingListeners.size() - 1; i >= 0; i--)
		{
			loadingListeners.get(i).loadingStateChanged(isLoading());
		}
	}

	protected void setLoading(boolean loading)
	{
		this.loading = loading;
	}

	@Override
	public boolean isLoading()
	{
		return loading;
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		loadingListeners.remove(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		loadingListeners.add(listener);
	}
}
