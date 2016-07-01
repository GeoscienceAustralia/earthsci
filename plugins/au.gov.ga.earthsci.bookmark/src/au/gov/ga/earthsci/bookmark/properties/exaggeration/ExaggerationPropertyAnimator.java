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
package au.gov.ga.earthsci.bookmark.properties.exaggeration;

import au.gov.ga.earthsci.bookmark.AbstractBookmarkPropertyAnimator;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyAnimator;
import au.gov.ga.earthsci.worldwind.common.exaggeration.VerticalExaggerationService;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * An {@link IBookmarkPropertyAnimator} used to animate the vertical
 * exaggeration between two states.
 * 
 * @author Michael de Hoog
 */
public class ExaggerationPropertyAnimator extends AbstractBookmarkPropertyAnimator implements IBookmarkPropertyAnimator
{
	private final ExaggerationProperty start;
	private final ExaggerationProperty end;
	private boolean inited = false;

	public ExaggerationPropertyAnimator(final ExaggerationProperty start,
			final ExaggerationProperty end,
			final long duration)
	{
		super(start, end, duration);
		this.start = start;
		this.end = end;
	}

	@Override
	public void init()
	{
		super.init();
		inited = true;
	}

	@Override
	public boolean isInitialised()
	{
		return inited;
	}

	@Override
	public void applyFrame()
	{
		double steps = 50;
		double percent = Math.round(getCurrentTimeAsPercent() * steps) / steps;
		double exaggeration = Util.mixDouble(percent, start.getExaggeration(), end.getExaggeration());
		VerticalExaggerationService.INSTANCE.set(exaggeration);
	}

	@Override
	public void dispose()
	{
	}
}
