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
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.view.orbit.AccessibleOrbitViewInputSupport;
import gov.nasa.worldwind.view.orbit.AccessibleOrbitViewInputSupport.AccessibleOrbitViewState;
import gov.nasa.worldwind.view.orbit.OrbitView;
import au.gov.ga.earthsci.bookmark.AbstractBookmarkPropertyAnimator;
import au.gov.ga.earthsci.bookmark.IBookmarkPropertyAnimator;
import au.gov.ga.earthsci.core.worldwind.view.FlyToOrbitViewAnimator;

/**
 * An {@link IBookmarkPropertyAnimator} used to animate the camera between two
 * states.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CameraPropertyAnimator extends AbstractBookmarkPropertyAnimator implements IBookmarkPropertyAnimator
{

	private final CameraProperty start;
	private final CameraProperty end;
	private final View view;

	private Animator animator;

	public CameraPropertyAnimator(final View view, final CameraProperty start, final CameraProperty end,
			final long duration)
	{
		super(start, end, duration);
		this.start = start;
		this.end = end;
		this.view = view;
	}

	@Override
	public void init()
	{
		if (view instanceof OrbitView)
		{
			final AccessibleOrbitViewState startOVS =
					AccessibleOrbitViewInputSupport.computeOrbitViewState(view.getGlobe(), view.getGlobe()
							.computePointFromPosition(start.getEyePosition()), view.getGlobe()
							.computePointFromPosition(start.getLookatPosition()), start.getUpVector());

			final AccessibleOrbitViewState endOVS =
					AccessibleOrbitViewInputSupport.computeOrbitViewState(view.getGlobe(), view.getGlobe()
							.computePointFromPosition(end.getEyePosition()),
							view.getGlobe().computePointFromPosition(end.getLookatPosition()), end.getUpVector());

			animator =
					FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator((OrbitView) view, start.getLookatPosition(),
							end.getLookatPosition(), startOVS.getHeading(), endOVS.getHeading(), startOVS.getPitch(),
							endOVS.getPitch(), startOVS.getZoom(), endOVS.getZoom(), getDuration(), WorldWind.ABSOLUTE);
		}
		else
		{
			throw new UnsupportedOperationException("Only OrbitView supported"); //$NON-NLS-1$
		}
	}

	@Override
	public boolean isInitialised()
	{
		return animator != null;
	}

	@Override
	public void applyFrame()
	{
		if (animator == null)
		{
			throw new IllegalStateException("init() must be called before attempting to apply frames"); //$NON-NLS-1$
		}

		animator.next();
	}

	@Override
	public void dispose()
	{
		animator = null;
	}

}
