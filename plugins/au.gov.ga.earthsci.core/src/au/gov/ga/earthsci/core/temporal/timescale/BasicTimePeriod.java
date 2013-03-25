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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import au.gov.ga.earthsci.core.temporal.BigTime;
import au.gov.ga.earthsci.core.util.Range;
import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * A basic (mostly) immutable implementation of the {@link ITimePeriod} interface that
 * provides a builder mechanism for populating fields.
 * <p/>
 * Enforces the requirement that all sub-periods have a level greater than the level
 * of this period.
 * <p/>
 * Provides the ability to plug in filters used to resolve overlapping sub-periods in the
 * {@link #getSubPeriod(BigTime)} method. The default filter will return all sub-periods with a 
 * range that contains the given time.
 * <p/>
 * Additionally, label generator strategies can be provided to create labels for time instants
 * that fall in this period. The default strategy is to return the name of the most specific
 * period the time falls into. 
 * <p/>
 * The only mutable field is the parent {@link ITimeScale}, which must be mutable to establish a
 * back-reference to the time scale this period belongs to. This field is not intended to be set
 * by client code.
 * <p/>
 * Equality is tested solely on the unique {@link #getId()}, and the natural order implemented in
 * {@link #compareTo(ITimePeriod)} is based on the start of the period range, from earliest to latest.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicTimePeriod implements ITimePeriod
{
	
	/**
	 * An filter interface that can be used to resolve overlapping sub-periods
	 * for a specified time instant. 
	 */
	public static interface SubPeriodFilter
	{
		
		/**
		 * Filter the provided list of candidate sub-periods to select appropriate sub-period(s)
		 * for the given time instant.
		 */
		List<ITimePeriod> filter(BigTime t, List<ITimePeriod> candidates);
	}
	
	/**
	 * A strategy interface for classes able to generate labels for a given time instant.
	 */
	public static interface LabelGenerator
	{
		/**
		 * Create and return a label for the given time instant in the given time period.
		 */
		String createLabel(BigTime t, ITimePeriod thisPeriod);
	}

	private static final SubPeriodFilter DEFAULT_SUB_PERIOD_FILTER = new SubPeriodFilter()
	{
		@Override
		public List<ITimePeriod> filter(BigTime t, List<ITimePeriod> candidates)
		{
			return candidates;
		}
	};
	
	private static final LabelGenerator DEFAULT_LABEL_GENERATOR = new LabelGenerator()
	{
		@Override
		public String createLabel(BigTime t, ITimePeriod thisPeriod)
		{
			if (!thisPeriod.hasSubPeriods())
			{
				return thisPeriod.getName();
			}
			
			List<ITimePeriod> subPeriods = thisPeriod.getSubPeriod(t);
			if (subPeriods == null || subPeriods.isEmpty())
			{
				return thisPeriod.getName();
			}
			
			StringBuffer buffer = new StringBuffer();
			Iterator<ITimePeriod> subPeriodIt = subPeriods.iterator();
			while(subPeriodIt.hasNext())
			{
				buffer.append(subPeriodIt.next().getLabel(t));
				if (subPeriodIt.hasNext())
				{
					buffer.append(" / "); //$NON-NLS-1$
				}
			}
			return buffer.toString();
		}
	};
	
	private final SubPeriodFilter subPeriodFilter;
	private final LabelGenerator labelGenerator;
	
	private final String id;
	private final String name;
	private final String description;
	
	private final ITimeScaleLevel level;
	private final Range<BigTime> range;
	private final List<ITimePeriod> subPeriods;
	
	
	
	@SuppressWarnings("unchecked")
	public BasicTimePeriod(String id, 
						   String name, 
						   String description, 
						   ITimeScaleLevel level, 
						   Range<BigTime> range,
						   List<ITimePeriod> subPeriods, 
						   SubPeriodFilter subPeriodFilter, 
						   LabelGenerator labelGenerator)
	{
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.level = level;
		this.range = range;
		this.subPeriods = (List<ITimePeriod>) (subPeriods == null ? Collections.emptyList() : Collections.unmodifiableList(subPeriods));
		this.subPeriodFilter = subPeriodFilter;
		this.labelGenerator = labelGenerator;
	}

	@Override
	public String getId()
	{
		return id;
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
	public int compareTo(ITimePeriod o)
	{
		// Comparison performed on the start time of the period range
		Range<BigTime> thisRange = getRange();
		Range<BigTime> otherRange = o.getRange();

		if (thisRange.isOpenLeft() && otherRange.isOpenLeft())
		{
			return 0;
		}
		else if (thisRange.isOpenLeft())
		{
			return -1;
		}
		else if (o.getRange().isOpenLeft())
		{
			return 1;
		}
		
		return thisRange.getMinValue().compareTo(o.getRange().getMinValue());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof ITimePeriod))
		{
			return false;
		}
		
		ITimePeriod other = (ITimePeriod)obj;
		return id.equals(other.getId());
	}
	
	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public ITimeScaleLevel getLevel()
	{
		return level;
	}

	@Override
	public boolean hasSubPeriods()
	{
		return !subPeriods.isEmpty();
	}

	@Override
	public boolean hasSubPeriod(ITimePeriod p)
	{
		if (p == null)
		{
			return false;
		}
		
		for (ITimePeriod sub : subPeriods)
		{
			if (sub.equals(p) || sub.hasSubPeriod(p))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public List<ITimePeriod> getSubPeriods()
	{
		return subPeriods;
	}

	@Override
	public List<ITimePeriod> getSubPeriod(BigTime t)
	{
		List<ITimePeriod> candidates = new ArrayList<ITimePeriod>();
		for (ITimePeriod p : subPeriods)
		{
			if (p.contains(t))
			{
				candidates.add(p);
			}
		}
		if (subPeriodFilter != null)
		{
			return subPeriodFilter.filter(t, candidates);
		}
		return DEFAULT_SUB_PERIOD_FILTER.filter(t, candidates);
	}

	@Override
	public Range<BigTime> getRange()
	{
		return range;
	}

	@Override
	public boolean contains(BigTime t)
	{
		return getRange().contains(t);
	}

	@Override
	public String getLabel(BigTime t)
	{
		if (!contains(t))
		{
			return null;
		}
		
		if (labelGenerator != null)
		{
			return labelGenerator.createLabel(t, this);
		}
		return DEFAULT_LABEL_GENERATOR.createLabel(t, this);
	}

	/**
	 * A builder class for creating instances of {@link BasicTimePeriod}s from collected values.
	 */
	public static class Builder
	{
		
		private String id;
		private String name;
		private String description;
		
		private ITimeScaleLevel level;
		private Range<BigTime> range;
		private List<ITimePeriod> subPeriods = new ArrayList<ITimePeriod>();
		
		private SubPeriodFilter subPeriodFilter = null;
		private LabelGenerator labelGenerator = null;
		
		private Builder(String id, String name, String description)
		{
			this.id = id;
			this.name = name;
			this.description = description;
		}
		
		/**
		 * Create and return a new {@link Builder} instance that can be used to create a new 
		 * {@link BasicTimePeriod}
		 */
		public static Builder buildTimePeriod(String id, String name, String description)
		{
			return new Builder(id, name, description);
		}
		
		public BasicTimePeriod build()
		{
			validate();
			
			Collections.sort(subPeriods);
			
			return new BasicTimePeriod(id, 
									   name, 
									   description, 
									   level, 
									   range == null ? new Range<BigTime>(null, null) : range, 
									   Collections.unmodifiableList(subPeriods), 
									   subPeriodFilter, 
									   labelGenerator);
		}
		
		private void validate()
		{
			Validate.notBlank(id, "An ID is required"); //$NON-NLS-1$
			Validate.notBlank(name, "A name is required"); //$NON-NLS-1$
			Validate.notNull(level, "A level is required"); //$NON-NLS-1$
		
			// Look for duplicate IDs in the sub-tree below this node.
			// This is not the most efficient way to do this (would be better to validate
			// once from the top-level periods), but the period structure is unlikely to be
			// very deep, and so it shouldn't be too expensive.
			
			Set<String> ids = new HashSet<String>();
			ids.add(id);
			int count = 1;
			for (ITimePeriod sub : subPeriods)
			{
				if (sub.getLevel().compareTo(level) <= 0)
				{
					throw new IllegalArgumentException("Sub-periods must have a higher level than the parent period"); //$NON-NLS-1$
				}
				count += collectSubPeriodIds(sub, ids);
			}
			
			Validate.isTrue(ids.size() == count, "Cannot have duplicate IDs in period hierarchy"); //$NON-NLS-1$
		}
		
		private int collectSubPeriodIds(ITimePeriod p, Set<String> ids)
		{
			ids.add(p.getId());
			int count = 1;
			for (ITimePeriod sub : p.getSubPeriods())
			{
				count += collectSubPeriodIds(sub, ids);
			}
			return count;
		}
		
		/**
		 * Provide an (inclusive) range for the period. A <code>null</code> parameter will imply 
		 * an open end to the range.
		 */
		public Builder withRange(BigTime start, BigTime end)
		{
			this.range = new Range<BigTime>(start, end);
			return this;
		}

		/**
		 * Set the start date of the range of the period
		 */
		public Builder from(BigTime start, boolean inclusive)
		{
			if (this.range == null)
			{
				this.range = new Range<BigTime>(start, inclusive, null, true);
			}
			else
			{
				this.range = new Range<BigTime>(start, inclusive, range.getMaxValue(), range.isInclusiveRight());
			}
			return this;
		}
		
		/**
		 * Set the end date of the range of the period
		 */
		public Builder to(BigTime end, boolean inclusive)
		{
			if (this.range == null)
			{
				this.range = new Range<BigTime>(null, true, end, inclusive);
			}
			else
			{
				this.range = new Range<BigTime>(range.getMinValue(), range.isInclusiveLeft(), end, inclusive);
			}
			return this;
		}
		
		/**
		 * Add all provided sub-periods to the period
		 */
		public Builder withSubPeriods(ITimePeriod... subPeriods)
		{
			if (subPeriods == null)
			{
				return this;
			}
			
			this.subPeriods.addAll(Arrays.asList(subPeriods));
			return this;
		}

		/**
		 * Add all provided sub-periods to the period
		 */
		public Builder withSubPeriods(Collection<ITimePeriod> subPeriods)
		{
			if (subPeriods == null)
			{
				return this;
			}
			
			this.subPeriods.addAll(subPeriods);
			return this;
		}
		
		/**
		 * Add all provided sub-periods to the period
		 */
		public Builder withSubPeriod(ITimePeriod subPeriod)
		{
			if (subPeriod == null)
			{
				return this;
			}
			
			this.subPeriods.add(subPeriod);
			return this;
		}
		
		/**
		 * Set a label generation strategy on the created period
		 */
		public Builder withLabelGenerator(LabelGenerator g)
		{
			this.labelGenerator = g;
			return this;
		}
		
		/**
		 * Set a sub-period filter on the created period
		 */
		public Builder withSubPeriodFilter(SubPeriodFilter f)
		{
			this.subPeriodFilter = f;
			return this;
		}
		
		/**
		 * Set the time scale level of this period
		 */
		public Builder atLevel(ITimeScaleLevel l)
		{
			this.level = l;
			return this;
		}
	}
	
}
