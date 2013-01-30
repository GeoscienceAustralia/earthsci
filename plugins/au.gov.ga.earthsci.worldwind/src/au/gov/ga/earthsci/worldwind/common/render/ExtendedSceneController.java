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
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.terrain.Tessellator;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import au.gov.ga.earthsci.worldwind.common.exaggeration.VerticalExaggerationListener;
import au.gov.ga.earthsci.worldwind.common.exaggeration.VerticalExaggerationService;

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
public abstract class ExtendedSceneController extends BasicSceneController implements VerticalExaggerationListener
{
	private FlatRectangularTessellator flatTessellator = new FlatRectangularTessellator();

	protected final Queue<PaintTask> prePaintTasks = new LinkedList<PaintTask>();
	protected final Lock prePaintTasksLock = new ReentrantLock(true);

	protected final Queue<PaintTask> postPaintTasks = new LinkedList<PaintTask>();
	protected final Lock postPaintTasksLock = new ReentrantLock(true);

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
		super.pickLayers(dc);
		afterPickLayers(dc);
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
	 * Called immediately after the layer list is drawn. Subclasses can override
	 * to add custom functionality.
	 * 
	 * @param dc
	 */
	protected void afterDrawLayers(DrawContext dc)
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

	@Override
	public void doRepaint(DrawContext dc)
	{
		doPrePaintTasks(dc);
		super.doRepaint(dc);
		doPostPaintTasks(dc);
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
