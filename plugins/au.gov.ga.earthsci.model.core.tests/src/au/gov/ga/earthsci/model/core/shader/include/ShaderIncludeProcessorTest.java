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
package au.gov.ga.earthsci.model.core.shader.include;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;

import au.gov.ga.earthsci.common.util.Util;
import au.gov.ga.earthsci.model.core.tests.util.GLTestUtil;

/**
 * Unit tests for the {@link ShaderIncludeProcessor}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@SuppressWarnings("nls")
public class ShaderIncludeProcessorTest
{

	private ShaderIncludeProcessor classUnderTest;

	@Before
	public void setup()
	{
		classUnderTest = new ShaderIncludeProcessor();
	}

	@Test
	public void testProcessWithNull() throws Exception
	{
		assertNull(classUnderTest.process(null));
	}

	@Test
	public void testProcessWithEmpty() throws Exception
	{
		assertEquals("", classUnderTest.process(""));
	}

	@Test
	public void testProcessWithNoIncludes() throws Exception
	{
		String source = loadSource("testNoIncludes.vert");
		String expected = source;
		String result = classUnderTest.process(source);

		assertEqualsIgnoreLineEndings(expected, result);
	}

	@Test
	public void testProcessWithNamedStringInclude() throws Exception
	{
		classUnderTest.namedString("include1", "uniform float included_1;");
		classUnderTest.namedString("include2", "float includedFunction(float arg)\n{\n\treturn arg;\n}");

		String source = loadSource("testNamedStringIncludes.vert");
		String expected = loadSource("testNamedStringIncludes_expected.vert");
		String result = classUnderTest.process(source);

		assertEqualsIgnoreLineEndings(expected, result);
	}

	@Test
	public void testProcessWithSameFolderInclude() throws Exception
	{
		String source = loadSource("testSameFolderIncludes.vert");
		String expected = loadSource("testSameFolderIncludes_expected.vert");
		String result = classUnderTest.process(source);

		assertEqualsIgnoreLineEndings(expected, result);
	}

	@Test
	public void testProcessWithNamedStringOverride() throws Exception
	{
		classUnderTest.namedString("include1.glsl", "// Included via override");

		String source = loadSource("testNamedStringOverrides.vert");
		String expected = loadSource("testNamedStringOverrides_expected.vert");

		String result = classUnderTest.process(source);

		assertEqualsIgnoreLineEndings(expected, result);
	}

	@Test
	public void testProcessWithNestedIncludes() throws Exception
	{
		classUnderTest.namedString("include2.glsl", "#include include3.glsl");
		classUnderTest.namedString("bob", "// bob");

		String source = loadSource("testNestedIncludes.vert");
		String expected = loadSource("testNestedIncludes_expected.vert");

		String result = classUnderTest.process(source);

		assertEqualsIgnoreLineEndings(expected, result);
	}

	@Test(expected = IOException.class)
	public void testProcessWithMissingIncludesFailLoud() throws Exception
	{
		String source = loadSource("testMissingIncludes.vert");
		String expected = loadSource("testMissingIncludes_expected.vert");

		String result = classUnderTest.process(source, false);

		assertEqualsIgnoreLineEndings(expected, result);
	}

	@Test
	public void testProcessWithMissingIncludesFailQuiet() throws Exception
	{
		String source = loadSource("testMissingIncludes.vert");
		String expected = loadSource("testMissingIncludes_expected.vert");

		String result = classUnderTest.process(source, true);

		assertEqualsIgnoreLineEndings(expected, result);
	}

	@Test
	public void testProcessResourceWithNullLoader() throws Exception
	{
		Class<?> loader = null;
		String expected = loadSource("testNoIncludes.vert");

		String result = classUnderTest.processResource(loader, "testNoIncludes.vert");

		assertEqualsIgnoreLineEndings(expected, result);

	}

	@Test
	public void testProcessResourceWithNonNullLoader() throws Exception
	{
		Class<?> loader = getClass();
		String expected = loadSource("testNoIncludes.vert");

		String result = classUnderTest.processResource(loader, "testNoIncludes.vert");

		assertEqualsIgnoreLineEndings(expected, result);

	}

	@Test(expected = IOException.class)
	public void testProcessResourceWithWrongLoaderLocationRelativeResource() throws Exception
	{
		Class<?> loader = String.class;
		String expected = null;

		String result = classUnderTest.processResource(loader, "testNoIncludes.vert");

		assertEqualsIgnoreLineEndings(expected, result);

	}

	@Test
	public void testProcessResourceWithWrongLoaderLocationRelativeResourceQiet() throws Exception
	{
		Class<?> loader = String.class;
		String expected = null;

		String result = classUnderTest.processResource(loader, "testNoIncludes.vert", true);

		assertEqualsIgnoreLineEndings(expected, result);

	}

	@Test
	public void testProcessResourceWithWrongLoaderLocationAbsoluteResource() throws Exception
	{
		Class<?> loader = GLTestUtil.class;
		String expected = loadSource("testNoIncludes.vert");

		String result =
				classUnderTest.processResource(loader,
						"/au/gov/ga/earthsci/model/core/shader/include/testNoIncludes.vert");

		assertEqualsIgnoreLineEndings(expected, result);

	}

	private static void assertEqualsIgnoreLineEndings(String expected, String result)
	{
		if (expected == null)
		{
			assertNull(result);
			return;
		}
		assertEquals(expected.replaceAll("[\n\r]+", "\n"), result.replaceAll("[\n\r]+", "\n"));
	}

	private static String loadSource(String name) throws Exception
	{
		InputStream stream = ShaderIncludeProcessorTest.class.getResourceAsStream(name);
		String result = Util.readStreamToString(stream, Charset.defaultCharset().name());
		return result;
	}
}
