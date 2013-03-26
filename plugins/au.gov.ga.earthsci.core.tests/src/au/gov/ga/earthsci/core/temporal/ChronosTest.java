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
package au.gov.ga.earthsci.core.temporal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link Chronos} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ChronosTest
{
	private Chronos classUnderTest;
	private ITemporal temporal;
	private PropertyChangeListener pcl;

	private Mockery mockContext;

	@Before
	public void setup()
	{
		classUnderTest = new Chronos();

		mockContext = new Mockery();

		temporal = mockContext.mock(ITemporal.class);
		pcl = mockContext.mock(PropertyChangeListener.class);

		classUnderTest.addPropertyChangeListener(pcl);
		classUnderTest.addTemporal(temporal);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetCurrentTimeWithNull()
	{
		final BigTime currentTime = null;

		mockContext.checking(new Expectations()
		{
			{
				{
					never(temporal).apply(with(currentTime));
					never(pcl).propertyChange(with(any(PropertyChangeEvent.class)));
				}
			}
		});

		classUnderTest.setCurrentTime(currentTime);
	}

	@Test
	public void testSetCurrentTimeWithNonNull()
	{
		final BigTime currentTime = BigTime.now();

		mockContext.checking(new Expectations()
		{
			{
				{
					oneOf(temporal).apply(with(currentTime));
					oneOf(pcl).propertyChange(with(any(PropertyChangeEvent.class)));
				}
			}
		});

		classUnderTest.setCurrentTime(currentTime);
	}

	@Test
	public void testSetCurrentTimeWithSameTime()
	{
		final BigTime currentTime = new BigTime(BigInteger.valueOf(1000));
		classUnderTest.setCurrentTime(currentTime);

		mockContext.checking(new Expectations()
		{
			{
				{
					never(temporal).apply(with(any(BigTime.class)));
					never(pcl).propertyChange(with(any(PropertyChangeEvent.class)));
				}
			}
		});

		final BigTime newTime = new BigTime(BigInteger.valueOf(1000));
		classUnderTest.setCurrentTime(newTime);
	}
}
