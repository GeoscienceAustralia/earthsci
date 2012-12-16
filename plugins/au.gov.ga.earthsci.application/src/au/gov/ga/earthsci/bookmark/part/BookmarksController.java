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
package au.gov.ga.earthsci.bookmark.part;

import gov.nasa.worldwind.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;

import au.gov.ga.earthsci.bookmark.BookmarkFactory;
import au.gov.ga.earthsci.bookmark.BookmarkPropertyApplicatorRegistry;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyApplicator;
import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.bookmark.model.IBookmarkPropertyAnimator;
import au.gov.ga.earthsci.bookmark.part.preferences.IBookmarksPreferences;

/**
 * The default implementation of the {@link IBookmarksController} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
@Singleton
public class BookmarksController implements IBookmarksController
{

	@Inject
	private IBookmarksPreferences preferences;
	
	@Inject
	private View worldWindView;
	
	private final ExecutorService applicatorService = Executors.newSingleThreadExecutor(new ThreadFactory()
	{
		@Override
		public Thread newThread(final Runnable r)
		{
			return new Thread(r, "Bookmark Applicator Thread"); //$NON-NLS-1$
		}
	});
	private transient Future<?> currentTask;
	
	
	@Override
	public void apply(final IBookmark bookmark)
	{
		if (currentTask != null)
		{
			currentTask.cancel(true);
			currentTask = null;
		}
		
		currentTask = applicatorService.submit(new BookmarkApplicatorRunnable(worldWindView, bookmark, getDuration(bookmark)));
	}
	
	private long getDuration(final IBookmark bookmark)
	{
		return bookmark.getTransitionDuration() == null ? preferences.getDefaultTransitionDuration() : bookmark.getTransitionDuration();
	}
	
	private static class BookmarkApplicatorRunnable implements Runnable
	{
		private final long duration;
		private final View view;
		private final List<IBookmarkPropertyAnimator> animators;
		
		private long endTime;
		
		BookmarkApplicatorRunnable(final View view, final IBookmark bookmark, final long duration)
		{
			this.duration = duration;
			this.view = view;
			
			this.animators = new ArrayList<IBookmarkPropertyAnimator>();
			final IBookmark currentState = BookmarkFactory.createBookmark();
			for (IBookmarkProperty property : bookmark.getProperties())
			{
				final IBookmarkProperty currentProperty = currentState.getProperty(property.getType());
				final IBookmarkPropertyApplicator applicator = BookmarkPropertyApplicatorRegistry.getApplicator(property);
				if (applicator != null)
				{
					animators.add(applicator.createAnimator(currentProperty, property, duration));
				}
			}
		}
		
		@Override
		public void run()
		{
			view.stopMovement();
			view.stopAnimations();
			
			endTime = System.currentTimeMillis() + duration;
			while (System.currentTimeMillis() <= endTime)
			{
				for (IBookmarkPropertyAnimator animator : animators)
				{
					if (!animator.isInitialised())
					{
						animator.init();
					}
					animator.applyFrame();
				}
				if (Thread.interrupted())
				{
					return;
				}
				view.getViewInputHandler().getWorldWindow().redraw();
			}
		}
	}
	
	/**
	 * Set the user preferences on this controller
	 */
	public void setPreferences(final IBookmarksPreferences preferences)
	{
		this.preferences = preferences;
	}
	
	/**
	 * Set the current world wind view on this controller
	 */
	public void setWorldWindView(final View worldWindView)
	{
		this.worldWindView = worldWindView;
	}
}
