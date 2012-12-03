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
package au.gov.ga.earthsci.core.bookmark.properties.camera;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

import java.util.Map;

import javax.inject.Inject;

import au.gov.ga.earthsci.core.bookmark.IBookmarkPropertyCreator;
import au.gov.ga.earthsci.core.bookmark.model.IBookmarkProperty;

/**
 * An {@link IBookmarkPropertyCreator} that can create a {@link CameraProperty} instance
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CameraPropertyCreator implements IBookmarkPropertyCreator
{
	private static final String[] SUPPORTED_TYPES = new String[] {CameraProperty.TYPE};

	@Inject
	private View worldWindView;
	
	@Override
	public String[] getSupportedTypes()
	{
		return SUPPORTED_TYPES;
	}

	@Override
	public IBookmarkProperty createFromContext(String type, Map<String, Object> context)
	{
		return null;
	}

	@Override
	public IBookmarkProperty createFromCurrentState(String type)
	{
		CameraProperty result = new CameraProperty();
		result.setEyePosition(worldWindView.getCurrentEyePosition());
		
		Vec4 center = worldWindView.getCenterPoint();
		Globe globe = worldWindView.getGlobe();
		if (center != null && globe != null)
		{
			result.setLookatPosition(globe.computePositionFromPoint(center));
		}
		
		result.setUpVector(worldWindView.getUpVector());
		
		return result;
	}

}
