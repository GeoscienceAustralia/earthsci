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
package au.gov.ga.earthsci.worldwind.common.layers.borehole;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.AnnotationRenderer;
import gov.nasa.worldwind.render.BasicAnnotationRenderer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;
import au.gov.ga.earthsci.worldwind.common.layers.Bounds;
import au.gov.ga.earthsci.worldwind.common.layers.point.types.MarkerPointLayer;
import au.gov.ga.earthsci.worldwind.common.layers.styled.Attribute;
import au.gov.ga.earthsci.worldwind.common.layers.styled.BasicStyleProvider;
import au.gov.ga.earthsci.worldwind.common.layers.styled.Style;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StyleAndText;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StyleProvider;
import au.gov.ga.earthsci.worldwind.common.render.DeepPickingMarkerRenderer;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.ColorMap;
import au.gov.ga.earthsci.worldwind.common.util.CoordinateTransformationUtil;
import au.gov.ga.earthsci.worldwind.common.util.DefaultLauncher;
import au.gov.ga.earthsci.worldwind.common.util.OnTopGlobeAnnotation;
import au.gov.ga.earthsci.worldwind.common.util.Util;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * Basic implementation of the {@link BoreholeLayer}. Draws markers for each
 * borehole location, and coloured lines for borehole samples.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BasicBoreholeLayer extends AbstractLayer implements BoreholeLayer, SelectListener
{
	private static final Color DEFAULT_SAMPLE_COLOR = Color.GRAY;

	protected BoreholeProvider boreholeProvider;
	protected StyleProvider boreholeStyleProvider = new BasicStyleProvider();
	protected StyleProvider sampleStyleProvider = new BasicStyleProvider();
	protected final List<Borehole> boreholes = new ArrayList<Borehole>();
	protected final Map<Object, BoreholeImpl> idToBorehole = new HashMap<Object, BoreholeImpl>();
	protected final DeepPickingMarkerRenderer markerRenderer = new DeepPickingMarkerRenderer();
	protected final AnnotationRenderer annotationRenderer = new BasicAnnotationRenderer();

	protected URL context;
	protected String url;
	protected String dataCacheName;
	protected String uniqueIdentifierAttribute;
	protected String sampleDepthFromAttribute;
	protected String sampleDepthToAttribute;
	protected boolean attributesRepresentPositiveDepth = true;
	protected double lineWidth = 5;
	protected Double minimumDistance;
	protected CoordinateTransformation coordinateTransformation;
	protected ColorMap colorMap;

	protected GlobeAnnotation tooltipAnnotation;

	protected FastShape pathShape;
	protected FastShape samplesShape;
	protected float[] pickingColorBuffer;
	protected final PickSupport pickSupport = new PickSupport();

	@SuppressWarnings("unchecked")
	public BasicBoreholeLayer(AVList params)
	{
		context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
		url = params.getStringValue(AVKey.URL);
		dataCacheName = params.getStringValue(AVKey.DATA_CACHE_NAME);
		boreholeProvider = (BoreholeProvider) params.getValue(AVKeyMore.DATA_LAYER_PROVIDER);

		boreholeStyleProvider.setStyles((List<Style>) params.getValue(AVKeyMore.DATA_LAYER_STYLES));
		boreholeStyleProvider.setAttributes((List<Attribute>) params.getValue(AVKeyMore.DATA_LAYER_ATTRIBUTES));
		sampleStyleProvider.setStyles((List<Style>) params.getValue(AVKeyMore.BOREHOLE_SAMPLE_STYLES));
		sampleStyleProvider.setAttributes((List<Attribute>) params.getValue(AVKeyMore.BOREHOLE_SAMPLE_ATTRIBUTES));

		uniqueIdentifierAttribute = params.getStringValue(AVKeyMore.BOREHOLE_UNIQUE_IDENTIFIER_ATTRIBUTE);
		sampleDepthFromAttribute = params.getStringValue(AVKeyMore.BOREHOLE_SAMPLE_DEPTH_FROM_ATTRIBUTE);
		sampleDepthToAttribute = params.getStringValue(AVKeyMore.BOREHOLE_SAMPLE_DEPTH_TO_ATTRIBUTE);

		Boolean b = (Boolean) params.getValue(AVKeyMore.BOREHOLE_SAMPLE_DEPTH_ATTRIBUTES_POSITIVE);
		if (b != null)
		{
			attributesRepresentPositiveDepth = b;
		}

		Double d = (Double) params.getValue(AVKeyMore.LINE_WIDTH);
		if (d != null)
		{
			lineWidth = d;
		}

		String s = (String) params.getValue(AVKey.COORDINATE_SYSTEM);
		if (s != null)
		{
			setCoordinateTransformation(CoordinateTransformationUtil.getTransformationToWGS84(s));
		}

		ColorMap cm = (ColorMap) params.getValue(AVKeyMore.COLOR_MAP);
		if (cm != null)
		{
			setColorMap(cm);
		}

		minimumDistance = (Double) params.getValue(AVKeyMore.MINIMUM_DISTANCE);

		Validate.notBlank(url, "Borehole data url not set");
		Validate.notBlank(dataCacheName, "Borehole data cache name not set");

		Validate.notNull(boreholeProvider, "Borehole data provider is null");
		Validate.notNull(boreholeStyleProvider.getStyles(), "Borehole style list is null");
		Validate.notNull(boreholeStyleProvider.getAttributes(), "Borehole attribute list is null");
		Validate.notNull(sampleStyleProvider.getStyles(), "Borehole sample style list is null");
		Validate.notNull(sampleStyleProvider.getAttributes(), "Borehole sample attribute list is null");

		// Init tooltip annotation
		this.tooltipAnnotation = new OnTopGlobeAnnotation("", Position.fromDegrees(0, 0, 0));
		Font font = Font.decode("Arial-Plain-15");
		this.tooltipAnnotation.getAttributes().setFont(font);
		this.tooltipAnnotation.getAttributes().setSize(new Dimension(270, 0));
		this.tooltipAnnotation.getAttributes().setDistanceMinScale(1);
		this.tooltipAnnotation.getAttributes().setDistanceMaxScale(1);
		this.tooltipAnnotation.getAttributes().setVisible(false);
		this.tooltipAnnotation.setPickEnabled(false);
		this.tooltipAnnotation.setAlwaysOnTop(true);
		this.tooltipAnnotation
				.setAltitudeMode(isFollowTerrain() ? WorldWind.RELATIVE_TO_GROUND : WorldWind.ABSOLUTE);

		markerRenderer.setKeepSeparated(false);
		markerRenderer.setDrawImmediately(true);
		if (isFollowTerrain())
		{
			markerRenderer.setOverrideMarkerElevation(true);
			markerRenderer.setElevation(0);
		}

		WorldWindowRegistry.INSTANCE.addSelectListener(this);
	}

	@Override
	public Bounds getBounds()
	{
		return boreholeProvider.getBounds();
	}

	@Override
	public boolean isFollowTerrain()
	{
		return boreholeProvider.isFollowTerrain();
	}

	@Override
	public URL getUrl() throws MalformedURLException
	{
		return new URL(context, url);
	}

	@Override
	public String getDataCacheName()
	{
		return dataCacheName;
	}

	@Override
	public void addBorehole(Borehole borehole)
	{
		synchronized (boreholes)
		{
			boreholes.add(borehole);
		}
	}

	@Override
	public void addBoreholeSample(Position position, AVList attributeValues)
	{
		Object id = attributeValues.getValue(uniqueIdentifierAttribute);
		Validate.notNull(id, "Borehole attributes do not contain an identifier");

		Double depthFrom = Util.objectToDouble(attributeValues.getValue(sampleDepthFromAttribute));
		Double depthTo = Util.objectToDouble(attributeValues.getValue(sampleDepthToAttribute));
		Validate.notNull(depthFrom, "Borehole sample attributes do not contain a valid depth-from");
		Validate.notNull(depthTo, "Borehole sample attributes do not contain a valid depth-to");

		BoreholeImpl borehole = idToBorehole.get(id);
		if (borehole == null)
		{
			MarkerAttributes markerAttributes = new BasicMarkerAttributes();
			borehole = new BoreholeImpl(position, markerAttributes);
			idToBorehole.put(id, borehole);
			synchronized (boreholes)
			{
				boreholes.add(borehole);
			}

			StyleAndText boreholeProperties = boreholeStyleProvider.getStyle(attributeValues);
			borehole.setUrl(boreholeProperties.link);
			borehole.setTooltipText(boreholeProperties.text);
			boreholeProperties.style.setPropertiesFromAttributes(context, attributeValues, markerAttributes, borehole);
			MarkerPointLayer.fixShapeType(markerAttributes);
		}

		BoreholeSampleImpl sample = new BoreholeSampleImpl(borehole);
		borehole.addSample(sample);
		sample.setDepthFrom(attributesRepresentPositiveDepth ? depthFrom : -depthFrom);
		sample.setDepthTo(attributesRepresentPositiveDepth ? depthTo : -depthTo);

		StyleAndText sampleProperties = sampleStyleProvider.getStyle(attributeValues);
		sample.setText(sampleProperties.text);
		sample.setLink(sampleProperties.link);
		sampleProperties.style.setPropertiesFromAttributes(context, attributeValues, sample);
	}

	@Override
	public void loadComplete()
	{
		List<Position> positions = new ArrayList<Position>();
		List<Color> colors = new ArrayList<Color>();
		List<Position> pathPositions = new ArrayList<Position>();

		for (Borehole borehole : boreholes)
		{
			borehole.loadComplete();

			BoreholePath path = borehole.getPath();
			for (BoreholeSample sample : borehole.getSamples())
			{
				Position sampleTop = path.getPosition(sample.getDepthFrom());
				Position sampleBottom = path.getPosition(sample.getDepthTo());

				positions.add(sampleTop);
				positions.add(sampleBottom);

				Color sampleColor = sample.getColor();
				sampleColor = sampleColor != null ? sampleColor : getDefaultSampleColor();
				colors.add(sampleColor);
				colors.add(sampleColor);
			}

			Position lastPosition = null;
			for (Position position : path.getPositions().values())
			{
				if (lastPosition != null)
				{
					pathPositions.add(lastPosition);
					pathPositions.add(position);
				}
				lastPosition = position;
			}
		}

		FastShape pathShape = new FastShape(pathPositions, GL2.GL_LINES);
		pathShape.setColor(Color.LIGHT_GRAY);
		pathShape.setLineWidth(1.0);
		pathShape.setFollowTerrain(isFollowTerrain());

		float[] boreholeColorBuffer = FastShape.color3ToFloats(colors);
		pickingColorBuffer = new float[colors.size() * 3];

		FastShape samplesShape = new FastShape(positions, GL2.GL_LINES);
		samplesShape.setColorBuffer(boreholeColorBuffer);
		samplesShape.setFollowTerrain(isFollowTerrain());
		samplesShape.setLineWidth(lineWidth);

		//set at the end, so that half-setup shape isn't rendered on the render thread 
		this.pathShape = pathShape;
		this.samplesShape = samplesShape;
	}

	@Override
	public Double getMinimumDistance()
	{
		return minimumDistance;
	}

	@Override
	protected void doPick(DrawContext dc, Point point)
	{
		doRender(dc);
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		if (!isEnabled())
		{
			return;
		}

		boreholeProvider.requestData(this);
		synchronized (boreholes)
		{
			markerRenderer.render(dc, allMarkers());
			annotationRenderer.render(dc, tooltipAnnotation, tooltipAnnotation.getAnnotationDrawPoint(dc), this);
			if (samplesShape != null)
			{
				if (!dc.isPickingMode())
				{
					samplesShape.render(dc);
					pathShape.render(dc);
				}
				else
				{
					boolean oldDeepPicking = dc.isDeepPickingEnabled();
					try
					{
						//deep picking needs to be enabled, because boreholes are below the surface
						dc.setDeepPickingEnabled(true);
						pickSupport.beginPicking(dc);

						//First pick on the entire object by setting the shape to a single color.
						//This will determine if we have to go further and pick individual samples.
						Color overallPickColor = dc.getUniquePickColor();
						pickSupport.addPickableObject(overallPickColor.getRGB(), samplesShape, getBounds().center);
						samplesShape.setColor(overallPickColor);
						samplesShape.setColorBufferEnabled(false);
						samplesShape.render(dc);
						samplesShape.setColorBufferEnabled(true);

						PickedObject object = pickSupport.getTopObject(dc, dc.getPickPoint());
						pickSupport.clearPickList();

						if (object != null && object.getObject() == samplesShape)
						{
							//This layer has been picked; now try picking the samples individually

							//Put unique pick colours into the pickingColorBuffer (2 per sample)
							int i = 0;
							for (Borehole borehole : boreholes)
							{
								for (BoreholeSample sample : borehole.getSamples())
								{
									Color color = dc.getUniquePickColor();
									pickSupport.addPickableObject(color.getRGB(), sample, getBounds().center);
									for (int j = 0; j < 2; j++)
									{
										pickingColorBuffer[i++] = color.getRed() / 255f;
										pickingColorBuffer[i++] = color.getGreen() / 255f;
										pickingColorBuffer[i++] = color.getBlue() / 255f;
									}
								}
							}

							//render the shape with the pickingColorBuffer, and then resolve the pick
							samplesShape.setPickingColorBuffer(pickingColorBuffer);
							samplesShape.render(dc);
							pickSupport.resolvePick(dc, dc.getPickPoint(), this);
						}
					}
					finally
					{
						pickSupport.endPicking(dc);
						dc.setDeepPickingEnabled(oldDeepPicking);
					}
				}
			}
		}
	}

	@Override
	public boolean isLoading()
	{
		return boreholeProvider.isLoading();
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		boreholeProvider.addLoadingListener(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		boreholeProvider.removeLoadingListener(listener);
	}

	@Override
	public void selected(SelectEvent e)
	{
		if (e == null)
		{
			return;
		}

		PickedObject topPickedObject = e.getTopPickedObject();
		Object object = topPickedObject != null ? topPickedObject.getObject() : null;
		if (object instanceof Borehole || object instanceof BoreholeSample || object instanceof BoreholeMarker)
		{
			highlight(object, true);

			if (e.getEventAction() == SelectEvent.LEFT_CLICK)
			{
				String link = null;

				if (object instanceof Borehole)
				{
					link = ((Borehole) object).getLink();
				}
				else if (object instanceof BoreholeSample)
				{
					link = ((BoreholeSample) object).getLink();
				}
				else if (object instanceof BoreholeMarker)
				{
					link = ((BoreholeMarker) object).getLink();
				}
				if (link != null)
				{
					try
					{
						URL url = new URL(link);
						DefaultLauncher.openURL(url);
					}
					catch (MalformedURLException m)
					{
					}
				}
			}
		}
		else
		{
			highlight(null, false);
		}
	}

	@Override
	public Color getDefaultSampleColor()
	{
		return DEFAULT_SAMPLE_COLOR;
	}

	@Override
	public CoordinateTransformation getCoordinateTransformation()
	{
		return coordinateTransformation;
	}

	public void setCoordinateTransformation(CoordinateTransformation coordinateTransformation)
	{
		this.coordinateTransformation = coordinateTransformation;
	}

	@Override
	public ColorMap getColorMap()
	{
		return colorMap;
	}

	public void setColorMap(ColorMap colorMap)
	{
		this.colorMap = colorMap;
	}

	/**
	 * Highlight the provided object by showing the tooltip annotation over it.
	 * 
	 * @param object
	 *            {@link Borehole} or {@link BoreholeSample} to highlight
	 * @param highlight
	 *            True to show tooltip annotation, false to hide it
	 */
	protected void highlight(Object object, boolean highlight)
	{
		if (highlight)
		{
			String text = null;
			Position position = null;

			if (object instanceof Borehole)
			{
				Borehole borehole = (Borehole) object;
				text = borehole.getText();
				position = borehole.getPosition();
			}
			else if (object instanceof BoreholeSample)
			{
				BoreholeSample sample = (BoreholeSample) object;
				text = sample.getText();
				double depth = (sample.getDepthTo() + sample.getDepthFrom()) / 2d;
				position = sample.getBorehole().getPath().getPosition(depth);
			}
			else if (object instanceof BoreholeMarker)
			{
				BoreholeMarker marker = (BoreholeMarker) object;
				text = marker.getText();
				position = marker.getBorehole().getPath().getPosition(marker.getDepth());
			}

			if (text != null)
			{
				tooltipAnnotation.setText(text);
				tooltipAnnotation.setPosition(position);
				tooltipAnnotation.getAttributes().setVisible(true);
			}
		}
		else
		{
			tooltipAnnotation.getAttributes().setVisible(false);
		}
	}

	protected Iterable<Marker> allMarkers()
	{
		return new Iterable<Marker>()
		{
			@Override
			public Iterator<Marker> iterator()
			{
				return new Iterator<Marker>()
				{
					private Iterator<? extends Marker> current = boreholes.iterator();
					private int boreholeIndex = 0;

					@Override
					public boolean hasNext()
					{
						if (current.hasNext())
						{
							return true;
						}
						while (boreholeIndex < boreholes.size())
						{
							current = boreholes.get(boreholeIndex++).getMarkers().iterator();
							if (current.hasNext())
							{
								return true;
							}
						}
						return false;
					}

					@Override
					public Marker next()
					{
						if (!hasNext())
						{
							return null;
						}
						return current.next();
					}

					@Override
					public void remove() {
					}
				};
			}
		};
	}
}
