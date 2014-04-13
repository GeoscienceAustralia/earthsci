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
package au.gov.ga.earthsci.worldwind.common.view.stereo;

import java.io.Serializable;

import au.gov.ga.earthsci.worldwind.common.util.EnumPersistenceDelegate;

/**
 * Enum of supported stereo modes
 */
public enum StereoMode implements Serializable
{
	STEREO_BUFFER("Hardware stereo buffer"),
	RC_ANAGLYPH("Red/cyan anaglyph"),
	GM_ANAGLYPH("Green/magenta anaglyph"),
	BY_ANAGLYPH("Blue/yellow anaglyph");

	private String pretty;

	StereoMode(String pretty)
	{
		this.pretty = pretty;
	}

	@Override
	public String toString()
	{
		return pretty;
	}

	static
	{
		EnumPersistenceDelegate.installFor(values());
	}
}
