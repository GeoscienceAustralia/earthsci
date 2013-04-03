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
package au.gov.ga.earthsci.core.tree;

import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link AbstractTreeNode}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AbstractTreeNodeTest
{

	private AbstractTreeNode<ConcreteTreeNode> classUnderTest;

	private MockPropertyChangeListener changeListener;

	@Before
	public void setup()
	{
		classUnderTest = new ConcreteTreeNode("classUnderTest");

		changeListener = new MockPropertyChangeListener();

		classUnderTest.addPropertyChangeListener(changeListener);
	}

	@Test(expected = NullPointerException.class)
	public void testAddWithNull()
	{
		ConcreteTreeNode child = null;
		classUnderTest.addChild(child);
	}

	@Test
	public void testAddWithNonNull()
	{
		ConcreteTreeNode child = new ConcreteTreeNode("child");

		classUnderTest.addChild(child);

		// A children changed event should fire
		assertEquals(1, changeListener.events.size());

		PropertyChangeEvent event = changeListener.events.get(0);
		assertEquals("children", event.getPropertyName());
		assertArrayEquals(new Object[] { child }, ((List) event.getNewValue()).toArray());

		// Child should be on node
		assertEquals(1, classUnderTest.getChildCount());
		assertArrayEquals(new Object[] { child }, classUnderTest.getChildren().toArray());

		// Node should be parent of child
		assertEquals(classUnderTest, child.getParent());
	}

	@Test
	public void testAddWithNonNullAndIndex()
	{
		ConcreteTreeNode child0 = new ConcreteTreeNode("child0");
		ConcreteTreeNode child1 = new ConcreteTreeNode("child1");
		ConcreteTreeNode child2 = new ConcreteTreeNode("child2");
		ConcreteTreeNode child3 = new ConcreteTreeNode("child3");

		classUnderTest.addChild(child0);
		classUnderTest.addChild(child1);
		classUnderTest.addChild(child2);
		classUnderTest.addChild(child3);

		assertArrayEquals(new Object[] { child0, child1, child2, child3 }, classUnderTest.getChildren().toArray());

		ConcreteTreeNode newChild = new ConcreteTreeNode("newChild");

		changeListener.clear();

		classUnderTest.addChild(2, newChild);

		assertArrayEquals(new Object[] { child0, child1, newChild, child2, child3 }, classUnderTest.getChildren()
				.toArray());

		// A children changed event should fire
		assertEquals(1, changeListener.events.size());
		assertEquals("children", changeListener.events.get(0).getPropertyName());

		assertEquals(classUnderTest, newChild.getParent());
	}

	@Test
	public void testAddWithNonNullAndIndexOutOfBounds()
	{
		ConcreteTreeNode child0 = new ConcreteTreeNode("child0");
		ConcreteTreeNode child1 = new ConcreteTreeNode("child1");
		ConcreteTreeNode child2 = new ConcreteTreeNode("child2");
		ConcreteTreeNode child3 = new ConcreteTreeNode("child3");

		classUnderTest.addChild(child0);
		classUnderTest.addChild(child1);
		classUnderTest.addChild(child2);
		classUnderTest.addChild(child3);

		assertArrayEquals(new Object[] { child0, child1, child2, child3 }, classUnderTest.getChildren().toArray());

		ConcreteTreeNode newChild = new ConcreteTreeNode("newChild");

		changeListener.clear();

		classUnderTest.addChild(200, newChild);

		assertArrayEquals(new Object[] { child0, child1, child2, child3, newChild }, classUnderTest.getChildren()
				.toArray());

		// A children changed event should fire
		assertEquals(1, changeListener.events.size());
		assertEquals("children", changeListener.events.get(0).getPropertyName());

		assertEquals(classUnderTest, newChild.getParent());
	}

	@Test
	public void testAddWithExistingSingleChild()
	{
		ConcreteTreeNode child = new ConcreteTreeNode("child");

		classUnderTest.addChild(child);

		changeListener.clear();

		classUnderTest.addChild(child);

		// No children changed event should fire
		assertEquals(0, changeListener.events.size());

		// Child should be on node
		assertEquals(1, classUnderTest.getChildCount());
		assertArrayEquals(new Object[] { child }, classUnderTest.getChildren().toArray());

		// Node should be parent of child
		assertEquals(classUnderTest, child.getParent());
	}

	@Test
	public void testAddWithExistingTwoChildren()
	{
		ConcreteTreeNode child0 = new ConcreteTreeNode("child0");
		ConcreteTreeNode child1 = new ConcreteTreeNode("child1");

		classUnderTest.addChild(child0);
		classUnderTest.addChild(child1);

		changeListener.clear();

		classUnderTest.addChild(child0);

		// A children changed event should fire
		assertEquals(1, changeListener.events.size());

		// Child should be on node
		assertEquals(2, classUnderTest.getChildCount());
		assertArrayEquals(new Object[] { child1, child0 }, classUnderTest.getChildren().toArray());

		// Node should be parent of child
		assertEquals(classUnderTest, child0.getParent());
		assertEquals(classUnderTest, child1.getParent());
	}

	@Test
	public void testAddToEndWithExistingMultipleChildren()
	{
		ConcreteTreeNode child0 = new ConcreteTreeNode("child0");
		ConcreteTreeNode child1 = new ConcreteTreeNode("child1");
		ConcreteTreeNode child2 = new ConcreteTreeNode("child2");
		ConcreteTreeNode child3 = new ConcreteTreeNode("child3");

		classUnderTest.addChild(child0);
		classUnderTest.addChild(child1);
		classUnderTest.addChild(child2);
		classUnderTest.addChild(child3);

		changeListener.clear();

		classUnderTest.addChild(child0);

		// A single children changed event should fire
		assertEquals(1, changeListener.events.size());
		assertEquals("children", changeListener.events.get(0).getPropertyName());

		// Added child should be at the end of the children list
		assertEquals(4, classUnderTest.getChildCount());
		assertArrayEquals(new Object[] { child1, child2, child3, child0 }, classUnderTest.getChildren().toArray());

		// Node should be parent of children
		assertEquals(classUnderTest, child0.getParent());
		assertEquals(classUnderTest, child1.getParent());
		assertEquals(classUnderTest, child2.getParent());
		assertEquals(classUnderTest, child3.getParent());

		// Indices should be updated
		assertEquals(3, child0.index());
		assertEquals(0, child1.index());
		assertEquals(1, child2.index());
		assertEquals(2, child3.index());
	}

	@Test
	public void testAddToMiddleWithExistingMultipleChildren()
	{
		ConcreteTreeNode child0 = new ConcreteTreeNode("child0");
		ConcreteTreeNode child1 = new ConcreteTreeNode("child1");
		ConcreteTreeNode child2 = new ConcreteTreeNode("child2");
		ConcreteTreeNode child3 = new ConcreteTreeNode("child3");
		ConcreteTreeNode child4 = new ConcreteTreeNode("child4");

		classUnderTest.addChild(child0);
		classUnderTest.addChild(child1);
		classUnderTest.addChild(child2);
		classUnderTest.addChild(child3);
		classUnderTest.addChild(child4);

		changeListener.clear();

		classUnderTest.addChild(2, child0);

		// A single children changed event should fire
		assertEquals(1, changeListener.events.size());
		assertEquals("children", changeListener.events.get(0).getPropertyName());

		// Added child should be in the middle of the children list
		assertEquals(5, classUnderTest.getChildCount());
		assertArrayEquals(new Object[] { child1, child2, child0, child3, child4 }, classUnderTest.getChildren()
				.toArray());

		// Node should be parent of children
		assertEquals(classUnderTest, child0.getParent());
		assertEquals(classUnderTest, child1.getParent());
		assertEquals(classUnderTest, child2.getParent());
		assertEquals(classUnderTest, child3.getParent());
		assertEquals(classUnderTest, child4.getParent());

		// Indices should be updated
		assertEquals(2, child0.index());
		assertEquals(0, child1.index());
		assertEquals(1, child2.index());
		assertEquals(3, child3.index());
		assertEquals(4, child4.index());
	}

	@Test
	public void testAddToStartWithExistingMultipleChildren()
	{
		ConcreteTreeNode child0 = new ConcreteTreeNode("child0");
		ConcreteTreeNode child1 = new ConcreteTreeNode("child1");
		ConcreteTreeNode child2 = new ConcreteTreeNode("child2");
		ConcreteTreeNode child3 = new ConcreteTreeNode("child3");
		ConcreteTreeNode child4 = new ConcreteTreeNode("child4");

		classUnderTest.addChild(child0);
		classUnderTest.addChild(child1);
		classUnderTest.addChild(child2);
		classUnderTest.addChild(child3);
		classUnderTest.addChild(child4);

		changeListener.clear();

		classUnderTest.addChild(1, child3);

		// A single children changed event should fire
		assertEquals(1, changeListener.events.size());
		assertEquals("children", changeListener.events.get(0).getPropertyName());

		// Added child should be in the middle of the children list
		assertEquals(5, classUnderTest.getChildCount());
		assertArrayEquals(new Object[] { child0, child3, child1, child2, child4 }, classUnderTest.getChildren()
				.toArray());

		// Node should be parent of children
		assertEquals(classUnderTest, child0.getParent());
		assertEquals(classUnderTest, child1.getParent());
		assertEquals(classUnderTest, child2.getParent());
		assertEquals(classUnderTest, child3.getParent());
		assertEquals(classUnderTest, child4.getParent());

		// Indices should be updated
		assertEquals(0, child0.index());
		assertEquals(2, child1.index());
		assertEquals(3, child2.index());
		assertEquals(1, child3.index());
		assertEquals(4, child4.index());
	}

	@Test(expected = NullPointerException.class)
	public void testRemoveWithEmptyChildrenAndNull()
	{
		ConcreteTreeNode child = null;

		classUnderTest.removeChild(child);
	}

	@Test
	public void testRemoveWithEmptyChildrenAndNonNull()
	{
		ConcreteTreeNode child = new ConcreteTreeNode("child");

		boolean removed = classUnderTest.removeChild(child);

		assertFalse(removed);

		assertEquals(0, changeListener.events.size());
	}

	@SuppressWarnings("unchecked")
	@Test(expected = NullPointerException.class)
	public void testRemoveWithNonEmptyChildrenNull()
	{
		ConcreteTreeNode child0 = new ConcreteTreeNode("child0");
		ConcreteTreeNode child1 = new ConcreteTreeNode("child1");
		ConcreteTreeNode child2 = new ConcreteTreeNode("child2");
		ConcreteTreeNode child3 = new ConcreteTreeNode("child3");
		List<ConcreteTreeNode> children = new ArrayList<ConcreteTreeNode>();
		children.add(child0);
		children.add(child1);
		children.add(child2);
		children.add(child3);
		classUnderTest.setChildren(children);

		changeListener.clear();

		classUnderTest.removeChild(null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveWithNonEmptyChildrenNonMember()
	{
		ConcreteTreeNode child0 = new ConcreteTreeNode("child0");
		ConcreteTreeNode child1 = new ConcreteTreeNode("child1");
		ConcreteTreeNode child2 = new ConcreteTreeNode("child2");
		ConcreteTreeNode child3 = new ConcreteTreeNode("child3");
		List<ConcreteTreeNode> children = new ArrayList<ConcreteTreeNode>();
		children.add(child0);
		children.add(child1);
		children.add(child2);
		children.add(child3);
		classUnderTest.setChildren(children);

		changeListener.clear();

		boolean removed = classUnderTest.removeChild(new ConcreteTreeNode("notAChild"));

		assertFalse(removed);

		assertEquals(0, changeListener.events.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveWithNonEmptyChildrenMember()
	{
		ConcreteTreeNode child0 = new ConcreteTreeNode("child0");
		ConcreteTreeNode child1 = new ConcreteTreeNode("child1");
		ConcreteTreeNode child2 = new ConcreteTreeNode("child2");
		ConcreteTreeNode child3 = new ConcreteTreeNode("child3");
		List<ConcreteTreeNode> children = new ArrayList<ConcreteTreeNode>();
		children.add(child0);
		children.add(child1);
		children.add(child2);
		children.add(child3);
		classUnderTest.setChildren(children);

		changeListener.clear();

		boolean removed = classUnderTest.removeChild(child1);

		assertTrue(removed);

		assertEquals(3, classUnderTest.getChildCount());
		assertArrayEquals(new Object[] { child0, child2, child3 }, classUnderTest.getChildren().toArray());

		assertEquals(1, changeListener.events.size());
		assertEquals("children", changeListener.events.get(0).getPropertyName());
	}

	@Test
	public void testRemoveAllWithEmptyChildren()
	{
		classUnderTest.clearChildren();

		assertEquals(0, classUnderTest.getChildCount());

		assertEquals(0, changeListener.events.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRemoveAllWithNonEmptyChildren()
	{
		ConcreteTreeNode child0 = new ConcreteTreeNode("child0");
		ConcreteTreeNode child1 = new ConcreteTreeNode("child1");
		ConcreteTreeNode child2 = new ConcreteTreeNode("child2");
		ConcreteTreeNode child3 = new ConcreteTreeNode("child3");
		List<ConcreteTreeNode> children = new ArrayList<ConcreteTreeNode>();
		children.add(child0);
		children.add(child1);
		children.add(child2);
		children.add(child3);
		classUnderTest.setChildren(children);

		changeListener.clear();

		classUnderTest.clearChildren();

		assertEquals(0, classUnderTest.getChildCount());

		assertNull(child0.getParent());
		assertNull(child1.getParent());
		assertNull(child2.getParent());
		assertNull(child3.getParent());

		assertEquals(1, changeListener.events.size());
		assertEquals("children", changeListener.events.get(0).getPropertyName());
	}

	@Test
	public void testMoveChild()
	{
		ConcreteTreeNode child0 = new ConcreteTreeNode("child0");
		ConcreteTreeNode child1 = new ConcreteTreeNode("child1");

		classUnderTest.addChild(child0);
		classUnderTest.addChild(child1);

		changeListener.clear();

		classUnderTest.moveChild(child0, 1);

		// A children changed event should fire
		assertEquals(1, changeListener.events.size());

		// Child should be on node
		assertEquals(2, classUnderTest.getChildCount());
		assertArrayEquals(new Object[] { child1, child0 }, classUnderTest.getChildren().toArray());

		// Node should be parent of child
		assertEquals(classUnderTest, child0.getParent());
		assertEquals(classUnderTest, child1.getParent());
	}

	@Test
	public void testMoveChildToInvalidIndex()
	{
		ConcreteTreeNode child0 = new ConcreteTreeNode("child0");
		ConcreteTreeNode child1 = new ConcreteTreeNode("child1");

		classUnderTest.addChild(child0);
		classUnderTest.addChild(child1);

		changeListener.clear();

		classUnderTest.moveChild(child0, 2);

		// A children changed event should fire
		assertEquals(1, changeListener.events.size());

		// Child should be on node
		assertEquals(2, classUnderTest.getChildCount());
		assertArrayEquals(new Object[] { child1, child0 }, classUnderTest.getChildren().toArray());

		// Node should be parent of child
		assertEquals(classUnderTest, child0.getParent());
		assertEquals(classUnderTest, child1.getParent());
	}

	private static class ConcreteTreeNode extends AbstractTreeNode<ConcreteTreeNode>
	{
		private String name;

		ConcreteTreeNode(String name)
		{
			super(ConcreteTreeNode.class);
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	private static class MockPropertyChangeListener implements PropertyChangeListener
	{

		List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();

		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			events.add(evt);
		}

		public void clear()
		{
			events.clear();
		}

	}
}
