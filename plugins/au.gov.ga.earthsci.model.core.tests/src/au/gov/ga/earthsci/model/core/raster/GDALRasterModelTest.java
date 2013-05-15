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
package au.gov.ga.earthsci.model.core.raster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.earthsci.model.IModel;
import au.gov.ga.earthsci.model.geometry.IVertexBasedGeometry;

/**
 * Unit tests for the {@link GDALRasterModel} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class GDALRasterModelTest
{

	private GDALRasterModel classUnderTest;
	private IVertexBasedGeometry dummyGeometry;

	private Mockery mockContext;

	@Before
	public void setup()
	{
		mockContext = new Mockery();

		dummyGeometry = mockContext.mock(IVertexBasedGeometry.class);

		classUnderTest = new GDALRasterModel("test", dummyGeometry); //$NON-NLS-1$
	}

	@Test
	public void testSetOpacity()
	{
		final double opacity = 0.5;

		// Expect a single event to be fired
		final List<Boolean> eventFiredCorrectly = new ArrayList<Boolean>();
		classUnderTest.addPropertyChangeListener(IModel.OPACITY_EVENT_NAME, new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				boolean correct = evt.getPropertyName().equals(IModel.OPACITY_EVENT_NAME)
						&& evt.getNewValue().equals(opacity);
				eventFiredCorrectly.add(correct);
			}
		});

		// And the opacity value to be propagated to the child geometries
		mockContext.checking(new Expectations()
		{
			{
				oneOf(dummyGeometry).getOpacity();
				will(returnValue(1.0));
				oneOf(dummyGeometry).setOpacity(with(opacity));
				oneOf(dummyGeometry).getOpacity();
				will(returnValue(opacity));
			}
		});

		classUnderTest.setOpacity(opacity);

		assertEquals(1, eventFiredCorrectly.size());
		assertTrue(eventFiredCorrectly.get(0));

		assertEquals(opacity, classUnderTest.getOpacity(), 0.001);
	}
}
