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
package au.gov.ga.earthsci.bookmark;

import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.bookmark.model.IBookmarkPropertyAnimator;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * A base class for {@link IBookmarkPropertyAnimator} implementations that provides convenience implementations of some methods.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class AbstractBookmarkPropertyAnimator implements IBookmarkPropertyAnimator
{

	private final IBookmarkProperty start;
	private final IBookmarkProperty end;
	private final long duration;
	
	private long startTime;
	private long endTime;
	
	public AbstractBookmarkPropertyAnimator(final IBookmarkProperty start, final IBookmarkProperty end, final long duration)
	{
		this.start = start;
		this.end = end;
		this.duration = duration;
	}
	
	@Override
	public IBookmarkProperty getStart()
	{
		return start;
	}

	@Override
	public IBookmarkProperty getEnd()
	{
		return end;
	}

	@Override
	public long getDuration()
	{
		return duration;
	}

	@Override
	public void init()
	{
		startTime = System.currentTimeMillis();
		endTime = startTime + duration;
	}
	
	protected double getCurrentTimeAsPercent()
	{
		return Util.percentDouble(System.currentTimeMillis(), startTime, endTime);
	}
	
	@Override
	public void applyFrame()
	{
		// For subclasses to override as needed
	}

	@Override
	public void dispose()
	{
		// For subclasses to override as needed
	}
	
}
