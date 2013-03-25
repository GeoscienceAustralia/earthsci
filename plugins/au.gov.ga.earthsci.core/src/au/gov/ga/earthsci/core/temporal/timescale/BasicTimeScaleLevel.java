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
package au.gov.ga.earthsci.core.temporal.timescale;

import java.math.BigInteger;

import au.gov.ga.earthsci.core.util.Validate;

/**
 * A basic immutable implementation of the {@link ITimeScaleLevel} interface
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class BasicTimeScaleLevel implements ITimeScaleLevel
{

	private final String name;
	private final String description;
	private final BigInteger resolution;
	private final int order;
	
	public BasicTimeScaleLevel(String name, 
							   String description, 
							   BigInteger resolution, 
							   int order)
	{
		
		Validate.notBlank(name, "A name is required"); //$NON-NLS-1$
		Validate.notNull(resolution, "A resolution is required"); //$NON-NLS-1$
		Validate.isTrue(resolution.signum() > 0, "Resolution must be greater than 0"); //$NON-NLS-1$
		Validate.isTrue(order >= 0, "Order cannot be negative"); //$NON-NLS-1$
		
		this.name = name;
		this.description = description;
		this.resolution = resolution;
		this.order = order;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public int compareTo(ITimeScaleLevel other)
	{
		return order - other.getOrder();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		if (!(obj instanceof ITimeScaleLevel))
		{
			return false;
		}
		
		ITimeScaleLevel other = (ITimeScaleLevel)obj;
		
		return name.equals(other.getName()) && 
				resolution.equals(other.getResolution()) &&
				order == other.getOrder();
	}
	
	@Override
	public int hashCode()
	{
		return name.hashCode() + resolution.hashCode() + 31*order;
	}
	
	@Override
	public BigInteger getResolution()
	{
		return resolution;
	}

	@Override
	public int getOrder()
	{
		return order;
	}
	
	@Override
	public String toString()
	{
		return "BasicTimeScaleLevel(" + name + ")";  //$NON-NLS-1$//$NON-NLS-2$
	}

}
