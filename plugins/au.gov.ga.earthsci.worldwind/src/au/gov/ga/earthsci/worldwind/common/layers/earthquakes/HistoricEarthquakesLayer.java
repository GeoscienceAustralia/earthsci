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
package au.gov.ga.earthsci.worldwind.common.layers.earthquakes;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWXML;

import java.awt.Color;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import javax.media.opengl.GL2;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.downloader.Downloader;
import au.gov.ga.earthsci.worldwind.common.downloader.RetrievalHandler;
import au.gov.ga.earthsci.worldwind.common.downloader.RetrievalResult;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.HSLColor;
import au.gov.ga.earthsci.worldwind.common.util.Loader;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * A specialised sub-surface layer that reads and displays earthquake data from
 * a reference data file.
 * <p/>
 * Each datapoint in the earthquake file is plotted as a point plotted at the
 * recorded earthquake depth.
 * <p/>
 * Colouring is configurable, and can be based on Date, Magnitude or Depth.
 * <p/>
 * This implementation makes use of the {@link FastShape} class to load
 * earthquake data outside the rendering thread to ensure the interface remains
 * responsive.
 * <p/>
 * Each record in the data file should have the following format (without line
 * breaks):
 * 
 * <pre>
 * double latitude (in degrees)
 * double longitude (in degrees)
 * double elevation (in metres - negative indicates subsurface)
 * double magnitude
 * long timestamp (in milliseconds since epoc 01 01 1970 00:00:00 UTC)
 * </pre>
 * 
 * To save on bandwidth, it is recommended that the data file be compressed into
 * a .zip file.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class HistoricEarthquakesLayer extends AbstractLayer implements Loader
{
	public final static String DATE_COLORING = "Date";
	public final static String MAGNITUDE_COLORING = "Magnitude";
	public final static String DEPTH_COLORING = "Depth";

	private final static int MAX_DOWNLOAD_ATTEMPTS = 3;

	private final URL url;
	private final String coloring;
	private Long coloringMinDate;
	private Long coloringMaxDate;

	private double pointSize;

	private boolean loaded = false;
	private int loadAttempts = 0;

	private boolean loading = false;
	private final List<LoadingListener> loadingListeners = new ArrayList<LoadingListener>();

	private FastShape shape;
	private final Object shapeLock = new Object();

	public HistoricEarthquakesLayer(AVList params)
	{
		if (params.getValue(AVKey.URL) == null)
		{
			throw new IllegalArgumentException("URL not defined");
		}
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

		String coloring = MAGNITUDE_COLORING;
		if (params.getValue(AVKeyMore.COLORING) != null)
		{
			coloring = params.getStringValue(AVKeyMore.COLORING);
		}
		this.coloring = coloring;

		pointSize = 1;
		if (params.getValue(AVKeyMore.POINT_SIZE) != null)
		{
			pointSize = (Double) params.getValue(AVKeyMore.POINT_SIZE);
		}

		coloringMinDate = (Long) params.getValue(AVKeyMore.COLORING_MIN_DATE);
		coloringMaxDate = (Long) params.getValue(AVKeyMore.COLORING_MAX_DATE);
	}

	public HistoricEarthquakesLayer(Document dom, AVList params)
	{
		this(dom.getDocumentElement(), params);
	}

	public HistoricEarthquakesLayer(Element domElement, AVList params)
	{
		this(getParamsFromDocument(domElement, params));
	}

	protected static AVList getParamsFromDocument(Element domElement, AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();

		// Common layer properties.
		AbstractLayer.getLayerConfigParams(domElement, params);

		WWXML.checkAndSetStringParam(domElement, params, AVKey.URL, "URL", xpath);
		WWXML.checkAndSetStringParam(domElement, params, AVKeyMore.COLORING, "Coloring", xpath);
		WWXML.checkAndSetDoubleParam(domElement, params, AVKeyMore.POINT_SIZE, "PointSize", xpath);
		XMLUtil.checkAndSetFormattedDateParam(domElement, params, AVKeyMore.COLORING_MIN_DATE, "ColoringMinDate", xpath);
		XMLUtil.checkAndSetFormattedDateParam(domElement, params, AVKeyMore.COLORING_MAX_DATE, "ColoringMaxDate", xpath);

		return params;
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

		synchronized (shapeLock)
		{
			if (shape != null)
			{
				shape.setPointSize(pointSize);
				shape.render(dc);
			}
		}
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
							loadData(result.getAsInputStream());
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

	protected void loadData(InputStream is)
	{
		try
		{
			boolean isZipFile = url.toExternalForm().toLowerCase().endsWith(".zip");
			if (isZipFile)
			{
				@SuppressWarnings("resource") //closed by the ObjectInputStream below
				ZipInputStream zis = new ZipInputStream(is);
				zis.getNextEntry(); //move to first entry
				is = zis;
			}

			List<Earthquake> quakes = new ArrayList<Earthquake>();
			ObjectInputStream ois = new ObjectInputStream(is);
			try
			{
				while (is.available() > 0)
				{
					double lat = ois.readDouble();
					double lon = ois.readDouble();
					double elevation = ois.readDouble();
					double magnitude = ois.readDouble();
					long timeInMillis = ois.readLong();

					Position position = Position.fromDegrees(lat, lon, elevation);
					Earthquake quake = new Earthquake(position, magnitude, timeInMillis);
					quakes.add(quake);
				}
			}
			catch (EOFException e)
			{
				//When reading from a ZipInputStream, the ObjectInputStream.available() method always returns 0,
				//so just read it anyway. This will throw an EOFException at some stage (when there's no data
				//left), this means we are at the end of the file. Ignore it.
			}
			finally
			{
				ois.close();
			}

			loadEarthquakes(quakes);
		}
		catch (IOException e)
		{
			e.printStackTrace();
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

	protected void loadEarthquakes(List<Earthquake> earthquakes)
	{
		List<Position> positions = new ArrayList<Position>();
		for (Earthquake earthquake : earthquakes)
		{
			positions.add(earthquake.position);
		}

		FloatBuffer colorBuffer = FloatBuffer.allocate(positions.size() * 3);
		generateColorBuffer(colorBuffer, earthquakes);

		FastShape shape = new FastShape(positions, GL2.GL_POINTS);
		shape.setColorBuffer(colorBuffer.array());
		shape.setColorBufferElementSize(3);

		synchronized (shapeLock)
		{
			this.shape = shape;
		}

		firePropertyChange(AVKey.LAYER, null, this);
	}

	private void generateColorBuffer(FloatBuffer colorBuffer, List<Earthquake> earthquakes)
	{
		if (DEPTH_COLORING.equalsIgnoreCase(coloring))
		{
			generateDepthColoring(colorBuffer, earthquakes);
		}
		else if (DATE_COLORING.equalsIgnoreCase(coloring))
		{
			generateDateColoring(colorBuffer, earthquakes);
		}
		else
		{
			generateMagnitudeColoring(colorBuffer, earthquakes);
		}
	}

	/**
	 * Populate the color buffer with colours based on earthquake date.
	 * <p/>
	 * Blue (shallow) -> Red (deep)
	 */
	protected void generateMagnitudeColoring(FloatBuffer colorBuffer, List<Earthquake> earthquakes)
	{
		//magnitude coloring
		double minMagnitude = Double.MAX_VALUE;
		double maxMagnitude = -Double.MAX_VALUE;
		for (Earthquake earthquake : earthquakes)
		{
			minMagnitude = Math.min(minMagnitude, earthquake.magnitude);
			maxMagnitude = Math.max(maxMagnitude, earthquake.magnitude);
		}
		for (Earthquake earthquake : earthquakes)
		{
			double percent = (earthquake.magnitude - minMagnitude) / (maxMagnitude - minMagnitude);

			//scale the magnitude (VERY crude equalisation)
			percent = 1 - Math.pow(percent, 0.2);

			Color color = new HSLColor((float) (240d * percent), 100f, 50f).getRGB();
			colorBuffer.put(color.getRed() / 255f).put(color.getGreen() / 255f).put(color.getBlue() / 255f);
		}
	}

	/**
	 * Populate the color buffer with colours based on earthquake date.
	 * <p/>
	 * Blue (old) -> Red (new)
	 */
	protected void generateDateColoring(FloatBuffer colorBuffer, List<Earthquake> earthquakes)
	{
		long minTime = Long.MAX_VALUE;
		long maxTime = Long.MIN_VALUE;

		//if either of the custom min/max dates are null, calculate from the data
		if (coloringMinDate == null || coloringMaxDate == null)
		{
			for (Earthquake earthquake : earthquakes)
			{
				minTime = Math.min(minTime, earthquake.timeInMillis);
				maxTime = Math.max(maxTime, earthquake.timeInMillis);
			}
		}

		minTime = coloringMinDate != null ? coloringMinDate : minTime;
		maxTime = coloringMaxDate != null ? coloringMaxDate : maxTime;

		for (Earthquake earthquake : earthquakes)
		{
			double percent = (earthquake.timeInMillis - minTime) / (double) (maxTime - minTime);
			percent = 1 - Math.max(0, Math.min(1, percent));
			Color color = new HSLColor((float) (240d * percent), 100f, 50f).getRGB();
			colorBuffer.put(color.getRed() / 255f).put(color.getGreen() / 255f).put(color.getBlue() / 255f);
		}
	}

	/**
	 * Populate the color buffer with colours based on earthquake depth.
	 * <p/>
	 * Blue (shallow) -> Red (deep)
	 */
	protected void generateDepthColoring(FloatBuffer colorBuffer, List<Earthquake> earthquakes)
	{
		double minElevation = Double.MAX_VALUE;
		double maxElevation = -Double.MAX_VALUE;
		for (Earthquake earthquake : earthquakes)
		{
			minElevation = Math.min(minElevation, earthquake.position.elevation);
			maxElevation = Math.max(maxElevation, earthquake.position.elevation);
		}
		for (Earthquake earthquake : earthquakes)
		{
			double percent = (earthquake.position.elevation - minElevation) / (maxElevation - minElevation);
			Color color = new HSLColor((float) (240d * percent), 100f, 50f).getRGB();
			colorBuffer.put(color.getRed() / 255f).put(color.getGreen() / 255f).put(color.getBlue() / 255f);
		}
	}

	protected static class Earthquake
	{
		public final Position position;
		public final double magnitude;
		public final long timeInMillis;

		public Earthquake(Position position, double magnitude, long timeInMillis)
		{
			this.position = position;
			this.magnitude = magnitude;
			this.timeInMillis = timeInMillis;
		}
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
