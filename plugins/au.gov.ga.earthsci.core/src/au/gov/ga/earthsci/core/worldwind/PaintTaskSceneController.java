/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.core.worldwind;

import gov.nasa.worldwind.render.DrawContext;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import au.gov.ga.earthsci.worldwind.common.render.ExtendedSceneController;

/**
 * SceneController that provides the ability to add {@link PaintTask}'s to call
 * before or after a repaint occurs.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PaintTaskSceneController extends ExtendedSceneController
{
	protected final Queue<PaintTask> prePaintTasks = new LinkedList<PaintTask>();
	protected final Lock prePaintTasksLock = new ReentrantLock(true);

	protected final Queue<PaintTask> postPaintTasks = new LinkedList<PaintTask>();
	protected final Lock postPaintTasksLock = new ReentrantLock(true);

	@Override
	public void doRepaint(DrawContext dc)
	{
		doPrePaintTasks(dc);
		performRepaint(dc);
		doPostPaintTasks(dc);
	}

	/**
	 * Actually perform the repaint, called by {@link #doRepaint(DrawContext)}
	 * after the pre PaintTasks and before the post PaintTasks.
	 * 
	 * @param dc
	 */
	protected void performRepaint(DrawContext dc)
	{
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
			this.draw(dc);
		}
		finally
		{
			this.finalizeFrame(dc);
		}
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
