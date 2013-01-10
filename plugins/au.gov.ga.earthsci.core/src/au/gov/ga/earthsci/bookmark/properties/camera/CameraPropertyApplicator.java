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
package au.gov.ga.earthsci.bookmark.properties.camera;

import gov.nasa.worldwind.View;

import javax.inject.Inject;

import au.gov.ga.earthsci.bookmark.IBookmarkPropertyAnimator;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyApplicator;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;

/**
 * An {@link IBookmarkPropertyApplicator} used to apply the state of {@link CameraProperty}s
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CameraPropertyApplicator implements IBookmarkPropertyApplicator
{

	@Inject
	private View worldWindView;
	
	@Override
	public String[] getSupportedTypes()
	{
		return new String[] {CameraProperty.TYPE};
	}

	@Override
	public void apply(IBookmarkProperty property)
	{
		if (property == null)
		{
			return;
		}
	
		CameraProperty cameraProperty = (CameraProperty)property;
		
		worldWindView.stopMovement();
		worldWindView.setOrientation(cameraProperty.getEyePosition(), cameraProperty.getLookatPosition());
	}

	@Override
	public IBookmarkPropertyAnimator createAnimator(IBookmarkProperty start, IBookmarkProperty end, long duration)
	{
		return new CameraPropertyAnimator(worldWindView, (CameraProperty)start, (CameraProperty)end, duration);
	}
	
	public void setWorldWindView(View worldWindView)
	{
		this.worldWindView = worldWindView;
	}

}
