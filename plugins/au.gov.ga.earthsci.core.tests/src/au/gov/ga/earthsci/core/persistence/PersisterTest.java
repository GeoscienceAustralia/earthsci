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
package au.gov.ga.earthsci.core.persistence;

import gov.nasa.worldwind.util.WWXML;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.core.persistence.ExportableWithAdapter.Adaptable;
import au.gov.ga.earthsci.core.util.XmlUtil;

/**
 * JUnit tests for the {@link Persister} class.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class PersisterTest
{
	@Test
	public void testArray()
	{
		ExportableWithArray array = new ExportableWithArray();
		array.setArray(new double[] { 1, 3, 5, 7, 9, 38742.5463 });
		performTest(array, "testArray.xml");
	}

	@Test
	public void testCollection()
	{
		ExportableWithCollection collection = new ExportableWithCollection();
		List<Integer> list = new ArrayList<Integer>();
		list.add(5);
		list.add(7);
		list.add(435);
		collection.setCollection(list);
		performTest(collection, "testCollection.xml");
	}

	@Test
	public void testDoubleArray()
	{
		ExportableWithDoubleArray doubleArray = new ExportableWithDoubleArray();
		doubleArray.setCollectionArray(new ExportableWithCollection[3][5]);
		performTest(doubleArray, "testDoubleArray.xml");
	}

	@Test
	public void testAttribute()
	{
		ExportableWithAttribute attribute = new ExportableWithAttribute();
		attribute.setAttribute(5);
		performTest(attribute, "testAttribute.xml");
	}

	@Test
	public void testAdapter()
	{
		ExportableWithAdapter adapter = new ExportableWithAdapter();
		Adaptable adaptable = new Adaptable();
		adaptable.setValue("adaptable value");
		adapter.setAdaptable(adaptable);
		performTest(adapter, "testAdapter.xml");
	}

	@Test
	public void testMethods()
	{
		ExportableWithMethods methods = new ExportableWithMethods();
		methods.setField(5);
		methods.setMethod(10);
		methods.setSetterMethodOther(15);
		performTest(methods, "testMethods.xml");
	}

	@Test
	public void testNamedExportable()
	{
		ExportableWithMethods methods = new ExportableWithMethods();
		methods.setField(5);
		methods.setMethod(10);
		methods.setSetterMethodOther(15);
		Persister persister = new Persister();
		persister.registerNamedExportable(ExportableWithMethods.class, "exportableWithMethods");
		performTest(methods, persister, "testNamedExportable.xml");
	}

	@Test
	public void testExportableObject()
	{
		ExportableWithObject object = new ExportableWithObject();
		ExportableWithAttribute attribute = new ExportableWithAttribute();
		attribute.setAttribute(3465);
		object.setExportableObject(attribute);
		performTest(object, "testExportableObject.xml");
	}

	@Test
	public void testNull()
	{
		ExportableWithNull nul = new ExportableWithNull();
		nul.setString(null);
		performTest(nul, "testNull.xml");
	}

	@Test
	public void testNamedPersistant()
	{
		ExportableWithNamedPersistant named = new ExportableWithNamedPersistant();
		named.setField(3254235);
		performTest(named, "testNamedPersistant.xml");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNonExportable()
	{
		performTest(new ArrayList<Integer>(), "testNonExportable.xml");
	}

	protected void performTest(Object o, String expectedResourceName)
	{
		performTest(o, new Persister(), expectedResourceName);
	}

	protected void performTest(Object saved, Persister persister, String expectedResourceName)
	{
		DocumentBuilder documentBuilder = WWXML.createDocumentBuilder(false);
		Document document = documentBuilder.newDocument();
		Element element = document.createElement("root");
		document.appendChild(element);
		persister.save(saved, element, null);

		Element child = XmlUtil.getFirstChildElement(element);
		Object loaded = persister.load(child, null);
		Assert.assertEquals(saved, loaded);

		try
		{
			InputStream is = this.getClass().getResourceAsStream(expectedResourceName);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			copyLarge(is, baos);
			String expected = baos.toString();
			is.close();
			baos.close();

			baos = new ByteArrayOutputStream();
			XmlUtil.saveDocumentToFormattedStream(document, baos);
			String actual = baos.toString();
			baos.close();

			expected = expected.replace("\r\n", "\n").replace("\r", "\n");
			actual = actual.replace("\r\n", "\n").replace("\r", "\n");

			Assert.assertEquals(expected, actual);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		/*try
		{
			File output = new File(expectedResourceName);
			FileOutputStream os = new FileOutputStream(output);
			XmlUtil.saveDocumentToFormattedStream(document, os);
			os.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}*/
	}

	protected static long copyLarge(InputStream input, OutputStream output) throws IOException
	{
		byte[] buffer = new byte[8192];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer)))
		{
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
}
