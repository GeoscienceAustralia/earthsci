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
package au.gov.ga.earthsci.worldwind.common.layers.volume;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Plane;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import javax.media.opengl.GL2;

import org.gdal.osr.CoordinateTransformation;

import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;
import au.gov.ga.earthsci.worldwind.common.layers.Bounds;
import au.gov.ga.earthsci.worldwind.common.layers.Wireframeable;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;
import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShapeRenderListener;
import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;
import au.gov.ga.earthsci.worldwind.common.util.ColorMap;
import au.gov.ga.earthsci.worldwind.common.util.CoordinateTransformationUtil;
import au.gov.ga.earthsci.worldwind.common.util.GeometryUtil;
import au.gov.ga.earthsci.worldwind.common.util.Util;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

import com.jogamp.opengl.util.awt.TextureRenderer;

/**
 * Basic implementation of the {@link VolumeLayer} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class BasicVolumeLayer extends AbstractLayer implements VolumeLayer, Wireframeable, SelectListener,
		FastShapeRenderListener
{
	protected URL context;
	protected String url;
	protected String dataCacheName;
	protected VolumeDataProvider dataProvider;
	protected Double minimumDistance;
	protected double maxVariance = 0;
	protected CoordinateTransformation coordinateTransformation;
	protected String paintedVariable;
	protected ColorMap colorMap;
	protected Color noDataColor;
	protected boolean reverseNormals = false;
	protected boolean useOrderedRendering = false;

	protected final Object dataLock = new Object();
	protected boolean dataAvailable = false;
	protected FastShape topSurface, bottomSurface;
	protected TopBottomFastShape minXCurtain, maxXCurtain, minYCurtain, maxYCurtain;
	protected FastShape boundingBoxShape;
	protected TextureRenderer topTexture, bottomTexture, minXTexture, maxXTexture, minYTexture, maxYTexture;
	protected int topOffset = 0, bottomOffset = 0, minXOffset = 0, maxXOffset = 0, minYOffset = 0, maxYOffset = 0;
	protected int lastTopOffset = -1, lastBottomOffset = -1, lastMinXOffset = -1, lastMaxXOffset = -1,
			lastMinYOffset = -1, lastMaxYOffset = -1;
	protected double lastVerticalExaggeration = -Double.MAX_VALUE;

	protected final double[] curtainTextureMatrix = new double[] { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };

	protected boolean minXClipDirty = false, maxXClipDirty = false, minYClipDirty = false, maxYClipDirty = false,
			topClipDirty = false, bottomClipDirty = false;
	protected final double[] topClippingPlanes = new double[4 * 4];
	protected final double[] bottomClippingPlanes = new double[4 * 4];
	protected final double[] curtainClippingPlanes = new double[4 * 4];

	protected boolean wireframe = false;

	protected boolean dragging = false;
	protected Position dragStartPosition;
	protected int dragStartSlice;
	protected Vec4 dragStartCenter;

	/**
	 * Create a new {@link BasicVolumeLayer}, using the provided layer params.
	 * 
	 * @param params
	 */
	public BasicVolumeLayer(AVList params)
	{
		context = (URL) params.getValue(AVKeyMore.CONTEXT_URL);
		url = params.getStringValue(AVKey.URL);
		dataCacheName = params.getStringValue(AVKey.DATA_CACHE_NAME);
		dataProvider = (VolumeDataProvider) params.getValue(AVKeyMore.DATA_LAYER_PROVIDER);

		minimumDistance = (Double) params.getValue(AVKeyMore.MINIMUM_DISTANCE);
		colorMap = (ColorMap) params.getValue(AVKeyMore.COLOR_MAP);
		noDataColor = (Color) params.getValue(AVKeyMore.NO_DATA_COLOR);

		Double d = (Double) params.getValue(AVKeyMore.MAX_VARIANCE);
		if (d != null)
		{
			maxVariance = d;
		}

		String s = (String) params.getValue(AVKey.COORDINATE_SYSTEM);
		if (s != null)
		{
			coordinateTransformation = CoordinateTransformationUtil.getTransformationToWGS84(s);
		}

		s = (String) params.getValue(AVKeyMore.PAINTED_VARIABLE);
		if (s != null)
		{
			paintedVariable = s;
		}

		Integer i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MIN_U);
		if (i != null)
		{
			minXOffset = i;
		}
		i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MAX_U);
		if (i != null)
		{
			maxXOffset = i;
		}
		i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MIN_V);
		if (i != null)
		{
			minYOffset = i;
		}
		i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MAX_V);
		if (i != null)
		{
			maxYOffset = i;
		}
		i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MIN_W);
		if (i != null)
		{
			topOffset = i;
		}
		i = (Integer) params.getValue(AVKeyMore.INITIAL_OFFSET_MAX_W);
		if (i != null)
		{
			bottomOffset = i;
		}

		Boolean b = (Boolean) params.getValue(AVKeyMore.REVERSE_NORMALS);
		if (b != null)
		{
			reverseNormals = b;
		}
		b = (Boolean) params.getValue(AVKeyMore.ORDERED_RENDERING);
		if (b != null)
		{
			useOrderedRendering = b;
		}

		Validate.notBlank(url, "Model data url not set");
		Validate.notBlank(dataCacheName, "Model data cache name not set");
		Validate.notNull(dataProvider, "Model data provider is null");

		WorldWindowRegistry.INSTANCE.addSelectListener(this);
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
	public boolean isLoading()
	{
		return dataProvider.isLoading();
	}

	@Override
	public void addLoadingListener(LoadingListener listener)
	{
		dataProvider.addLoadingListener(listener);
	}

	@Override
	public void removeLoadingListener(LoadingListener listener)
	{
		dataProvider.removeLoadingListener(listener);
	}

	@Override
	public Bounds getBounds()
	{
		return dataProvider.getBounds();
	}

	@Override
	public boolean isFollowTerrain()
	{
		return false;
	}

	@Override
	public void dataAvailable(VolumeDataProvider provider)
	{
		calculateSurfaces();
		dataAvailable = true;
	}

	/**
	 * Calculate the 4 curtain and 2 horizontal surfaces used to render this
	 * volume. Should be called once after the {@link VolumeDataProvider}
	 * notifies this layer that the data is available.
	 */
	protected void calculateSurfaces()
	{
		double topElevation = 0;
		double bottomElevation = -dataProvider.getDepth();

		minXCurtain = dataProvider.createXCurtain(0);
		minXCurtain.addRenderListener(this);
		minXCurtain.setLighted(true);
		minXCurtain.setCalculateNormals(true);
		minXCurtain.setReverseNormals(reverseNormals);
		minXCurtain.setTopElevationOffset(topElevation);
		minXCurtain.setBottomElevationOffset(bottomElevation);
		minXCurtain.setTextureMatrix(curtainTextureMatrix);
		minXCurtain.setUseOrderedRendering(useOrderedRendering);

		maxXCurtain = dataProvider.createXCurtain(dataProvider.getXSize() - 1);
		maxXCurtain.addRenderListener(this);
		maxXCurtain.setLighted(true);
		maxXCurtain.setCalculateNormals(true);
		maxXCurtain.setReverseNormals(!reverseNormals);
		maxXCurtain.setTopElevationOffset(topElevation);
		maxXCurtain.setBottomElevationOffset(bottomElevation);
		maxXCurtain.setTextureMatrix(curtainTextureMatrix);
		maxXCurtain.setUseOrderedRendering(useOrderedRendering);

		minYCurtain = dataProvider.createYCurtain(0);
		minYCurtain.addRenderListener(this);
		minYCurtain.setLighted(true);
		minYCurtain.setCalculateNormals(true);
		minYCurtain.setReverseNormals(!reverseNormals);
		minYCurtain.setTopElevationOffset(topElevation);
		minYCurtain.setBottomElevationOffset(bottomElevation);
		minYCurtain.setTextureMatrix(curtainTextureMatrix);
		minYCurtain.setUseOrderedRendering(useOrderedRendering);

		maxYCurtain = dataProvider.createYCurtain(dataProvider.getYSize() - 1);
		maxYCurtain.addRenderListener(this);
		maxYCurtain.setLighted(true);
		maxYCurtain.setCalculateNormals(true);
		maxYCurtain.setReverseNormals(reverseNormals);
		maxYCurtain.setTopElevationOffset(topElevation);
		maxYCurtain.setBottomElevationOffset(bottomElevation);
		maxYCurtain.setTextureMatrix(curtainTextureMatrix);
		maxYCurtain.setUseOrderedRendering(useOrderedRendering);

		Rectangle rectangle = new Rectangle(0, 0, dataProvider.getXSize(), dataProvider.getYSize());
		topSurface = dataProvider.createHorizontalSurface((float) maxVariance, rectangle);
		topSurface.addRenderListener(this);
		topSurface.setLighted(true);
		topSurface.setCalculateNormals(true);
		topSurface.setReverseNormals(reverseNormals);
		topSurface.setElevation(topElevation);
		topSurface.setUseOrderedRendering(useOrderedRendering);

		bottomSurface = dataProvider.createHorizontalSurface((float) maxVariance, rectangle);
		bottomSurface.addRenderListener(this);
		bottomSurface.setLighted(true);
		bottomSurface.setCalculateNormals(true);
		bottomSurface.setReverseNormals(!reverseNormals);
		bottomSurface.setElevation(bottomElevation);
		bottomSurface.setUseOrderedRendering(useOrderedRendering);

		//update each shape's wireframe property so they match the layer's
		setWireframe(isWireframe());
	}

	/**
	 * Recalculate any surfaces that require recalculation. This includes
	 * regenerating textures when the user has dragged a surface to a different
	 * slice.
	 */
	protected void recalculateSurfaces()
	{
		if (!dataAvailable)
		{
			return;
		}

		int xSize = dataProvider.getXSize();
		int ySize = dataProvider.getYSize();
		int zSize = dataProvider.getZSize();

		//ensure the min/max offsets don't overlap one-another
		minXOffset = Util.clamp(minXOffset, 0, xSize - 1);
		maxXOffset = Util.clamp(maxXOffset, 0, xSize - 1 - minXOffset);
		minYOffset = Util.clamp(minYOffset, 0, ySize - 1);
		maxYOffset = Util.clamp(maxYOffset, 0, ySize - 1 - minYOffset);
		topOffset = Util.clamp(topOffset, 0, zSize - 1);
		bottomOffset = Util.clamp(bottomOffset, 0, zSize - 1 - topOffset);

		int maxXSlice = xSize - 1 - maxXOffset;
		int maxYSlice = ySize - 1 - maxYOffset;
		int bottomSlice = zSize - 1 - bottomOffset;

		//only recalculate those that have changed
		boolean recalculateMinX = lastMinXOffset != minXOffset;
		boolean recalculateMaxX = lastMaxXOffset != maxXOffset;
		boolean recalculateMinY = lastMinYOffset != minYOffset;
		boolean recalculateMaxY = lastMaxYOffset != maxYOffset;
		boolean recalculateTop = lastTopOffset != topOffset;
		boolean recalculateBottom = lastBottomOffset != bottomOffset;

		Dimension xTextureSize = new Dimension(ySize, zSize);
		Dimension yTextureSize = new Dimension(xSize, zSize);
		Dimension zTextureSize = new Dimension(xSize, ySize);
		double topPercent = dataProvider.getSliceElevationPercent(topOffset);
		double bottomPercent = dataProvider.getSliceElevationPercent(bottomSlice);

		if (recalculateMinX)
		{
			minXClipDirty = true;

			TopBottomFastShape newMinXCurtain = dataProvider.createXCurtain(minXOffset);
			minXCurtain.setPositions(newMinXCurtain.getPositions());

			updateTexture(generateTexture(0, minXOffset, xTextureSize), minXTexture, minXCurtain);
			lastMinXOffset = minXOffset;
		}
		if (recalculateMaxX)
		{
			maxXClipDirty = true;

			TopBottomFastShape newMaxXCurtain = dataProvider.createXCurtain(xSize - 1 - maxXOffset);
			maxXCurtain.setPositions(newMaxXCurtain.getPositions());

			updateTexture(generateTexture(0, maxXSlice, xTextureSize), maxXTexture, maxXCurtain);
			lastMaxXOffset = maxXOffset;
		}
		if (recalculateMinY)
		{
			minYClipDirty = true;

			TopBottomFastShape newMinYCurtain = dataProvider.createYCurtain(minYOffset);
			minYCurtain.setPositions(newMinYCurtain.getPositions());

			updateTexture(generateTexture(1, minYOffset, yTextureSize), minYTexture, minYCurtain);
			lastMinYOffset = minYOffset;
		}
		if (recalculateMaxY)
		{
			maxYClipDirty = true;

			TopBottomFastShape newMaxYCurtain = dataProvider.createYCurtain(ySize - 1 - maxYOffset);
			maxYCurtain.setPositions(newMaxYCurtain.getPositions());

			updateTexture(generateTexture(1, maxYSlice, yTextureSize), maxYTexture, maxYCurtain);
			lastMaxYOffset = maxYOffset;
		}
		if (recalculateTop)
		{
			topClipDirty = true;
			double elevation = -dataProvider.getDepth() * topPercent;

			updateTexture(generateTexture(2, topOffset, zTextureSize), topTexture, topSurface);
			lastTopOffset = topOffset;

			topSurface.setElevation(elevation);
			minXCurtain.setTopElevationOffset(elevation);
			maxXCurtain.setTopElevationOffset(elevation);
			minYCurtain.setTopElevationOffset(elevation);
			maxYCurtain.setTopElevationOffset(elevation);

			recalculateTextureMatrix(topPercent, bottomPercent);
		}
		if (recalculateBottom)
		{
			bottomClipDirty = true;
			double elevation = -dataProvider.getDepth() * bottomPercent;

			updateTexture(generateTexture(2, bottomSlice, zTextureSize), bottomTexture, bottomSurface);
			lastBottomOffset = bottomOffset;

			bottomSurface.setElevation(elevation);
			minXCurtain.setBottomElevationOffset(elevation);
			maxXCurtain.setBottomElevationOffset(elevation);
			minYCurtain.setBottomElevationOffset(elevation);
			maxYCurtain.setBottomElevationOffset(elevation);

			recalculateTextureMatrix(topPercent, bottomPercent);
		}
	}

	/**
	 * Recalculate the curtain texture matrix. When the top and bottom surface
	 * offsets aren't 0, the OpenGL texture matrix is used to offset the curtain
	 * textures.
	 * 
	 * @param topPercent
	 *            Location of the top surface (normalized to 0..1)
	 * @param bottomPercent
	 *            Location of the bottom surface (normalized to 0..1)
	 */
	protected void recalculateTextureMatrix(double topPercent, double bottomPercent)
	{
		Matrix m =
				Matrix.fromTranslation(0, topPercent, 0).multiply(Matrix.fromScale(1, bottomPercent - topPercent, 1));
		m.toArray(curtainTextureMatrix, 0, false);
	}

	/**
	 * Recalculate the clipping planes used to clip the surfaces when the user
	 * drags them.
	 * 
	 * @param dc
	 */
	protected void recalculateClippingPlanes(DrawContext dc)
	{
		if (!dataAvailable || dataProvider.isSingleSliceVolume())
		{
			return;
		}

		boolean verticalExaggerationChanged = lastVerticalExaggeration != dc.getVerticalExaggeration();
		lastVerticalExaggeration = dc.getVerticalExaggeration();

		boolean minX = minXClipDirty || verticalExaggerationChanged;
		boolean maxX = maxXClipDirty || verticalExaggerationChanged;
		boolean minY = minYClipDirty || verticalExaggerationChanged;
		boolean maxY = maxYClipDirty || verticalExaggerationChanged;

		boolean sw = minX || minY;
		boolean nw = minX || maxY;
		boolean se = maxX || minY;
		boolean ne = maxX || maxY;

		minX |= topClipDirty || bottomClipDirty;
		maxX |= topClipDirty || bottomClipDirty;
		minY |= topClipDirty || bottomClipDirty;
		maxY |= topClipDirty || bottomClipDirty;

		if (!(minX || maxX || minY || maxY))
		{
			return;
		}

		int maxXSlice = dataProvider.getXSize() - 1 - maxXOffset;
		int maxYSlice = dataProvider.getYSize() - 1 - maxYOffset;
		int bottomSlice = dataProvider.getZSize() - 1 - bottomOffset;

		double top = dataProvider.getTop();
		double depth = dataProvider.getDepth();

		double topPercent = dataProvider.getSliceElevationPercent(topOffset);
		double bottomPercent = dataProvider.getSliceElevationPercent(bottomSlice);
		double topElevation = top - topPercent * depth;
		double bottomElevation = top - bottomPercent * depth;

		Position swPosTop = dataProvider.getPosition(minXOffset, minYOffset);
		Position nwPosTop = dataProvider.getPosition(minXOffset, maxYSlice);
		Position sePosTop = dataProvider.getPosition(maxXSlice, minYOffset);
		Position nePosTop = dataProvider.getPosition(maxXSlice, maxYSlice);

		if (depth != 0 && dc.getVerticalExaggeration() > 0)
		{
			Position origin = dataProvider.getPosition(0, 0);
			Position originPlusOne = dataProvider.getPosition(1, 1);
			Angle azimuth = LatLon.linearAzimuth(originPlusOne, origin);
			double delta = 0.005;
			double sin = azimuth.sin() * delta;
			double cos = azimuth.cos() * delta;

			if (se)
			{
				Position sePosBottom = new Position(sePosTop, sePosTop.elevation - depth);
				Position otherPos = sePosTop.add(Position.fromDegrees(cos, sin, 0));
				insertClippingPlaneForPositions(dc, curtainClippingPlanes, 8, sePosTop, otherPos, sePosBottom);
			}
			if (ne)
			{
				Position nePosBottom = new Position(nePosTop, nePosTop.elevation - depth);
				Position otherPos = nePosTop.add(Position.fromDegrees(-sin, cos, 0));
				insertClippingPlaneForPositions(dc, curtainClippingPlanes, 12, nePosTop, nePosBottom, otherPos);
			}
			if (nw)
			{
				Position nwPosBottom = new Position(nwPosTop, nwPosTop.elevation - depth);
				Position otherPos = nwPosTop.add(Position.fromDegrees(-cos, -sin, 0));
				insertClippingPlaneForPositions(dc, curtainClippingPlanes, 4, nwPosTop, otherPos, nwPosBottom);
			}
			if (sw)
			{
				Position swPosBottom = new Position(swPosTop, swPosTop.elevation - depth);
				Position otherPos = swPosTop.add(Position.fromDegrees(sin, -cos, 0));
				insertClippingPlaneForPositions(dc, curtainClippingPlanes, 0, swPosTop, swPosBottom, otherPos);
			}
		}

		//the following only works for a spherical earth (as opposed to flat earth), as it relies on adjacent
		//points not being colinear (3 points along a latitude are not colinear when wrapped around a sphere)

		if (minX)
		{
			Position middlePos = dataProvider.getPosition(minXOffset, (maxYSlice + minYOffset) / 2);
			middlePos = midpointPositionIfEqual(middlePos, nwPosTop, swPosTop);
			insertClippingPlaneForLatLons(dc, topClippingPlanes, 0, middlePos, nwPosTop, swPosTop, topElevation);
			insertClippingPlaneForLatLons(dc, bottomClippingPlanes, 0, middlePos, nwPosTop, swPosTop, bottomElevation);
		}
		if (maxX)
		{
			Position middlePos = dataProvider.getPosition(maxXSlice, (maxYSlice + minYOffset) / 2);
			middlePos = midpointPositionIfEqual(middlePos, sePosTop, nePosTop);
			insertClippingPlaneForLatLons(dc, topClippingPlanes, 4, middlePos, sePosTop, nePosTop, topElevation);
			insertClippingPlaneForLatLons(dc, bottomClippingPlanes, 4, middlePos, sePosTop, nePosTop, bottomElevation);
		}
		if (minY)
		{
			Position middlePos = dataProvider.getPosition((maxXSlice + minXOffset) / 2, minYOffset);
			middlePos = midpointPositionIfEqual(middlePos, swPosTop, sePosTop);
			insertClippingPlaneForLatLons(dc, topClippingPlanes, 8, middlePos, swPosTop, sePosTop, topElevation);
			insertClippingPlaneForLatLons(dc, bottomClippingPlanes, 8, middlePos, swPosTop, sePosTop, bottomElevation);
		}
		if (maxY)
		{
			Position middlePos = dataProvider.getPosition((maxXSlice + minXOffset) / 2, maxYSlice);
			middlePos = midpointPositionIfEqual(middlePos, nePosTop, nwPosTop);
			insertClippingPlaneForLatLons(dc, topClippingPlanes, 12, middlePos, nePosTop, nwPosTop, topElevation);
			insertClippingPlaneForLatLons(dc, bottomClippingPlanes, 12, middlePos, nePosTop, nwPosTop, bottomElevation);
		}

		minXClipDirty = maxXClipDirty = minYClipDirty = maxYClipDirty = topClipDirty = bottomClipDirty = false;
	}

	/**
	 * Return the midpoint of the two end positions if the given middle position
	 * is equal to one of the ends.
	 * 
	 * @param middle
	 * @param end1
	 * @param end2
	 * @return Midpoint between end1 and end2 if middle equals end1 or end2.
	 */
	protected Position midpointPositionIfEqual(Position middle, Position end1, Position end2)
	{
		if (middle.equals(end1) || middle.equals(end2))
		{
			return Position.interpolate(0.5, end1, end2);
		}
		return middle;
	}

	/**
	 * Insert a clipping plane vector into the given array. The vector is
	 * calculated by finding a plane that intersects the three given positions.
	 * 
	 * @param dc
	 * @param clippingPlaneArray
	 *            Array to insert clipping plane vector into
	 * @param arrayOffset
	 *            Array start offset to begin inserting values at
	 * @param p1
	 *            First position that the plane must intersect
	 * @param p2
	 *            Second position that the plane must intersect
	 * @param p3
	 *            Third position that the plane must intersect
	 */
	protected void insertClippingPlaneForPositions(DrawContext dc, double[] clippingPlaneArray, int arrayOffset,
			Position p1, Position p2, Position p3)
	{
		Globe globe = dc.getGlobe();
		Vec4 v1 = globe.computePointFromPosition(p1, p1.getElevation() * dc.getVerticalExaggeration());
		Vec4 v2 = globe.computePointFromPosition(p2, p2.getElevation() * dc.getVerticalExaggeration());
		Vec4 v3 = globe.computePointFromPosition(p3, p3.getElevation() * dc.getVerticalExaggeration());
		insertClippingPlaneForPoints(clippingPlaneArray, arrayOffset, v1, v2, v3);
	}

	/**
	 * Insert a clipping plane vector into the given array. The vector is
	 * calculated by finding a plane that intersects the three given latlons at
	 * the given elevation.
	 * 
	 * @param dc
	 * @param clippingPlaneArray
	 *            Array to insert clipping plane vector into
	 * @param arrayOffset
	 *            Array start offset to begin inserting values at
	 * @param l1
	 *            First latlon that the plane must intersect
	 * @param l2
	 *            Second latlon that the plane must intersect
	 * @param l3
	 *            Third latlon that the plane must intersect
	 * @param elevation
	 *            Elevation of the latlons
	 */
	protected void insertClippingPlaneForLatLons(DrawContext dc, double[] clippingPlaneArray, int arrayOffset,
			LatLon l1, LatLon l2, LatLon l3, double elevation)
	{
		Globe globe = dc.getGlobe();
		double exaggeratedElevation = elevation * dc.getVerticalExaggeration();
		Vec4 v1 = globe.computePointFromPosition(l1, exaggeratedElevation);
		Vec4 v2 = globe.computePointFromPosition(l2, exaggeratedElevation);
		Vec4 v3 = globe.computePointFromPosition(l3, exaggeratedElevation);
		insertClippingPlaneForPoints(clippingPlaneArray, arrayOffset, v1, v2, v3);
	}

	/**
	 * Insert a clipping plane vector into the given array. The vector is
	 * calculated by finding a plane that intersects the three given points.
	 * 
	 * @param clippingPlaneArray
	 *            Array to insert clipping plane vector into
	 * @param arrayOffset
	 *            Array start offset to begin inserting values at
	 * @param v1
	 *            First point that the plane must intersect
	 * @param v2
	 *            Second point that the plane must intersect
	 * @param v3
	 *            Third point that the plane must intersect
	 */
	protected void insertClippingPlaneForPoints(double[] clippingPlaneArray, int arrayOffset, Vec4 v1, Vec4 v2, Vec4 v3)
	{
		if (v1 == null || v2 == null || v3 == null || v1.equals(v2) || v1.equals(v3))
		{
			return;
		}

		Line l1 = Line.fromSegment(v1, v3);
		Line l2 = Line.fromSegment(v1, v2);
		Plane plane = GeometryUtil.createPlaneContainingLines(l1, l2);
		Vec4 v = plane.getVector();
		clippingPlaneArray[arrayOffset + 0] = v.x;
		clippingPlaneArray[arrayOffset + 1] = v.y;
		clippingPlaneArray[arrayOffset + 2] = v.z;
		clippingPlaneArray[arrayOffset + 3] = v.w;
	}

	/**
	 * Generate a texture slice through the volume at the given position. Uses a
	 * {@link ColorMap} to map values to colors (or simply interpolates the hue
	 * if no colormap is provided - assumes values between 0 and 1).
	 * 
	 * @param axis
	 *            Slicing axis (0 for a longitude slice, 1 for a latitude slice,
	 *            2 for an elevation slice).
	 * @param position
	 *            Longitude, latitude, or elevation at which to slice.
	 * @param size
	 *            Size of the texture to generate.
	 * @return A {@link BufferedImage} containing a representation of the volume
	 *         slice.
	 */
	protected BufferedImage generateTexture(int axis, int position, Dimension size)
	{
		int zSubsamples = dataProvider.getZSubsamples();
		boolean subsample = axis != 2 && zSubsamples > 1;
		int height = size.height;
		if (subsample)
		{
			height *= zSubsamples;
		}

		BufferedImage image = new BufferedImage(size.width, height, BufferedImage.TYPE_INT_ARGB);
		float minimum = dataProvider.getMinValue();
		float maximum = dataProvider.getMaxValue();
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < size.width; x++)
			{
				int vx = axis == 2 ? x : axis == 1 ? x : position;
				int vy = axis == 2 ? y : axis == 1 ? position : x;
				int vz = axis == 2 ? position : y;
				float value;
				if (subsample)
				{
					double percent = y / (double) (height - 1);
					double z = dataProvider.getElevationPercentSlice(percent);
					int z1 = (int) Math.floor(z);
					int z2 = (int) Math.ceil(z);
					float value1 = dataProvider.getValue(vx, vy, z1);
					float value2 = dataProvider.getValue(vx, vy, z2);
					float zp = (float) (z % 1.0);
					value = value1 * (1f - zp) + value2 * zp;
				}
				else
				{
					value = dataProvider.getValue(vx, vy, vz);
				}
				int rgb = noDataColor != null ? noDataColor.getRGB() : 0;
				if (value != dataProvider.getNoDataValue())
				{
					if (colorMap != null)
					{
						rgb = colorMap.calculateColorNotingIsValuesPercentages(value, minimum, maximum).getRGB();
					}
					else
					{
						rgb = Color.HSBtoRGB(-0.3f - value * 0.7f, 1.0f, 1.0f);
					}
				}
				image.setRGB(x, y, rgb);
			}
		}
		return image;
	}

	/**
	 * Update the given {@link TextureRenderer} with the provided image, and
	 * sets the {@link FastShape}'s texture it.
	 * 
	 * @param image
	 *            Image to update texture with
	 * @param texture
	 *            Texture to update
	 * @param shape
	 *            Shape to set texture in
	 */
	protected void updateTexture(BufferedImage image, TextureRenderer texture, FastShape shape)
	{
		Graphics2D g = null;
		try
		{
			g = (Graphics2D) texture.getImage().getGraphics();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
			g.drawImage(image, 0, 0, null);
		}
		finally
		{
			if (g != null)
			{
				g.dispose();
			}
		}
		texture.markDirty(0, 0, texture.getWidth(), texture.getHeight());
		shape.setTexture(texture.getTexture());
	}

	@Override
	public CoordinateTransformation getCoordinateTransformation()
	{
		return coordinateTransformation;
	}

	@Override
	public String getPaintedVariableName()
	{
		return paintedVariable;
	}

	@Override
	protected void doPick(DrawContext dc, Point point)
	{
		if (!dataProvider.isSingleSliceVolume())
		{
			doRender(dc);
		}
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		if (!isEnabled())
		{
			return;
		}

		dataProvider.requestData(this);

		synchronized (dataLock)
		{
			if (!dataAvailable)
			{
				return;
			}

			if (topTexture == null)
			{
				topTexture = new TextureRenderer(dataProvider.getXSize(), dataProvider.getYSize(), true, true);
				bottomTexture = new TextureRenderer(dataProvider.getXSize(), dataProvider.getYSize(), true, true);
				minXTexture = new TextureRenderer(dataProvider.getYSize(),
						dataProvider.getZSize() * dataProvider.getZSubsamples(), true, true);
				maxXTexture = new TextureRenderer(dataProvider.getYSize(),
						dataProvider.getZSize() * dataProvider.getZSubsamples(), true, true);
				minYTexture = new TextureRenderer(dataProvider.getXSize(),
						dataProvider.getZSize() * dataProvider.getZSubsamples(), true, true);
				maxYTexture = new TextureRenderer(dataProvider.getXSize(),
						dataProvider.getZSize() * dataProvider.getZSubsamples(), true, true);
			}

			//recalculate surfaces and clipping planes each frame (in case user drags one of the surfaces)
			recalculateSurfaces();
			recalculateClippingPlanes(dc);

			//when only one slice is shown in any given direction, only one of the curtains needs to be rendered
			boolean singleXSlice = dataProvider.getXSize() - minXOffset - maxXOffset <= 1;
			boolean singleYSlice = dataProvider.getYSize() - minYOffset - maxYOffset <= 1;
			boolean singleZSlice = dataProvider.getZSize() - topOffset - bottomOffset <= 1;
			boolean anySingleSlice = singleXSlice || singleYSlice || singleZSlice;
			FastShape[] shapes =
					anySingleSlice ? new FastShape[] { singleXSlice ? maxXCurtain : singleYSlice ? minYCurtain
							: topSurface } : new FastShape[] { topSurface, bottomSurface, minXCurtain, maxXCurtain,
							minYCurtain, maxYCurtain };

			//sort the shapes from back-to-front
			if (!anySingleSlice)
			{
				Arrays.sort(shapes, new ShapeComparator(dc));
			}

			//test all the shapes with the minimum distance, culling them if they are outside
			if (minimumDistance != null)
			{
				for (int i = 0; i < shapes.length; i++)
				{
					Extent extent = shapes[i].getExtent();
					if (extent != null)
					{
						double distanceToEye =
								extent.getCenter().distanceTo3(dc.getView().getEyePoint()) - extent.getRadius();
						if (distanceToEye > minimumDistance)
						{
							shapes[i] = null;
						}
					}
				}
			}

			//draw each shape
			for (FastShape shape : shapes)
			{
				if (shape != null)
				{
					shape.setTwoSidedLighting(anySingleSlice);
					if (dc.isPickingMode())
					{
						shape.pick(dc, dc.getPickPoint());
					}
					else
					{
						shape.render(dc);
					}
				}
			}

			if (dragging)
			{
				//render a bounding box around the data if the user is dragging a surface
				renderBoundingBox(dc);
			}
		}
	}

	@Override
	public void shapePreRender(DrawContext dc, FastShape shape)
	{
		//push the OpenGL clipping plane state on the attribute stack
		dc.getGL().getGL2().glPushAttrib(GL2.GL_TRANSFORM_BIT);
		setupClippingPlanes(dc, shape == topSurface, shape == bottomSurface);
	}

	@Override
	public void shapePostRender(DrawContext dc, FastShape shape)
	{
		dc.getGL().getGL2().glPopAttrib();
	}

	protected void setupClippingPlanes(DrawContext dc, boolean top, boolean bottom)
	{
		boolean minX = minXOffset > 0;
		boolean maxX = maxXOffset > 0;
		boolean minY = minYOffset > 0;
		boolean maxY = maxYOffset > 0;

		boolean[] enabled;
		double[] array;

		GL2 gl = dc.getGL().getGL2();
		if (top || bottom)
		{
			array = top ? topClippingPlanes : bottomClippingPlanes;
			enabled = new boolean[] { minX, maxX, minY, maxY };
		}
		else
		{
			array = curtainClippingPlanes;
			boolean sw = minX || minY;
			boolean nw = minX || maxY;
			boolean se = maxX || minY;
			boolean ne = maxX || maxY;
			enabled = new boolean[] { sw, nw, se, ne };
		}

		for (int i = 0; i < 4; i++)
		{
			gl.glClipPlane(GL2.GL_CLIP_PLANE0 + i, array, i * 4);
			if (enabled[i])
			{
				gl.glEnable(GL2.GL_CLIP_PLANE0 + i);
			}
			else
			{
				gl.glDisable(GL2.GL_CLIP_PLANE0 + i);
			}
		}
	}

	/**
	 * Render a bounding box around the data. Used when dragging surfaces, so
	 * user has an idea of where the data extents lie when slicing.
	 * 
	 * @param dc
	 */
	protected void renderBoundingBox(DrawContext dc)
	{
		if (boundingBoxShape == null)
		{
			boundingBoxShape = dataProvider.createBoundingBox();
		}
		boundingBoxShape.render(dc);
	}

	@Override
	public boolean isWireframe()
	{
		return wireframe;
	}

	@Override
	public void setWireframe(boolean wireframe)
	{
		this.wireframe = wireframe;
		synchronized (dataLock)
		{
			if (topSurface != null)
			{
				topSurface.setWireframe(wireframe);
				bottomSurface.setWireframe(wireframe);
				minXCurtain.setWireframe(wireframe);
				maxXCurtain.setWireframe(wireframe);
				minYCurtain.setWireframe(wireframe);
				maxYCurtain.setWireframe(wireframe);
			}
		}
	}

	@Override
	public void selected(SelectEvent event)
	{
		//ignore this event if ctrl, alt, or shift are down
		if (event.getMouseEvent() != null)
		{
			int onmask = MouseEvent.SHIFT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK | MouseEvent.ALT_DOWN_MASK;
			if ((event.getMouseEvent().getModifiersEx() & onmask) != 0)
			{
				return;
			}
		}

		//don't allow dragging if there's only one layer in any one direction
		if (dataProvider.isSingleSliceVolume())
		{
			return;
		}

		//we only care about drag events
		boolean drag = event.getEventAction().equals(SelectEvent.DRAG);
		boolean dragEnd = event.getEventAction().equals(SelectEvent.DRAG_END);
		if (!(drag || dragEnd))
		{
			return;
		}

		Object topObject = event.getTopObject();
		FastShape pickedShape = topObject instanceof FastShape ? (FastShape) topObject : null;
		if (pickedShape == null)
		{
			return;
		}

		boolean top = pickedShape == topSurface;
		boolean bottom = pickedShape == bottomSurface;
		boolean minX = pickedShape == minXCurtain;
		boolean maxX = pickedShape == maxXCurtain;
		boolean minY = pickedShape == minYCurtain;
		boolean maxY = pickedShape == maxYCurtain;
		if (top || bottom || minX || maxX || minY || maxY)
		{
			if (dragEnd)
			{
				dragging = false;
				event.consume();
			}
			else if (drag)
			{
				if (!dragging || dragStartCenter == null)
				{
					Extent extent = pickedShape.getExtent();
					if (extent != null)
					{
						dragStartCenter = extent.getCenter();
					}
				}

				if (dragStartCenter != null)
				{
					WorldWindow wwd = WorldWindowRegistry.INSTANCE.getRendering();
					if (wwd != null)
					{
						View view = wwd.getView();

						if (top || bottom)
						{
							dragElevation(event.getPickPoint(), pickedShape, view);
						}
						else if (minX || maxX)
						{
							dragX(event.getPickPoint(), pickedShape, view);
						}
						else
						{
							dragY(event.getPickPoint(), pickedShape, view);
						}
					}
				}
				dragging = true;
				event.consume();
			}
		}
	}

	/**
	 * Drag an elevation surface up and down.
	 * 
	 * @param pickPoint
	 *            Point at which the user is dragging the mouse.
	 * @param shape
	 *            Shape to drag
	 */
	protected void dragElevation(Point pickPoint, FastShape shape, View view)
	{
		// Calculate the plane projected from screen y=pickPoint.y
		Line screenLeftRay = view.computeRayFromScreenPoint(pickPoint.x - 100, pickPoint.y);
		Line screenRightRay = view.computeRayFromScreenPoint(pickPoint.x + 100, pickPoint.y);

		// As the two lines are very close to parallel, use an arbitrary line joining them rather than the two lines to avoid precision problems
		Line joiner = Line.fromSegment(screenLeftRay.getPointAt(500), screenRightRay.getPointAt(500));
		Plane screenPlane = GeometryUtil.createPlaneContainingLines(screenLeftRay, joiner);
		if (screenPlane == null)
		{
			return;
		}

		// Calculate the origin-marker ray
		Globe globe = view.getGlobe();
		Line centreRay = Line.fromSegment(globe.getCenter(), dragStartCenter);
		Vec4 intersection = screenPlane.intersect(centreRay);
		if (intersection == null)
		{
			return;
		}

		Position intersectionPosition = globe.computePositionFromPoint(intersection);
		if (!dragging)
		{
			dragStartPosition = intersectionPosition;
			dragStartSlice = shape == topSurface ? topOffset : bottomOffset;
		}
		else
		{
			double deltaElevation =
					(dragStartPosition.elevation - intersectionPosition.elevation)
							/ (lastVerticalExaggeration == 0 ? 1 : lastVerticalExaggeration);
			double deltaPercentage = deltaElevation / dataProvider.getDepth();
			int sliceMovement = (int) (deltaPercentage * (dataProvider.getZSize() - 1));
			if (shape == topSurface)
			{
				topOffset = Util.clamp(dragStartSlice + sliceMovement, 0, dataProvider.getZSize() - 1);
				bottomOffset = Util.clamp(bottomOffset, 0, dataProvider.getZSize() - 1 - topOffset);
			}
			else
			{
				bottomOffset = Util.clamp(dragStartSlice - sliceMovement, 0, dataProvider.getZSize() - 1);
				topOffset = Util.clamp(topOffset, 0, dataProvider.getZSize() - 1 - bottomOffset);
			}
		}
	}

	/**
	 * Drag a X curtain left and right.
	 * 
	 * @param pickPoint
	 *            Point at which the user is dragging the mouse.
	 * @param shape
	 *            Shape to drag
	 */
	protected void dragX(Point pickPoint, FastShape shape, View view)
	{
		Globe globe = view.getGlobe();
		double centerElevation = globe.computePositionFromPoint(dragStartCenter).elevation;

		// Compute the ray from the screen point
		Line ray = view.computeRayFromScreenPoint(pickPoint.x, pickPoint.y);
		Intersection[] intersections = globe.intersect(ray, centerElevation);
		if (intersections == null || intersections.length == 0)
		{
			return;
		}
		Vec4 intersection = ray.nearestIntersectionPoint(intersections);
		if (intersection == null)
		{
			return;
		}

		Position position = globe.computePositionFromPoint(intersection);
		if (!dragging)
		{
			dragStartPosition = position;
			dragStartSlice = shape == minXCurtain ? minXOffset : maxXOffset;
		}
		else
		{
			Position p0 = dataProvider.getPosition(0, dataProvider.getYSize() / 2);
			Position p1 = dataProvider.getPosition(dataProvider.getXSize() - 1, dataProvider.getYSize() / 2);
			Angle volumeAzimuth = LatLon.linearAzimuth(p0, p1);
			Angle volumeDistance = LatLon.linearDistance(p0, p1);
			Angle movementAzimuth = LatLon.linearAzimuth(position, dragStartPosition);
			Angle movementDistance = LatLon.linearDistance(position, dragStartPosition);
			Angle deltaAngle = volumeAzimuth.subtract(movementAzimuth);
			double delta = movementDistance.degrees * deltaAngle.cos();
			double deltaPercentage = delta / volumeDistance.degrees;
			int sliceMovement = (int) (-deltaPercentage * (dataProvider.getXSize() - 1));
			if (shape == minXCurtain)
			{
				minXOffset = Util.clamp(dragStartSlice + sliceMovement, 0, dataProvider.getXSize() - 1);
				maxXOffset = Util.clamp(maxXOffset, 0, dataProvider.getXSize() - 1 - minXOffset);
			}
			else
			{
				maxXOffset = Util.clamp(dragStartSlice - sliceMovement, 0, dataProvider.getXSize() - 1);
				minXOffset = Util.clamp(minXOffset, 0, dataProvider.getXSize() - 1 - maxXOffset);
			}
		}
	}

	/**
	 * Drag a Y curtain left and right.
	 * 
	 * @param pickPoint
	 *            Point at which the user is dragging the mouse.
	 * @param shape
	 *            Shape to drag
	 */
	protected void dragY(Point pickPoint, FastShape shape, View view)
	{
		Globe globe = view.getGlobe();
		double centerElevation = globe.computePositionFromPoint(dragStartCenter).elevation;

		// Compute the ray from the screen point
		Line ray = view.computeRayFromScreenPoint(pickPoint.x, pickPoint.y);
		Intersection[] intersections = globe.intersect(ray, centerElevation);
		if (intersections == null || intersections.length == 0)
		{
			return;
		}
		Vec4 intersection = ray.nearestIntersectionPoint(intersections);
		if (intersection == null)
		{
			return;
		}

		Position position = globe.computePositionFromPoint(intersection);
		if (!dragging)
		{
			dragStartPosition = position;
			dragStartSlice = shape == minYCurtain ? minYOffset : maxYOffset;
		}
		else
		{
			Position p0 = dataProvider.getPosition(dataProvider.getXSize() / 2, 0);
			Position p1 = dataProvider.getPosition(dataProvider.getXSize() / 2, dataProvider.getYSize() - 1);
			Angle volumeAzimuth = LatLon.linearAzimuth(p0, p1);
			Angle volumeDistance = LatLon.linearDistance(p0, p1);
			Angle movementAzimuth = LatLon.linearAzimuth(position, dragStartPosition);
			Angle movementDistance = LatLon.linearDistance(position, dragStartPosition);
			Angle deltaAngle = volumeAzimuth.subtract(movementAzimuth);
			double delta = movementDistance.degrees * deltaAngle.cos();
			double deltaPercentage = delta / volumeDistance.degrees;
			int sliceMovement = (int) (-deltaPercentage * (dataProvider.getYSize() - 1));
			if (shape == minYCurtain)
			{
				minYOffset = Util.clamp(dragStartSlice + sliceMovement, 0, dataProvider.getYSize() - 1);
				maxYOffset = Util.clamp(maxYOffset, 0, dataProvider.getYSize() - 1 - minYOffset);
			}
			else
			{
				maxYOffset = Util.clamp(dragStartSlice - sliceMovement, 0, dataProvider.getYSize() - 1);
				minYOffset = Util.clamp(minYOffset, 0, dataProvider.getYSize() - 1 - maxYOffset);
			}
		}
	}

	/**
	 * {@link Comparator} used to sort {@link FastShape}s from back-to-front
	 * (from the view eye point).
	 */
	protected class ShapeComparator implements Comparator<FastShape>
	{
		private final DrawContext dc;

		public ShapeComparator(DrawContext dc)
		{
			this.dc = dc;
		}

		@Override
		public int compare(FastShape o1, FastShape o2)
		{
			if (o1 == o2)
			{
				return 0;
			}
			if (o2 == null)
			{
				return -1;
			}
			if (o1 == null)
			{
				return 1;
			}

			Extent e1 = o1.getExtent();
			Extent e2 = o2.getExtent();
			if (e2 == null)
			{
				return -1;
			}
			if (e1 == null)
			{
				return 1;
			}

			Vec4 eyePoint = dc.getView().getEyePoint();
			double d1 = e1.getCenter().distanceToSquared3(eyePoint);
			double d2 = e2.getCenter().distanceToSquared3(eyePoint);
			return -Double.compare(d1, d2);
		}
	}
}
