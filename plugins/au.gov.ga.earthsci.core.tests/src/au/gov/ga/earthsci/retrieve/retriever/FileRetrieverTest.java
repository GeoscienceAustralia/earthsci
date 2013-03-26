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
package au.gov.ga.earthsci.retrieve.retriever;

import static org.junit.Assert.*;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.earthsci.core.retrieve.IRetrievalResult;
import au.gov.ga.earthsci.core.retrieve.IRetrieverMonitor;
import au.gov.ga.earthsci.core.retrieve.RetrievalProperties;
import au.gov.ga.earthsci.core.retrieve.RetrievalStatus;
import au.gov.ga.earthsci.core.retrieve.retriever.FileRetriever;

/**
 * Unit tests for the {@link FileRetriever} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class FileRetrieverTest
{

	private final FileRetriever classUnderTest = new FileRetriever();

	private IRetrieverMonitor monitor;

	private Mockery mockContext;

	@Before
	public void setup()
	{
		mockContext = new Mockery();

		monitor = mockContext.mock(IRetrieverMonitor.class);
	}

	@Test
	public void testSupportsWithNonFileProtocol() throws Exception
	{
		URL url = new URL("http://somewhere.com/something");
		assertFalse(classUnderTest.supports(url));
	}

	@Test
	public void testSupportsWithFile() throws Exception
	{
		URL url = new URL("FilE:///file.txt");
		assertTrue(classUnderTest.supports(url));
	}

	public void testRetrieveWithNullUrl() throws Exception
	{
		URL url = null;

		mockContext.checking(new Expectations()
		{
			{
				{
					oneOf(monitor).updateStatus(RetrievalStatus.READING);
				}
			}
		});

		IRetrievalResult result = classUnderTest.retrieve(url, monitor, new RetrievalProperties(), null).result;
		assertEquals(result.getError().getClass(), NullPointerException.class);
	}

	@Test(expected = NullPointerException.class)
	public void testRetrieveWithNullMonitor() throws Exception
	{
		URL url = new URL("file:///file.txt");

		classUnderTest.retrieve(url, null, new RetrievalProperties(), null);
	}

	@Test
	public void testRetrieveWithValidFile() throws Exception
	{
		String expectedContent = "success!";
		File tmpFile = createTmpFile(expectedContent);

		mockContext.checking(new Expectations()
		{
			{
				{
					oneOf(monitor).updateStatus(RetrievalStatus.READING);
				}
			}
		});

		IRetrievalResult result =
				classUnderTest.retrieve(tmpFile.toURI().toURL(), monitor, new RetrievalProperties(), null).result;

		assertNotNull(result);
		String string = WWIO.readStreamToString(result.getData().getInputStream(), "UTF-8");
		assertEquals(expectedContent, string);
		assertNull(result.getError());
	}

	@Test
	public void testRetrieveWithInvalidFile() throws Exception
	{
		URL url = new URL("file:///nonexistant.file");

		mockContext.checking(new Expectations()
		{
			{
				{
					oneOf(monitor).updateStatus(RetrievalStatus.READING);
				}
			}
		});

		IRetrievalResult result = classUnderTest.retrieve(url, monitor, new RetrievalProperties(), null).result;

		assertNotNull(result);
		assertEquals(null, result.getData());
		assertNotNull(result.getError());
	}

	private File createTmpFile(String content) throws Exception
	{
		File tempFile = File.createTempFile("FileRetrieverTest", ".txt");

		FileWriter writer = new FileWriter(tempFile);
		writer.write(content);
		writer.close();

		tempFile.deleteOnExit();

		return tempFile;
	}

}
