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
package au.gov.ga.earthsci.bookmark.ui;

import static au.gov.ga.earthsci.bookmark.ui.Messages.*;
import gov.nasa.worldwind.View;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.bookmark.BookmarkFactory;
import au.gov.ga.earthsci.bookmark.BookmarkPropertyApplicatorRegistry;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyAnimator;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyApplicator;
import au.gov.ga.earthsci.bookmark.model.Bookmark;
import au.gov.ga.earthsci.bookmark.model.BookmarkList;
import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmarkList;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.bookmark.model.IBookmarks;
import au.gov.ga.earthsci.bookmark.ui.editor.BookmarkEditorDialog;
import au.gov.ga.earthsci.bookmark.ui.preferences.IBookmarksPreferences;
import au.gov.ga.earthsci.common.ui.dialogs.EmptyStringInputValidator;
import au.gov.ga.earthsci.common.util.AbstractPropertyChangeBean;
import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;
import au.gov.ga.earthsci.worldwind.common.util.Util;


/**
 * The default implementation of the {@link IBookmarksController} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Creatable
@Singleton
public class BookmarksController extends AbstractPropertyChangeBean implements IBookmarksController
{

	private static final Logger logger = LoggerFactory.getLogger(BookmarksController.class);

	@Inject
	private IBookmarksPreferences preferences;

	@Inject
	private IBookmarks bookmarks;

	private IBookmarkList currentList;

	private BookmarksPart part;

	/**
	 * A property change listener used to stop the bookmark applicator thread on
	 * {@link View#VIEW_STOPPED} events.
	 */
	private final PropertyChangeListener viewStopListener = new PropertyChangeListener()
	{
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			if (evt.getPropertyName().equals(View.VIEW_STOPPED))
			{
				stopCurrentTransition();
			}
		}
	};

	/**
	 * A mouse listener used to stop the bookmark applicator thread on mouse
	 * pressed events
	 */
	private final MouseListener mouseStopListener = new MouseAdapter()
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			stop();
		}
	};

	/**
	 * An executor service used to execute the application of a single bookmark
	 * state to the world
	 */
	private final ExecutorService applicatorService = Executors.newSingleThreadExecutor(new ThreadFactory()
	{
		@Override
		public Thread newThread(final Runnable runnable)
		{
			return new Thread(runnable, "Bookmark Applicator Thread"); //$NON-NLS-1$
		}
	});

	/**
	 * The currently executing bookmark applicator. <code>null</code> implies no
	 * running application.
	 */
	private transient Future<?> currentApplicatorTask;

	/**
	 * An executor service used to play through a bookmark list
	 */
	private final ExecutorService playlistService = Executors.newSingleThreadExecutor(new ThreadFactory()
	{
		@Override
		public Thread newThread(Runnable r)
		{
			return new Thread(r, "Bookmark Playlist Thread"); //$NON-NLS-1$
		}
	});

	/**
	 * The currently executing playlist applicator. <code>null</code> implies no
	 * running playlist.
	 */
	private transient Future<?> currentPlaylistTask;

	public BookmarksController()
	{
		addPropertyChangeListener("currentList", new PropertyChangeListener() //$NON-NLS-1$
				{
					@Override
					public void propertyChange(PropertyChangeEvent evt)
					{
						part.refreshDropdown();
					}
				});
	}

	@Override
	public IBookmark createNew()
	{
		return createNew(getCurrentList());
	}

	@Override
	public IBookmark createNew(IBookmarkList list)
	{
		stop();
		IBookmark b = BookmarkFactory.createBookmark(preferences.getDefaultPropertyTypes());
		list.getBookmarks().add(b);
		return b;
	}

	@Override
	public void apply(final IBookmark bookmark)
	{
		stop();
		doApply(bookmark);
	}

	/**
	 * Perform the application of the bookmark to the world, without stopping
	 * any running animations
	 */
	private void doApply(final IBookmark bookmark)
	{
		View view = WorldWindowRegistry.INSTANCE.getActiveView();
		if (view != null)
		{
			part.highlight(bookmark);
			currentApplicatorTask =
					applicatorService.submit(new BookmarkApplicatorRunnable(view, bookmark, getDuration(bookmark)));
		}
	}

	@Override
	public void edit(final IBookmark bookmark)
	{
		stop();
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				BookmarkEditorDialog dialog = new BookmarkEditorDialog(bookmark, Display.getDefault().getActiveShell());
				dialog.open();
			}
		});
	}

	@Override
	public IBookmarkList createNewBookmarkList()
	{
		stop();
		InputDialog dialog =
				new InputDialog(Display.getDefault().getActiveShell(), BookmarksController_NewListDialogTitle,
						BookmarksController_NewListDialogMessage, BookmarksController_DefaultNewListName,
						new EmptyStringInputValidator(BookmarksController_NewListValidationMessage));
		dialog.open();

		if (dialog.getReturnCode() != InputDialog.OK)
		{
			return null;
		}

		BookmarkList result = new BookmarkList(dialog.getValue());
		bookmarks.addList(result);
		return result;
	}

	@Override
	public void renameBookmarkList(IBookmarkList list)
	{
		stop();

		InputDialog dialog =
				new InputDialog(Display.getDefault().getActiveShell(), BookmarksController_RenameListDialogTitle,
						BookmarksController_RenameListDialogMessage, list.getName(), new EmptyStringInputValidator(
								BookmarksController_RenameListValidationMessation));
		dialog.open();

		if (dialog.getReturnCode() != InputDialog.OK)
		{
			return;
		}

		list.setName(dialog.getValue());
	}

	@Override
	public boolean deleteBookmarkList(IBookmarkList list)
	{
		if (list == null || list == bookmarks.getDefaultList())
		{
			return false;
		}

		if (preferences.askForListDeleteConfirmation())
		{
			MessageDialogWithToggle dialog =
					MessageDialogWithToggle.openOkCancelConfirm(Display.getDefault().getActiveShell(),
							Messages.BookmarksController_BookmarkListDeleteDialogTitle,
							Messages.BookmarksController_BookmarkListDeleteDialogMessage,
							Messages.BookmarksController_BookmarkListDeleteDialogToggleMessage,
							preferences.askForListDeleteConfirmation(), null, null);
			if (dialog.getReturnCode() != MessageDialog.OK)
			{
				return false;
			}

			preferences.setAskForListDeleteConfirmation(dialog.getToggleState());
		}

		return bookmarks.removeList(list);
	}

	@Override
	public void delete(IBookmark bookmark)
	{
		if (bookmark == null)
		{
			return;
		}

		stop();

		getCurrentList().getBookmarks().remove(bookmark);
	}

	@Override
	public void delete(IBookmark... bookmarks)
	{
		if (bookmarks == null || bookmarks.length == 0)
		{
			return;
		}

		stop();

		for (IBookmark b : bookmarks)
		{
			getCurrentList().getBookmarks().remove(b);
		}
	}

	@Override
	public IBookmarkList getCurrentList()
	{
		if (currentList == null)
		{
			return bookmarks.getDefaultList();
		}
		return currentList;
	}

	@Override
	public void setCurrentList(IBookmarkList list)
	{
		stop();
		if (list == currentList)
		{
			return;
		}
		firePropertyChange("currentList", currentList, currentList = list); //$NON-NLS-1$
	}

	@Override
	public void play(IBookmark bookmark)
	{
		play(getCurrentList(), bookmark);
	}

	@Override
	public void play(IBookmarkList list, IBookmark bookmark)
	{
		stop();
		if (list == null)
		{
			logger.debug("No bookmark list provided. Aborting play."); //$NON-NLS-1$
			return;
		}

		View wwview = WorldWindowRegistry.INSTANCE.getActiveView();
		if (wwview == null)
		{
			logger.debug("No view found. Aborting play."); //$NON-NLS-1$
			return;
		}

		currentPlaylistTask = playlistService.submit(new BookmarkPlaylistRunnable(wwview, list, bookmark));
	}

	@Override
	public boolean isPlaying()
	{
		return currentPlaylistTask != null;
	}

	@Override
	public void stop()
	{
		if (currentPlaylistTask != null)
		{
			currentPlaylistTask.cancel(true);
			currentPlaylistTask = null;
		}
		stopCurrentTransition();
	}

	@Override
	public void moveBookmarks(IBookmark[] bookmarks, int targetIndex)
	{
		moveBookmarks(getCurrentList(), bookmarks, getCurrentList(), targetIndex);
	}

	@Override
	public void moveBookmarks(IBookmarkList sourceList, IBookmark[] bookmarks, IBookmarkList targetList, int targetIndex)
	{
		if (bookmarks == null || bookmarks.length == 0)
		{
			return;
		}

		stop();

		targetIndex = Util.clamp(targetIndex, 0, targetList.getBookmarks().size());

		ArrayList<IBookmark> sourceBookmarksList = new ArrayList<IBookmark>(sourceList.getBookmarks());
		ArrayList<IBookmark> targetBookmarksList;
		if (sourceList == targetList)
		{
			targetBookmarksList = sourceBookmarksList;
		}
		else
		{
			targetBookmarksList = new ArrayList<IBookmark>(targetList.getBookmarks());
		}

		int[] currentIndices = new int[bookmarks.length];
		for (int i = 0; i < bookmarks.length; i++)
		{
			currentIndices[i] = sourceBookmarksList.indexOf(bookmarks[i]);
		}

		for (int i = bookmarks.length - 1; i >= 0; i -= 1)
		{
			sourceBookmarksList.remove(bookmarks[i]);

			if (currentIndices[i] < targetIndex && sourceList == targetList)
			{
				targetIndex--;
			}

			targetBookmarksList.add(targetIndex, bookmarks[i]);
		}

		sourceList.setBookmarks(sourceBookmarksList);
		targetList.setBookmarks(targetBookmarksList);
	}

	@Override
	public void copyBookmarks(IBookmark[] bookmarks, int targetIndex)
	{
		copyBookmarks(getCurrentList(), bookmarks, getCurrentList(), targetIndex);
	}

	@Override
	public void copyBookmarks(IBookmarkList sourceList, IBookmark[] bookmarks, IBookmarkList targetList, int targetIndex)
	{
		if (bookmarks == null || bookmarks.length == 0)
		{
			return;
		}

		targetIndex = Util.clamp(targetIndex, 0, targetList.getBookmarks().size());

		ArrayList<IBookmark> targetBookmarksList = new ArrayList<IBookmark>(targetList.getBookmarks());

		IBookmark[] copies = copy(bookmarks);
		for (int i = copies.length - 1; i >= 0; i -= 1)
		{
			targetBookmarksList.add(targetIndex, copies[i]);
		}

		targetList.setBookmarks(targetBookmarksList);
	}

	private IBookmark[] copy(IBookmark... bookmarks)
	{
		BookmarkTransferData btd = BookmarkTransferData.fromBookmarks(bookmarks);

		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			BookmarkTransferData.save(btd, os);
			btd = BookmarkTransferData.load(new ByteArrayInputStream(os.toByteArray()));

			IBookmark[] copies = new IBookmark[bookmarks.length];
			for (int i = 0; i < copies.length; i++)
			{
				copies[i] = new Bookmark(btd.getBookmarks()[i]);
			}
			return copies;
		}
		catch (Exception e)
		{
			logger.error("Exception copying bookmarks", e); //$NON-NLS-1$
		}

		return btd.getBookmarks();
	}

	/**
	 * Stop any current bookmark transitions that are running
	 */
	public void stopCurrentTransition()
	{
		if (currentApplicatorTask != null)
		{
			currentApplicatorTask.cancel(true);
			currentApplicatorTask = null;
		}
	}

	/**
	 * Return the duration (in milliseconds) to be used for transitioning to the
	 * given bookmark
	 */
	private long getDuration(final IBookmark bookmark)
	{
		return bookmark.getTransitionDuration() == null ? preferences.getDefaultTransitionDuration() : bookmark
				.getTransitionDuration();
	}

	/**
	 * A {@link Runnable} that triggers the animation between the current world
	 * state and the selected bookmark.
	 */
	private class BookmarkApplicatorRunnable implements Runnable
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
				final IBookmarkPropertyApplicator applicator =
						BookmarkPropertyApplicatorRegistry.getApplicator(property);
				if (applicator != null)
				{
					IBookmarkPropertyAnimator animator = applicator.createAnimator(currentProperty, property, duration);
					if (animator != null)
					{
						animators.add(animator);
					}
				}
			}
		}

		@Override
		public void run()
		{
			view.stopMovement();
			view.stopAnimations();

			view.addPropertyChangeListener(View.VIEW_STOPPED, viewStopListener);
			view.getViewInputHandler().getWorldWindow().getInputHandler().addMouseListener(mouseStopListener);

			endTime = System.currentTimeMillis() + duration;
			while (System.currentTimeMillis() <= endTime)
			{
				if (Thread.interrupted())
				{
					break;
				}
				applyAnimators();
			}
			applyAnimators();

			view.getViewInputHandler().getWorldWindow().getInputHandler().removeMouseListener(mouseStopListener);
			view.removePropertyChangeListener(View.VIEW_STOPPED, viewStopListener);
		}

		private void applyAnimators()
		{
			for (IBookmarkPropertyAnimator animator : animators)
			{
				try
				{
					if (!animator.isInitialised())
					{
						animator.init();
					}
					animator.applyFrame();
				}
				catch (Exception e)
				{
					logger.error("Error applying animator frame", e); //$NON-NLS-1$
				}
			}
			view.getViewInputHandler().getWorldWindow().redraw();
		}
	}

	/**
	 * A runnable that loops through the provided bookmark list and applies each
	 * bookmark in turn
	 */
	private class BookmarkPlaylistRunnable implements Runnable
	{
		private final View view;
		private final List<IBookmark> list;
		private IBookmark currentBookmark;

		BookmarkPlaylistRunnable(View view, IBookmarkList list, IBookmark bookmark)
		{
			this.view = view;
			this.list = new ArrayList<IBookmark>(list.getBookmarks());
			this.currentBookmark = this.list.contains(bookmark) ? bookmark : this.list.get(0);
		}

		@Override
		public void run()
		{
			view.stopAnimations();
			view.stopMovement();

			while (true)
			{
				try
				{
					// Apply current bookmark and wait for completion
					doApply(currentBookmark);
					currentApplicatorTask.get();

					// Wait for user specified time
					view.getViewInputHandler().getWorldWindow().getInputHandler().addMouseListener(mouseStopListener);
					Thread.sleep(preferences.getPlayBookmarksWaitDuration());
					view.getViewInputHandler().getWorldWindow().getInputHandler()
							.removeMouseListener(mouseStopListener);

					// Proceed to next bookmark
					int nextIndex = (list.indexOf(currentBookmark) + 1) % list.size();
					currentBookmark = list.get(nextIndex);

					if (Thread.interrupted())
					{
						break;
					}
				}
				catch (InterruptedException e)
				{
					// Expected if user cancels playlist during wait phase
					break;
				}
				catch (Exception e)
				{
					logger.error("Exception occurred while running playlist.", e); //$NON-NLS-1$
					break;
				}

			}
			currentPlaylistTask = null;
		}
	}

	/**
	 * Set the user preferences on this controller
	 */
	public void setPreferences(final IBookmarksPreferences preferences)
	{
		this.preferences = preferences;
	}

	@Override
	public void setView(BookmarksPart part)
	{
		this.part = part;
	}

	/**
	 * Set the current bookmarks model on this controller
	 */
	public void setBookmarks(IBookmarks bookmarks)
	{
		this.bookmarks = bookmarks;
	}
}
