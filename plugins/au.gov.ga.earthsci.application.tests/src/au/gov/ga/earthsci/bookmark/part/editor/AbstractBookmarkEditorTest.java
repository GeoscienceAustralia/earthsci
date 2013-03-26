package au.gov.ga.earthsci.bookmark.part.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.earthsci.bookmark.part.editor.IBookmarkEditorMessage.Level;

/**
 * Unit tests for the {@link AbstractBookmarkEditor} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AbstractBookmarkEditorTest
{

	private AbstractBookmarkEditor classUnderTest;
	private Mockery mockContext;
	private IBookmarkEditorListener listener;

	@Before
	public void setup()
	{
		classUnderTest = new DummyBookmarkEditor();

		mockContext = new Mockery();

		listener = mockContext.mock(IBookmarkEditorListener.class);
		classUnderTest.addListener(listener);
	}

	@Test
	public void testValidateWithSingleValid()
	{
		mockContext.checking(new Expectations()
		{
			{
				{
					never(listener).editorInvalid(with(any(IBookmarkEditor.class)),
							with(any(IBookmarkEditorMessage[].class)));
					never(listener).editorValid(with(any(IBookmarkEditor.class)));
				}
			}
		});

		classUnderTest.validate("field1", true, new BookmarkEditorMessage(Level.ERROR, "error1", "error message"));

		assertTrue(classUnderTest.isValid());
		assertEquals(0, classUnderTest.getMessages().length);
	}

	@Test
	public void testValidateWithMultipleValid()
	{
		mockContext.checking(new Expectations()
		{
			{
				{
					never(listener).editorInvalid(with(any(IBookmarkEditor.class)),
							with(any(IBookmarkEditorMessage[].class)));
					never(listener).editorValid(with(any(IBookmarkEditor.class)));
				}
			}
		});

		classUnderTest.validate("field1", true, new BookmarkEditorMessage(Level.ERROR, "error1", "error1"));
		classUnderTest.validate("field1", true, new BookmarkEditorMessage(Level.ERROR, "error2", "error2"));

		assertTrue(classUnderTest.isValid());
		assertEquals(0, classUnderTest.getMessages().length);
	}

	@Test
	public void testValidateWithSingleInvalid()
	{
		mockContext.checking(new Expectations()
		{
			{
				{
					oneOf(listener).editorInvalid(with(classUnderTest), with(any(IBookmarkEditorMessage[].class)));
					never(listener).editorValid(with(any(IBookmarkEditor.class)));
				}
			}
		});

		classUnderTest.validate("field1", false, new BookmarkEditorMessage(Level.ERROR, "error1", "error1"));

		assertFalse(classUnderTest.isValid());
		assertEquals(1, classUnderTest.getMessages().length);

		assertEquals(new BookmarkEditorMessage(Level.ERROR, "error1", "error1"), classUnderTest.getMessages()[0]);
	}

	@Test
	public void testValidateWithMultipleInvalid()
	{
		mockContext.checking(new Expectations()
		{
			{
				{
					exactly(2).of(listener).editorInvalid(with(classUnderTest),
							with(any(IBookmarkEditorMessage[].class)));
					never(listener).editorValid(with(any(IBookmarkEditor.class)));
				}
			}
		});

		classUnderTest.validate("field1", false, new BookmarkEditorMessage(Level.ERROR, "error1", "error1"));
		classUnderTest.validate("field1", false, new BookmarkEditorMessage(Level.ERROR, "error2", "error2"));

		assertFalse(classUnderTest.isValid());
		assertEquals(2, classUnderTest.getMessages().length);

		assertEquals(new BookmarkEditorMessage(Level.ERROR, "error1", "error1"), classUnderTest.getMessages()[0]);
		assertEquals(new BookmarkEditorMessage(Level.ERROR, "error2", "error2"), classUnderTest.getMessages()[1]);
	}

	@Test
	public void testValidateWithInvalidFollowedByValidSameError()
	{
		mockContext.checking(new Expectations()
		{
			{
				{
					exactly(1).of(listener).editorInvalid(with(classUnderTest),
							with(any(IBookmarkEditorMessage[].class)));
					exactly(1).of(listener).editorValid(with(classUnderTest));
				}
			}
		});

		classUnderTest.validate("field1", false, new BookmarkEditorMessage(Level.ERROR, "error1", "error1"));
		classUnderTest.validate("field1", true, new BookmarkEditorMessage(Level.ERROR, "error1", "error1"));

		assertTrue(classUnderTest.isValid());
		assertEquals(0, classUnderTest.getMessages().length);
	}

	@Test
	public void testValidateWithInvalidFollowedByValidDifferentError()
	{
		mockContext.checking(new Expectations()
		{
			{
				{
					exactly(1).of(listener).editorInvalid(with(classUnderTest),
							with(any(IBookmarkEditorMessage[].class)));
					never(listener).editorValid(with(classUnderTest));
				}
			}
		});

		classUnderTest.validate("field1", false, new BookmarkEditorMessage(Level.ERROR, "error1", "error1"));
		classUnderTest.validate("field1", true, new BookmarkEditorMessage(Level.ERROR, "error2", "error2"));

		assertFalse(classUnderTest.isValid());
		assertEquals(1, classUnderTest.getMessages().length);

		assertEquals(new BookmarkEditorMessage(Level.ERROR, "error1", "error1"), classUnderTest.getMessages()[0]);
	}

	@Test
	public void testValidateWithInvalidFollowedByValidDifferentField()
	{
		mockContext.checking(new Expectations()
		{
			{
				{
					exactly(1).of(listener).editorInvalid(with(classUnderTest),
							with(any(IBookmarkEditorMessage[].class)));
					never(listener).editorValid(with(classUnderTest));
				}
			}
		});

		classUnderTest.validate("field1", false, new BookmarkEditorMessage(Level.ERROR, "error1", "error1"));
		classUnderTest.validate("field2", true, new BookmarkEditorMessage(Level.ERROR, "error1", "error1"));

		assertFalse(classUnderTest.isValid());
		assertEquals(1, classUnderTest.getMessages().length);

		assertEquals(new BookmarkEditorMessage(Level.ERROR, "error1", "error1"), classUnderTest.getMessages()[0]);
	}

	@Test
	public void testValidateWithInvalidFollowedByInvalidDifferentField()
	{
		mockContext.checking(new Expectations()
		{
			{
				{
					exactly(2).of(listener).editorInvalid(with(classUnderTest),
							with(any(IBookmarkEditorMessage[].class)));
					never(listener).editorValid(with(classUnderTest));
				}
			}
		});

		classUnderTest.validate("field1", false, new BookmarkEditorMessage(Level.ERROR, "error1", "error1"));
		classUnderTest.validate("field2", false, new BookmarkEditorMessage(Level.ERROR, "error2", "error2"));

		assertFalse(classUnderTest.isValid());
		assertEquals(2, classUnderTest.getMessages().length);

		assertEquals(new BookmarkEditorMessage(Level.ERROR, "error1", "error1"), classUnderTest.getMessages()[0]);
		assertEquals(new BookmarkEditorMessage(Level.ERROR, "error2", "error2"), classUnderTest.getMessages()[1]);
	}

	private class DummyBookmarkEditor extends AbstractBookmarkEditor
	{

		@Override
		public void okPressed()
		{
		}

		@Override
		public void cancelPressed()
		{
		}

		@Override
		public void restoreOriginalValues()
		{
		}

		@Override
		public Control createControl(Composite parent)
		{
			return null;
		}

		@Override
		public Control getControl()
		{
			return null;
		}

		@Override
		public String getName()
		{
			return null;
		}

		@Override
		public String getDescription()
		{
			return null;
		}

	}

}
