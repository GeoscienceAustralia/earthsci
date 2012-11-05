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
package au.gov.ga.earthsci.worldwind.common.layers.stereo;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.layers.SkyGradientLayer;

/**
 * An extension of the {@link SkyGradientLayer} that is modified to use {@link View#computeHorizonDistance()} rather than
 * {@link View#getFarClipDistance()} to make it suitable for use in views that dynamically modify the far clip distance 
 * (e.g. for occasions when {@link View#getFarClipDistance()} != {@link View#computeHorizonDistance()}).
 * 
 * @deprecated The standard {@link SkyGradientLayer} makes use of the horizon distance rather than the clip plane 
 * (as of build worldwind-71-66). This change makes this class redundant, and it will be removed in future releases.
 */
@Deprecated
public class StereoSkyGradientLayer extends SkyGradientLayer
{
	// TODO: Base class now uses horizon instead of far clip. Remove when all layer definitions update.
}
