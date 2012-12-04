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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import au.gov.ga.earthsci.core.bookmark.Messages;
import au.gov.ga.earthsci.core.bookmark.model.IBookmarkProperty;

/**
 * A bookmark property that contains information on camera position
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CameraProperty implements IBookmarkProperty
{
	public static final String TYPE = "au.gov.ga.earthsci.core.bookmark.properties.camera"; //$NON-NLS-1$

	/** The position of the camera eye */
	private Position eyePosition = null;
	
	/** The position of the camera lookat target */
	private Position lookatPosition = null;
	
	/** The up vector used to resolve camera orientation in the orbit camera */
	private Vec4 upVector = null;
	
	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public String getName()
	{
		return Messages.CameraProperty_Name;
	}

	public Position getEyePosition()
	{
		return eyePosition;
	}

	public void setEyePosition(Position eyePosition)
	{
		this.eyePosition = eyePosition;
	}

	public Position getLookatPosition()
	{
		return lookatPosition;
	}

	public void setLookatPosition(Position lookatPosition)
	{
		this.lookatPosition = lookatPosition;
	}

	public Vec4 getUpVector()
	{
		return upVector;
	}

	public void setUpVector(Vec4 upVector)
	{
		this.upVector = upVector;
	}

	
	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		return "CameraProperty {type: " + TYPE + ", eyePosition: " + eyePosition + ", lookat: " + lookatPosition + ", upVector: " + upVector + "}";
	}
}
