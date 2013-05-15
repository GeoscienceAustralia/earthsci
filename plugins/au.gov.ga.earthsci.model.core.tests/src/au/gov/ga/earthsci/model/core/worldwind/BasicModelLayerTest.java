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
package au.gov.ga.earthsci.model.core.worldwind;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import au.gov.ga.earthsci.model.IModel;

/**
 * Unit tests for the {@link BasicModelLayer}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicModelLayerTest
{

	private BasicModelLayer classUnderTest;
	private IModel dummyModel;

	private Mockery mockContext;

	@Before
	public void setup()
	{
		mockContext = new Mockery();

		dummyModel = mockContext.mock(IModel.class);
		mockContext.checking(new Expectations()
		{
			{
				oneOf(dummyModel).setOpacity(with(1.0));
			}
		});

		classUnderTest = new BasicModelLayer("Test", dummyModel); //$NON-NLS-1$
	}

	@Test
	public void testOpacityOnCreate()
	{
		assertEquals(1.0, classUnderTest.getOpacity(), 0.001);
	}

	@Test
	public void testSetOpacity()
	{
		final double opacity = 0.5;

		mockContext.checking(new Expectations()
		{
			{
				oneOf(dummyModel).setOpacity(with(opacity));
			}
		});

		classUnderTest.setOpacity(opacity);
		assertEquals(opacity, classUnderTest.getOpacity(), 0.001);

	}

}
