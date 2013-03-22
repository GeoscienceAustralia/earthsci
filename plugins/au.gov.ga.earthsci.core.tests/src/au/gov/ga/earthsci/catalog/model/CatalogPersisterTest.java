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
package au.gov.ga.earthsci.catalog.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXParseException;

import au.gov.ga.earthsci.catalog.model.CatalogFactory;
import au.gov.ga.earthsci.catalog.model.CatalogModel;
import au.gov.ga.earthsci.catalog.model.CatalogPersister;
import au.gov.ga.earthsci.catalog.model.ICatalogModel;
import au.gov.ga.earthsci.catalog.model.dataset.DatasetReader;
import au.gov.ga.earthsci.catalog.model.dataset.DatasetXMLCatalogProvider;
import au.gov.ga.earthsci.core.persistence.PersistenceException;

/**
 * Unit tests for the {@link CatalogPersister} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CatalogPersisterTest
{

	@BeforeClass
	public static void init()
	{
		CatalogFactory.registerProvider(new DatasetXMLCatalogProvider());
	}
	
	@AfterClass
	public static void destroy()
	{
		CatalogFactory.reset();
	}
	
	@Test
	public void testSaveModelNullModel() throws Exception
	{
		ICatalogModel model = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		CatalogPersister.saveCatalogModel(model, os);
		
		assertEquals(0, os.size());
	}
	
	@Test
	public void testSaveModelEmptyModel() throws Exception
	{
		ICatalogModel model = new CatalogModel();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		CatalogPersister.saveCatalogModel(model, os);
		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><catalogModel><model><catalogs/></model></catalogModel>";
		String actual = stripNewLineIndents(os.toString());
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testSaveModelNonEmptyModel() throws Exception
	{
		ICatalogModel model = new CatalogModel();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		URL source = getClass().getResource("dataset/validdataset.xml");
		model.addTopLevelCatalog(DatasetReader.read(source, null));
		
		CatalogPersister.saveCatalogModel(model, os);
		
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><catalogModel><model><catalogs><element><catalog uri=\"" + source.toExternalForm() + "\"/></element></catalogs></model></catalogModel>";
		String actual = stripNewLineIndents(os.toString());
		
		assertEquals(expected, actual);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testLoadModelNullStream() throws Exception
	{
		InputStream is = null;
		
		CatalogPersister.loadCatalogModel(is, null);
	}
	
	@Test(expected=SAXParseException.class)
	public void testLoadModelInvalidXMLDocument() throws Exception
	{
		String document = "This is not a document!";
		
		InputStream is = new ByteArrayInputStream(document.getBytes());
		
		CatalogPersister.loadCatalogModel(is, null);
	}
	
	@Test(expected=PersistenceException.class)
	public void testLoadModelValidXMLDocumentInvalidModelDocument() throws Exception
	{
		String document = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><catalogModels><model></model></catalogModels>";
		
		InputStream is = new ByteArrayInputStream(document.getBytes());
		
		CatalogPersister.loadCatalogModel(is, null);
	}
	
	@Test
	public void testLoadModelValidEmptyModelDocument() throws Exception
	{
		String document = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><catalogModel><model><catalogs></catalogs></model></catalogModel>";
		
		InputStream is = new ByteArrayInputStream(document.getBytes());
		
		ICatalogModel catalogModel = CatalogPersister.loadCatalogModel(is, null);
		
		assertNotNull(catalogModel);
		assertNotNull(catalogModel.getRoot());
		assertNotNull(catalogModel.getTopLevelCatalogs());
		assertEquals(0, catalogModel.getTopLevelCatalogs().length);
	}
	
	@Test
	public void testLoadModelValidNonEmptyModelDocument() throws Exception
	{
		URL catalog = getClass().getResource("dataset/validdataset.xml");
		String document = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><catalogModel><model><catalogs><element><catalog uri=\"" + catalog.toExternalForm() + "\"/></element></catalogs></model></catalogModel>";
		
		InputStream is = new ByteArrayInputStream(document.getBytes());
		
		ICatalogModel catalogModel = CatalogPersister.loadCatalogModel(is, null);
		
		assertNotNull(catalogModel);
		assertNotNull(catalogModel.getRoot());
		assertNotNull(catalogModel.getTopLevelCatalogs());
		assertEquals(1, catalogModel.getTopLevelCatalogs().length);
		
		assertEquals(catalog.toExternalForm(), catalogModel.getTopLevelCatalogs()[0].getURI().toASCIIString());
	}
	
	private static String stripNewLineIndents(String s)
	{
		return s.replaceAll("[\\r|\\n]+ +", "").replaceAll("\\r?\\n?", "");
	}
	
}
