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
package au.gov.ga.earthsci.catalog.model.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Test;

import au.gov.ga.earthsci.catalog.model.ICatalogTreeNode;
import au.gov.ga.earthsci.catalog.model.dataset.DatasetCatalogTreeNode;
import au.gov.ga.earthsci.catalog.model.dataset.DatasetLayerCatalogTreeNode;
import au.gov.ga.earthsci.catalog.model.dataset.DatasetReader;

/**
 * Unit tests for the {@link DatasetReader} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DatasetReaderTest
{

	@Test
	public void testReadNullSourceNullContext() throws Exception
	{
		ICatalogTreeNode result = DatasetReader.read(null, null);
		
		assertNotNull(result);
		assertFalse(result.hasChildren());
	}
	
	@Test
	public void testReadNonexistantSource() throws Exception
	{
		URL source = new URL("file:/somefile.nnn");
		
		ICatalogTreeNode result = DatasetReader.read(source, null);
		
		assertNotNull(result);
		assertFalse(result.hasChildren());
	}
	
	@Test
	public void testReadValidSource() throws Exception
	{
		URL source = getClass().getResource("validdataset.xml");
		
		ICatalogTreeNode result = DatasetReader.read(source, null);
		
		assertNotNull(result);
		assertTrue(result.hasChildren());
		
		assertEquals(2, result.getChildCount());
		
		assertTrue(result.getChild(0) instanceof DatasetLayerCatalogTreeNode);
		assertTrue(result.getChild(1) instanceof DatasetCatalogTreeNode);
	}
	
	@Test
	public void testReadInvalidSource() throws Exception
	{
		URL source = getClass().getResource("invaliddataset.xml");
		
		ICatalogTreeNode result = DatasetReader.read(source, null);
		
		assertNotNull(result);
		assertFalse(result.hasChildren());
	}
	
	@Test
	public void testReadEmptySource() throws Exception
	{
		URL source = getClass().getResource("emptydataset.xml");
		
		ICatalogTreeNode result = DatasetReader.read(source, null);
		
		assertNotNull(result);
		assertFalse(result.hasChildren());
	}
	
}
