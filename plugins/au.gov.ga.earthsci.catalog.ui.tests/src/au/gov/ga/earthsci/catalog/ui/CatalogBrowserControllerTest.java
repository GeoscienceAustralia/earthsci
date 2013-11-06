package au.gov.ga.earthsci.catalog.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URL;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.earthsci.catalog.AbstractCatalogTreeNode;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.layer.tree.ILayerTreeNode;
import au.gov.ga.earthsci.layer.worldwind.ITreeModel;

/**
 * Unit tests for the {@link CatalogBrowserController}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@SuppressWarnings("unchecked")
public class CatalogBrowserControllerTest
{

	private CatalogBrowserController classUnderTest;

	private ITreeModel currentLayerModel;
	private ILayerTreeNode rootNode;

	private Mockery mockContext;

	@Before
	public void setup()
	{
		mockContext = new Mockery();

		currentLayerModel = mockContext.mock(ITreeModel.class);
		rootNode = mockContext.mock(ILayerTreeNode.class);

		mockContext.checking(new Expectations()
		{
			{
				{
					allowing(currentLayerModel).getRootNode();
					will(returnValue(rootNode));
				}
			}
		});

		classUnderTest = new CatalogBrowserController();


		classUnderTest.setCurrentLayerModel(currentLayerModel);
	}

	// ************************ 
	// areAllLayerNodes()
	// ************************

	@Test
	public void testAreAllLayerNodesWithNull()
	{
		ICatalogTreeNode[] nodes = null;
		assertTrue(classUnderTest.areAllLayerNodes(nodes));
	}

	@Test
	public void testAreAllLayerNodesWithEmpty()
	{
		ICatalogTreeNode[] nodes = new ICatalogTreeNode[0];
		assertTrue(classUnderTest.areAllLayerNodes(nodes));
	}

	@Test
	public void testAreAllLayerNodesWithSingleLayerNode()
	{
		DummyCatalogNode node0 = new DummyCatalogNode();
		node0.layerNode = true;

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0 };

		assertTrue(classUnderTest.areAllLayerNodes(nodes));
	}

	@Test
	public void testAreAllLayerNodesWithSingleNonLayerNode()
	{
		DummyCatalogNode node0 = new DummyCatalogNode();
		node0.layerNode = false;

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0 };

		assertFalse(classUnderTest.areAllLayerNodes(nodes));
	}

	@Test
	public void testAreAllLayerNodesWithLayerNodesNoNulls()
	{
		DummyCatalogNode node0 = new DummyCatalogNode();
		DummyCatalogNode node1 = new DummyCatalogNode();
		DummyCatalogNode node2 = new DummyCatalogNode();
		DummyCatalogNode node3 = new DummyCatalogNode();

		node0.layerNode = true;
		node1.layerNode = true;
		node2.layerNode = true;
		node3.layerNode = true;

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0, node1, node2, node3 };

		assertTrue(classUnderTest.areAllLayerNodes(nodes));
	}

	@Test
	public void testAreAllLayerNodesWithLayerNodesNulls()
	{
		DummyCatalogNode node0 = new DummyCatalogNode();
		DummyCatalogNode node1 = new DummyCatalogNode();
		DummyCatalogNode node2 = null;
		DummyCatalogNode node3 = new DummyCatalogNode();

		node0.layerNode = true;
		node1.layerNode = true;
		node3.layerNode = true;

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0, node1, node2, node3 };

		assertTrue(classUnderTest.areAllLayerNodes(nodes));
	}

	@Test
	public void testAreAllLayerNodesWithNonLayerNodesNoNulls()
	{
		DummyCatalogNode node0 = new DummyCatalogNode();
		DummyCatalogNode node1 = new DummyCatalogNode();
		DummyCatalogNode node2 = new DummyCatalogNode();
		DummyCatalogNode node3 = new DummyCatalogNode();

		node0.layerNode = true;
		node1.layerNode = true;
		node2.layerNode = false;
		node3.layerNode = true;

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0, node1, node2, node3 };

		assertFalse(classUnderTest.areAllLayerNodes(nodes));
	}

	@Test
	public void testAreAllLayerNodesWithNonLayerNodesNulls()
	{
		DummyCatalogNode node0 = new DummyCatalogNode();
		DummyCatalogNode node1 = null;
		DummyCatalogNode node2 = new DummyCatalogNode();
		DummyCatalogNode node3 = new DummyCatalogNode();

		node0.layerNode = true;
		node2.layerNode = false;
		node3.layerNode = true;

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0, node1, node2, node3 };

		assertFalse(classUnderTest.areAllLayerNodes(nodes));
	}

	private static class DummyCatalogNode extends AbstractCatalogTreeNode
	{
		public DummyCatalogNode()
		{
			super(null);
		}

		boolean removable;
		boolean layerNode;
		URI layerURI;
		String name;

		@Override
		public boolean isRemoveable()
		{
			return removable;
		}

		@Override
		public boolean isLayerNode()
		{
			return layerNode;
		}

		@Override
		public URI getLayerURI()
		{
			return layerURI;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public URL getInformationURL()
		{
			return null;
		}

		@Override
		public String getInformationString()
		{
			return null;
		}
	}

}
