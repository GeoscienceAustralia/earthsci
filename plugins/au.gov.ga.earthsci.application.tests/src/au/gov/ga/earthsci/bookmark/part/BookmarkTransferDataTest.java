package au.gov.ga.earthsci.bookmark.part;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

import au.gov.ga.earthsci.bookmark.model.Bookmark;
import au.gov.ga.earthsci.bookmark.model.IBookmark;

/**
 * Unit tests for the {@link BookmarkTransferData} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarkTransferDataTest
{

	@Test
	public void testSaveLoadEmpty() throws Exception
	{
		IBookmark[] bookmarks = new IBookmark[0];

		BookmarkTransferData loadedData = doSaveLoad(bookmarks);

		assertLoadedDataCorrect(loadedData, bookmarks);
	}

	@Test
	public void testSaveLoadSingle() throws Exception
	{
		IBookmark[] bookmarks = new IBookmark[] { createBookmark("b1") };

		BookmarkTransferData loadedData = doSaveLoad(bookmarks);

		assertLoadedDataCorrect(loadedData, bookmarks);
	}

	@Test
	public void testSaveLoadMulti() throws Exception
	{
		IBookmark[] bookmarks = new IBookmark[] { createBookmark("b1"), createBookmark("b2"), createBookmark("b3") };

		BookmarkTransferData loadedData = doSaveLoad(bookmarks);

		assertLoadedDataCorrect(loadedData, bookmarks);
	}

	private BookmarkTransferData doSaveLoad(IBookmark[] bookmarks) throws Exception
	{
		BookmarkTransferData data = BookmarkTransferData.fromBookmarks(bookmarks);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		BookmarkTransferData.save(data, os);
		return BookmarkTransferData.load(new ByteArrayInputStream(os.toByteArray()));
	}

	private void assertLoadedDataCorrect(BookmarkTransferData loadedData, IBookmark[] bookmarks)
	{
		assertNotNull(loadedData);
		assertNotNull(loadedData.getBookmarks());
		assertEquals(bookmarks.length, loadedData.getBookmarks().length);
		assertArrayEquals(bookmarks, loadedData.getBookmarks());
	}

	private IBookmark createBookmark(String name)
	{
		Bookmark result = new Bookmark();

		result.setName(name);

		return result;
	}

}
