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

import gov.nasa.worldwind.BasicSceneController;
import gov.nasa.worldwind.SceneController;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.SurfaceObjectTileBuilder;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.terrain.Tessellator;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import au.gov.ga.earthsci.worldwind.common.effects.Effect;
import au.gov.ga.earthsci.worldwind.common.exaggeration.VerticalExaggerationListener;
import au.gov.ga.earthsci.worldwind.common.exaggeration.VerticalExaggerationService;
import au.gov.ga.earthsci.worldwind.common.util.SectorClipPlanes;
import au.gov.ga.earthsci.worldwind.common.view.delegate.IDelegateView;

/**
 * {@link SceneController} that uses a separate {@link Tessellator} to generate
 * a separate set of flat geometry, used by layers that are rendered onto a flat
 * surface.
 * <p/>
 * Also provides the ability to add {@link PaintTask}'s to call before or after
 * a repaint occurs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ExtendedSceneController extends BasicSceneController implements DrawableSceneController,
		VerticalExaggerationListener
{
	private FlatRectangularTessellator flatTessellator = new FlatRectangularTessellator();
	protected final SectorClipPlanes sectorClipping = new SectorClipPlanes();

	protected final Queue<PaintTask> prePaintTasks = new LinkedList<PaintTask>();
	protected final Lock prePaintTasksLock = new ReentrantLock(true);

	protected final Queue<PaintTask> postPaintTasks = new LinkedList<PaintTask>();
	protected final Lock postPaintTasksLock = new ReentrantLock(true);

	protected final List<Effect> effects = new ArrayList<Effect>();

	public ExtendedSceneController()
	{
		dc = wrapDrawContext(dc);
		VerticalExaggerationService.INSTANCE.addListener(this);
		setVerticalExaggeration(VerticalExaggerationService.INSTANCE.get());
	}

	protected ExtendedDrawContext wrapDrawContext(DrawContext dc)
	{
		return new ExtendedDrawContext(dc);
	}

	@Override
	protected SurfaceObjectTileBuilder createSurfaceObjectTileBuilder()
	{
		return new OffscreenSurfaceObjectRenderer();
	}

	public void clipSector(Sector sector)
	{
		sectorClipping.clipSector(sector);
	}

	public void clearClipping()
	{
		sectorClipping.clear();
	}

	@Override
	protected void createTerrain(DrawContext dc)
	{
		super.createTerrain(dc);

		if (dc instanceof ExtendedDrawContext)
		{
			ExtendedDrawContext edc = (ExtendedDrawContext) dc;
			if (edc.getFlatSurfaceGeometry() == null)
			{
				if (dc.getModel() != null && dc.getModel().getGlobe() != null)
				{
					SectorGeometryList sgl = flatTessellator.tessellate(dc);
					edc.setFlatSurfaceGeometry(sgl);
				}
			}
		}
	}

	@Override
	public void doRepaint(DrawContext dc)
	{
		doPrePaintTasks(dc);
		this.initializeFrame(dc);
		try
		{
			this.applyView(dc);
			this.createPickFrustum(dc);
			this.createTerrain(dc);
			this.preRender(dc);
			this.clearFrame(dc);
			this.pick(dc);
			this.clearFrame(dc);
			if (view instanceof IDelegateView)
			{
				((IDelegateView) view).draw(dc, this);
			}
			else
			{
				this.draw(dc);
			}
		}
		finally
		{
			this.finalizeFrame(dc);
		}
		doPostPaintTasks(dc);
	}

	/**
	 * @return Draw dimensions used when drawing with this scene controller
	 */
	protected Dimension getDrawDimensions()
	{
		return new Dimension(dc.getDrawableWidth(), dc.getDrawableHeight());
	}

	/**
	 * @return {@link Effect}s used when drawing with this scene controller
	 */
	public List<Effect> getEffects()
	{
		return effects;
	}

	/**
	 * Same as {@link #getEffects()}, with a generic return type, for subclasses
	 * to override.
	 * 
	 * @return Same as {@link #getEffects()}
	 */
	protected List<? extends Effect> getDrawEffects()
	{
		return getEffects();
	}

	@Override
	public void draw(DrawContext dc)
	{
		//The draw call is the lowest level rendering call for the SceneController,
		//so we still have depth information at this level, which is needed for DoF.
		Dimension dimensions = getDrawDimensions();

		//retrieve the list of effects, put them in a new list
		List<Effect> effects = new ArrayList<Effect>(getDrawEffects());

		//remove any disabled effects
		for (int i = effects.size() - 1; i >= 0; i--)
		{
			Effect effect = effects.get(i);
			if (!effect.isEnabled())
			{
				effect.releaseResources(dc);
				effects.remove(i);
			}
		}

		//if there's no enabled effects, draw normally
		if (effects.isEmpty())
		{
			clippedDraw(dc);
			return;
		}

		Effect firstEffect = effects.get(0);
		Effect lastEffect = effects.get(effects.size() - 1);

		try
		{
			firstEffect.bindFrameBuffer(dc, dimensions);
			this.clearFrame(dc);
			//draw the actual scene onto the first effect's frame buffer:
			clippedDraw(dc);
		}
		finally
		{
			firstEffect.unbindFrameBuffer(dc, dimensions);
		}

		for (int i = 1; i < effects.size(); i++)
		{
			try
			{
				effects.get(i).bindFrameBuffer(dc, dimensions);
				this.clearFrame(dc);
				//draw the previous effect's frame buffer onto the current frame buffer:
				effects.get(i - 1).drawFrameBufferWithEffect(dc, dimensions);
			}
			finally
			{
				effects.get(i).unbindFrameBuffer(dc, dimensions);
			}
		}

		lastEffect.drawFrameBufferWithEffect(dc, dimensions); //draw the final effect's frame buffer onto the final buffer
	}

	protected void clippedDraw(DrawContext dc)
	{
		try
		{
			sectorClipping.enableClipping(dc);
			doDraw(dc);
		}
		finally
		{
			sectorClipping.disableClipping(dc);
		}
	}

	protected void doDraw(DrawContext dc)
	{
		beforeDrawLayers(dc);
		super.draw(dc);
	}

	@Override
	protected void preRender(DrawContext dc)
	{
		try
		{
			dc.setPreRenderMode(true);
			beforePreRenderLayers(dc);
			super.preRender(dc);
		}
		finally
		{
			dc.setPreRenderMode(false);
		}
	}

	@Override
	protected void preRenderOrderedSurfaceRenderables(DrawContext dc)
	{
		//preRenderOrderedSurfaceRenderables is called immediately after prerendering the layer list, so we
		//can inject our overridable function here
		afterPreRenderLayers(dc);
		super.preRenderOrderedSurfaceRenderables(dc);
	}

	@Override
	protected void drawOrderedSurfaceRenderables(DrawContext dc)
	{
		//drawOrderedSurfaceRenderables is called immediately after drawing the layer list, so we can inject
		//our overridable function here
		afterDrawLayers(dc);
		super.drawOrderedSurfaceRenderables(dc);

		//If we disable sector clipping here, the ordered renderables are not clipped.
		//This means the HUD layers are not clipped, but this has the side-effect that
		//wireframe elevation isn't clipped, and nor is the graticule.
		sectorClipping.disableClipping(dc);
	}

	@Override
	public void clearFrame(DrawContext dc)
	{
		super.clearFrame(dc);
	}

	@Override
	public void applyView(DrawContext dc)
	{
		super.applyView(dc);
	}

	@Override
	protected void pickTerrain(DrawContext dc)
	{
		try
		{
			super.pickTerrain(dc);
		}
		catch (ConcurrentModificationException e)
		{
			//ignore CME, seems to be a bug in the SectorGeometryList
		}
	}

	@Override
	protected void pickLayers(DrawContext dc)
	{
		beforePickLayers(dc);
		super.pickLayers(dc);
		afterPickLayers(dc);
	}

	/**
	 * Called immediately before the layer list is prerendered. Subclasses can
	 * override to add custom functionality.
	 * 
	 * @param dc
	 */
	protected void beforePreRenderLayers(DrawContext dc)
	{
	}

	/**
	 * Called immediately after the layer list is prerendered. Subclasses can
	 * override to add custom functionality.
	 * 
	 * @param dc
	 */
	protected void afterPreRenderLayers(DrawContext dc)
	{
	}

	/**
	 * Called immediately before the layer list is drawn. Subclasses can
	 * override to add custom functionality.
	 * 
	 * @param dc
	 */
	protected void beforeDrawLayers(DrawContext dc)
	{
	}

	/**
	 * Called immediately after the layer list is drawn. Subclasses can override
	 * to add custom functionality.
	 * 
	 * @param dc
	 */
	protected void afterDrawLayers(DrawContext dc)
	{
	}

	/**
	 * Called immediately before the layer list is picked. Subclasses can
	 * override to add custom functionality.
	 * 
	 * @param dc
	 */
	protected void beforePickLayers(DrawContext dc)
	{
	}

	/**
	 * Called immediately after the layer list is picked. Subclasses can
	 * override to add custom functionality.
	 * 
	 * @param dc
	 */
	protected void afterPickLayers(DrawContext dc)
	{
	}

	@Override
	public void verticalExaggerationChanged(double oldValue, double newValue)
	{
		if (getVerticalExaggeration() != newValue)
		{
			//keep this in sync with the vertical exaggeration service
			setVerticalExaggeration(newValue);
		}
	}

	@Override
	public void setVerticalExaggeration(double verticalExaggeration)
	{
		super.setVerticalExaggeration(verticalExaggeration);
		//keep the vertical exaggeration service in sync with this 
		VerticalExaggerationService.INSTANCE.set(verticalExaggeration);
	}

	/**
	 * Add a task to be executed on the render thread prior to painting
	 */
	public void addPrePaintTask(PaintTask r)
	{
		prePaintTasksLock.lock();
		prePaintTasks.add(r);
		prePaintTasksLock.unlock();
	}

	/**
	 * Add a task to be executed on the render thread immediately after
	 */
	public void addPostPaintTask(PaintTask r)
	{
		postPaintTasksLock.lock();
		postPaintTasks.add(r);
		postPaintTasksLock.unlock();
	}

	protected void doPrePaintTasks(DrawContext dc)
	{
		prePaintTasksLock.lock();
		try
		{
			while (!prePaintTasks.isEmpty())
			{
				prePaintTasks.remove().run(dc);
			}
		}
		finally
		{
			prePaintTasksLock.unlock();
		}
	}

	protected void doPostPaintTasks(DrawContext dc)
	{
		postPaintTasksLock.lock();
		try
		{
			while (!postPaintTasks.isEmpty())
			{
				postPaintTasks.remove().run(dc);
			}
		}
		finally
		{
			postPaintTasksLock.unlock();
		}
	}
}
