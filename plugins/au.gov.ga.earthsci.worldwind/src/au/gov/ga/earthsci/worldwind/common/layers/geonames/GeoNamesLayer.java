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
package au.gov.ga.earthsci.worldwind.common.layers.geonames;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GeographicText;
import gov.nasa.worldwind.render.GeographicTextRenderer;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.Logging;

import java.awt.Color;
import java.awt.Font;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import au.gov.ga.earthsci.worldwind.common.util.ColorFont;
import au.gov.ga.earthsci.worldwind.common.util.IterableProxy;

/**
 * Place name layer which uses place data from geonames.org. Uses
 * level-of-detail to download levels in the GeoName hirarchy according to
 * camera altitude.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GeoNamesLayer extends AbstractLayer
{
	private final static String GEONAMES_CHILDREN = "http://ws.geonames.org/children";
	private final static int GEONAMES_GLOBE_ID = 6295630;
	private final static String GEONAMES_USERNAME = "gam3dv";

	private GeoName topGeoName;
	private VisibilityCalculatorImpl visibilityCalculator = new VisibilityCalculatorImpl();
	private Queue<GeoName> requestQ;

	private final GeographicTextRenderer nameRenderer = new GeographicTextRenderer();
	private Object lock = new Object();

	//TODO different attributes for different feature codes (fcode)

	public GeoNamesLayer()
	{
		setName("GeoNames");
		setPickEnabled(false);

		ColorFontProvider fontProvider = setupFontProvider();
		topGeoName =
				new GeoName(null, GEONAMES_GLOBE_ID, LatLon.ZERO, null, null, -1, fontProvider, visibilityCalculator);

		requestQ = new PriorityBlockingQueue<GeoName>(64, new Comparator<GeoName>()
		{
			@Override
			public int compare(GeoName o1, GeoName o2)
			{
				double distance1 = visibilityCalculator.distanceSquaredFromEye(o1);
				double distance2 = visibilityCalculator.distanceSquaredFromEye(o2);
				return distance1 > distance2 ? 1 : distance1 == distance2 ? 0 : -1;
			}
		});
	}

	private ColorFontProvider setupFontProvider()
	{
		ColorFont def = new ColorFont(Font.decode("Arial-PLAIN-10"), Color.lightGray, Color.black);

		ColorFont continents = new ColorFont(Font.decode("Arial-BOLD-12"), new Color(255, 255, 240), Color.black);
		ColorFont countries = new ColorFont(Font.decode("Arial-BOLD-11"), Color.white, Color.black);
		ColorFont states = new ColorFont(Font.decode("Arial-BOLD-10"), Color.yellow, Color.black);
		ColorFont admin = new ColorFont(Font.decode("Arial-BOLD-10"), Color.orange, Color.black);
		ColorFont capitals = new ColorFont(Font.decode("Arial-BOLD-10"), Color.red, Color.black);
		ColorFont firstorder = new ColorFont(Font.decode("Arial-PLAIN-10"), Color.green, Color.black);
		ColorFont towns = new ColorFont(Font.decode("Arial-PLAIN-10"), Color.cyan, Color.black);

		ColorFontProvider fontProvider = new ColorFontProvider(def);
		fontProvider.put("CONT", continents);
		fontProvider.put("PCL", countries);
		fontProvider.put("PCLD", countries);
		fontProvider.put("PCLF", countries);
		fontProvider.put("PCLI", countries);
		fontProvider.put("PCLIX", countries);
		fontProvider.put("PCLS", countries);
		fontProvider.put("TERR", countries);
		fontProvider.put("ADM1", states);
		fontProvider.put("ADM2", admin);
		fontProvider.put("ADM3", admin);
		fontProvider.put("ADM4", admin);
		fontProvider.put("ADMD", admin);
		fontProvider.put("PPLC", capitals);
		fontProvider.put("PPLA", firstorder);
		fontProvider.put("PPL", towns);
		fontProvider.put("PPLG", towns);
		fontProvider.put("PPLL", towns);
		fontProvider.put("PPLQ", towns);
		fontProvider.put("PPLR", towns);
		fontProvider.put("PPLS", towns);
		fontProvider.put("PPLW", towns);
		fontProvider.put("PPLX", towns);

		return fontProvider;
	}

	@Override
	public void doRender(DrawContext dc)
	{
		int levels = calculateLevel(dc);

		Position eye = dc.getView().getEyePosition();
		Sector sector = dc.getVisibleSector();
		visibilityCalculator.setSector(sector);
		visibilityCalculator.setLevels(levels);
		visibilityCalculator.setEye(eye);

		synchronized (lock)
		{
			render(dc, topGeoName);
		}

		sendRequests();
	}

	public void sendRequests()
	{
		GeoName geoname = requestQ.poll();
		while (geoname != null && !WorldWind.getTaskService().isFull())
		{
			WorldWind.getTaskService().addTask(new RequestTask(geoname));
			geoname = requestQ.poll();
		}
		requestQ.clear();
	}

	private class RequestTask implements Runnable
	{
		private GeoName geoname;

		public RequestTask(GeoName geoname)
		{
			this.geoname = geoname;
		}

		@Override
		public void run()
		{
			if (geoname.cacheFileExists())
			{
				loadChildren(geoname);
			}
			else
			{
				download(geoname);
			}
		}
	}

	private int calculateLevel(DrawContext dc)
	{
		double altitude = computeAltitudeAboveGround(dc);
		return (int) (Earth.WGS84_EQUATORIAL_RADIUS / altitude) + 1;
	}

	private double computeAltitudeAboveGround(DrawContext dc)
	{
		View view = dc.getView();
		Position eyePosition = view.getEyePosition();
		Vec4 surfacePoint = getSurfacePoint(dc, eyePosition.getLatitude(), eyePosition.getLongitude());
		return view.getEyePoint().distanceTo3(surfacePoint);
	}

	private Vec4 getSurfacePoint(DrawContext dc, Angle latitude, Angle longitude)
	{
		Vec4 surfacePoint = dc.getSurfaceGeometry().getSurfacePoint(latitude, longitude);
		if (surfacePoint == null)
			surfacePoint =
					dc.getGlobe().computePointFromPosition(
							new Position(latitude, longitude, dc.getGlobe().getElevation(latitude, longitude)));
		return surfacePoint;
	}

	public void render(DrawContext dc, GeoName geoname)
	{
		if (visibilityCalculator.isVisible(geoname))
		{
			if (!geoname.loadedChildren())
			{
				requestChildren(geoname);
			}
			else
			{
				Collection<GeoName> children = geoname.getChildren();
				for (GeoName child : children)
				{
					render(dc, child);
				}
				nameRenderer.render(dc, new IterableProxy<GeographicText>(children));
			}
		}
	}

	public void requestChildren(GeoName geoname)
	{
		requestQ.add(geoname);
	}

	private void loadChildren(GeoName geoname)
	{
		synchronized (lock)
		{
			geoname.loadChildren();
		}
	}

	private void download(GeoName geoname)
	{
		if (!WorldWind.getRetrievalService().isAvailable())
			return;

		URL url = null;
		try
		{
			url = new URL(GEONAMES_CHILDREN + "?username=" + GEONAMES_USERNAME + "&geonameId=" + geoname.geonameId);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}

		if (WorldWind.getNetworkStatus().isHostUnavailable(url))
			return;

		Retriever retriever;
		DownloadPostProcessor dpp = new DownloadPostProcessor(geoname);

		if ("http".equalsIgnoreCase(url.getProtocol()))
		{
			retriever = new HTTPRetriever(url, dpp);
		}
		else
		{
			Logging.logger().severe("UnknownRetrievalProtocol: " + url.toString());
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

		WorldWind.getRetrievalService().runRetriever(retriever);
	}

	private class DownloadPostProcessor implements RetrievalPostProcessor
	{
		private GeoName geoname;

		public DownloadPostProcessor(GeoName geoname)
		{
			this.geoname = geoname;
		}

		@Override
		public ByteBuffer run(Retriever retriever)
		{
			ByteBuffer buffer = getBuffer(retriever);
			if (buffer != null)
			{
				try
				{
					if (!geoname.cacheFileExists())
					{
						geoname.saveChildren(buffer);
					}
					loadChildren(geoname);
				}
				catch (Exception e)
				{
					Logging.logger().log(java.util.logging.Level.SEVERE, "Error saving GeoNames .xml", e);
				}
			}
			return buffer;
		}

		public ByteBuffer getBuffer(Retriever retriever)
		{
			if (retriever == null)
			{
				String msg = Logging.getMessage("nullValue.RetrieverIsNull");
				Logging.logger().severe(msg);
				throw new IllegalArgumentException(msg);
			}

			if (!retriever.getState().equals(Retriever.RETRIEVER_STATE_SUCCESSFUL))
				return null;

			URLRetriever r = (URLRetriever) retriever;
			ByteBuffer buffer = r.getBuffer();

			if (retriever instanceof HTTPRetriever)
			{
				HTTPRetriever htr = (HTTPRetriever) retriever;
				if (htr.getResponseCode() != HttpURLConnection.HTTP_OK)
				{
					return null;
				}
			}

			if (buffer == null)
				return null;

			String contentType = r.getContentType();
			if (contentType != null
					&& (contentType.contains("xml") || contentType.contains("html") || contentType.contains("text")))
			{
				return buffer;
			}

			return null;
		}
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}
}
