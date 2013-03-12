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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * A basic immutable implementation of the {@link ITimeScale} class that provides
 * a builder mechanism for populating fields.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicTimeScale implements ITimeScale
{
	
	private final String id;
	private final String name;
	private final String description;
	private final List<ITimeScaleLevel> levels;
	private final List<ITimePeriod> periods;
	
	
	// Use the builder class for creating instances
	private BasicTimeScale(String id, 
						   String name, 
						   String description, 
						   List<ITimeScaleLevel> levels,
						   List<ITimePeriod> periods)
	{
		this.id = id;
		this.name = name;
		this.description = description;
		this.levels = Collections.unmodifiableList(levels);
		this.periods = Collections.unmodifiableList(periods);
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
	public String getId()
	{
		return id;
	}

	@Override
	public List<ITimeScaleLevel> getLevels()
	{
		return levels;
	}

	@Override
	public List<ITimePeriod> getPeriods()
	{
		return periods;
	}
	
	@Override
	public boolean hasPeriod(ITimePeriod p)
	{
		if (p == null)
		{
			return false;
		}
		
		for (ITimePeriod period : periods)
		{
			if (period.equals(p) || period.hasSubPeriod(p))
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * A builder class for {@link BasicTimeScale} instances
	 */
	public static class Builder
	{
		
		private String id;
		private String name;
		private String description;
		private List<ITimeScaleLevel> levels = new ArrayList<ITimeScaleLevel>();
		private List<ITimePeriod> periods = new ArrayList<ITimePeriod>();
		
		private Builder() {}
		
		public static Builder buildTimeScale(String id, String name, String description)
		{
			Builder result = new Builder();
			result.id = id;
			result.name = name;
			result.description = description;
			return result;
		}

		/**
		 * Add all provided levels to the created time scale
		 */
		public Builder withLevels(ITimeScaleLevel... levels)
		{
			if (levels == null)
			{
				return this;
			}
			
			this.levels.addAll(Arrays.asList(levels));
			return this;
		}
		
		/**
		 * Add all provided levels to the created time scale
		 */
		public Builder withLevels(Collection<ITimeScaleLevel> levels)
		{
			if (levels == null)
			{
				return this;
			}
			
			this.levels.addAll(levels);
			return this;
		}
		
		/**
		 * Add the provided level to the created time scale
		 */
		public Builder withLevel(ITimeScaleLevel level)
		{
			if (level == null)
			{
				return this;
			}
			
			this.levels.add(level);
			return this;
		}
		
		/**
		 * Add the provided top-level periods to the created time scale
		 */
		public Builder withTopLevelPeriods(ITimePeriod... periods)
		{
			if (periods == null)
			{
				return this;
			}
			
			this.periods.addAll(Arrays.asList(periods));
			return this;
		}
		
		/**
		 * Add the provided top-level periods to the created time scale
		 */
		public Builder withTopLevelPeriods(Collection<ITimePeriod> periods)
		{
			if (periods == null)
			{
				return this;
			}
			
			this.periods.addAll(periods);
			return this;
		}
		
		/**
		 * Add the provided top-level period to the created time scale
		 */
		public Builder withTopLevelPeriod(ITimePeriod period)
		{
			if (period == null)
			{
				return this;
			}
			
			this.periods.add(period);
			return this;
		}
		
		/**
		 * Create and return a {@link BasicTimeScale} instance from the values collected with 
		 * this builder.
		 * 
		 * @return a new {@link BasicTimeScale} instance created from the values collected with
		 * this builder.
		 * 
		 * @throws IllegalArgumentException If this method is called before all required
		 * values have been provided.
		 */
		public BasicTimeScale build() throws IllegalArgumentException
		{
			validate();
			
			Collections.sort(levels);
			Collections.sort(periods);
			
			return new BasicTimeScale(id, name, description, levels, periods);
		}
		
		private void validate()
		{
			Validate.notBlank(id, "An ID is required"); //$NON-NLS-1$
			Validate.notBlank(name, "A name is required"); //$NON-NLS-1$
			Validate.notEmpty(levels, "At least one level is required"); //$NON-NLS-1$
			Validate.notEmpty(periods, "At least one period is required"); //$NON-NLS-1$
			
			Set<ITimeScaleLevel> periodLevels = new HashSet<ITimeScaleLevel>();
			for (ITimePeriod p : periods)
			{
				collectLevels(p, periodLevels);
			}
			for (ITimeScaleLevel periodLevel : periodLevels)
			{
				Validate.isTrue(levels.contains(periodLevel), 
								"Periods must only use levels from the parent time scale (found unknown level \"" +  //$NON-NLS-1$
								 periodLevel.getName() + "\")"); //$NON-NLS-1$ 
			}
		}
		
		private void collectLevels(ITimePeriod p, Set<ITimeScaleLevel> levels)
		{
			levels.add(p.getLevel());
			for (ITimePeriod sub : p.getSubPeriods())
			{
				collectLevels(sub, levels);
			}
		}
	}
	
}
