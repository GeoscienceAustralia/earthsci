package au.gov.ga.earthsci.bookmark.part.handlers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.earthsci.bookmark.model.Bookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.part.IBookmarksController;

/**
 * Unit tests for the {@link DeleteBookmarkHandler} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DeleteBookmarkHandlerTest
{

	private DeleteBookmarkHandler classUnderTest;
	private Mockery mockContext;
	private IBookmarksController controller;

	@Before
	public void setup()
	{
		classUnderTest = new DeleteBookmarkHandler();

		mockContext = new Mockery();

		controller = mockContext.mock(IBookmarksController.class);
		classUnderTest.setController(controller);
	}

	@Test
	public void testCanExecuteNullSelection()
	{
		final IBookmark[] selection = null;

		assertFalse(classUnderTest.canExecute(selection));
	}

	@Test
	public void testCanExecuteEmptySelection()
	{
		final IBookmark[] selection = new IBookmark[] {};

		assertFalse(classUnderTest.canExecute(selection));
	}

	@Test
	public void testCanExecuteSingleSelection()
	{
		final IBookmark[] selection = new IBookmark[] { new Bookmark() };

		assertTrue(classUnderTest.canExecute(selection));
	}

	@Test
	public void testCanExecuteMultiSelection()
	{
		final IBookmark[] selection = new IBookmark[] { new Bookmark(), new Bookmark(), new Bookmark() };

		assertTrue(classUnderTest.canExecute(selection));
	}

	@Test
	public void testExecute()
	{
		final IBookmark[] selection = new IBookmark[] { new Bookmark(), new Bookmark(), new Bookmark() };

		mockContext.checking(new Expectations()
		{
			{
				{
					oneOf(controller).delete(with(selection));
				}
			}
		});

		classUnderTest.execute(selection);
	}

}
