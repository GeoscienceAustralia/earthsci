package au.gov.ga.earthsci.catalog.part;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;

import org.junit.Test;

import au.gov.ga.earthsci.catalog.part.CatalogTransferData.TransferredCatalogNode;
import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.model.catalog.dataset.DatasetLayerCatalogTreeNode;

/**
 * Unit tests for the {@link CatalogTransferData} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CatalogTransferDataTest
{
	@Test
	public void testFromNodesWithNull()
	{
		ICatalogTreeNode[] nodes = null;
		
		CatalogTransferData result = CatalogTransferData.fromNodes(nodes);
		
		assertNotNull(result);
		assertNotNull(result.getCatalogNodes());
		assertEquals(0, result.getCatalogNodes().length);
	}
	
	@Test
	public void testFromNodesWithEmpty()
	{
		ICatalogTreeNode[] nodes = new ICatalogTreeNode[0];
		
		CatalogTransferData result = CatalogTransferData.fromNodes(nodes);
		
		assertNotNull(result);
		assertNotNull(result.getCatalogNodes());
		assertEquals(0, result.getCatalogNodes().length);
	}
	
	@Test
	public void testFromNodesWithUninitialised()
	{
		ICatalogTreeNode[] nodes = new ICatalogTreeNode[5];
		
		CatalogTransferData result = CatalogTransferData.fromNodes(nodes);
		
		assertNotNull(result);
		assertNotNull(result.getCatalogNodes());
		assertEquals(0, result.getCatalogNodes().length);
	}
	
	@Test
	public void testFromNodesWithLeaf() throws Exception
	{
		ICatalogTreeNode[] nodes = new ICatalogTreeNode[1];
		
		nodes[0] = new DatasetLayerCatalogTreeNode(new URI("file://somewhere.xml"), "Dummy", new URL("file://somewhere.xml"), null, null, true, false, true);
		
		CatalogTransferData result = CatalogTransferData.fromNodes(nodes);
		
		assertNotNull(result);
		assertNotNull(result.getCatalogNodes());
		assertEquals(1, result.getCatalogNodes().length);
		
		TransferredCatalogNode transfer = result.getCatalogNodes()[0];
		assertNotNull(transfer);
		assertEquals(nodes[0], transfer.getNode());
		assertArrayEquals(new int[] {}, transfer.getTreePath());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSaveNullData() throws Exception
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		CatalogTransferData data = null;
		
		CatalogTransferData.save(data, os);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSaveNullStream() throws Exception
	{
		ByteArrayOutputStream os = null;
		
		ICatalogTreeNode[] nodes = new ICatalogTreeNode[1];
		nodes[0] = new DatasetLayerCatalogTreeNode(new URI("file://somewhere.xml"), "Dummy", new URL("file://somewhere.xml"), null, null, true, false, true);
		
		CatalogTransferData data = CatalogTransferData.fromNodes(nodes);
		
		CatalogTransferData.save(data, os);
	}
	
	@Test
	public void testSave() throws Exception
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		ICatalogTreeNode[] nodes = new ICatalogTreeNode[1];
		nodes[0] = new DatasetLayerCatalogTreeNode(new URI("file://somewhere.xml"), "Dummy", new URL("file://somewhere.xml"), null, null, true, false, true);
		nodes[0].add(new DatasetLayerCatalogTreeNode(new URI("file://somewhereElse.xml"), "DummyChild", new URL("file://somewhereelse.xml"), null, null, true, false, true));
		
		CatalogTransferData data = CatalogTransferData.fromNodes(nodes);
		
		CatalogTransferData.save(data, os);
		
		System.out.println(os.toString());
	}
}
