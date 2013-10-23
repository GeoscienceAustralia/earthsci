package au.gov.ga.earthsci.layer.ui.dnd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.dnd.TransferData;
import org.junit.Test;

import au.gov.ga.earthsci.layer.tree.FolderNode;
import au.gov.ga.earthsci.layer.tree.ILayerTreeNode;

/**
 * Unit tests for the {@link LocalLayerTransfer} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class LocalLayerTransferTest
{

	private final LocalLayerTransfer classUnderTest = LocalLayerTransfer.getInstance();

	@Test
	public void testGetSupportedTypes()
	{
		TransferData[] supportedTypes = classUnderTest.getSupportedTypes();

		assertNotNull(supportedTypes);
		assertEquals(1, supportedTypes.length);
		assertEquals(classUnderTest.getTypeIds()[0], supportedTypes[0].type);
	}

	@Test
	public void testValidateWithNull()
	{
		LayerTransferData data = null;

		assertFalse(classUnderTest.validate(data));
	}

	@Test
	public void testValidateWithEmpty()
	{
		LayerTransferData data = new LayerTransferData();
		assertFalse(classUnderTest.validate(data));
	}

	@Test
	public void testValidateWithValid()
	{
		ILayerTreeNode[] nodes = new ILayerTreeNode[1];
		nodes[0] = new FolderNode();
		nodes[0].setName("Test");
		LayerTransferData data = LayerTransferData.fromNodes(nodes);

		assertTrue(classUnderTest.validate(data));
	}
}
