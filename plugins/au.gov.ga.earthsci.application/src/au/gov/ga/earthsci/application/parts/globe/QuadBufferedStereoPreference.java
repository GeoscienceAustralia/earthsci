/*******************************************************************************
 * Copyright 2015 Geoscience Australia
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
package au.gov.ga.earthsci.application.parts.globe;

import gov.nasa.worldwind.avlist.AVKey;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.extensions.Preference;

import au.gov.ga.earthsci.newt.swt.WorldWindowNewtCanvasSWT;

/**
 * Injected object that tracks the World Window's quad-buffered stereo canvas
 * preference, and sets the system property accordingly.
 * <p/>
 * 
 * @see WorldWindowNewtCanvasSWT#getCaps()
 *
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
public class QuadBufferedStereoPreference
{
	@Inject
	public void stereoSetting(
			@Preference(nodePath = GlobePreferencePage.QUALIFIER_ID, value = GlobePreferencePage.STEREO_PREFERENCE_NAME) boolean stereo)
	{
		if (stereo)
		{
			System.setProperty(AVKey.STEREO_MODE, "device"); //$NON-NLS-1$
		}
		else
		{
			System.clearProperty(AVKey.STEREO_MODE);
		}
	}
}
