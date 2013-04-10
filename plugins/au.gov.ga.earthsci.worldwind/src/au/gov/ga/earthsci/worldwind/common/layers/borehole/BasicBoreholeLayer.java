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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.AnnotationRenderer;
import gov.nasa.worldwind.render.BasicAnnotationRenderer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerRenderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;

import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;
import au.gov.ga.earthsci.worldwind.common.layers.point.types.MarkerPointLayer;
import au.gov.ga.earthsci.worldwind.common.layers.styled.Attribute;
import au.gov.ga.earthsci.worldwind.common.layers.styled.BasicStyleProvider;
import au.gov.ga.earthsci.worldwind.common.layers.styled.Style;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StyleAndText;
import au.gov.ga.earthsci.worldwind.common.layers.styled.StyleProvider;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
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
	protected final List<BoreholeImpl> boreholes = new ArrayList<BoreholeImpl>();
	protected final List<Marker> markers = new ArrayList<Marker>();
	protected final Map<Object, BoreholeImpl> idToBorehole = new HashMap<Object, BoreholeImpl>();
	protected final MarkerRenderer markerRenderer = new MarkerRenderer();
	protected final AnnotationRenderer annotationRenderer = new BasicAnnotationRenderer();

	protected URL context;
	protected String url;
	protected String dataCacheName;
	protected String uniqueIdentifierAttribute;
	protected String sampleDepthFromAttribute;
	protected String sampleDepthToAttribute;
	protected boolean attributesRepresentPositiveDepth = true;
	protected double lineWidth = 10;
	protected Double minimumDistance;

	protected GlobeAnnotation tooltipAnnotation;

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

		minimumDistance = (Double) params.getValue(AVKeyMore.MINIMUM_DISTANCE);

		Validate.notBlank(url, "Borehole data url not set");
		Validate.notBlank(dataCacheName, "Borehole data cache name not set");

		Validate.notNull(boreholeProvider, "Borehole data provider is null");
		Validate.notNull(boreholeStyleProvider.getStyles(), "Borehole style list is null");
		Validate.notNull(boreholeStyleProvider.getAttributes(), "Borehole attribute list is null");
		Validate.notNull(sampleStyleProvider.getStyles(), "Borehole sample style list is null");
		Validate.notNull(sampleStyleProvider.getAttributes(), "Borehole sample attribute list is null");

		Validate.notBlank(uniqueIdentifierAttribute, "Borehole unique identifier attribute not set");
		Validate.notBlank(sampleDepthFromAttribute, "Borehole sample depth-from attribute not set");
		Validate.notBlank(sampleDepthToAttribute, "Borehole sample depth-to attribute not set");

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

		markerRenderer.setOverrideMarkerElevation(true);
		markerRenderer.setElevation(0);

		WorldWindowRegistry.INSTANCE.addSelectListener(this);
	}

	@Override
	public Sector getSector()
	{
		return boreholeProvider.getSector();
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
			borehole = new BoreholeImpl(this, position, markerAttributes);
			idToBorehole.put(id, borehole);
			synchronized (boreholes)
			{
				boreholes.add(borehole);
				markers.add(borehole);
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
		for (BoreholeImpl borehole : boreholes)
		{
			borehole.loadComplete();
		}
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
			markerRenderer.render(dc, markers);
			annotationRenderer.render(dc, tooltipAnnotation, tooltipAnnotation.getAnnotationDrawPoint(dc), this);

			GL2 gl = dc.getGL().getGL2();
			try
			{
				gl.glPushAttrib(GL2.GL_LINE_BIT);
				gl.glLineWidth((float) lineWidth);

				for (BoreholeImpl borehole : boreholes)
				{
					borehole.render(dc);
				}
			}
			finally
			{
				gl.glPopAttrib();
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
		if (topPickedObject != null
				&& (topPickedObject.getObject() instanceof Borehole || topPickedObject.getObject() instanceof BoreholeSample))
		{
			highlight(topPickedObject.getObject(), true);

			if (e.getEventAction() == SelectEvent.LEFT_CLICK)
			{
				String link = null;
				if (topPickedObject.getObject() instanceof Borehole)
				{
					link = ((Borehole) topPickedObject.getObject()).getLink();
				}
				else if (topPickedObject.getObject() instanceof BoreholeSample)
				{
					link = ((BoreholeSample) topPickedObject.getObject()).getLink();
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
		else if (topPickedObject != null && topPickedObject.getObject() instanceof BoreholeSample)
		{

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
				position =
						Position.fromDegrees(sample.getBorehole().getPosition().getLatitude().degrees, sample
								.getBorehole().getPosition().getLongitude().degrees,
								-(sample.getDepthTo() + sample.getDepthFrom()) / 2d);
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
}
