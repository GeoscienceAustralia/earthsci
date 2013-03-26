package au.gov.ga.earthsci.bookmark.part;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.gov.ga.earthsci.bookmark.model.Bookmark;
import au.gov.ga.earthsci.bookmark.model.Bookmarks;
import au.gov.ga.earthsci.bookmark.model.IBookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmarkList;
import au.gov.ga.earthsci.bookmark.model.IBookmarks;
import au.gov.ga.earthsci.test.util.DummyRealm;

/**
 * Unit tests for the {@link BookmarksController} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarksControllerTest
{

	private BookmarksController classUnderTest;
	private IBookmarks bookmarks;
	private IBookmark[] testBookmarks;

	@BeforeClass
	public static void init()
	{
		DummyRealm.init();
	}

	@Before
	public void setup()
	{
		classUnderTest = new BookmarksController();

		bookmarks = new Bookmarks();
		classUnderTest.setBookmarks(bookmarks);

		testBookmarks = new IBookmark[] { createBookmark("b0"), createBookmark("b1"), createBookmark("b2") };

		bookmarks.getDefaultList().setBookmarks(Arrays.asList(testBookmarks));
	}

	@Test
	public void testGetCurrentListWithOnlyDefaultList()
	{
		IBookmarkList currentList = classUnderTest.getCurrentList();

		assertNotNull(currentList);
		assertEquals(bookmarks.getDefaultList(), currentList);
	}

	@Test
	public void testDeleteWithNull()
	{
		IBookmark bookmark = null;

		classUnderTest.delete(bookmark);

		assertDefaultBookmarkListCorrect(testBookmarks[0], testBookmarks[1], testBookmarks[2]);
		assertFalse(classUnderTest.isPlaying());
	}

	@Test
	public void testDeleteWithNonNull()
	{
		IBookmark bookmark = testBookmarks[1];

		classUnderTest.delete(bookmark);

		assertDefaultBookmarkListCorrect(testBookmarks[0], testBookmarks[2]);
		assertFalse(classUnderTest.isPlaying());
	}

	@Test
	public void testDeleteWithNonIncluded()
	{
		IBookmark bookmark = createBookmark("new");

		classUnderTest.delete(bookmark);

		assertDefaultBookmarkListCorrect(testBookmarks[0], testBookmarks[1], testBookmarks[2]);
		assertFalse(classUnderTest.isPlaying());
	}

	@Test
	public void testDeleteMultiWithNull()
	{
		IBookmark[] toDelete = null;

		classUnderTest.delete(toDelete);

		assertDefaultBookmarkListCorrect(testBookmarks[0], testBookmarks[1], testBookmarks[2]);
		assertFalse(classUnderTest.isPlaying());
	}

	@Test
	public void testDeleteMultiWithEmpty()
	{
		IBookmark[] toDelete = new IBookmark[0];

		classUnderTest.delete(toDelete);

		assertDefaultBookmarkListCorrect(testBookmarks[0], testBookmarks[1], testBookmarks[2]);
		assertFalse(classUnderTest.isPlaying());
	}

	@Test
	public void testDeleteMultiWithNonEmpty()
	{
		IBookmark[] toDelete = new IBookmark[] { testBookmarks[2], createBookmark("new"), testBookmarks[0] };

		classUnderTest.delete(toDelete);

		assertDefaultBookmarkListCorrect(testBookmarks[1]);
		assertFalse(classUnderTest.isPlaying());
	}

	@Test
	public void testMoveToWithNull()
	{
		IBookmark[] toMove = null;
		int targetIndex = 2;

		classUnderTest.moveBookmarks(toMove, targetIndex);

		assertDefaultBookmarkListCorrect(testBookmarks[0], testBookmarks[1], testBookmarks[2]);
	}

	@Test
	public void testMoveToWithEmpty()
	{
		IBookmark[] toMove = new IBookmark[0];
		int targetIndex = 2;

		classUnderTest.moveBookmarks(toMove, targetIndex);

		assertDefaultBookmarkListCorrect(testBookmarks[0], testBookmarks[1], testBookmarks[2]);
	}

	@Test
	public void testMoveToWithNonEmpty()
	{
		IBookmark[] toMove = new IBookmark[] { testBookmarks[1], testBookmarks[0] };
		int targetIndex = 3;

		classUnderTest.moveBookmarks(toMove, targetIndex);

		assertDefaultBookmarkListCorrect(testBookmarks[2], testBookmarks[1], testBookmarks[0]);
	}

	@Test
	public void testCopyToWithNull()
	{
		IBookmark[] toCopy = null;
		int targetIndex = 3;

		classUnderTest.copyBookmarks(toCopy, targetIndex);

		assertDefaultBookmarkListCorrect(testBookmarks[0], testBookmarks[1], testBookmarks[2]);
	}

	@Test
	public void testCopyToWithEmpty()
	{
		IBookmark[] toCopy = new IBookmark[0];
		int targetIndex = 3;

		classUnderTest.copyBookmarks(toCopy, targetIndex);

		assertDefaultBookmarkListCorrect(testBookmarks[0], testBookmarks[1], testBookmarks[2]);
	}

	@Test
	public void testCopyToWithNonEmpty()
	{
		IBookmark[] toCopy = new IBookmark[] { testBookmarks[0], testBookmarks[1] };
		int targetIndex = 3;

		classUnderTest.copyBookmarks(toCopy, targetIndex);

		List<IBookmark> bookmarkList = classUnderTest.getCurrentList().getBookmarks();
		assertEquals(5, bookmarkList.size());
		assertEquals(testBookmarks[0], bookmarkList.get(0));
		assertEquals(testBookmarks[1], bookmarkList.get(1));
		assertEquals(testBookmarks[2], bookmarkList.get(2));
		assertEquals(testBookmarks[0].getName(), bookmarkList.get(3).getName());
		assertEquals(testBookmarks[1].getName(), bookmarkList.get(4).getName());
	}

	private void assertDefaultBookmarkListCorrect(IBookmark... expected)
	{
		try
		{
			List<IBookmark> defaultBookmarks = classUnderTest.getCurrentList().getBookmarks();
			assertEquals(expected.length, defaultBookmarks.size());
			for (int i = 0; i < expected.length; i++)
			{
				assertEquals(expected[i], defaultBookmarks.get(i));
			}
		}
		catch (AssertionError e)
		{
			System.out.println("Expected: " + Arrays.asList(expected) + "| Actual: "
					+ classUnderTest.getCurrentList().getBookmarks());
			fail("Default bookmark list not as expected.");
		}
	}

	private IBookmark createBookmark(String name)
	{
		Bookmark result = new Bookmark();
		result.setName(name);
		return result;
	}
}
