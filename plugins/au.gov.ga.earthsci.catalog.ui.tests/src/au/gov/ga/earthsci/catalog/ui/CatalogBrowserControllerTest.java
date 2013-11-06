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

	// ************************
	// allExistInLayerModel()
	// ************************

	@Test
	public void testAllExistInLayerModelWithNull()
	{
		ICatalogTreeNode[] nodes = null;

		assertTrue(classUnderTest.allExistInLayerModel(nodes));
	}

	@Test
	public void testAllExistInLayerModelWithEmpty()
	{
		ICatalogTreeNode[] nodes = new ICatalogTreeNode[0];

		assertTrue(classUnderTest.allExistInLayerModel(nodes));
	}

	@Test
	public void testAllExistInLayerModelWithSingleExists() throws Exception
	{
		final DummyCatalogNode node0 = new DummyCatalogNode();
		node0.layerNode = true;
		node0.layerURI = new URI("file://layer0");

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0 };

		mockContext.checking(new Expectations()
		{
			{
				{
					allowing(rootNode).hasNodesForURI(with(node0.layerURI));
					will(returnValue(true));
				}
			}
		});

		assertTrue(classUnderTest.allExistInLayerModel(nodes));
	}

	@Test
	public void testAllExistInLayerModelWithSingleNotExists() throws Exception
	{
		final DummyCatalogNode node0 = new DummyCatalogNode();
		node0.layerNode = true;
		node0.layerURI = new URI("file://layer0");

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0 };

		mockContext.checking(new Expectations()
		{
			{
				{
					allowing(rootNode).hasNodesForURI(with(node0.layerURI));
					will(returnValue(false));
				}
			}
		});

		assertFalse(classUnderTest.allExistInLayerModel(nodes));
	}

	@Test
	public void testAllExistInLayerModelWithMultipleExists() throws Exception
	{
		final DummyCatalogNode node0 = new DummyCatalogNode();
		node0.layerNode = true;
		node0.layerURI = new URI("file://layer0");

		final DummyCatalogNode node1 = new DummyCatalogNode();
		node1.layerNode = false;

		final DummyCatalogNode node10 = new DummyCatalogNode();
		node10.layerNode = true;
		node10.layerURI = new URI("file://layer10");

		node1.addChild(node10);

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0, node1, node10, null };

		mockContext.checking(new Expectations()
		{
			{
				{
					allowing(rootNode).hasNodesForURI(with(node0.layerURI));
					will(returnValue(true));
					allowing(rootNode).hasNodesForURI(with(node10.layerURI));
					will(returnValue(true));
				}
			}
		});

		assertTrue(classUnderTest.allExistInLayerModel(nodes));
	}

	@Test
	public void testAllExistInLayerModelWithMultipleNotExists() throws Exception
	{
		final DummyCatalogNode node0 = new DummyCatalogNode();
		node0.layerNode = true;
		node0.layerURI = new URI("file://layer0");

		final DummyCatalogNode node1 = new DummyCatalogNode();
		node1.layerNode = false;

		final DummyCatalogNode node10 = new DummyCatalogNode();
		node10.layerNode = true;
		node10.layerURI = new URI("file://layer10");

		node1.addChild(node10);

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0, node1, node10, null };

		mockContext.checking(new Expectations()
		{
			{
				{
					allowing(rootNode).hasNodesForURI(with(node0.layerURI));
					will(returnValue(true));
					allowing(rootNode).hasNodesForURI(with(node10.layerURI));
					will(returnValue(false));
				}
			}
		});

		assertFalse(classUnderTest.allExistInLayerModel(nodes));
	}

	// ************************
	// anyExistInLayerModel()
	// ************************

	@Test
	public void testAnyExistInLayerModelWithNull()
	{
		ICatalogTreeNode[] nodes = null;

		assertFalse(classUnderTest.anyExistInLayerModel(nodes));
	}

	@Test
	public void testAnyExistInLayerModelWithEmpty()
	{
		ICatalogTreeNode[] nodes = new ICatalogTreeNode[0];

		assertFalse(classUnderTest.anyExistInLayerModel(nodes));
	}

	@Test
	public void testAnyExistInLayerModelWithSingleExists() throws Exception
	{
		final DummyCatalogNode node0 = new DummyCatalogNode();
		node0.layerNode = true;
		node0.layerURI = new URI("file://layer0");

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0 };

		mockContext.checking(new Expectations()
		{
			{
				{
					allowing(rootNode).hasNodesForURI(with(node0.layerURI));
					will(returnValue(true));
				}
			}
		});

		assertTrue(classUnderTest.anyExistInLayerModel(nodes));
	}

	@Test
	public void testAnyExistInLayerModelWithSingleNotExists() throws Exception
	{
		final DummyCatalogNode node0 = new DummyCatalogNode();
		node0.layerNode = true;
		node0.layerURI = new URI("file://layer0");

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0 };

		mockContext.checking(new Expectations()
		{
			{
				{
					allowing(rootNode).hasNodesForURI(with(node0.layerURI));
					will(returnValue(false));
				}
			}
		});

		assertFalse(classUnderTest.anyExistInLayerModel(nodes));
	}

	@Test
	public void testAnyExistInLayerModelWithMultipleExists() throws Exception
	{
		final DummyCatalogNode node0 = new DummyCatalogNode();
		node0.layerNode = true;
		node0.layerURI = new URI("file://layer0");

		final DummyCatalogNode node1 = new DummyCatalogNode();
		node1.layerNode = false;

		final DummyCatalogNode node10 = new DummyCatalogNode();
		node10.layerNode = true;
		node10.layerURI = new URI("file://layer10");

		node1.addChild(node10);

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0, node1, node10, null };

		mockContext.checking(new Expectations()
		{
			{
				{
					allowing(rootNode).hasNodesForURI(with(node0.layerURI));
					will(returnValue(true));
					allowing(rootNode).hasNodesForURI(with(node10.layerURI));
					will(returnValue(true));
				}
			}
		});

		assertTrue(classUnderTest.anyExistInLayerModel(nodes));
	}

	@Test
	public void testAnyExistInLayerModelWithMultipleNotExists() throws Exception
	{
		final DummyCatalogNode node0 = new DummyCatalogNode();
		node0.layerNode = true;
		node0.layerURI = new URI("file://layer0");

		final DummyCatalogNode node1 = new DummyCatalogNode();
		node1.layerNode = false;

		final DummyCatalogNode node10 = new DummyCatalogNode();
		node10.layerNode = true;
		node10.layerURI = new URI("file://layer10");

		node1.addChild(node10);

		ICatalogTreeNode[] nodes = new ICatalogTreeNode[] { node0, node1, node10, null };

		mockContext.checking(new Expectations()
		{
			{
				{
					allowing(rootNode).hasNodesForURI(with(node0.layerURI));
					will(returnValue(true));
					allowing(rootNode).hasNodesForURI(with(node10.layerURI));
					will(returnValue(false));
				}
			}
		});

		assertTrue(classUnderTest.anyExistInLayerModel(nodes));
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
