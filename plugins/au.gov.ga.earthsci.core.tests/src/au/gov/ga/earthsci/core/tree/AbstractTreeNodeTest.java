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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

	private AbstractTreeNode<Object> classUnderTest;
	
	private MockPropertyChangeListener changeListener;
	
	@Before
	public void setup()
	{
		classUnderTest = new ConcreteTreeNode();
		
		changeListener = new MockPropertyChangeListener();
		
		classUnderTest.addPropertyChangeListener(changeListener);
	}
	
	@Test
	public void testAddWithNull()
	{
		ConcreteTreeNode child = null;
		classUnderTest.add(child);
		
		assertTrue(changeListener.events.isEmpty());
		assertEquals(0, classUnderTest.children.length);
	}
	
	@Test
	public void testAddWithNonNull()
	{
		ConcreteTreeNode child = new ConcreteTreeNode();
		
		classUnderTest.add(child);
		
		// A children changed event should fire
		assertEquals(1, changeListener.events.size());
	
		PropertyChangeEvent event = changeListener.events.get(0);
		assertEquals("children", event.getPropertyName());
		assertArrayEquals(new Object[] {child}, (Object[])event.getNewValue());
		
		// Child should be on node
		assertEquals(1, classUnderTest.getChildCount());
		assertArrayEquals(new Object[] {child}, classUnderTest.children);
		
		// Node should be parent of child
		assertEquals(classUnderTest, child.getParent());
	}
	
	@Test
	public void testAddWithExisting()
	{
		ConcreteTreeNode child = new ConcreteTreeNode();
		
		classUnderTest.add(child);
		classUnderTest.add(child);
		
		// A single children changed event should fire
		assertEquals(1, changeListener.events.size());
	
		PropertyChangeEvent event = changeListener.events.get(0);
		assertEquals("children", event.getPropertyName());
		assertArrayEquals(new Object[] {child}, (Object[])event.getNewValue());
		
		// Child should be on node
		assertEquals(1, classUnderTest.getChildCount());
		assertArrayEquals(new Object[] {child}, classUnderTest.children);
		
		// Node should be parent of child
		assertEquals(classUnderTest, child.getParent());
	}
	
	private static class ConcreteTreeNode extends AbstractTreeNode<Object> { }
	
	private static class MockPropertyChangeListener implements PropertyChangeListener
	{

		List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
		
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			events.add(evt);
		}
		
	}
}
