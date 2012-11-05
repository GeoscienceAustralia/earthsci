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
package au.gov.ga.earthsci.worldwind.common.render;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.AnnotationRenderer;
import gov.nasa.worldwind.render.DeclutteringTextRenderer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GLRuntimeCapabilities;
import gov.nasa.worldwind.render.LightingModel;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.OutlinedShape;
import gov.nasa.worldwind.render.ScreenCredit;
import gov.nasa.worldwind.render.SurfaceTileRenderer;
import gov.nasa.worldwind.render.TextRendererCache;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.PerformanceStatistic;
import gov.nasa.worldwind.util.PickPointFrustumList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.texture.TextureCoords;

/**
 * {@link DrawContext} implementation that simply delegates all of it's
 * functionality to a local DrawContext instance.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DrawContextDelegate implements DrawContext
{
	private final DrawContext delegate;

	public DrawContextDelegate(DrawContext delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public void dispose()
	{
		delegate.dispose();
	}

	@Override
	public void onMessage(Message msg)
	{
		delegate.onMessage(msg);
	}

	@Override
	public Object setValue(String key, Object value)
	{
		return delegate.setValue(key, value);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		delegate.propertyChange(evt);
	}

	@Override
	public void setGLContext(GLContext glContext)
	{
		delegate.setGLContext(glContext);
	}

	@Override
	public AVList setValues(AVList avList)
	{
		return delegate.setValues(avList);
	}

	@Override
	public GLContext getGLContext()
	{
		return delegate.getGLContext();
	}

	@Override
	public Object getValue(String key)
	{
		return delegate.getValue(key);
	}

	@Override
	public GL2 getGL()
	{
		return delegate.getGL();
	}

	@Override
	public Collection<Object> getValues()
	{
		return delegate.getValues();
	}

	@Override
	public GLU getGLU()
	{
		return delegate.getGLU();
	}

	@Override
	public String getStringValue(String key)
	{
		return delegate.getStringValue(key);
	}

	@Override
	public GLDrawable getGLDrawable()
	{
		return delegate.getGLDrawable();
	}

	@Override
	public Set<Entry<String, Object>> getEntries()
	{
		return delegate.getEntries();
	}

	@Override
	public boolean hasKey(String key)
	{
		return delegate.hasKey(key);
	}

	@Override
	public int getDrawableWidth()
	{
		return delegate.getDrawableWidth();
	}

	@Override
	public int getDrawableHeight()
	{
		return delegate.getDrawableHeight();
	}

	@Override
	public Object removeKey(String key)
	{
		return delegate.removeKey(key);
	}

	@Override
	public GLRuntimeCapabilities getGLRuntimeCapabilities()
	{
		return delegate.getGLRuntimeCapabilities();
	}

	@Override
	public void setGLRuntimeCapabilities(GLRuntimeCapabilities capabilities)
	{
		delegate.setGLRuntimeCapabilities(capabilities);
	}

	@Override
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		delegate.addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public void initialize(GLContext glContext)
	{
		delegate.initialize(glContext);
	}

	@Override
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		delegate.removePropertyChangeListener(propertyName, listener);
	}

	@Override
	public void setView(View view)
	{
		delegate.setView(view);
	}

	@Override
	public View getView()
	{
		return delegate.getView();
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		delegate.addPropertyChangeListener(listener);
	}

	@Override
	public void setModel(Model model)
	{
		delegate.setModel(model);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		delegate.removePropertyChangeListener(listener);
	}

	@Override
	public Model getModel()
	{
		return delegate.getModel();
	}

	@Override
	public Globe getGlobe()
	{
		return delegate.getGlobe();
	}

	@Override
	public void firePropertyChange(String propertyName, Object oldValue, Object newValue)
	{
		delegate.firePropertyChange(propertyName, oldValue, newValue);
	}

	@Override
	public LayerList getLayers()
	{
		return delegate.getLayers();
	}

	@Override
	public Sector getVisibleSector()
	{
		return delegate.getVisibleSector();
	}

	@Override
	public void firePropertyChange(PropertyChangeEvent propertyChangeEvent)
	{
		delegate.firePropertyChange(propertyChangeEvent);
	}

	@Override
	public void setVisibleSector(Sector s)
	{
		delegate.setVisibleSector(s);
	}

	@Override
	public AVList copy()
	{
		return delegate.copy();
	}

	@Override
	public void setVerticalExaggeration(double verticalExaggeration)
	{
		delegate.setVerticalExaggeration(verticalExaggeration);
	}

	@Override
	public AVList clearList()
	{
		return delegate.clearList();
	}

	@Override
	public double getVerticalExaggeration()
	{
		return delegate.getVerticalExaggeration();
	}

	@Override
	public SectorGeometryList getSurfaceGeometry()
	{
		return delegate.getSurfaceGeometry();
	}

	@Override
	public PickedObjectList getPickedObjects()
	{
		return delegate.getPickedObjects();
	}

	@Override
	public void addPickedObjects(PickedObjectList pickedObjects)
	{
		delegate.addPickedObjects(pickedObjects);
	}

	@Override
	public void addPickedObject(PickedObject pickedObject)
	{
		delegate.addPickedObject(pickedObject);
	}

	@Override
	public PickedObjectList getObjectsInPickRectangle()
	{
		return delegate.getObjectsInPickRectangle();
	}

	@Override
	public void addObjectInPickRectangle(PickedObject pickedObject)
	{
		delegate.addObjectInPickRectangle(pickedObject);
	}

	@Override
	public Color getUniquePickColor()
	{
		return delegate.getUniquePickColor();
	}

	@Override
	public Color getClearColor()
	{
		return delegate.getClearColor();
	}

	@Override
	public int getPickColorAtPoint(Point point)
	{
		return delegate.getPickColorAtPoint(point);
	}

	@Override
	public int[] getPickColorsInRectangle(Rectangle rectangle, int[] minAndMaxColorCodes)
	{
		return delegate.getPickColorsInRectangle(rectangle, minAndMaxColorCodes);
	}

	@Override
	public void enablePickingMode()
	{
		delegate.enablePickingMode();
	}

	@Override
	public boolean isPickingMode()
	{
		return delegate.isPickingMode();
	}

	@Override
	public void disablePickingMode()
	{
		delegate.disablePickingMode();
	}

	@Override
	public void setDeepPickingEnabled(boolean tf)
	{
		delegate.setDeepPickingEnabled(tf);
	}

	@Override
	public boolean isDeepPickingEnabled()
	{
		return delegate.isDeepPickingEnabled();
	}

	@Override
	public void addOrderedRenderable(OrderedRenderable orderedRenderable)
	{
		delegate.addOrderedRenderable(orderedRenderable);
	}

	@Override
	public void addOrderedRenderable(OrderedRenderable orderedRenderable, boolean isBehind)
	{
		delegate.addOrderedRenderable(orderedRenderable, isBehind);
	}

	@Override
	public void addOrderedSurfaceRenderable(OrderedRenderable orderedRenderable)
	{
		delegate.addOrderedSurfaceRenderable(orderedRenderable);
	}

	@Override
	public Queue<OrderedRenderable> getOrderedSurfaceRenderables()
	{
		return delegate.getOrderedSurfaceRenderables();
	}

	@Override
	public void drawUnitQuad()
	{
		delegate.drawUnitQuad();
	}

	@Override
	public void drawUnitQuad(TextureCoords texCoords)
	{
		delegate.drawUnitQuad(texCoords);
	}

	@Override
	public void drawUnitQuadOutline()
	{
		delegate.drawUnitQuadOutline();
	}

	@Override
	public void setSurfaceGeometry(SectorGeometryList surfaceGeometry)
	{
		delegate.setSurfaceGeometry(surfaceGeometry);
	}

	@Override
	public Vec4 getPointOnTerrain(Angle latitude, Angle longitude)
	{
		return delegate.getPointOnTerrain(latitude, longitude);
	}

	@Override
	public SurfaceTileRenderer getGeographicSurfaceTileRenderer()
	{
		return delegate.getGeographicSurfaceTileRenderer();
	}

	@Override
	public Point getPickPoint()
	{
		return delegate.getPickPoint();
	}

	@Override
	public void setPickPoint(Point pickPoint)
	{
		delegate.setPickPoint(pickPoint);
	}

	@Override
	public Rectangle getPickRectangle()
	{
		return delegate.getPickRectangle();
	}

	@Override
	public void setPickRectangle(Rectangle pickRect)
	{
		delegate.setPickRectangle(pickRect);
	}

	@Override
	public GpuResourceCache getTextureCache()
	{
		return delegate.getTextureCache();
	}

	@Override
	public GpuResourceCache getGpuResourceCache()
	{
		return delegate.getGpuResourceCache();
	}

	@Override
	public void setGpuResourceCache(GpuResourceCache gpuResourceCache)
	{
		delegate.setGpuResourceCache(gpuResourceCache);
	}

	@Override
	public Collection<PerformanceStatistic> getPerFrameStatistics()
	{
		return delegate.getPerFrameStatistics();
	}

	@Override
	public void setPerFrameStatisticsKeys(Set<String> statKeys, Collection<PerformanceStatistic> stats)
	{
		delegate.setPerFrameStatisticsKeys(statKeys, stats);
	}

	@Override
	public void setPerFrameStatistic(String key, String displayName, Object statistic)
	{
		delegate.setPerFrameStatistic(key, displayName, statistic);
	}

	@Override
	public void setPerFrameStatistics(Collection<PerformanceStatistic> stats)
	{
		delegate.setPerFrameStatistics(stats);
	}

	@Override
	public Set<String> getPerFrameStatisticsKeys()
	{
		return delegate.getPerFrameStatisticsKeys();
	}

	@Override
	public Point getViewportCenterScreenPoint()
	{
		return delegate.getViewportCenterScreenPoint();
	}

	@Override
	public void setViewportCenterScreenPoint(Point viewportCenterPoint)
	{
		delegate.setViewportCenterScreenPoint(viewportCenterPoint);
	}

	@Override
	public Position getViewportCenterPosition()
	{
		return delegate.getViewportCenterPosition();
	}

	@Override
	public void setViewportCenterPosition(Position viewportCenterPosition)
	{
		delegate.setViewportCenterPosition(viewportCenterPosition);
	}

	@Override
	public TextRendererCache getTextRendererCache()
	{
		return delegate.getTextRendererCache();
	}

	@Override
	public void setTextRendererCache(TextRendererCache textRendererCache)
	{
		delegate.setTextRendererCache(textRendererCache);
	}

	@Override
	public AnnotationRenderer getAnnotationRenderer()
	{
		return delegate.getAnnotationRenderer();
	}

	@Override
	public void setAnnotationRenderer(AnnotationRenderer annotationRenderer)
	{
		delegate.setAnnotationRenderer(annotationRenderer);
	}

	@Override
	public long getFrameTimeStamp()
	{
		return delegate.getFrameTimeStamp();
	}

	@Override
	public void setFrameTimeStamp(long frameTimeStamp)
	{
		delegate.setFrameTimeStamp(frameTimeStamp);
	}

	@Override
	public List<Sector> getVisibleSectors(double[] resolutions, long timeLimit, Sector searchSector)
	{
		return delegate.getVisibleSectors(resolutions, timeLimit, searchSector);
	}

	@Override
	public void setCurrentLayer(Layer layer)
	{
		delegate.setCurrentLayer(layer);
	}

	@Override
	public Layer getCurrentLayer()
	{
		return delegate.getCurrentLayer();
	}

	@Override
	public void addScreenCredit(ScreenCredit credit)
	{
		delegate.addScreenCredit(credit);
	}

	@Override
	public Map<ScreenCredit, Long> getScreenCredits()
	{
		return delegate.getScreenCredits();
	}

	@Override
	public int getRedrawRequested()
	{
		return delegate.getRedrawRequested();
	}

	@Override
	public void setRedrawRequested(int redrawRequested)
	{
		delegate.setRedrawRequested(redrawRequested);
	}

	@Override
	public PickPointFrustumList getPickFrustums()
	{
		return delegate.getPickFrustums();
	}

	@Override
	public void setPickPointFrustumDimension(Dimension dim)
	{
		delegate.setPickPointFrustumDimension(dim);
	}

	@Override
	public Dimension getPickPointFrustumDimension()
	{
		return delegate.getPickPointFrustumDimension();
	}

	@Override
	public void addPickPointFrustum()
	{
		delegate.addPickPointFrustum();
	}

	@Override
	public void addPickRectangleFrustum()
	{
		delegate.addPickRectangleFrustum();
	}

	@Override
	public Collection<Throwable> getRenderingExceptions()
	{
		return delegate.getRenderingExceptions();
	}

	@Override
	public void setRenderingExceptions(Collection<Throwable> exceptions)
	{
		delegate.setRenderingExceptions(exceptions);
	}

	@Override
	public void addRenderingException(Throwable t)
	{
		delegate.addRenderingException(t);
	}

	@Override
	public void pushProjectionOffest(Double offset)
	{
		delegate.pushProjectionOffest(offset);
	}

	@Override
	public void popProjectionOffest()
	{
		delegate.popProjectionOffest();
	}

	@Override
	public boolean isOrderedRenderingMode()
	{
		return delegate.isOrderedRenderingMode();
	}

	@Override
	public void setOrderedRenderingMode(boolean tf)
	{
		delegate.setOrderedRenderingMode(tf);
	}

	@Override
	public void drawOutlinedShape(OutlinedShape renderer, Object shape)
	{
		delegate.drawOutlinedShape(renderer, shape);
	}

	@Override
	public void beginStandardLighting()
	{
		delegate.beginStandardLighting();
	}

	@Override
	public void endStandardLighting()
	{
		delegate.endStandardLighting();
	}

	@Override
	public LightingModel getStandardLightingModel()
	{
		return delegate.getStandardLightingModel();
	}

	@Override
	public void setStandardLightingModel(LightingModel standardLighting)
	{
		delegate.setStandardLightingModel(standardLighting);
	}

	@Override
	public Vec4 computeTerrainPoint(Angle lat, Angle lon, double offset)
	{
		return delegate.computeTerrainPoint(lat, lon, offset);
	}

	@Override
	public boolean isSmall(Extent extent, int numPixels)
	{
		return delegate.isSmall(extent, numPixels);
	}

	@Override
	public void drawNormals(float length, FloatBuffer vBuf, FloatBuffer nBuf)
	{
		delegate.drawNormals(length, vBuf, nBuf);
	}

	@Override
	public OrderedRenderable peekOrderedRenderables()
	{
		return delegate.peekOrderedRenderables();
	}

	@Override
	public OrderedRenderable pollOrderedRenderables()
	{
		return delegate.pollOrderedRenderables();
	}

	@Override
	public Terrain getTerrain()
	{
		return delegate.getTerrain();
	}

	@Override
	public void restoreDefaultBlending()
	{
		delegate.restoreDefaultBlending();
	}

	@Override
	public void restoreDefaultCurrentColor()
	{
		delegate.restoreDefaultCurrentColor();
	}

	@Override
	public void restoreDefaultDepthTesting()
	{
		delegate.restoreDefaultDepthTesting();
	}

	@Override
	public boolean isPreRenderMode()
	{
		return delegate.isPreRenderMode();
	}

	@Override
	public void setPreRenderMode(boolean preRenderMode)
	{
		delegate.setPreRenderMode(preRenderMode);
	}

	@Override
	public Vec4 computePointFromPosition(Position position, int altitudeMode)
	{
		return delegate.computePointFromPosition(position, altitudeMode);
	}

	@Override
	public DeclutteringTextRenderer getDeclutteringTextRenderer()
	{
		return delegate.getDeclutteringTextRenderer();
	}

	@Override
	public void applyDeclutterFilter()
	{
		delegate.applyDeclutterFilter();
	}
}
