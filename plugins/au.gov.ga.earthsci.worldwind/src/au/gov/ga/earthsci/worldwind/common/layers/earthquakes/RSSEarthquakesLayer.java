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
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLStackHandler;
import gov.nasa.worldwind.util.WWXML;
import gov.nasa.worldwind.view.BasicView;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.DoubleBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL2;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;
import au.gov.ga.earthsci.worldwind.common.downloader.Downloader;
import au.gov.ga.earthsci.worldwind.common.downloader.RetrievalHandler;
import au.gov.ga.earthsci.worldwind.common.downloader.RetrievalResult;
import au.gov.ga.earthsci.worldwind.common.util.DefaultLauncher;
import au.gov.ga.earthsci.worldwind.common.util.HSLColor;
import au.gov.ga.earthsci.worldwind.common.util.Loader;
import au.gov.ga.earthsci.worldwind.common.util.MapBackedNamespaceContext;

/**
 * A {@link RenderableLayer} that displays recent earthquake data sourced from a
 * RSS feed.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RSSEarthquakesLayer extends RenderableLayer implements Loader, SelectListener
{
	private static final String RSS_URL = "https://earthquakes.ga.gov.au/feeds/all_recent_unformatted.rss";

	private static final int UPDATE_TIME = 10 * 60 * 1000; //10 minutes
	private static final long ONE_DAY = 24 * 60 * 60 * 1000; //1 day
	private static final long MAX_TIME = 30 * ONE_DAY;

	private AnnotationAttributes attributes;
	private Timer updateTimer;
	private SurfaceEarthquakeAnnotation mouseEq, latestEq;
	private GlobeAnnotation tooltipAnnotation;
	private List<LoadingListener> loadingListeners = new ArrayList<LoadingListener>();
	private boolean loading;

	public RSSEarthquakesLayer()
	{
		// Init tooltip annotation
		this.tooltipAnnotation = new GlobeAnnotation("", Position.fromDegrees(0, 0, 0));
		Font font = Font.decode("Arial-Plain-16");
		this.tooltipAnnotation.getAttributes().setFont(font);
		this.tooltipAnnotation.getAttributes().setSize(new Dimension(270, 0));
		this.tooltipAnnotation.getAttributes().setDistanceMinScale(1);
		this.tooltipAnnotation.getAttributes().setDistanceMaxScale(1);
		this.tooltipAnnotation.getAttributes().setVisible(false);
		this.tooltipAnnotation.setPickEnabled(false);
		this.tooltipAnnotation.setAlwaysOnTop(true);

		updateTimer = new Timer(UPDATE_TIME, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				startEarthquakeDownload();
			}
		});
		updateTimer.start();
		startEarthquakeDownload();

		WorldWindowRegistry.INSTANCE.addSelectListener(this);
	}

	/**
	 * Start the earthquake RSS feed download, performing it on a separate
	 * daemon thread.
	 */
	protected void startEarthquakeDownload()
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				downloadEarthquakes();
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Download the earthquake RSS feed.
	 */
	protected void downloadEarthquakes()
	{
		setLoading(true);
		try
		{
			RetrievalHandler handler = new RetrievalHandler()
			{
				@Override
				public void handle(RetrievalResult result)
				{
					synchronized (this)
					{
						try
						{
							DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
							builderFactory.setNamespaceAware(true); // Required to account for the georss namespace used
							DocumentBuilder builder = builderFactory.newDocumentBuilder();
							StringReader reader = new StringReader(result.getAsString().trim());
							InputSource source = new InputSource(reader);
							Document document = builder.parse(source);

							Element[] items = WWXML.getElements(document.getDocumentElement(), "//item", null);
							if (items != null)
							{
								List<Earthquake> earthquakes =
										new ArrayList<RSSEarthquakesLayer.Earthquake>(items.length);
								for (Element item : items)
								{
									earthquakes.add(new Earthquake(item));
								}

								clearRenderables();
								for (Earthquake earthquake : earthquakes)
								{
									addEarthquake(earthquake);
								}
								addRenderable(tooltipAnnotation);
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						setLoading(false);
						firePropertyChange(AVKey.LAYER, null, this);
					}
				}
			};

			URL url = new URL(RSS_URL);
			Downloader.downloadAnyway(url, handler, handler, true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Add an earthquake to this layer. Called by the {@link RetrievalHandler}
	 * of the download.
	 * 
	 * @param earthquake
	 *            Earthquake to add.
	 */
	protected void addEarthquake(Earthquake earthquake)
	{
		if (attributes == null)
		{
			// Init default attributes for all eq
			attributes = new AnnotationAttributes();
			attributes.setLeader(AVKey.SHAPE_NONE);
			attributes.setDrawOffset(new Point(0, -16));
			attributes.setSize(new Dimension(32, 32));
			attributes.setBorderWidth(0);
			attributes.setCornerRadius(0);
			attributes.setBackgroundColor(new Color(0, 0, 0, 0));
		}

		Position surfacePosition = new Position(earthquake.position, 0);
		SurfaceEarthquakeAnnotation surfaceAnnotation =
				new SurfaceEarthquakeAnnotation(surfacePosition, earthquake, attributes);
		long time = MAX_TIME;
		if (earthquake.date != null)
		{
			// Compute days since
			Date now = new Date();
			time = now.getTime() - earthquake.date.getTime();

			// Update latestEq
			if (this.latestEq == null || this.latestEq.earthquake.date.getTime() < earthquake.date.getTime())
			{
				this.latestEq = surfaceAnnotation;
			}
		}

		double percent = Math.max(0, Math.min(1, time / (double) MAX_TIME));
		HSLColor hslColor = new HSLColor((float) percent * 240f, 80f, 50f);
		Color color = hslColor.getRGB();
		surfaceAnnotation.getAttributes().setTextColor(color);
		surfaceAnnotation.getAttributes().setScale(earthquake.magnitude.doubleValue() / 10);
		addRenderable(surfaceAnnotation);

		EarthquakeAnnotation deepAnnotation =
				new SubSurfaceEarthquakeAnnotation(earthquake.position, earthquake, attributes, surfaceAnnotation);
		deepAnnotation.getAttributes().setTextColor(color);
		deepAnnotation.getAttributes().setScale(earthquake.magnitude.doubleValue() / 10);
		deepAnnotation.setPickEnabled(false);
		addRenderable(deepAnnotation);
	}

	@Override
	public void selected(SelectEvent event)
	{
		Object o = event.getTopObject();
		if (event.getEventAction().equals(SelectEvent.ROLLOVER))
		{
			highlight(o);
		}
		else if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
		{
			click(o);
		}

		if (event.getSource() instanceof Component)
		{
			Cursor cursor =
					(o instanceof SurfaceEarthquakeAnnotation) ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : null;
			((Component) event.getSource()).setCursor(cursor);
		}
	}

	private void highlight(Object o)
	{
		if (this.mouseEq == o)
		{
			return; // same thing selected
		}

		if (this.mouseEq != null)
		{
			this.mouseEq.getAttributes().setHighlighted(false);
			this.mouseEq = null;
			this.tooltipAnnotation.getAttributes().setVisible(false);
		}

		if (o != null && o instanceof SurfaceEarthquakeAnnotation)
		{
			this.mouseEq = (SurfaceEarthquakeAnnotation) o;
			this.mouseEq.getAttributes().setHighlighted(true);
			this.tooltipAnnotation.setText(createTooltipAnnotationFromEarthquake(mouseEq.earthquake));
			this.tooltipAnnotation.setPosition(this.mouseEq.getPosition());
			this.tooltipAnnotation.getAttributes().setVisible(true);
		}
	}

	private String createTooltipAnnotationFromEarthquake(Earthquake quake)
	{
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");
		StringBuilder result = new StringBuilder();
		result.append("<p><b>").append(quake.title).append("</b></p>").append(dateFormat.format(quake.date))
				.append("<br/>").append("Magnitude: <b>").append(quake.magnitude).append("</b><br/>")
				.append("Depth: <b>").append((quake.position.elevation / -1000)).append(" km</b>");
		return result.toString();
	}

	private void click(Object o)
	{
		if (o != null && o instanceof SurfaceEarthquakeAnnotation)
		{
			String link = ((SurfaceEarthquakeAnnotation) o).earthquake.link;
			try
			{
				DefaultLauncher.openURL(new URL(link));
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);

		if (enabled != updateTimer.isRunning())
		{
			if (enabled)
			{
				updateTimer.start();
			}
			else
			{
				updateTimer.stop();
			}
		}
	}

	/**
	 * Represents a single Earthquake occurrence.
	 * <p/>
	 * Contains constructors able to interpret the GA RSS feed for earthquake
	 * data.
	 */
	public static class Earthquake
	{
		public final String title;
		public final Date date;
		public final String link;
		public final Position position;
		public final BigDecimal magnitude;

		private static final Pattern MAGNITUDE_TITLE_PATTERN = Pattern
				.compile("(?i)(?:magnitude )?(?-i)(\\d+(?:\\.\\d+)?),[\\s]*([\\w\\s'-,]*)");
		private static final Pattern DATE_ELEVATION_PATTERN =
				Pattern.compile("(?s)(?i)date and time(?-i).*?(UTC:[\\s]?\\d+\\s+\\w+\\s+\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}).*(?i)depth(?-i).*:\\s+(\\d+(?:\\.\\d+))");
		private static final String DATE_FORMAT = "Z: dd MMMM yyyy HH:mm:ss";

		public Earthquake(Element content)
		{
			if (content == null)
			{
				throw new IllegalArgumentException("An XML element is required.");
			}
			XPath xpath = WWXML.makeXPath();

			// Add the georss namespace to the xpath
			MapBackedNamespaceContext context = new MapBackedNamespaceContext();
			context.addMapping("georss", "http://www.georss.org/georss");
			xpath.setNamespaceContext(context);

			// Use the link as-is
			link = WWXML.getText(content, "link", xpath);

			// Parse magnitude and title from the 'title' element
			String titleElement = WWXML.getText(content, "title", xpath);
			{
				Matcher matcher = MAGNITUDE_TITLE_PATTERN.matcher(titleElement);
				if (matcher.find())
				{
					this.magnitude = new BigDecimal(matcher.group(1));
					this.title = matcher.group(2);
				}
				else
				{
					this.magnitude = new BigDecimal(0);
					this.title = titleElement;
				}
			}

			// Parse location from the 'georss:point' element
			String pointElement = WWXML.getText(content, "georss:point", xpath);
			String[] latLonElements = pointElement.split(" ");
			LatLon latlon =
					LatLon.fromDegrees(Double.parseDouble(latLonElements[0]), Double.parseDouble(latLonElements[1]));

			// Parse date and depth from the 'description' element
			String descriptionElement = WWXML.getText(content, "description", xpath);
			double elevation = 0;
			Date theDate = null;
			{
				Matcher matcher = DATE_ELEVATION_PATTERN.matcher(descriptionElement);
				if (matcher.find())
				{
					try
					{
						theDate = new SimpleDateFormat(DATE_FORMAT).parse(matcher.group(1));
					}
					catch (ParseException e)
					{
					}
					elevation = Double.parseDouble(matcher.group(2)) * -1000; // km -> m
				}
			}
			this.date = theDate;
			this.position = new Position(latlon, elevation);
		}
	}

	/**
	 * {@link GlobeAnnotation} subclass used for rendering a single earthquake.
	 */
	private abstract class EarthquakeAnnotation extends GlobeAnnotation
	{
		protected final Earthquake earthquake;
		protected DoubleBuffer shapeBuffer;

		public EarthquakeAnnotation(Position position, Earthquake earthquake, AnnotationAttributes defaults)
		{
			super("", position, defaults);
			this.earthquake = earthquake;
		}

		@Override
		protected void applyScreenTransform(DrawContext dc, int x, int y, int width, int height, double scale)
		{
			double finalScale = scale * this.computeScale(dc);

			GL2 gl = dc.getGL().getGL2();
			gl.glTranslated(x, y, 0);
			gl.glScaled(finalScale, finalScale, 1);
		}

		@Override
		public void render(DrawContext dc)
		{
			//override to pass the annotation point to the AnnotationRenderer, so that
			//frustum culling works for subsurface annotations

			if (dc == null)
			{
				String message = Logging.getMessage("nullValue.DrawContextIsNull");
				Logging.logger().severe(message);
				throw new IllegalArgumentException(message);
			}

			if (!this.getAttributes().isVisible())
			{
				return;
			}

			Vec4 annotationPoint = getAnnotationDrawPoint(dc);
			dc.getAnnotationRenderer().render(dc, this, annotationPoint, dc.getCurrentLayer());
		}

		@Override
		protected void doDraw(DrawContext dc, int width, int height, double opacity, Position pickPosition)
		{
			// Draw colored circle around screen point - use annotation's text color
			if (dc.isPickingMode())
			{
				this.bindPickableObject(dc, pickPosition);
			}

			this.applyColor(dc, this.getAttributes().getTextColor(), 0.6 * opacity, true);
			drawEarthquake(dc);
		}

		protected abstract void drawEarthquake(DrawContext dc);
	}

	/**
	 * {@link EarthquakeAnnotation} subclass used for rendering earthquakes on
	 * the surface of the globe.
	 */
	private class SurfaceEarthquakeAnnotation extends EarthquakeAnnotation
	{
		public SurfaceEarthquakeAnnotation(Position position, Earthquake earthquake, AnnotationAttributes defaults)
		{
			super(position, earthquake, defaults);
		}

		@Override
		protected void drawEarthquake(DrawContext dc)
		{
			// Draw 32x32 shape from its bottom left corner
			int size = 32;
			if (this.shapeBuffer == null)
			{
				this.shapeBuffer = FrameFactory.createShapeBuffer(AVKey.SHAPE_ELLIPSE, size, size, 0, null);
			}

			dc.getGL().getGL2().glTranslated(-size / 2, -size / 2, 0);
			FrameFactory.drawBuffer(dc, GL2.GL_TRIANGLE_FAN, this.shapeBuffer);
		}
	}

	/**
	 * {@link EarthquakeAnnotation} subclass used for rendering earthquakes
	 * below the globe's surface. Uses a line to visualize the earthquake's
	 * depth.
	 */
	private class SubSurfaceEarthquakeAnnotation extends EarthquakeAnnotation
	{
		private final SurfaceEarthquakeAnnotation surfaceAnnotation;

		public SubSurfaceEarthquakeAnnotation(Position position, Earthquake earthquake, AnnotationAttributes defaults,
				SurfaceEarthquakeAnnotation surfaceAnnotation)
		{
			super(position, earthquake, defaults);
			this.surfaceAnnotation = surfaceAnnotation;
		}

		@Override
		protected void drawEarthquake(DrawContext dc)
		{
			// Draw 32x32 shape from its bottom left corner
			int size = 32;
			if (this.shapeBuffer == null)
			{
				this.shapeBuffer = FrameFactory.createShapeBuffer(AVKey.SHAPE_RECTANGLE, size, 4, 0, null);
			}

			dc.getGL().getGL2().glTranslated(-size / 2, -2, 0);
			FrameFactory.drawBuffer(dc, GL2.GL_TRIANGLE_FAN, this.shapeBuffer);


			//if this is the subsurface annotation, draw a connected line
			if (surfaceAnnotation != null)
			{
				Vec4 drawPoint = getAnnotationDrawPoint(dc);
				Vec4 surfacePoint = surfaceAnnotation.getAnnotationDrawPoint(dc);

				if (drawPoint != null && surfacePoint != null)
				{
					GL2 gl = dc.getGL().getGL2();
					OGLStackHandler stack = new OGLStackHandler();

					try
					{
						stack.pushModelview(gl);
						stack.pushProjection(gl);
						BasicView.loadGLViewState(dc, dc.getView().getModelviewMatrix(), dc.getView()
								.getProjectionMatrix());

						gl.glLineWidth(1f);
						gl.glBegin(GL2.GL_LINES);
						{
							gl.glVertex3d(drawPoint.x, drawPoint.y, drawPoint.z);
							gl.glVertex3d(surfacePoint.x, surfacePoint.y, surfacePoint.z);
						}
						gl.glEnd();
					}
					finally
					{
						stack.pop(gl);
					}
				}
			}
		}
	}

	protected void fireLoadingStateChanged()
	{
		for (int i = loadingListeners.size() - 1; i >= 0; i--)
		{
			LoadingListener listener = loadingListeners.get(i);
			listener.loadingStateChanged(this, isLoading());
		}
	}

	protected void setLoading(boolean loading)
	{
		this.loading = loading;
		fireLoadingStateChanged();
	}

	@Override
	public boolean isLoading()
	{
		return loading;
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		loadingListeners.add(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		loadingListeners.remove(listener);
	}
}
