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

import au.gov.ga.earthsci.bookmark.IBookmarkPropertyAnimator;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyApplicator;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.worldwind.common.exaggeration.VerticalExaggerationService;

/**
 * An {@link IBookmarkPropertyApplicator} used to apply the state of
 * {@link ExaggerationProperty}s
 * 
 * @author Michael de Hoog
 */
public class ExaggerationPropertyApplicator implements IBookmarkPropertyApplicator
{
	@Override
	public String[] getSupportedTypes()
	{
		return new String[] { ExaggerationProperty.TYPE };
	}

	@Override
	public void apply(IBookmarkProperty property)
	{
		if (property == null)
		{
			return;
		}

		ExaggerationProperty exaggerationProperty = (ExaggerationProperty) property;
		VerticalExaggerationService.INSTANCE.set(exaggerationProperty.getExaggeration());
	}

	@Override
	public IBookmarkPropertyAnimator createAnimator(IBookmarkProperty start, IBookmarkProperty end, long duration)
	{
		return new ExaggerationPropertyAnimator((ExaggerationProperty) start, (ExaggerationProperty) end,
				duration);
	}
}
